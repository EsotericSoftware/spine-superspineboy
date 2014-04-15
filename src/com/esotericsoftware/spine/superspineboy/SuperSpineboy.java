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

import com.esotericsoftware.spine.superspineboy.Assets.SoundEffect;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.math.Vector2;

/** The controller class for the game. It knows about both the model and view and provides a way for the view to know about events
 * that occur in the model. */
class SuperSpineboy extends ApplicationAdapter {
	static Vector2 temp = new Vector2();

	View view;
	Model model;

	public void create () {
		model = new Model(this);
		view = new View(model);
	}

	public void render () {
		float delta = Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f) * model.getTimeScale();
		if (delta > 0) {
			model.update(delta);
			view.update(delta);
		}
		view.render();
	}

	public void resize (int width, int height) {
		view.resize(width, height);
	}

	void restart () {
		model.restart();
		view.restart();
	}

	void eventHitPlayer (Enemy enemy) {
		SoundEffect.hurtPlayer.play();
		if (view.player.hp > 0 && view.player.view.hitAnimation != null)
			view.player.view.animationState.setAnimation(1, view.player.view.hitAnimation, false);
	}

	void eventHitEnemy (Enemy enemy) {
		SoundEffect.hurtAlien.play();
		if (enemy.view.hitAnimation != null) enemy.view.animationState.setAnimation(1, enemy.view.hitAnimation, false);
	}

	void eventHitBullet (float x, float y, float vx, float vy) {
		Vector2 offset = temp.set(vx, vy).nor().scl(15 * Model.scale);
		view.hits.add(View.bulletHitTime);
		view.hits.add(x + offset.x);
		view.hits.add(y + offset.y);
		view.hits.add(temp.angle() + 90);
		SoundEffect.hit.play();
	}

	void eventGameOver (boolean win) {
		if (!view.ui.splashTable.hasParent()) {
			view.ui.showSplash(view.assets.gameOverRegion, win ? view.assets.youWinRegion : view.assets.youLoseRegion);
			view.ui.inputTimer = win ? 5 : 1;
		}
		view.jumpPressed = false;
		view.leftPressed = false;
		view.rightPressed = false;
	}

	public static void main (String[] args) throws Exception {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Super Spineboy";
		config.width = 800;
		config.height = 450;
		new LwjglApplication(new SuperSpineboy(), config);
	}
}
