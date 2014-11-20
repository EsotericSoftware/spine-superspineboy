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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationState.AnimationStateAdapter;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.EventData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.superspineboy.Assets.SoundEffect;
import com.esotericsoftware.spine.superspineboy.Model.State;

/** The view class for the player. */
class PlayerView extends CharacterView {
	Player player;
	Bone rearUpperArmBone, rearBracerBone, gunBone, headBone, torsoBone, frontUpperArmBone;
	Animation shootAnimation, hitAnimation;
	boolean canShoot;
	float burstShots, burstTimer;
	Vector2 temp1 = new Vector2(), temp2 = new Vector2();

	PlayerView (final View view) {
		super(view);
		player = view.player;

		skeleton = new Skeleton(view.assets.playerSkeletonData);
		rearUpperArmBone = skeleton.findBone("rear_upper_arm");
		rearBracerBone = skeleton.findBone("rear_bracer");
		gunBone = skeleton.findBone("gun");
		headBone = skeleton.findBone("head");
		torsoBone = skeleton.findBone("torso");
		frontUpperArmBone = skeleton.findBone("front_upper_arm");

		shootAnimation = view.assets.playerSkeletonData.findAnimation("shoot");
		hitAnimation = view.assets.playerSkeletonData.findAnimation("hit");

		animationState = new AnimationState(view.assets.playerAnimationData);

		// Play footstep sounds.
		final EventData footstepEvent = view.assets.playerSkeletonData.findEvent("footstep");
		animationState.addListener(new AnimationStateAdapter() {
			public void event (int trackIndex, Event event) {
				if (event.getData() == footstepEvent) {
					if (event.getInt() == 1)
						SoundEffect.footstep1.play();
					else
						SoundEffect.footstep2.play();
				}
			}
		});
	}

	void update (float delta) {
		// When not shooting, reset the number of burst shots.
		if (!view.touched && burstTimer > 0) {
			burstTimer -= delta;
			if (burstTimer < 0) burstShots = 0;
		}

		// If jump was pressed in the air, jump as soon as grounded.
		if (view.jumpPressed && player.isGrounded()) jump();

		skeleton.setX(player.position.x + width / 2);
		skeleton.setY(player.position.y);

		if (!setAnimation(view.assets.playerStates.get(player.state), player.stateChanged)) animationState.update(delta);
		animationState.apply(skeleton);

		Vector2 mouse = temp1.set(Gdx.input.getX(), Gdx.input.getY());
		view.viewport.unproject(mouse);

		// Determine if the player can shoot at the mouse position.
		canShoot = false;
		if (rearUpperArmBone == null || rearBracerBone == null || gunBone == null)
			canShoot = true;
		else if (player.hp > 0 && !view.ui.hasSplash
			&& (Math.abs(skeleton.getY() - mouse.y) > 2.7f || Math.abs(skeleton.getX() - mouse.x) > 0.75f)) {
			// Store bone rotations from the animation that was applied.
			float rearUpperArmRotation = rearUpperArmBone.getRotation();
			float rearBracerRotation = rearBracerBone.getRotation();
			float gunRotation = gunBone.getRotation();
			// Straighten the arm and don't flipX, so the arm can more easily point at the mouse.
			rearUpperArmBone.setRotation(0);
			float shootRotation = 11;
			if (animationState.getCurrent(1) == null) {
				rearBracerBone.setRotation(0);
				gunBone.setRotation(0);
			} else
				shootRotation += 25; // Use different rotation when shoot animation was applied.
			skeleton.setFlipX(false);
			skeleton.updateWorldTransform();

			// Compute the arm's angle to the mouse, flipping it based on the direction the player faces.
			Vector2 bonePosition = temp2.set(rearUpperArmBone.getWorldX() + skeleton.getX(),
				rearUpperArmBone.getWorldY() + skeleton.getY());
			float angle = bonePosition.sub(mouse).angle();
			float behind = (angle < 90 || angle > 270) ? -1 : 1;
			if (behind == -1) angle = -angle;
			if (player.state == State.idle || (view.touched && (player.state == State.jump || player.state == State.fall)))
				player.dir = behind;
			if (behind != player.dir) angle = -angle;
			if (player.state != State.idle && behind != player.dir) {
				// Don't allow the player to shoot behind themselves unless idle. Use the rotations stored earlier from the animation.
				rearBracerBone.setRotation(rearBracerRotation);
				rearUpperArmBone.setRotation(rearUpperArmRotation);
				gunBone.setRotation(gunRotation);
			} else {
				if (behind == 1) angle += 180;
				// Adjust the angle upward based on the number of shots in the current burst.
				angle += kickbackAngle * Math.min(1, burstShots / kickbackShots) * (burstTimer / burstDuration);
				float gunArmAngle = angle - shootRotation;
				// Compute the head, torso and front arm angles so the player looks up or down.
				float headAngle;
				if (player.dir == -1) {
					angle += 360;
					if (angle < 180)
						headAngle = 25 * Interpolation.pow2In.apply(Math.min(1, angle / 50f));
					else
						headAngle = -15 * Interpolation.pow2In.apply(1 - Math.max(0, angle - 310) / 50f);
				} else {
					if (angle < 360)
						headAngle = -15 * Interpolation.pow2In.apply(1 - Math.max(0, (angle - 310) / 50f));
					else
						headAngle = 25 * Interpolation.pow2In.apply(1 - Math.max(0, (410 - angle) / 50f));
				}
				float torsoAngle = headAngle * 0.75f;
				if (headBone != null) headBone.setRotation(headBone.getRotation() + headAngle);
				if (torsoBone != null) torsoBone.setRotation(torsoBone.getRotation() + torsoAngle);
				if (frontUpperArmBone != null) frontUpperArmBone.setRotation(frontUpperArmBone.getRotation() - headAngle * 1.4f);
				rearUpperArmBone.setRotation(gunArmAngle - torsoAngle - rearUpperArmBone.getWorldRotation());
				canShoot = true;
			}
		}

		skeleton.setFlipX(player.dir == -1);
		skeleton.updateWorldTransform();
	}

	void jump () {
		view.jumpPressed = false;
		player.jump();
		setAnimation(view.assets.playerStates.get(State.jump), true);
	}

	void shoot () {
		if (!canShoot || player.shootTimer >= 0) return;
		player.shootTimer = shootDelay;
		burstTimer = burstDuration;

		// Compute the position and velocity to spawn a new bullet.
		float x = skeleton.getX(), y = skeleton.getY();
		if (rearUpperArmBone != null && rearBracerBone != null && gunBone != null) {
			x += rearUpperArmBone.getWorldX();
			y += rearUpperArmBone.getWorldY();
		} else {
			x += width / 2;
			y += height / 2;
		}
		float mouseX = Gdx.input.getX(), mouseY = Gdx.input.getY();

		float angle = view.viewport.unproject(temp1.set(mouseX, mouseY)).sub(x, y).angle();
		angle += kickbackAngle * Math.min(1, burstShots / kickbackShots) * player.dir;
		float variance = kickbackVariance * Math.min(1, burstShots / kickbackVarianceShots);
		angle += MathUtils.random(-variance, variance);

		float cos = MathUtils.cosDeg(angle), sin = MathUtils.sinDeg(angle);
		float vx = cos * bulletSpeed + player.velocity.x * bulletInheritVelocity;
		float vy = sin * bulletSpeed + player.velocity.y * bulletInheritVelocity;
		if (rearUpperArmBone != null && rearBracerBone != null && gunBone != null) {
			x = skeleton.getX() + gunBone.getWorldX();
			y = skeleton.getY() + gunBone.getWorldY() + shootOffsetY * scale;
			x += cos * shootOffsetX * scale;
			y += sin * shootOffsetX * scale;
		}
		model.addBullet(x, y, vx, vy, temp1.set(vx, vy).angle());
		if (shootAnimation != null) animationState.setAnimation(1, shootAnimation, false);

		view.camera.position.sub(view.shakeX, view.shakeY, 0);
		view.shakeX += View.cameraShake * (MathUtils.randomBoolean() ? 1 : -1);
		view.shakeY += View.cameraShake * (MathUtils.randomBoolean() ? 1 : -1);
		view.camera.position.add(view.shakeX, view.shakeY, 0);

		player.velocity.x -= kickback * player.dir;
		SoundEffect.shoot.play();

		burstShots = Math.min(kickbackShots, burstShots + 1);
	}
}
