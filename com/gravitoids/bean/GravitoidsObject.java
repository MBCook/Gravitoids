package com.gravitoids.bean;

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

public abstract class GravitoidsObject {
	private double radius;
	
	private double xPosition;
	private double yPosition;
	
	private double oldXPosition;	// These are internal, used only to check for collisions between frames
	private double oldYPosition;
	
	private double xSpeed;
	private double ySpeed;

	private double mass;
	
	private boolean moveable;
	
	public final double getSpeedFactor() {
		return 0.25;
	}
	
	public double getOldXPosition() {
		return oldXPosition;
	}
	
	public double getOldYPosition() {
		return oldYPosition;
	}
	
	public boolean hasCollided(GravitoidsObject other) {
		// TODO: This could be optimized with some simple bounds checks to let us quickly
		//			rule out any possibility anything ever happened
		
		// Check along three points, incase the objects are moving really fast
		
		double oneXDelta = getXPosition() - getOldXPosition();
		double oneYDelta = getYPosition() - getOldYPosition();
		
		double twoXDelta = other.getXPosition() - other.getOldXPosition();
		double twoYDelta = other.getYPosition() - other.getOldYPosition();
		
		double i = 1.0;
		
		//for (double i = 0.25; i <= 1.0; i += 0.25) {
			// X distance between the two at that time point
			
			double xDist = ((oneXDelta * i) + getOldXPosition()) - ((twoXDelta * i) + other.getOldXPosition());
			
			// Y distance between the two at that time point
			
			double yDist = ((oneYDelta * i) + getOldYPosition()) - ((twoYDelta * i) + other.getOldYPosition());
			
			// Now the actual distance
			
			double dist = Math.sqrt(xDist * xDist + yDist * yDist);
			
			if (dist < getRadius() + other.getRadius()) {
				// Circles overlap, colission
			
//				System.out.println(getXPosition() + ", " + getYPosition());
//				System.out.println(other.getXPosition() + ", " + other.getYPosition());
//				System.out.println(getRadius() + ", " + other.getRadius());

				return true;
			}
		//}
		
		// If we got here, they never collided
		
		return false;
	}
	
	public boolean isMoveable() {
		return moveable;
	}

	public void setMoveable(boolean moveable) {
		this.moveable = moveable;
	}

	public double getMass() {
		return mass;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getXPosition() {
		return xPosition;
	}

	public void setXPosition(double position) {
		oldXPosition = xPosition;
		xPosition = position;
	}

	public double getXSpeed() {
		return xSpeed;
	}

	public void setXSpeed(double speed) {
		xSpeed = speed;
	}

	public double getYPosition() {
		return yPosition;
	}

	public void setYPosition(double position) {
		oldYPosition = yPosition;
		yPosition = position;
	}

	public double getYSpeed() {
		return ySpeed;
	}

	public void setYSpeed(double speed) {
		ySpeed = speed;
	}

	public void move() {
		if (isMoveable()) {
			// Just use our speed to move, that's it, we're easy
			
			setXPosition(getXPosition() + getXSpeed() * getSpeedFactor());
			setYPosition(getYPosition() + getYSpeed() * getSpeedFactor());
		}
	}

	public abstract void draw(Graphics g);
}

