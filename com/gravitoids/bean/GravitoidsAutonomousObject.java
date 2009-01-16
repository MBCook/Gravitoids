package com.gravitoids.bean;

import java.util.ArrayList;
import java.util.List;

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

public abstract class GravitoidsAutonomousObject extends GravitoidsObject {
	protected static final double MAX_OBJECT_SPEED = 50.0;
	protected static final double MAX_OBJECT_THRUST = 25.0;
	
	protected double xThrustPortion = 0.0;
	protected double yThrustPortion = 0.0;
	
	protected double thrust = 0.0;
	
	protected List<Double> oldXThrusts = new ArrayList<Double>();
	protected List<Double> oldYThrusts = new ArrayList<Double>();

	private static boolean THRUST_ENABLED = true;
	
	public static boolean isThrustEnabled() {
		return THRUST_ENABLED;
	}
	
	public static void setThrustEnabled(boolean enabled) {
		THRUST_ENABLED = enabled;
	}
	
	public void resetObject() {
		super.resetObject();
		
		xThrustPortion = 0.0;
		yThrustPortion = 0.0;
		thrust = 0.0;
		
		oldXThrusts.clear();
		oldYThrusts.clear();
	}
	
	public void normalizeThrust() {
		double total = Math.sqrt(Math.pow(xThrustPortion, 2.0) + Math.pow(yThrustPortion, 2.0));
		
		xThrustPortion = xThrustPortion / total;
		yThrustPortion = yThrustPortion / total;
	}
	
	public double getThrust() {
		return thrust;
	}

	public void setThrust(double thrust) {
		if (thrust > MAX_OBJECT_THRUST) {
			thrust = MAX_OBJECT_THRUST;
		}
		
		this.thrust = thrust;
	}

	public double getXThrustPortion() {
		return xThrustPortion;
	}

	public void setXThrustPortion(double thurstPortion) {
		xThrustPortion = thurstPortion;
	}

	public double getYThrustPortion() {
		return yThrustPortion;
	}

	public void setYThrustPortion(double thrustPortion) {
		yThrustPortion = thrustPortion;
	}

	public abstract void prepareMove(GravitoidsObject[] stuff);
	
	public void move() {
		// Calculate how fast they want to move this time
		
		oldXThrusts.add(thrust * xThrustPortion * getSpeedFactor());
		oldYThrusts.add(thrust * yThrustPortion * getSpeedFactor());
		
		// Now remove any extra entries
		
		if (oldXThrusts.size() > 3) {
			oldXThrusts.remove(0);
			oldYThrusts.remove(0);
		}
		
		// Now figure out how much to thrust, since we are adding 'thrust momentum' of 3 frames
		
		double newXThrust = 0.0;
		double newYThrust = 0.0;
		
		if (oldXThrusts.size() == 1) {
			newXThrust = oldXThrusts.get(0);
			newYThrust = oldYThrusts.get(0);
		} else if (oldXThrusts.size() == 2) {
			newXThrust = .75 * oldXThrusts.get(1) + .25 * oldXThrusts.get(0);
			newYThrust = .75 * oldYThrusts.get(1) + .25 * oldYThrusts.get(0);
		} else {
			newXThrust = .75 * oldXThrusts.get(2) + .1875 * oldXThrusts.get(1) + .0625 * oldXThrusts.get(0);
			newYThrust = .75 * oldYThrusts.get(2) + .1875 * oldYThrusts.get(1) + .0625 * oldYThrusts.get(0);
		}
		
		// Use our thrust to alter our speed
		
		if (THRUST_ENABLED) {
			setXSpeed(getXSpeed() + newXThrust);
			setYSpeed(getYSpeed() + newYThrust);
		}

		// Clamp things
		
		if (Math.sqrt(Math.pow(getXSpeed(), 2.0) + Math.pow(getYSpeed(), 2.0)) > MAX_OBJECT_SPEED) {
			// OK, time to clamp
			
			double angle = 0.0;
			
			if (getXSpeed() == 0.0) {
				if (getYSpeed() > 0.0) {
					angle = 1.5 * Math.PI;
				} else {
					angle = 0.5 * Math.PI;
				}
			} else {
				angle = Math.atan(getYSpeed() / getXSpeed());
			}
			
			setXSpeed(MAX_OBJECT_SPEED * Math.cos(angle));
			setYSpeed(MAX_OBJECT_SPEED * Math.sin(angle));
		}
		
		// Now move us based on our speed,
		
		super.move();
	}
}

