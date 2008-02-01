package com.gravitoids.helper;

import com.gravitoids.bean.GravitoidsObject;

public class GravityHelper {
	private static GravityHelper instance = null;
	
	private static final double MAX_INFLUENCE = 1.0d;
	
	private double gravitationalConstant;
	
	public static synchronized GravityHelper getInstance() {
		if (instance == null)
			instance = new GravityHelper(10.0d);
		
		return instance;
	}
	
	public GravityHelper(double gravitationalConstant) {
		this.gravitationalConstant = gravitationalConstant;
	}
	
	public void simulateGravity(GravitoidsObject one, GravitoidsObject two) {
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
		
		// Get the X and Y distances between the objects
		
		double xDistance = one.getXPosition() - two.getXPosition();
		double yDistance = one.getYPosition() - two.getYPosition();
		
		// Now the straight distance between them
		
		double distance = Math.sqrt(xDistance * xDistance + yDistance * yDistance);
		
		// Figure out the force from gravity
		
		double massProduct = one.getMass() * two.getMass();
		
		double forceOfGravity = (gravitationalConstant * massProduct) / (distance * distance);
		
		if (forceOfGravity > MAX_INFLUENCE)
			forceOfGravity = MAX_INFLUENCE;
		
		// Now the angle between the two things
		
		double angle = Math.atan(yDistance / xDistance);
		
		// The numbers
		
		double yForce = Math.sin(angle) * forceOfGravity * (one.getXPosition() > two.getXPosition() ? -1 : 1);
		double xForce = Math.cos(angle) * forceOfGravity * (one.getXPosition() > two.getXPosition() ? -1 : 1);
		
		// Now set the new velocities, dividing by the mass so results are correct
		
		one.setXSpeed(one.getXSpeed() + (xForce / one.getMass()));
		one.setYSpeed(one.getYSpeed() + (yForce / one.getMass()));
		
		two.setXSpeed(two.getXSpeed() - (xForce / two.getMass()));
		two.setYSpeed(two.getYSpeed() - (yForce / two.getMass()));
	}
}
