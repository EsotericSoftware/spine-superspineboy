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

import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationState.AnimationStateAdapter;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.EventData;
import com.esotericsoftware.spine.Skeleton;
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

		skeleton.setFlipX(enemy.dir == -1);
		skeleton.updateWorldTransform();
	}
}
