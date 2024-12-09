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

/** The model class for the player. */
class Player extends Character {
	static float heightSource = 625, width = 67 * scale, height = 285 * scale;
	static float hpStart = 4, hpDuration = 15;

	static float maxVelocityGroundX = 12.5f, maxVelocityAirX = 13.5f;
	static float playerJumpVelocity = 22f, jumpDamping = 0.5f, jumpOffsetVelocity = 10, jumpOffsetY = 120 * scale;
	static float airJumpTime = 0.1f;

	static float shootDelay = 0.1f, shootOffsetX = 160, shootOffsetY = 11;
	static float bulletSpeed = 34, bulletInheritVelocity = 0.4f, burstDuration = 0.18f;
	static float kickbackShots = 33, kickbackAngle = 30, kickbackVarianceShots = 11, kickbackVariance = 6, kickback = 1.6f;

	static float knockbackX = 14, knockbackY = 5, collisionDelay = 2.5f, flashTime = 0.07f;
	static float headBounceX = 12, headBounceY = 20;

	float shootTimer;
	float collisionTimer;
	float hpTimer;

	// This is here for convenience, the model should never touch the view.
	PlayerView view;

	Player (Model model) {
		super(model);
		rect.width = width;
		rect.height = height;
		hp = hpStart;
		jumpVelocity = playerJumpVelocity;
	}

	void update (float delta) {
		stateChanged = false;

		shootTimer -= delta;

		if (hp > 0) {
			hpTimer -= delta;
			if (hpTimer < 0) {
				hpTimer = hpDuration;
				if (hp < hpStart) hp++;
			}
		}

		collisionTimer -= delta;
		rect.height = height - collisionOffsetY;
		maxVelocityX = isGrounded() ? maxVelocityGroundX : maxVelocityAirX;
		super.update(delta);
	}
}
