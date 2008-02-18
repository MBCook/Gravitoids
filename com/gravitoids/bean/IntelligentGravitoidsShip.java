package com.gravitoids.bean;

import java.awt.Color;
import java.awt.Graphics;

import com.gravitoids.panel.GravitoidsPanel;

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
	
	// How big our brain is
	
	private static final int BRAIN_SIZE = 22;
	
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
	
	private static final int WALL_DISTANCE_A_TERM = 19;
	private static final int WALL_DISTANCE_B_TERM = 20;
	private static final int WALL_DISTANCE_C_TERM = 21;
	
	private static final double MAXIMUM_THRUST = 5.0;
	
	private static final double WALL_FACTOR = 3.0;
	
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
	
	public double[] getBrain() {
		return brain;
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
		
		weights[sideWallIndex] = WALL_FACTOR * distance * distance * brain[WALL_DISTANCE_A_TERM] +	// The 3 is to make this competitive
									distance * brain[WALL_DISTANCE_B_TERM] +
									brain[WALL_DISTANCE_C_TERM];
		
		double direction = Math.atan((getXPosition() - sideWall.getXPosition()) / 
										(getYPosition() - sideWall.getYPosition()));
		
		direction = direction * direction * brain[OBJECT_DIRECTION_A_TERM] +
					direction * brain[OBJECT_DIRECTION_B_TERM] +
					brain[OBJECT_DIRECTION_C_TERM];
		
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

		weights[topWallIndex] = WALL_FACTOR * distance * distance * brain[WALL_DISTANCE_A_TERM] +
									distance * brain[WALL_DISTANCE_B_TERM] +
									brain[WALL_DISTANCE_C_TERM];

		direction = Math.atan((getXPosition() - topWall.getXPosition()) / 
								(getYPosition() - topWall.getYPosition()));

		direction = direction * direction * brain[OBJECT_DIRECTION_A_TERM] +
					direction * brain[OBJECT_DIRECTION_B_TERM] +
					brain[OBJECT_DIRECTION_C_TERM];

		while (direction > 2.0 * Math.PI) {	// Clamp it
			direction -= 2.0 * Math.PI;
		}
		
		while (direction < 0.0) {
			direction += 2.0 * Math.PI;
		}
		
		headings[topWallIndex] = direction;
		
		// Now that we have all that, we need to find what we care about most
		
		int importantIndex = 0;
		double mostImportant = 0.0;
		double totalSquares = 0.0;
		
		for (int i = 0; i < weights.length; i++) {
			if (weights[i] >= mostImportant) {
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
		
		if (drawThrust && getThrust() > 0.0) {
			g.setColor(Color.RED);
			g.drawLine((int) getXPosition(), (int) getYPosition(),
						(int) (getXPosition() + getXThrustPortion() * 2.0 * getThrust()),
						(int) (getYPosition() + getYThrustPortion() * 2.0 * getThrust()));
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
	
	public static double[] breed(IntelligentGravitoidsShip one, IntelligentGravitoidsShip two) {
		// Come up with the space to hold our new brain
		
		double[] brain = new double[BRAIN_SIZE];
		
		// Do the simple breeding
		
		for (int i = 0; i < BRAIN_SIZE; i++) {	// Randomly select someone's genes
			brain[i] = Math.random() > 0.5 ? one.brain[i] : two.brain[i];
		}
		
		// Now, handle mutation
		
		for (int i = 0; i < BRAIN_SIZE; i++) {
			double num = Math.random();
			
			if (num < MUTATION_RATE) {
				// Total replacement
				
				brain[i] = Math.random() * 2.0 - 1.0;
			} else if (num < 2.0 * MUTATION_RATE) {
				// Minor mutation
				
				brain[i] = brain[i] + (Math.random() * 2.0 - 1.0) * MINI_MUTATION;	// Change value by up to MINI_MUTATION, plus or minus
				
				if (brain[i] > 1.0) {	// Clamp
					brain[i] = 1.0;
				} else if (brain[i] < -1.0) {
					brain[i] = -1.0;
				}
			}
		}
		
		// Return it
		
		return brain;
	}
}
