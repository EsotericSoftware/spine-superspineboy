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

import static com.esotericsoftware.spine.superspineboy.Model.*;

import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonJson;
import com.esotericsoftware.spine.superspineboy.Model.State;
import com.esotericsoftware.spine.superspineboy.View.StateView;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ObjectMap;

/** Centralized place to load and store assets. */
class Assets {
	TextureAtlas playerAtlas, enemyAtlas;
	TextureRegion bulletRegion, hitRegion, crosshair;
	TextureRegion titleRegion, gameOverRegion, youLoseRegion, youWinRegion, startRegion;

	SkeletonData playerSkeletonData, enemySkeletonData;
	AnimationStateData playerAnimationData, enemyAnimationData;
	ObjectMap<State, StateView> playerStates = new ObjectMap(), enemyStates = new ObjectMap();

	Assets () {
		bulletRegion = loadRegion("bullet.png");
		hitRegion = loadRegion("bullet-hit.png");
		titleRegion = loadRegion("title.png");
		gameOverRegion = loadRegion("gameOver.png");
		youLoseRegion = loadRegion("youLose.png");
		youWinRegion = loadRegion("youWin.png");
		startRegion = loadRegion("start.png");
		crosshair = loadRegion("crosshair.png");

		SoundEffect.shoot.sound = Gdx.audio.newSound(Gdx.files.internal("sounds/shoot.ogg"));
		SoundEffect.hit.sound = Gdx.audio.newSound(Gdx.files.internal("sounds/hit.ogg"));
		SoundEffect.footstep1.sound = Gdx.audio.newSound(Gdx.files.internal("sounds/footstep1.ogg"));
		SoundEffect.footstep2.sound = Gdx.audio.newSound(Gdx.files.internal("sounds/footstep2.ogg"));
		SoundEffect.squish.sound = Gdx.audio.newSound(Gdx.files.internal("sounds/squish.ogg"));
		SoundEffect.squish.volume = 0.6f;
		SoundEffect.hurtPlayer.sound = Gdx.audio.newSound(Gdx.files.internal("sounds/hurt-player.ogg"));
		SoundEffect.hurtAlien.sound = Gdx.audio.newSound(Gdx.files.internal("sounds/hurt-alien.ogg"));
		SoundEffect.hurtAlien.volume = 0.5f;

		loadPlayerAssets();
		loadEnemyAssets();
	}

	TextureRegion loadRegion (String name) {
		Texture texture = new Texture(name);
		texture.setFilter(TextureFilter.Nearest, TextureFilter.Linear);
		return new TextureRegion(texture);
	}

	void loadPlayerAssets () {
		playerAtlas = new TextureAtlas(Gdx.files.internal("spineboy/spineboy.atlas"));

		SkeletonJson json = new SkeletonJson(playerAtlas);
		json.setScale(Player.height / Player.heightSource);
		playerSkeletonData = json.readSkeletonData(Gdx.files.internal("spineboy/spineboy.json"));

		playerAnimationData = new AnimationStateData(playerSkeletonData);
		playerAnimationData.setDefaultMix(0.2f);
		setMix(playerAnimationData, "idle", "run", 0.3f);
		setMix(playerAnimationData, "run", "idle", 0.1f);
		setMix(playerAnimationData, "shoot", "shoot", 0);

		setupState(playerStates, State.death, playerSkeletonData, "death", false);
		StateView idle = setupState(playerStates, State.idle, playerSkeletonData, "idle", true);
		StateView jump = setupState(playerStates, State.jump, playerSkeletonData, "jump", false);
		StateView run = setupState(playerStates, State.run, playerSkeletonData, "run", true);
		if (idle.animation != null) run.startTimes.put(idle.animation, 8 * fps);
		if (jump.animation != null) run.startTimes.put(jump.animation, 22 * fps);
		StateView fall = setupState(playerStates, State.fall, playerSkeletonData, "jump", false);
		fall.defaultStartTime = 22 * fps;
	}

	void loadEnemyAssets () {
		enemyAtlas = new TextureAtlas(Gdx.files.internal("alien/alien.atlas"));

		SkeletonJson json = new SkeletonJson(enemyAtlas);
		json.setScale(Enemy.height / Enemy.heightSource);
		enemySkeletonData = json.readSkeletonData(Gdx.files.internal("alien/alien.json"));

		enemyAnimationData = new AnimationStateData(enemySkeletonData);
		enemyAnimationData.setDefaultMix(0.1f);

		setupState(enemyStates, State.idle, enemySkeletonData, "run", true);
		setupState(enemyStates, State.jump, enemySkeletonData, "jump", true);
		setupState(enemyStates, State.run, enemySkeletonData, "run", true);
		setupState(enemyStates, State.death, enemySkeletonData, "death", false);
		setupState(enemyStates, State.fall, enemySkeletonData, "run", false);
	}

	void setMix (AnimationStateData data, String from, String to, float mix) {
		Animation fromAnimation = data.getSkeletonData().findAnimation(from);
		Animation toAnimation = data.getSkeletonData().findAnimation(to);
		if (fromAnimation == null || toAnimation == null) return;
		data.setMix(fromAnimation, toAnimation, mix);
	}

	StateView setupState (ObjectMap map, State state, SkeletonData skeletonData, String name, boolean loop) {
		StateView stateView = new StateView();
		stateView.animation = skeletonData.findAnimation(name);
		stateView.loop = loop;
		map.put(state, stateView);
		return stateView;
	}

	void dispose () {
		playerAtlas.dispose();
		enemyAtlas.dispose();
	}

	enum SoundEffect {
		shoot, hit, footstep1, footstep2, squish, hurtPlayer, hurtAlien;

		Sound sound;
		float volume = 1;

		void play () {
			sound.play(volume);
		}
	}
}
