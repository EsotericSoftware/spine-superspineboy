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
import com.esotericsoftware.spine.AnimationState.TrackEntry;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.superspineboy.View.StateView;

/** The view class for an enemy or player that moves around the map. */
class CharacterView {
	View view;
	Model model;
	Skeleton skeleton;
	AnimationState animationState;

	CharacterView (View view) {
		this.view = view;
		model = view.model;
	}

	boolean setAnimation (StateView state, boolean force) {
		// Changes the current animation on track 0 of the AnimationState, if needed.
		Animation animation = state.animation;
		TrackEntry current = animationState.getCurrent(0);
		Animation oldAnimation = current == null ? null : current.getAnimation();
		if (force || oldAnimation != animation) {
			if (state.animation == null) return true;
			TrackEntry entry = animationState.setAnimation(0, state.animation, state.loop);
			if (oldAnimation != null) entry.setTrackTime(state.startTimes.get(oldAnimation, state.defaultStartTime));
			if (!state.loop) entry.setTrackEnd(Float.MAX_VALUE);
			return true;
		}
		return false;
	}
}
