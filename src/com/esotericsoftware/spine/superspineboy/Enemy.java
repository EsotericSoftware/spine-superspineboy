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

import com.esotericsoftware.spine.superspineboy.Model.State;

import com.badlogic.gdx.math.MathUtils;

/** The model class for an enemy. */
class Enemy extends Character {
	static float heightSource = 398, width = 105 * scale, height = 200 * scale;

	static float maxVelocityMinX = 4f, maxVelocityMaxX = 8.5f, maxVelocityAirX = 19f;
	static float hpWeak = 1, hpSmall = 2, hpNormal = 3, hpStrong = 5, hpBecomesBig = 8, hpBig = 20;
	static float corpseTime = 5 * 60, fadeTime = 3;
	static float jumpDistanceNormal = 20, jumpDelayNormal = 1.6f, jumpVelocityNormal = 12, jumpVelocityBig = 18;
	static float sizeSmall = 0.5f, sizeBig = 2.5f, sizeStrong = 1.3f, bigDuration = 2, smallCount = 14;

	static float normalKnockbackX = 19, normalKnockbackY = 9, bigKnockbackX = 12, bigKnockbackY = 6;
	static float collisionDelay = 0.3f;

	float deathTimer = corpseTime;
	float maxVelocityGroundX;
	float collisionTimer;
	float jumpDelayTimer, jumpDistance, jumpDelay;
	Type type;
	float size = 1;
	float bigTimer;
	float spawnSmallsTimer;
	boolean move;
	boolean forceJump;
	int collisions;
	float knockbackX = normalKnockbackX, knockbackY = normalKnockbackY;

	// This is here for convenience, the model should never touch the view.
	EnemyView view;

	Enemy (Model model, Type type) {
		super(model);
		this.type = type;

		rect.width = width;
		rect.height = height;

		maxVelocityGroundX = MathUtils.random(maxVelocityMinX, maxVelocityMaxX);
		maxVelocityX = maxVelocityGroundX;
		jumpVelocity = jumpVelocityNormal;
		jumpDelay = jumpDelayNormal;
		jumpDistance = jumpDistanceNormal;

		if (type == Type.big) {
			size = sizeBig;
			rect.width = width * size * 0.7f;
			rect.height = height * size * 0.7f;
			hp = hpBig;
			knockbackX = normalKnockbackX;
			knockbackY = normalKnockbackY;
		} else if (type == Type.small) {
			size = sizeSmall;
			rect.width = width * size;
			rect.height = height * size;
			hp = hpSmall;
		} else if (type == Type.weak)
			hp = hpWeak;
		else if (type == Type.becomesBig)
			hp = hpBecomesBig;
		else if (type == Type.strong) {
			hp = hpStrong;
			size = sizeStrong;
			jumpVelocity *= 1.5f;
			jumpDistance *= 1.4f;
		} else
			hp = hpNormal;

		jumpDelayTimer = MathUtils.random(0, jumpDelay);
	}

	void update (float delta) {
		stateChanged = false;

		if (state == State.death) {
			if (type == Type.becomesBig && size == 1) {
				bigTimer = bigDuration;
				collisionTimer = bigDuration;
				state = State.run;
				hp = hpBig;
				knockbackX = bigKnockbackX;
				knockbackY = bigKnockbackY;
				type = Type.big;
				jumpVelocity = jumpVelocityBig;
			} else if (type == Type.big) {
				spawnSmallsTimer = 0.8333f;
				type = Type.normal;
			}
		}

		// Enemy grows to a big enemy.
		if (bigTimer > 0) {
			bigTimer -= delta;
			size = 1 + (sizeBig - 1) * (1 - Math.max(0, bigTimer / bigDuration));
			rect.width = width * size * 0.7f;
			rect.height = height * size * 0.7f;
		}

		// Big enemy explodes into small ones.
		if (spawnSmallsTimer > 0) {
			spawnSmallsTimer -= delta;
			if (spawnSmallsTimer < 0) {
				for (int i = 0; i < smallCount; i++) {
					Enemy small = new Enemy(model, Type.small);
					small.position.set(position.x, position.y + 2);
					small.velocity.x = MathUtils.random(5, 15) * (MathUtils.randomBoolean() ? 1 : -1);
					small.velocity.y = MathUtils.random(10, 25);
					small.setGrounded(false);
					model.enemies.add(small);
				}
			}
		}

		// Nearly dead enemies jump at the player right away.
		if (hp == 1 && type != Type.weak && type != Type.small) jumpDelayTimer = 0;

		// Kill enemies stuck in the map or those that have somehow fallen out of the map.
		if (state != State.death && (hp <= 0 || position.y < -100 || collisions > 100)) {
			state = State.death;
			hp = 0;
		}

		// Simple enemy AI.
		boolean grounded = isGrounded();
		if (grounded) move = true;
		collisionTimer -= delta;
		maxVelocityX = grounded ? maxVelocityGroundX : maxVelocityAirX;
		if (state == State.death)
			deathTimer -= delta;
		else if (collisionTimer < 0) {
			if (model.player.hp == 0) {
				// Enemies win, jump for joy!
				if (grounded && velocity.x == 0) {
					jumpVelocity = jumpVelocityNormal / 2;
					dir = -dir;
					jump();
				}
			} else {
				// Jump if within range of the player.
				if (grounded && (forceJump || Math.abs(model.player.position.x - position.x) < jumpDistance)) {
					jumpDelayTimer -= delta;
					if (state != State.jump && jumpDelayTimer < 0 && position.y <= model.player.position.y) {
						jump();
						jumpDelayTimer = MathUtils.random(0, jumpDelay);
						forceJump = false;
					}
				}
				// Move toward the player.
				if (move) {
					if (model.player.position.x > position.x) {
						if (velocity.x >= 0) moveRight(delta);
					} else if (velocity.x <= 0) //
						moveLeft(delta);
				}
			}
		}

		int previousCollision = collisions;
		super.update(delta);
		if (!grounded || collisions == previousCollision) collisions = 0;
	}

	boolean collideX () {
		boolean result = super.collideX();
		if (result) {
			// If grounded and collided with the map, jump to avoid the obstacle.
			if (isGrounded()) forceJump = true;
			collisions++;
		}
		return result;
	}

	enum Type {
		weak, normal, strong, becomesBig, big, small
	}
}
