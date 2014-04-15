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

import com.esotericsoftware.spine.superspineboy.Model.State;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/** The model class for an enemy or player that moves around the map. */
class Character {
	static float minVelocityX = 0.001f, maxVelocityY = 20f;
	static float groundedTime = 0.15f;
	static float dampingGroundX = 36, dampingAirX = 15, collideDampingX = 0.7f;
	static float runGroundX = 80, runAirSame = 45, runAirOpposite = 45;

	Model model;
	Vector2 position = new Vector2();
	Vector2 velocity = new Vector2();
	State state = State.idle;
	float stateTime;
	float dir;
	float airTime;
	Rectangle rect = new Rectangle();
	boolean stateChanged;
	float hp;

	float maxVelocityX;
	float collisionOffsetY;
	float jumpVelocity;

	Character (Model model) {
		this.model = model;
	}

	void setState (State newState) {
		if ((state == newState && state != State.fall) || state == State.death) return;
		state = newState;
		stateTime = 0;
		stateChanged = true;
	}

	void update (float delta) {
		stateTime += delta;

		// If moving downward, change state to fall.
		if (velocity.y < 0 && state != State.jump && state != State.fall) {
			setState(State.fall);
			setGrounded(false);
		}

		// Apply gravity.
		velocity.y -= gravity * delta;
		if (velocity.y < 0 && -velocity.y > maxVelocityY) velocity.y = Math.signum(velocity.y) * maxVelocityY;

		boolean grounded = isGrounded();

		// Damping reduces velocity so the character eventually comes to a complete stop.
		float damping = (grounded ? dampingGroundX : dampingAirX) * delta;
		if (velocity.x > 0)
			velocity.x = Math.max(0, velocity.x - damping);
		else
			velocity.x = Math.min(0, velocity.x + damping);
		if (Math.abs(velocity.x) < minVelocityX && grounded) {
			velocity.x = 0;
			setState(State.idle);
		}

		velocity.scl(delta); // Change velocity from units/sec to units since last frame.
		collideX();
		collideY();
		position.add(velocity);
		velocity.scl(1 / delta); // Change velocity back.
	}

	boolean isGrounded () {
		// The character is considered grounded for a short time after leaving the ground, making jumping over gaps easier.
		return airTime < groundedTime;
	}

	void setGrounded (boolean grounded) {
		airTime = grounded ? 0 : groundedTime;
	}

	boolean collideX () {
		rect.x = position.x + velocity.x;
		rect.y = position.y + collisionOffsetY;

		int x;
		if (velocity.x >= 0)
			x = (int)(rect.x + rect.width);
		else
			x = (int)rect.x;
		int startY = (int)rect.y;
		int endY = (int)(rect.y + rect.height);
		for (Rectangle tile : model.getCollisionTiles(x, startY, x, endY)) {
			if (!rect.overlaps(tile)) continue;
			if (velocity.x >= 0)
				position.x = tile.x - rect.width;
			else
				position.x = tile.x + tile.width;
			velocity.x *= collideDampingX;
			return true;
		}
		return false;
	}

	boolean collideY () {
		rect.x = position.x;
		rect.y = position.y + velocity.y + collisionOffsetY;

		int y;
		if (velocity.y > 0)
			y = (int)(rect.y + rect.height);
		else
			y = (int)rect.y;
		int startX = (int)rect.x;
		int endX = (int)(rect.x + rect.width);
		for (Rectangle tile : model.getCollisionTiles(startX, y, endX, y)) {
			if (!rect.overlaps(tile)) continue;
			if (velocity.y > 0)
				position.y = tile.y - rect.height;
			else {
				position.y = tile.y + tile.height;
				if (state == State.jump) setState(State.idle);
				setGrounded(true);
			}
			velocity.y = 0;
			return true;
		}
		return false;
	}

	void moveLeft (float delta) {
		float adjust;
		if (isGrounded()) {
			adjust = runGroundX;
			setState(State.run);
		} else
			adjust = velocity.x <= 0 ? runAirSame : runAirOpposite;
		if (velocity.x > -maxVelocityX) velocity.x = Math.max(velocity.x - adjust * delta, -maxVelocityX);
		dir = -1;
	}

	void moveRight (float delta) {
		float adjust;
		if (isGrounded()) {
			adjust = runGroundX;
			setState(State.run);
		} else
			adjust = velocity.x >= 0 ? runAirSame : runAirOpposite;
		if (velocity.x < maxVelocityX) velocity.x = Math.min(velocity.x + adjust * delta, maxVelocityX);
		dir = 1;
	}

	void jump () {
		velocity.y += jumpVelocity;
		setState(State.jump);
		setGrounded(false);
	}
}
