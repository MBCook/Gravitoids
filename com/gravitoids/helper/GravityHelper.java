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

public class GravityHelper {
	private static GravityHelper instance = null;
	
	private static boolean GRAVITY_ENABLED = true;
	
	private double maxInfluence = 5.0d;
	private double gravitationalConstant;
	
	public static synchronized GravityHelper getInstance() {
		if (instance == null)
			instance = new GravityHelper(10.0d);
		
		return instance;
	}
	
	public GravityHelper(double gravitationalConstant) {
		this.gravitationalConstant = gravitationalConstant;
	}
	
	public static boolean isGravityEnabled() {
		return GRAVITY_ENABLED;
	}
	
	public static void setGravityEnabled(boolean enabled) {
		GRAVITY_ENABLED = enabled;
	}
	
	private void simulateGravity(GravitoidsObject one, GravitoidsObject two, boolean moveObjectTwo) {
		/*
		 * Fg = G * (M1 * M2) / R^2 (Force Gravity = constant * masses / distance squared)
		 * Vt = V0 + At				(Velocity = old velocity + acceleration)
		 * F = M * A				(Force = Mass * Acceleration)
		 * 
		 * thus
		 * 
		 * A = ((G * M1 * M2) / R^2) / M (Acceleration due to gravity)
		 */
		
		// First, make sure that there is something to move
		
		if ((!one.isMoveable()) && (!two.isMoveable())) {
			return;			// Nothing to do
		}

		// Now the straight distance between them
		
		double distance = WrappingHelper.calculateDistanceToObject(one, two);
		
		// Figure out the force from gravity
		
		double massProduct = one.getMass() * two.getMass();
		
		double forceOfGravity = (gravitationalConstant * massProduct) / Math.pow(distance, 2.0);
		
		if (forceOfGravity > maxInfluence)
			forceOfGravity = maxInfluence;
		
		// Now the angle between the two things
		
		double angle = WrappingHelper.calculateDirectionToObject(one, two);
		
		// The numbers
		
		double theirXPosition = two.getXPosition();
		
		if (Math.abs(one.getXPosition() - theirXPosition) > GravitoidsPanel.PANEL_WIDTH / 2.0) {
			if (theirXPosition > one.getXPosition()) {
				theirXPosition -= GravitoidsPanel.PANEL_WIDTH;
			} else {
				theirXPosition += GravitoidsPanel.PANEL_WIDTH;
			}
		}
		
		// Why do BOTH of these use theirXPosition and not one X one Y? I don't know!
		// I'm guessing it's a negative angle thing, but I can't remember that bit of trig
		//		well enough at the moment to really care. This works, that's what matters.
		
		double xForce = Math.cos(angle) * forceOfGravity * (one.getXPosition() > theirXPosition ? -1 : 1);
		double yForce = Math.sin(angle) * forceOfGravity * (one.getXPosition() > theirXPosition ? -1 : 1);
		
		// Now set the new velocities, dividing by the mass so results are correct

		one.setXGravitationalForce((xForce / one.getMass()));
		one.setYGravitationalForce((yForce / one.getMass()));
		
		if (moveObjectTwo) {
			two.setXGravitationalForce((xForce / two.getMass()));
			two.setYGravitationalForce((yForce / two.getMass()));
		}
	}
	
	public void simulateGravity(GravitoidsObject one, GravitoidsObject two) {
		simulateGravity(one, two, true);
	}
	
	public void simulateGravityForOne(GravitoidsObject one, GravitoidsObject two) {
		simulateGravity(one, two, false);
	}

	/**
	 * @return the maxInfluence
	 */
	public double getMaxInfluence() {
		return maxInfluence;
	}

	/**
	 * @param maxInfluence the maxInfluence to set
	 */
	public void setMaxInfluence(double maxInfluence) {
		this.maxInfluence = maxInfluence;
	}

	/**
	 * @return the gravitationalConstant
	 */
	public double getGravitationalConstant() {
		return gravitationalConstant;
	}

	/**
	 * @param gravitationalConstant the gravitationalConstant to set
	 */
	public void setGravitationalConstant(double gravitationalConstant) {
		this.gravitationalConstant = gravitationalConstant;
	}
}
