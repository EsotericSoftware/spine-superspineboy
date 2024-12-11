/******************************************************************************
* Spine Runtimes License Agreement
* Last updated February 20, 2024. Replaces all prior versions.
*
* Copyright (c) 2013-2024, Esoteric Software LLC
*
* Integration of the Spine Runtimes into software or otherwise creating derivative works 
* of the Spine Runtimes is permitted under the terms and conditions of Section 2 of the 
* Spine Editor License Agreement:
* https://esotericsoftware.com/spine-editor-license
*
* Otherwise, it is permitted to integrate the Spine Runtimes into software or otherwise
* create derivative works of the Spine Runtimes (collectively, "Products"), provided that 
* each user of the Products must obtain their own Spine Editor license and redistribution 
* of the Products in any form must include this license and copyright notice.
*
* THE SPINE RUNTIMES ARE PROVIDED BY ESOTERIC SOFTWARE LLC "AS IS" AND ANY EXPRESS OR IMPLIED 
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
* FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL ESOTERIC SOFTWARE LLC BE LIABLE FOR 
* ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES, 
* BUSINESS INTERRUPTION, OR LOSS OF USE, DATA, OR PROFITS) HOWEVER CAUSED AND ON ANY 
* THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THE SPINE RUNTIMES, 
* EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *****************************************************************************/

package com.esotericsoftware.spine.superspineboy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.math.Vector2;

import com.esotericsoftware.spine.AnimationState.TrackEntry;
import com.esotericsoftware.spine.superspineboy.Assets.SoundEffect;

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
		if (view.player.hp > 0 && view.player.view.hitAnimation != null) {
			TrackEntry entry = view.player.view.animationState.setAnimation(1, view.player.view.hitAnimation, false);
			entry.setTrackEnd(view.player.view.hitAnimation.getDuration());
		}
	}

	void eventHitEnemy (Enemy enemy) {
		SoundEffect.hurtAlien.play();
		if (enemy.view.hitAnimation != null) {
			TrackEntry entry = enemy.view.animationState.setAnimation(1, enemy.view.hitAnimation, false);
			entry.setTrackEnd(enemy.view.hitAnimation.getDuration());
		}
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
