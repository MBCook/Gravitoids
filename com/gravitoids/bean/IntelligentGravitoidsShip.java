package com.gravitoids.bean;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;

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

public class IntelligentGravitoidsShip extends GravitoidsAutonomousObject {
	// The chances of a mutation, change rate of a small mutation
	
	private static final double MUTATION_RATE = 0.1;
	private static final double MINI_MUTATION = 0.2;
	
	// Whether we want to draw the insight into why this object is behaving like it does
	
	private static boolean drawMotiviation = true;
	private static boolean drawThrust = true;
	
	// The position of the object that is motivating us
	
	private double motivationX = -1.0;
	private double motivationY = -1.0;
	
	private double otherMotivationX = -1.0;
	private double otherMotivationY = -1.0;
	
	// How big our brain is
	
	private static final int BRAIN_SIZE = 25;
	
	// What each element in the brain represents
	
	private static final int THRUST_SPLIT = 0;
	private static final int MINIMUM_FEAR = 1;
	private static final int WALL_FACTOR = 2;
	
	private static final int OBJECT_DIRECTION_A_TERM = 3;
	private static final int OBJECT_DIRECTION_B_TERM = 4;
	private static final int OBJECT_DIRECTION_C_TERM = 5;
	
	private static final int OBJECT_GRAVITY_A_TERM = 6;
	private static final int OBJECT_GRAVITY_B_TERM = 7;
	private static final int OBJECT_GRAVITY_C_TERM = 8;
	
	private static final int OBJECT_SIZE_A_TERM = 9;
	private static final int OBJECT_SIZE_B_TERM = 10;
	private static final int OBJECT_SIZE_C_TERM = 11;

	private static final int OBJECT_MOVEABLE_FACTOR = 12;
	
	private static final int CARE_TO_THRUST_A_TERM = 13;
	private static final int CARE_TO_THRUST_B_TERM = 14;
	private static final int CARE_TO_THRUST_C_TERM = 15;
	
	private static final int OBJECT_SPEED_A_TERM = 16;
	private static final int OBJECT_SPEED_B_TERM = 17;
	private static final int OBJECT_SPEED_C_TERM = 18;
	
	private static final int WALL_DISTANCE_A_TERM = 19;
	private static final int WALL_DISTANCE_B_TERM = 20;
	private static final int WALL_DISTANCE_C_TERM = 21;
	
	private double brain[] = null; 
	private int evolutionDirection[] = null;	// Which way the brian term is moving
	
	private String name = null;
	
	private long age = 0L;
	
	public IntelligentGravitoidsShip() {
		// Give us a random brain
		
		brain = new double[BRAIN_SIZE];
		evolutionDirection = new int[BRAIN_SIZE];
		
		for (int i = 0; i < BRAIN_SIZE; i++) {
			brain[i] = Math.random() * 2.0 - 1.0;
			evolutionDirection[i] = 0;
		}
	}
	
	public IntelligentGravitoidsShip(IntelligentGravitoidsShip source) {
		// Copy our brain
		
		brain = new double[BRAIN_SIZE];
		evolutionDirection = new int[BRAIN_SIZE];
		
		for (int i = 0; i < BRAIN_SIZE; i++) {
			brain[i] = source.brain[i];
			evolutionDirection[i] = source.evolutionDirection[i];
		}
	}
	
	public double[] getBrain() {
		return brain;
	}
	
	public int[] getEvolutionDirection() {
		return evolutionDirection;
	}
	
	public static boolean isDrawThrust() {
		return drawThrust;
	}
	
	public static void setDrawThrust(boolean drawThrust) {
		IntelligentGravitoidsShip.drawThrust = drawThrust;
	}
	
	public static boolean isDrawMotivation() {
		return drawMotiviation;
	}
	
	public static void setDrawMotivation(boolean drawMotivation) {
		IntelligentGravitoidsShip.drawMotiviation = drawMotivation;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void incrementAge() {
		age++;
	}
	
	public long getAge() {
		return age;
	}
	
	public double calculateFromBrain(double input, int aTerm, int bTerm, int cTerm) {
		return input * input * brain[aTerm] +
				input * brain[bTerm] +
				brain[cTerm];
	}
	
	public void prepareMove(GravitoidsObject[] theirObjects) {
		// Look at each object, decide how much we care, and how we will respond
		// Then add it all together
		
		GravitoidsObject[] objects = new GravitoidsObject[theirObjects.length + 2];	// 2 more so we can track walls
		
		double[] weights = new double[objects.length];	// How much we care
		double[] headings = new double[objects.length];	// Which heading to go in
		
		for (int i = 0; i < objects.length - 2; i++) {
			// Get what we're working with
			
			GravitoidsObject object = theirObjects[i];
			
			objects[i] = object;
			
			// Figure out our distance from it, used in the gravity calculation
			
			double distance = Math.sqrt(Math.pow(getXPosition() - object.getXPosition(), 2) + 
											Math.pow(getYPosition() - object.getYPosition(), 2));
			
			// Now handle it's mass to get gravity
			
			double gravity = calculateFromBrain(distance / object.getMass(), OBJECT_GRAVITY_A_TERM, OBJECT_GRAVITY_B_TERM, OBJECT_GRAVITY_C_TERM);
			
			// Now the direction
			
			double direction = Math.atan((getXPosition() - object.getXPosition()) / 
											(getYPosition() - object.getYPosition()));
			
			direction = calculateFromBrain(direction, OBJECT_DIRECTION_A_TERM, OBJECT_DIRECTION_B_TERM, OBJECT_DIRECTION_C_TERM);
			
			while (direction > 2.0 * Math.PI) {	// Clamp it
				direction -= 2.0 * Math.PI;
			}
			
			while (direction < 0.0) {
				direction += 2.0 * Math.PI;
			}
			
			// The object's speed relative to us
			
			double xSpeedDelta = getXSpeed() - object.getXSpeed();
			double ySpeedDelta = getYSpeed() - object.getYSpeed();
			
			if ((xSpeedDelta < 0.0) && (getXPosition() > object.getXPosition())) {	// Copensate for relative positions
				xSpeedDelta *= -1.0;
			}
			
			if ((ySpeedDelta < 0.0) && (getYPosition() > object.getYPosition())) {
				ySpeedDelta *= -1.0;
			}
			
			xSpeedDelta = xSpeedDelta < 0.0 ? 0.0 : xSpeedDelta;	// Mark we don't care about negative speeds
			ySpeedDelta = ySpeedDelta < 0.0 ? 0.0 : ySpeedDelta;
			
			double speed = Math.sqrt(xSpeedDelta * xSpeedDelta + ySpeedDelta * ySpeedDelta);
			
			speed = calculateFromBrain(speed, OBJECT_SPEED_A_TERM, OBJECT_SPEED_B_TERM, OBJECT_SPEED_C_TERM);
			
			// Now the object's size
			
			double size = calculateFromBrain(object.getRadius(), OBJECT_SIZE_A_TERM, OBJECT_SIZE_B_TERM, OBJECT_SIZE_C_TERM);
			
			// Now moveability
			
			double moveability = object.isMoveable() ? brain[OBJECT_MOVEABLE_FACTOR] : 0.0;
			
			// Store things we need to for this object
			
			weights[i] = moveability + gravity + size + speed;
			headings[i] = direction;
		}
		
		// Now, we'll insert the closest X and Y walls, with apropriate weights
		
		GravitoidsObject sideWall = new WallObject();
		GravitoidsObject topWall = new WallObject();
		
		sideWall.setYPosition(getYPosition());
		topWall.setXPosition(getXPosition());
		
		if (getXPosition() < GravitoidsPanel.PANEL_WIDTH / 2.0) {
			sideWall.setXPosition(0.0);
		} else {
			sideWall.setXPosition(GravitoidsPanel.PANEL_WIDTH);
		}
		
		if (getYPosition() < GravitoidsPanel.PANEL_HEIGHT / 2.0) {
			topWall.setYPosition(0.0);
		} else {
			topWall.setYPosition(GravitoidsPanel.PANEL_HEIGHT);
		}
		
		int sideWallIndex = objects.length - 1;
		int topWallIndex = objects.length - 2;
		
		objects[sideWallIndex] = sideWall;
		objects[topWallIndex] = topWall;
		
		// Direction and distance for side wall
		
		double distance = Math.sqrt(Math.pow(getXPosition() - sideWall.getXPosition(), 2) + 
										Math.pow(getYPosition() - sideWall.getYPosition(), 2));
		
		weights[sideWallIndex] = ((brain[WALL_FACTOR] + 1.0) / 2.0) * 7.0 *
									calculateFromBrain(distance, WALL_DISTANCE_A_TERM, WALL_DISTANCE_B_TERM, WALL_DISTANCE_C_TERM);
		
		double direction = Math.atan((getXPosition() - sideWall.getXPosition()) / 
										(getYPosition() - sideWall.getYPosition()));
		
		direction = calculateFromBrain(direction, OBJECT_DIRECTION_A_TERM, OBJECT_DIRECTION_B_TERM, OBJECT_DIRECTION_C_TERM);
		
		while (direction > 2.0 * Math.PI) {	// Clamp it
			direction -= 2.0 * Math.PI;
		}
		
		while (direction < 0.0) {
			direction += 2.0 * Math.PI;
		}
		
		headings[sideWallIndex] = direction;
		
		// Now for the top wall
		
		distance = Math.sqrt(Math.pow(getXPosition() - topWall.getXPosition(), 2) + 
								Math.pow(getYPosition() - topWall.getYPosition(), 2));

		weights[topWallIndex] = ((brain[WALL_FACTOR] + 1.0) / 2.0) * 7.0 *
									calculateFromBrain(distance, WALL_DISTANCE_A_TERM, WALL_DISTANCE_B_TERM, WALL_DISTANCE_C_TERM);

		direction = Math.atan((getXPosition() - topWall.getXPosition()) / 
								(getYPosition() - topWall.getYPosition()));

		direction = calculateFromBrain(direction, OBJECT_DIRECTION_A_TERM, OBJECT_DIRECTION_B_TERM, OBJECT_DIRECTION_C_TERM);

		while (direction > 2.0 * Math.PI) {	// Clamp it
			direction -= 2.0 * Math.PI;
		}
		
		while (direction < 0.0) {
			direction += 2.0 * Math.PI;
		}
		
		headings[topWallIndex] = direction;
		
		// Now that we have all that, we need to find what we care about most
		
		int importantIndex = -1;
		int secondIndex = -1;
		
		double mostImportant = Double.NEGATIVE_INFINITY;
		double secondMostImportant = Double.NEGATIVE_INFINITY;
		
		for (int i = 0; i < weights.length; i++) {
			if (weights[i] >= mostImportant) {
				secondMostImportant = mostImportant;
				mostImportant = weights[i];
				
				secondIndex = importantIndex;
				importantIndex = i;
			} else if (weights[i] >= secondMostImportant) {
				secondMostImportant = weights[i];
				secondIndex = i;
			}
		}

		// Record the position of the object we are being motivated by

		double minimumFear = (1.0 - (brain[MINIMUM_FEAR] + 1.0) / 2.0) * Double.MAX_VALUE * -1.0;
		
		if (mostImportant > minimumFear) {
			motivationX = objects[importantIndex].getXPosition();
			motivationY = objects[importantIndex].getYPosition();
		} else {
			motivationX = -1.0;
			motivationY = -1.0;
		}
		
		if (secondMostImportant > minimumFear) {
			otherMotivationX = objects[secondIndex].getXPosition();
			otherMotivationY = objects[secondIndex].getYPosition();
		} else {
			otherMotivationX = -1.0;
			otherMotivationY = -1.0;
		}
		
		// Now that we have that, we'll scale thrust to the normal of the weights

		if ((importantIndex >= 0) && (mostImportant > minimumFear)) {
			if ((secondIndex >= 0) && (secondMostImportant > minimumFear)) {
				// OK, two things to handle
				
				double newThrustA = weights[importantIndex];
				
				newThrustA = calculateFromBrain(newThrustA, CARE_TO_THRUST_A_TERM, CARE_TO_THRUST_B_TERM, CARE_TO_THRUST_C_TERM);
				
				double newThrustB = weights[secondIndex];
				
				newThrustB = calculateFromBrain(newThrustB, CARE_TO_THRUST_A_TERM, CARE_TO_THRUST_B_TERM, CARE_TO_THRUST_C_TERM);
				
				newThrustA = newThrustA * ((brain[THRUST_SPLIT] + 1.0) / 2.0);
				newThrustB = newThrustB * (1.0 - ((brain[THRUST_SPLIT] + 1.0) / 2.0));
				
				// Now we have to add the two thrust vectors
				
				double xThrust = Math.cos(headings[importantIndex]) * newThrustA;
				double yThrust = Math.sin(headings[importantIndex]) * newThrustA;
				
				xThrust += Math.cos(headings[secondIndex]) * newThrustB;
				yThrust += Math.sin(headings[secondIndex]) * newThrustB;
				
				// Now we have to get that back into a thrust and thrust percentages
				// Get the total thrust, then normalize the two vector parts
				
				double factor = Math.sqrt(xThrust * xThrust + yThrust * yThrust);
				
				setThrust(factor);
				
				setXThrustPortion(xThrust / factor);
				setYThrustPortion(yThrust / factor);
			} else {
				// Just one, this is easy
				
				double newThrust = weights[importantIndex];
				
				newThrust = calculateFromBrain(newThrust, CARE_TO_THRUST_A_TERM, CARE_TO_THRUST_B_TERM, CARE_TO_THRUST_C_TERM);
				
				setThrust(newThrust);
				
				// Set our heading
				
				setXThrustPortion(Math.cos(headings[importantIndex]));
				setYThrustPortion(Math.sin(headings[importantIndex]));
			}
		} else {
			// We ain't scared of nothing
			
			setThrust(0.0);
			setXThrustPortion(0.0);
			setYThrustPortion(0.0);
		}
	}
	
	public void draw(Graphics g) {
		// Figure out where we are
		
		int r = (int) getRadius();
		
		int ovalDrawX = (int) getXPosition() - r;
		int ovalDrawY = (int) getYPosition() - r;
		
		// Draw us
		
		g.setColor(Color.BLUE);
		g.fillOval(ovalDrawX, ovalDrawY, r * 2, r * 2);
		
		// Now, if requested and in existance, draw our motivation
		
		if (drawMotiviation) {
			if ((otherMotivationX >= 0.0) && (otherMotivationY >= 0.0)) {
				g.setColor(Color.LIGHT_GRAY);
				g.drawLine((int) getXPosition(), (int) getYPosition(), (int) otherMotivationX, (int) otherMotivationY);
			}
			if ((motivationX >= 0.0) && (motivationY >= 0.0)) {
				g.setColor(Color.BLUE);
				g.drawLine((int) getXPosition(), (int) getYPosition(), (int) motivationX, (int) motivationY);
			}
		}
		
		if (drawThrust && getThrust() > 0.0) {
			g.setColor(Color.RED);
			g.drawLine((int) getXPosition(), (int) getYPosition(),
						(int) (getXPosition() + getXThrustPortion() * getThrust()),
						(int) (getYPosition() + getYThrustPortion() * getThrust()));
		}
	}
	
	private class WallObject extends GravitoidsObject {
		public WallObject() {
			// Init everything to 0, mostly
			
			this.setMass(0.0);
			this.setMoveable(false);
			this.setRadius(1.0);		// So collision detection works
			this.setXPosition(0.0);
			this.setXSpeed(0.0);
			this.setYPosition(0.0);
			this.setYSpeed(0.0);
		}
		
		public void draw(Graphics g) {
			// Ignore this, we don't exist
		}
	}
	
	public static IntelligentGravitoidsShip breed(IntelligentGravitoidsShip one, IntelligentGravitoidsShip two) {
		// Come up with the space to hold our new brain
		
		IntelligentGravitoidsShip ship = new IntelligentGravitoidsShip();
		
		double[] brain = ship.getBrain();
		int[] evolutionDirection = ship.getEvolutionDirection();
		
		// Do the simple breeding
		
		for (int i = 0; i < BRAIN_SIZE; i++) {	// Randomly select someone's genes
			brain[i] = Math.random() > 0.5 ? one.brain[i] : two.brain[i];
		}
		
		// Now, handle mutation
		
		for (int i = 0; i < BRAIN_SIZE; i++) {
			// First, we'll figure out what direction to evolve in
			
			if (Math.random() > 0.1) {
				// Things go in the direction of our parrents
				
				evolutionDirection[i] = Math.random() > 0.5 ? one.evolutionDirection[i] : two.evolutionDirection[i];
			} else {
				// New direction
				
				evolutionDirection[i] = Math.random() > 0.5 ? 1 : -1;
			}
			
			// Now figure out if we are evolving
			
			double num = Math.random();
			
			if (num < MUTATION_RATE) {
				// Total replacement
				
				brain[i] = Math.random() * 2.0 - 1.0;
				evolutionDirection[i] = 0;					// We didn't move
			} else if (num < 2.0 * MUTATION_RATE) {
				// Minor mutation
				// See if we need a direction to mutate in
				
				if (evolutionDirection[i] == 0) {
					evolutionDirection[i] = Math.random() > 0.5 ? 1 : -1;
				}
				
				// Change value by up to MINI_MUTATION, plus or minus based on evolutionDirection
				
				brain[i] = brain[i] + (Math.random() * ((double) evolutionDirection[i])) * MINI_MUTATION;
				
				if (brain[i] > 1.0) {	// Clamp to the limits
					brain[i] = 1.0;
				} else if (brain[i] < -1.0) {
					brain[i] = -1.0;
				}
			} else {
				// Nothing changes
			}
		}
		
		// Return it
		
		return ship;
	}
}
