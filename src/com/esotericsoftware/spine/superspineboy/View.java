/******************************************************************************
 * Spine Runtimes Software License
 * Version 2.1
 * 
 * Copyright (c) 2013, Esoteric Software
 * All rights reserved.
 * 
 * You are granted a perpetual, non-exclusive, non-sublicensable and
 * non-transferable license to install, execute and perform the Spine Runtimes
 * Software (the "Software") solely for internal use. Without the written
 * permission of Esoteric Software (typically granted by licensing Spine), you
 * may not (a) modify, translate, adapt or otherwise create derivative works,
 * improvements of the Software or develop new applications using the Software
 * or (b) remove, delete, alter or obscure any trademarks or any copyright,
 * trademark, patent or other intellectual property or proprietary rights notices
 * on or in the Software, including any copy thereof. Redistributions in binary
 * or source form must include this license and terms.
 * 
 * THIS SOFTWARE IS PROVIDED BY ESOTERIC SOFTWARE "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL ESOTERIC SOFTARE BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *****************************************************************************/

package com.esotericsoftware.spine.superspineboy;

import static com.esotericsoftware.spine.superspineboy.Model.*;
import static com.esotericsoftware.spine.superspineboy.Player.*;

import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.superspineboy.Model.State;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthoCachedTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

/** The core of the view logic. The view knows about the model and manages everything needed to draw to the screen. */
class View extends InputAdapter {
	static float bulletHitTime = 0.2f, bulletHitOffset = 50 * scale;

	static float cameraMinWidth = 16, cameraMaxWidth = 28, cameraHeight = 16, cameraZoom = 0.4f, cameraZoomSpeed = 0.5f;
	static float cameraBottom = 2, cameraTop = 7, cameraMinX = 1;
	static float cameraLookahead = 0.75f, cameraLookaheadSpeed = 8f, cameraLookaheadSpeedSlow = 3f;
	static float cameraSpeed = 5f, cameraShake = 6 * scale;

	static int[] mapLayersOpaque1 = {1};
	static int[] mapLayersBackground2 = {2, 3, 4, 5, 6};
	static int[] mapLayersOpaque3 = {10};
	static int[] mapForegroundLayers4 = {7, 8,};
	static int[] mapForegroundLayers5 = {11};

	Model model;
	Player player;
	OrthographicCamera camera;
	ExtendViewport viewport;
	SpriteBatch batch;
	SkeletonRenderer skeletonRenderer;
	OrthoCachedTiledMapRenderer mapRenderer;
	Assets assets;
	UI ui;

	float shakeX, shakeY, lookahead, zoom = 1;
	FloatArray hits = new FloatArray();
	boolean touched, jumpPressed, leftPressed, rightPressed;

	View (Model model) {
		this.model = model;

		mapRenderer = new OrthoCachedTiledMapRenderer(model.map, scale, 3000);
		mapRenderer.setOverCache(0.6f);
		mapRenderer.setMaxTileSize(512, 512);

		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		viewport = new ExtendViewport(cameraMinWidth, cameraHeight, cameraMaxWidth, cameraHeight, camera);

		skeletonRenderer = new SkeletonRenderer();
		skeletonRenderer.setPremultipliedAlpha(true);

		assets = new Assets();

		ui = new UI(this);

		Gdx.input.setInputProcessor(new InputMultiplexer(ui, ui.stage, this));

		restart();
	}

	void restart () {
		player = model.player;
		player.view = new PlayerView(this);
		lookahead = 0;
		touched = false;
		hits.clear();
	}

	void update (float delta) {
		// Update the hit marker images.
		for (int i = hits.size - 4; i >= 0; i -= 4) {
			float time = hits.get(i) - delta;
			if (time < 0)
				hits.removeRange(i, i + 3);
			else
				hits.set(i, time);
		}

		updateInput(delta);
		updateCamera(delta);

		player.view.update(delta);

		for (Enemy enemy : model.enemies) {
			if (enemy.view == null) enemy.view = new EnemyView(this, enemy);
			enemy.view.update(delta);
		}
	}

	void updateInput (float delta) {
		if (player.hp == 0) return;

		if (leftPressed)
			player.moveLeft(delta);
		else if (rightPressed)
			player.moveRight(delta);
		else if (player.state == State.run) //
			player.setState(State.idle);

		if (touched) player.view.shoot();
	}

	void updateCamera (float delta) {
		if (player.hp > 0) {
			// Reduce camera lookahead based on distance of enemies behind the player.
			float enemyBehindDistance = 0;
			for (Enemy enemy : model.enemies) {
				float dist = enemy.position.x - player.position.x;
				if (enemy.hp > 0 && Math.signum(dist) == -player.dir) {
					dist = Math.abs(dist);
					enemyBehindDistance = enemyBehindDistance == 0 ? dist : Math.min(enemyBehindDistance, dist);
				}
			}
			float lookaheadDist = cameraLookahead * viewport.getWorldWidth() / 2 * (1 - Math.min(1, enemyBehindDistance / 22));
			float lookaheadDiff = player.position.x + lookaheadDist * player.dir - camera.position.x;
			float lookaheadAdjust = (enemyBehindDistance > 0 ? cameraLookaheadSpeedSlow : cameraLookaheadSpeed) * delta;
			if (Math.abs(lookahead - lookaheadDiff) > 1) {
				if (lookahead < lookaheadDiff)
					lookahead = Math.min(lookaheadDist, lookahead + lookaheadAdjust);
				else if (lookahead > lookaheadDiff) //
					lookahead = Math.max(-lookaheadDist, lookahead - lookaheadAdjust);
			}
			if (player.position.x + lookahead < cameraMinX) lookahead = cameraLookahead;
		}

		// Move camera to the player position over time, adjusting for lookahead.
		float minX = player.position.x + lookahead, maxX = player.position.x + lookahead;
		if (camera.position.x < minX) {
			camera.position.x += (minX - camera.position.x) * cameraSpeed * delta;
			if (camera.position.x > minX) camera.position.x = minX;
			if (Math.abs(camera.position.x - minX) < 0.1f) camera.position.x = minX;
		} else if (camera.position.x > maxX) {
			camera.position.x += (maxX - camera.position.x) * cameraSpeed * delta;
			if (camera.position.x < maxX) camera.position.x = maxX;
			if (Math.abs(camera.position.x - maxX) < 0.1f) camera.position.x = maxX;
		}
		camera.position.x = Math.max(viewport.getWorldWidth() / 2 + cameraMinX, camera.position.x);

		float top = zoom != 1 ? 5 : cameraTop;
		float bottom = zoom != 1 ? 0 : cameraBottom;
		float maxY = player.position.y + viewport.getWorldHeight() / 2 - bottom;
		float minY = player.position.y - viewport.getWorldHeight() / 2 + top;
		if (camera.position.y < minY) {
			camera.position.y += (minY - camera.position.y) * cameraSpeed / zoom * delta;
			if (Math.abs(camera.position.y - minY) < 0.1f) camera.position.y = minY;
		} else if (camera.position.y > maxY) {
			camera.position.y += (maxY - camera.position.y) * cameraSpeed / zoom * delta;
			if (Math.abs(camera.position.y - maxY) < 0.1f) camera.position.y = maxY;
		}

		camera.update();
		batch.setProjectionMatrix(camera.combined);
		mapRenderer.setView(camera);

		camera.position.add(-shakeX, -shakeY, 0);
		shakeX = 0;
		shakeY = 0;
	}

	void render () {
		viewport.apply();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (ui.bgButton.isChecked()) {
			mapRenderer.setBlending(false);
			mapRenderer.render(mapLayersOpaque1);

			mapRenderer.setBlending(true);
			mapRenderer.render(mapLayersBackground2);
		}

		batch.begin();
		// Draw enemies.
		for (Enemy enemy : model.enemies) {
			enemy.view.skeleton.getColor().a = Math.min(1, enemy.deathTimer / Enemy.fadeTime);
			skeletonRenderer.draw(batch, enemy.view.skeleton);
		}
		// Draw player.
		if (player.collisionTimer < 0 || (int)(player.collisionTimer / flashTime) % 3 != 0)
			skeletonRenderer.draw(batch, player.view.skeleton);
		batch.end();

		if (ui.bgButton.isChecked()) {
			mapRenderer.setBlending(false);
			mapRenderer.render(mapLayersOpaque3);

			mapRenderer.setBlending(true);
			mapRenderer.render(mapForegroundLayers4);
		}

		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
		batch.begin();

		// Draw bullets.
		TextureRegion bulletRegion = assets.bulletRegion;
		float bulletWidth = bulletRegion.getRegionWidth() * scale;
		float bulletHeight = bulletRegion.getRegionHeight() * scale / 2;
		for (int i = 2, n = model.bullets.size; i < n; i += 5) {
			float x = model.bullets.get(i), y = model.bullets.get(i + 1);
			float angle = model.bullets.get(i + 2);
			float vx = MathUtils.cosDeg(angle);
			float vy = MathUtils.sinDeg(angle);
			// Adjust position so bullet region is drawn with the bullet position in the center of the fireball.
			x -= vx * bulletWidth * 0.65f;
			y -= vy * bulletWidth * 0.65f;
			x += vy * bulletHeight / 2;
			y += -vx * bulletHeight / 2;
			batch.draw(bulletRegion, x, y, 0, 0, bulletWidth, bulletHeight, 1, 1, angle);
		}

		// Draw hit markers.
		TextureRegion hitRegion = assets.hitRegion;
		Color color = batch.getColor().set(1, 1, 1, 1);
		float hitWidth = hitRegion.getRegionWidth() * scale;
		float hitHeight = hitRegion.getRegionWidth() * scale;
		for (int i = hits.size - 4; i >= 0; i -= 4) {
			float time = hits.get(i);
			float x = hits.get(i + 1);
			float y = hits.get(i + 2);
			float angle = hits.get(i + 3);
			color.a = time / bulletHitTime;
			batch.setColor(color);
			float vx = MathUtils.cosDeg(angle);
			float vy = MathUtils.sinDeg(angle);
			// Adjust position so bullet region is drawn with the bullet position in the center of the fireball.
			x += vy * bulletHeight * 0.2f;
			y += -vx * bulletHeight * 0.2f;
			batch.draw(hitRegion, x - hitWidth / 2, y, hitWidth / 2, 0, hitWidth, hitHeight, 1, 1, angle);
		}
		batch.setColor(Color.WHITE);

		batch.end();
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		if (ui.bgButton.isChecked()) mapRenderer.render(mapForegroundLayers5);

		ui.render();
	}

	void resize (int width, int height) {
		viewport.update(width, height);
		camera.position.x = player.position.x;
		camera.position.y = player.position.y + viewport.getWorldHeight() / 2 - cameraBottom;
		mapRenderer.setView(camera);
		ui.resize(width, height);
	}

	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		touched = true;
		player.view.shoot();
		return true;
	}

	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		touched = false;
		return true;
	}

	public boolean keyDown (int keycode) {
		switch (keycode) {
		case Keys.W:
		case Keys.UP:
		case Keys.SPACE:
			if (player.hp == 0) return false;
			jumpPressed = true;
			if (player.isGrounded()) player.view.jump();
			return true;
		case Keys.A:
		case Keys.LEFT:
			leftPressed = true;
			return true;
		case Keys.D:
		case Keys.RIGHT:
			rightPressed = true;
			return true;
		}
		return false;
	}

	public boolean keyUp (int keycode) {
		switch (keycode) {
		case Keys.W:
		case Keys.UP:
		case Keys.SPACE:
			if (player.hp == 0) return false;
			// Releasing jump on the way up reduces jump height.
			if (player.velocity.y > 0) player.velocity.y *= jumpDamping;
			jumpPressed = false;
			return true;
		case Keys.A:
		case Keys.LEFT:
			leftPressed = false;
			return true;
		case Keys.D:
		case Keys.RIGHT:
			rightPressed = false;
			return true;
		}
		return false;
	}

	/** Stores information needed by the view for a character state. */
	static class StateView {
		Animation animation;
		boolean loop;
		// Controls the start frame when changing from another animation to this animation.
		ObjectFloatMap<Animation> startTimes = new ObjectFloatMap();
		float defaultStartTime;
	}
}
