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

import com.esotericsoftware.spine.superspineboy.Enemy.Type;

import com.badlogic.gdx.maps.tiled.AtlasTmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Pools;

/** The core of the game logic. The model manages all game information but knows nothing about the view, ie it knows nothing about
 * how this information might be drawn to the screen. This model-view separation is a clean way to organize the code. */
class Model {
	static float scale = 1 / 64f;
	static float gravity = 32;
	static float fps = 1 / 30f;
	static float gameOverSlowdown = 5.5f;
	static int mapCollisionLayer = 0;

	SuperSpineboy controller;
	Player player;
	TiledMap map;
	TiledMapTileLayer collisionLayer;
	Array<Rectangle> tiles = new Array();
	float timeScale = 1;
	Array<Trigger> triggers = new Array();
	FloatArray bullets = new FloatArray();
	Array<Enemy> enemies = new Array();
	Vector2 temp = new Vector2();
	float gameOverTimer;

	Model (SuperSpineboy controller) {
		this.controller = controller;

		map = new AtlasTmxMapLoader().load("map/map.tmx");
		collisionLayer = (TiledMapTileLayer)map.getLayers().get(mapCollisionLayer);

		restart();
	}

	void restart () {
		player = new Player(this);
		player.position.set(4, 8);

		bullets.clear();
		enemies.clear();
		gameOverTimer = 0;

		// Setup triggers to spawn enemies based on the x coordinate of the player.
		triggers.clear();
		addTrigger(17, 17 + 22, 8, Type.normal, 2);
		addTrigger(17, 17 + 22, 8, Type.strong, 1);
		addTrigger(31, 31 + 22, 8, Type.normal, 3);
		addTrigger(43, 43 + 22, 8, Type.strong, 3);
		addTrigger(64, 64 + 22, 8, Type.normal, 10);
		addTrigger(64, 64 + 29, 8, Type.strong, 1);
		addTrigger(76, 76 - 19, 8, Type.strong, 2);
		addTrigger(87, 87 - 19, 8, Type.normal, 2);
		addTrigger(97, 97 - 19, 8, Type.normal, 2);
		addTrigger(100, 100 + 34, 8, Type.strong, 2);
		addTrigger(103, 103 + 34, 8, Type.normal, 4);
		addTrigger(125, 60 - 19, 8, Type.normal, 10);
		addTrigger(125, 125 - 19, 8, Type.weak, 10);
		addTrigger(125, 125 - 45, 8, Type.becomesBig, 1);
		addTrigger(125, 125 + 22, 22, Type.normal, 5);
		addTrigger(125, 125 + 32, 22, Type.normal, 2);
		addTrigger(125, 220, 23, Type.strong, 3);
		addTrigger(158, 158 - 19, 8, Type.weak, 10);
		addTrigger(158, 158 - 23, 8, Type.strong, 1);
		addTrigger(158, 158 + 22, 23, Type.normal, 3);
		addTrigger(165, 165 + 22, 23, Type.strong, 4);
		addTrigger(176, 176 + 22, 23, Type.normal, 12);
		addTrigger(176, 176 + 22, 23, Type.weak, 10);
		addTrigger(176, 151, 8, Type.strong, 1);
		addTrigger(191, 191 - 19, 23, Type.normal, 5);
		addTrigger(191, 191 - 19, 23, Type.weak, 15);
		addTrigger(191, 191 - 27, 23, Type.strong, 2);
		addTrigger(191, 191 + 34, 23, Type.weak, 10);
		addTrigger(191, 191 + 34, 23, Type.weak, 8);
		addTrigger(191, 191 + 34, 23, Type.normal, 2);
		addTrigger(191, 191 + 42, 23, Type.strong, 2);
		addTrigger(213, 213 + 22, 23, Type.normal, 3);
		addTrigger(213, 213 + 22, 23, Type.strong, 3);
		addTrigger(213, 213 - 19, 23, Type.normal, 7);
		addTrigger(246, 247 - 30, 23, Type.strong, 7);
		addTrigger(246, 225, 23, Type.normal, 2);
		addTrigger(246, 220, 23, Type.becomesBig, 3);
	}

	void addTrigger (float triggerX, float spawnX, float spawnY, Type type, int count) {
		Trigger trigger = new Trigger();
		trigger.x = triggerX;
		triggers.add(trigger);
		int offset = spawnX > triggerX ? 2 : -2;
		for (int i = 0; i < count; i++) {
			Enemy enemy = new Enemy(this, type);
			enemy.position.set(spawnX, spawnY);
			trigger.enemies.add(enemy);
			spawnX += offset;
		}
	}

	void update (float delta) {
		if (player.hp == 0) {
			gameOverTimer += delta / getTimeScale() * timeScale; // Isn't affected by player death time scaling.
			controller.eventGameOver(false);
		}
		updateEnemies(delta);
		updateBullets(delta);
		player.update(delta);
		updateTriggers();
	}

	void updateTriggers () {
		for (int i = 0, n = triggers.size; i < n; i++) {
			Trigger trigger = triggers.get(i);
			if (player.position.x > trigger.x) {
				enemies.addAll(trigger.enemies);
				triggers.removeIndex(i);
				break;
			}
		}
	}

	void updateEnemies (float delta) {
		int alive = 0;
		for (int i = enemies.size - 1; i >= 0; i--) {
			Enemy enemy = enemies.get(i);
			enemy.update(delta);
			if (enemy.deathTimer < 0) {
				enemies.removeIndex(i);
				continue;
			}
			if (enemy.hp > 0) alive++;
			if (enemy.hp > 0 && player.hp > 0) {
				if (enemy.collisionTimer < 0 && enemy.rect.overlaps(player.rect)) {
					if (enemy.rect.y + enemy.rect.height * 0.6f < player.rect.y) {
						// Enemy head bounce.
						float bounceX = Player.headBounceX
							* (enemy.position.x + enemy.rect.width / 2 < player.position.x + player.rect.width / 2 ? 1 : -1);

						enemy.collisionTimer = Enemy.collisionDelay;
						enemy.velocity.x -= bounceX;
						enemy.velocity.y -= 10f;
						enemy.setGrounded(false);
						enemy.hp -= 2;
						if (enemy.hp <= 0)
							enemy.state = State.death;
						else
							enemy.state = State.fall;

						player.velocity.x = bounceX;
						player.velocity.y = Player.headBounceY;
						player.setGrounded(false);
						player.setState(State.fall);

						controller.eventHitEnemy(enemy);

					} else if (player.collisionTimer < 0) {
						// Player gets hit.
						player.dir = enemy.position.x + enemy.rect.width / 2 < player.position.x + player.rect.width / 2 ? -1 : 1;
						float amount = Player.knockbackX * player.dir;
						player.velocity.x = -amount;
						player.velocity.y += Player.knockbackY;
						player.setGrounded(false);
						player.hp--;
						if (player.hp > 0) {
							player.setState(State.fall);
							player.collisionTimer = Player.collisionDelay;

							enemy.velocity.x = amount * 1.6f;
							enemy.velocity.y += 5f;
							enemy.setState(State.fall);
							enemy.jumpDelayTimer = MathUtils.random(0, enemy.jumpDelay);
						} else {
							player.setState(State.death);
							player.velocity.y *= 0.5f;
						}
						enemy.setGrounded(false);
						enemy.collisionTimer = Enemy.collisionDelay;

						controller.eventHitPlayer(enemy);
					}
				}
			}
		}
		// End the game when all enemies are dead and all triggers have occurred.
		if (alive == 0 && triggers.size == 0) controller.eventGameOver(true);
	}

	void updateBullets (float delta) {
		outer:
		for (int i = bullets.size - 5; i >= 0; i -= 5) {
			float vx = bullets.get(i);
			float vy = bullets.get(i + 1);
			float x = bullets.get(i + 2);
			float y = bullets.get(i + 3);
			if (collisionLayer.getCell((int)x, (int)y) != null) {
				// Bullet hit map.
				controller.eventHitBullet(x, y, vx, vy);
				bullets.removeRange(i, i + 4);
				continue;
			}
			if (Math.abs(x - player.position.x) > 25) {
				// Bullet traveled too far.
				bullets.removeRange(i, i + 4);
				continue;
			}
			for (Enemy enemy : enemies) {
				if (enemy.state == State.death) continue;
				if (enemy.bigTimer <= 0 && enemy.rect.contains(x, y)) {
					// Bullet hit enemy.
					bullets.removeRange(i, i + 4);
					controller.eventHitBullet(x, y, vx, vy);
					controller.eventHitEnemy(enemy);
					enemy.collisionTimer = Enemy.collisionDelay;
					enemy.hp--;
					if (enemy.hp <= 0) {
						enemy.state = State.death;
						enemy.velocity.y *= 0.5f;
					} else
						enemy.state = State.fall;
					enemy.velocity.x = MathUtils.random(enemy.knockbackX / 2, enemy.knockbackX)
						* (player.position.x < enemy.position.x + enemy.rect.width / 2 ? 1 : -1);
					enemy.velocity.y += MathUtils.random(enemy.knockbackY / 2, enemy.knockbackY);
					continue outer;
				}
			}
			x += vx * delta;
			y += vy * delta;
			bullets.set(i + 2, x);
			bullets.set(i + 3, y);
		}
	}

	void addBullet (float startX, float startY, float vx, float vy, float angle) {
		bullets.add(vx);
		bullets.add(vy);
		bullets.add(startX);
		bullets.add(startY);
		bullets.add(angle);
	}

	/** Returns rectangles for the tiles within the specified area. */
	Array<Rectangle> getCollisionTiles (int startX, int startY, int endX, int endY) {
		Pools.freeAll(tiles, true);
		tiles.clear();
		for (int y = startY; y <= endY; y++) {
			for (int x = startX; x <= endX; x++) {
				Cell cell = collisionLayer.getCell(x, y);
				if (cell != null) {
					Rectangle rect = Pools.obtain(Rectangle.class);
					rect.set(x, y, 1, 1);
					tiles.add(rect);
				}
			}
		}
		return tiles;
	}

	float getTimeScale () {
		if (player.hp == 0)
			return timeScale * Interpolation.pow2In.apply(0, 1, MathUtils.clamp(gameOverTimer / gameOverSlowdown, 0.01f, 1));
		return timeScale;
	}

	enum State {
		idle, run, jump, death, fall
	}

	static class Trigger {
		float x;
		Array<Enemy> enemies = new Array();
	}
}
