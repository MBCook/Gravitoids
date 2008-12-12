package com.gravitoids.helper;

import com.gravitoids.bean.GravitoidsObject;
import com.gravitoids.panel.GravitoidsPanel;

/**
 * Copyright (c) 2008, Michael Cook
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Michael Cook nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Cook ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Michael Cook BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

public class WrappingHelper {
	// In this file, "we/our" refers to object one
	
	public static double calculateDistanceToObject(GravitoidsObject one, GravitoidsObject two) {
		// Takes into account wrapping around the screen
		
		double xDistance = one.getXPosition() - two.getXPosition();
		double yDistance = one.getYPosition() - two.getYPosition(); 
		
		if (Math.abs(xDistance) > (GravitoidsPanel.PANEL_WIDTH / 2.0)) {
			if (one.getXPosition() < two.getXPosition()) {
				// We're to the left of them, move them to our left
				
				xDistance = one.getXPosition() - (two.getXPosition() - GravitoidsPanel.PANEL_WIDTH);
			} else {
				// We're to the right of them, move them to our right
				
				xDistance = one.getXPosition() - (two.getXPosition() + GravitoidsPanel.PANEL_WIDTH);
			}
		}
		
		if (Math.abs(yDistance) > (GravitoidsPanel.PANEL_HEIGHT / 2.0)) {
			if (one.getYPosition() < two.getYPosition()) {
				// We're above them, move them above us
				
				yDistance = one.getYPosition() - (two.getYPosition() - GravitoidsPanel.PANEL_HEIGHT);
			} else {
				// We're below them, move them below us
				
				yDistance = one.getYPosition() - (two.getYPosition() + GravitoidsPanel.PANEL_HEIGHT);
			}
		}
		
		return Math.sqrt(Math.pow(xDistance, 2.0) + Math.pow(yDistance, 2.0));
	}
	
	public static double calculateDirectionToObject(GravitoidsObject one, GravitoidsObject two) {
		// Takes into account wrapping around the screen
		
		double xDistance = one.getXPosition() - two.getXPosition();
		double yDistance = one.getYPosition() - two.getYPosition(); 
		
		double theirX = two.getXPosition();
		double theirY = two.getYPosition();
		
		if (Math.abs(xDistance) > (GravitoidsPanel.PANEL_WIDTH / 2.0)) {
			if (one.getXPosition() < two.getXPosition()) {
				// We're to the left of them, move them to our left
				
				theirX = two.getXPosition() - GravitoidsPanel.PANEL_WIDTH;
			} else {
				// We're to the right of them, move them to our right
				
				theirX = two.getXPosition() + GravitoidsPanel.PANEL_WIDTH;
			}
		}
		
		if (Math.abs(yDistance) > (GravitoidsPanel.PANEL_HEIGHT / 2.0)) {
			if (one.getYPosition() < two.getYPosition()) {
				// We're above them, move them above us
				
				theirY = two.getYPosition() - GravitoidsPanel.PANEL_HEIGHT;
			} else {
				// We're below them, move them below us
				
				theirY = two.getYPosition() + GravitoidsPanel.PANEL_HEIGHT;
			}
		}
		
		return Math.atan((one.getXPosition() - theirX) / (one.getYPosition() - theirY));
	}
}
