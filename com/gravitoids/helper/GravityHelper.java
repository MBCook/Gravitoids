package com.gravitoids.helper;

import com.gravitoids.bean.GravitoidsObject;

public class GravityHelper {
	private static GravityHelper instance = null;
	
	private double gravitationalConstant;
	
	public static synchronized GravityHelper getInstance() {
		if (instance == null)
			instance = new GravityHelper(0.10d);
		
		return instance;
	}
	
	public GravityHelper(double gravitationalConstant) {
		this.gravitationalConstant = gravitationalConstant;
	}
	
	public void simulateGravity(GravitoidsObject one, GravitoidsObject two) {
		// Get the X and Y distances between the objects
		
		double xDistance = one.getXPosition() - two.getXPosition();
		double yDistance = one.getYPosition() - two.getYPosition();
		
		// Now the straight distance between them
		
		double distance = Math.sqrt(xDistance * xDistance + yDistance * yDistance);
		
		// Figure out the force from gravity
		
		double massProduct = one.getMass() * two.getMass();
		
		double forceOfGravity = (gravitationalConstant * massProduct) / (distance * distance);
		
		// Now the angle between the two things
		
		double angle = Math.atan(yDistance / xDistance);
		
		// The numbers
		
		double yForce = Math.sin(angle) * forceOfGravity;
		double xForce = Math.cos(angle) * forceOfGravity;
		
		// Now change things
		
		one.setXSpeed(one.getXSpeed() + xForce);
		one.setYSpeed(one.getYSpeed() + yForce);
		
		two.setXSpeed(two.getXSpeed() - xForce);
		two.setYSpeed(two.getYSpeed() - yForce);
	}
}
