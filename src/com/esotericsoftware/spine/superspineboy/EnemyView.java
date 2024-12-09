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

import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationState.AnimationStateAdapter;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.EventData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.Skeleton.Physics;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.superspineboy.Assets.SoundEffect;
import com.esotericsoftware.spine.superspineboy.Enemy.Type;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/** The view class for an enemy. */
class EnemyView extends CharacterView {
	Enemy enemy;
	Animation hitAnimation;
	Slot headSlot;
	Attachment burstHeadAttachment;
	Color headColor;

	EnemyView (final View view, Enemy enemy) {
		super(view);
		this.enemy = enemy;

		skeleton = new Skeleton(view.assets.enemySkeletonData);
		burstHeadAttachment = skeleton.getAttachment("head", "burst01");
		headSlot = skeleton.findSlot("head");
		hitAnimation = skeleton.getData().findAnimation("hit");

		animationState = new AnimationState(view.assets.enemyAnimationData);

		// Play squish sound when enemies die.
		final EventData squishEvent = view.assets.enemySkeletonData.findEvent("squish");
		animationState.addListener(new AnimationStateAdapter() {
			public void event (int trackIndex, Event event) {
				if (event.getData() == squishEvent) SoundEffect.squish.play();
			}
		});

		// Enemies have slight color variations.
		if (enemy.type == Type.strong)
			headColor = new Color(1, 0.6f, 1, 1);
		else
			headColor = new Color(MathUtils.random(0.8f, 1), MathUtils.random(0.8f, 1), MathUtils.random(0.8f, 1), 1);
		headSlot.getColor().set(headColor);
	}

	void update (float delta) {
		// Change head attachment for enemies that are about to die.
		if (enemy.hp == 1 && enemy.type != Type.weak) headSlot.setAttachment(burstHeadAttachment);

		// Change color for big enemies.
		if (enemy.type == Type.big) headSlot.getColor().set(headColor).lerp(0, 1, 1, 1, 1 - enemy.bigTimer / Enemy.bigDuration);

		skeleton.setX(enemy.position.x + Enemy.width / 2);
		skeleton.setY(enemy.position.y);

		if (!setAnimation(view.assets.enemyStates.get(enemy.state), enemy.stateChanged)) animationState.update(delta);
		animationState.apply(skeleton);

		Bone root = skeleton.getRootBone();
		root.setScaleX(root.getScaleX() * enemy.size);
		root.setScaleY(root.getScaleY() * enemy.size);

		skeleton.setScaleX(enemy.dir);
		skeleton.updateWorldTransform(Physics.none);
	}
}
