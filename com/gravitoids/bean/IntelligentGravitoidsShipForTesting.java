package com.gravitoids.bean;

import java.awt.Color;
import java.awt.Graphics;

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

public class IntelligentGravitoidsShipForTesting extends IntelligentGravitoidsShip {
	public IntelligentGravitoidsShipForTesting(IntelligentGravitoidsShip source) {
		// Copy our brain
		
		brain = new double[BRAIN_SIZE];
		evolutionDirection = new int[BRAIN_SIZE];
		
		for (int i = 0; i < BRAIN_SIZE; i++) {
			brain[i] = source.brain[i];
			evolutionDirection[i] = source.evolutionDirection[i];
		}
	}
	
	public void draw(Graphics g) {
		// Figure out where we are
		
		int r = (int) getRadius();
		
		int ovalDrawX = (int) getXPosition() - r;
		int ovalDrawY = (int) getYPosition() - r;
		
		// Draw us
		
		g.setColor(Color.MAGENTA);
		g.fillOval(ovalDrawX, ovalDrawY, r * 2, r * 2);
		
		// Now, if requested and in existance, draw our motivation
		
		if (isDrawMotivation()) {
			if (secondMotivation != null) {
				g.setColor(Color.LIGHT_GRAY);
				drawMotivation(g, secondMotivation);
			}
			if (mainMotivation != null) {
				g.setColor(Color.BLUE);
				drawMotivation(g, mainMotivation);
			}
		}
		
		if (isDrawThrust() && getThrust() > 0.0) {
			g.setColor(Color.RED);
			g.drawLine((int) getXPosition(), (int) getYPosition(),
						(int) (getXPosition() - getXThrustPortion() * getThrust()),
						(int) (getYPosition() - getYThrustPortion() * getThrust()));
		}
		
		if (isDrawGravitationalPull()) {
			g.setColor(Color.GREEN);
			g.drawLine((int) getXPosition(), (int) getYPosition(),
						(int) (getXPosition() + 100.0 * getXGravitationalForce()),
						(int) (getYPosition() + 100.0 * getYGravitationalForce()));
			resetGravitationalForce();
		}
	}
	
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
		
		setXSpeed(getXSpeed() + newXThrust);
		setYSpeed(getYSpeed() + newYThrust);

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
		
		// Now don't move a bit
		
		setXSpeed(0.0);
		setYSpeed(0.0);
	}
}
