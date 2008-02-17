package com.gravitoids.bean;

import java.awt.Color;
import java.awt.Graphics;

public class IntelligentGravitoidsShip extends GravitoidsAutonomousObject {
	// Whether we want to draw the insight into why this object is behaving like it does
	
	private static boolean drawMotiviation = false;
	
	// The position of the object that is motivating us
	
	private double motivationX = -1.0;
	private double motivationY = -1.0;
	
	// How big our brain is
	
	private static final int BRAIN_SIZE = 19;
	
	// What each element in the brain represents
	
	private static final int OBJECT_DISTANCE_A_TERM = 0;
	private static final int OBJECT_DISTANCE_B_TERM = 1;
	private static final int OBJECT_DISTANCE_C_TERM = 2;
	
	private static final int OBJECT_DIRECTION_A_TERM = 3;
	private static final int OBJECT_DIRECTION_B_TERM = 4;
	private static final int OBJECT_DIRECTION_C_TERM = 5;
	
	private static final int OBJECT_MASS_A_TERM = 6;
	private static final int OBJECT_MASS_B_TERM = 7;
	private static final int OBJECT_MASS_C_TERM = 8;
	
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
	
	private static final double MAXIMUM_THRUST = 3.5;
	
	private double brain[] = null; 
	
	private String name = null;
	
	private long age = 0L;
	
	public IntelligentGravitoidsShip() {
		// Give us a random brain
		
		brain = new double[BRAIN_SIZE];
		
		for (int i = 0; i < BRAIN_SIZE; i++) {
			brain[i] = Math.random() * 2.0 - 1.0;
		}
	}
	
	public IntelligentGravitoidsShip(double[] brain) {
		this.brain = brain;
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
	
	public void prepareMove(GravitoidsObject[] objects) {
		// Look at each object, decide how much we care, and how we will respond
		// Then add it all together
		
		double[] weights = new double[objects.length];	// How much we care
		double[] headings = new double[objects.length];	// Which heading to go in
		
		for (int i = 0; i < objects.length; i++) {
			// Get what we're working with
			
			GravitoidsObject object = objects[i];
			
			// Figure out our distance from it
			
			double distance = Math.sqrt(Math.pow(getXPosition() - object.getXPosition(), 2) + 
											Math.pow(getYPosition() - object.getYPosition(), 2));
			
			distance = distance * distance * brain[OBJECT_DISTANCE_A_TERM] +
						distance * brain[OBJECT_DISTANCE_B_TERM] +
						brain[OBJECT_DISTANCE_C_TERM];
			
			// Now handle it's mass
			
			double mass = object.getMass() * object.getMass() * brain[OBJECT_MASS_A_TERM] +
							object.getMass() * brain[OBJECT_MASS_B_TERM] +
							brain[OBJECT_MASS_C_TERM];
			
			// Now the direction
			
			double direction = Math.atan((getXPosition() - object.getXPosition()) / 
											(getYPosition() - object.getYPosition()));
			
			direction = direction * direction * brain[OBJECT_DIRECTION_A_TERM] +
						direction * brain[OBJECT_DIRECTION_B_TERM] +
						brain[OBJECT_DIRECTION_C_TERM];
			
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
			
			speed = speed * speed * brain[OBJECT_SPEED_A_TERM] +
					speed * brain[OBJECT_SPEED_B_TERM] +
					brain[OBJECT_SPEED_C_TERM];
			
			// Now the object's size
			
			double size = object.getRadius() * object.getRadius() * brain[OBJECT_SIZE_A_TERM] +
							object.getRadius() * brain[OBJECT_SIZE_B_TERM] +
							brain[OBJECT_SIZE_C_TERM];
			
			// Now moveability
			
			double moveability = object.isMoveable() ? brain[OBJECT_MOVEABLE_FACTOR] : 0.0;
			
			// Store things we need to for this object
			
			weights[i] = moveability + mass + size + distance + speed;
			headings[i] = direction;
		}
		
		// Now that we have all that, we need to find what we care about most
		
		int importantIndex = 0;
		double mostImportant = 0.0;
		double totalSquares = 0.0;
		
		for (int i = 0; i < weights.length; i++) {
			if (weights[i] > mostImportant) {
				mostImportant = weights[i];
				importantIndex = i;
			}
			
			totalSquares += weights[i] * weights[i];
		}
		
		// Record the position of the object we are being motivated by
		
		motivationX = objects[importantIndex].getXPosition();
		motivationY = objects[importantIndex].getYPosition();
		
		// Now that we have that, we'll scale thrust to the normal of the weights
		
		double newThrust = weights[importantIndex] / Math.sqrt(totalSquares);
		
		newThrust = newThrust * newThrust * CARE_TO_THRUST_A_TERM +
					newThrust * CARE_TO_THRUST_B_TERM +
					CARE_TO_THRUST_C_TERM;
		
		if (newThrust > MAXIMUM_THRUST) {
			newThrust = MAXIMUM_THRUST;
		}
		
		setThrust(newThrust);
		
		// Set our heading
		
		setXThrustPortion(Math.cos(headings[importantIndex]));
		setYThrustPortion(Math.sin(headings[importantIndex]));
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
			if ((motivationX >= 0.0) && (motivationY >= 0.0)) {
				g.drawLine((int) getXPosition(), (int) getYPosition(), (int) motivationX, (int) motivationY);
			}
		}
	}
}
