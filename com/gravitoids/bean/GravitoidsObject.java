package com.gravitoids.bean;

import java.awt.Graphics;

public abstract class GravitoidsObject {
	private double radius;
	
	private double xPosition;
	private double yPosition;
	
	private double xSpeed;
	private double ySpeed;

	private double mass;
	
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
		yPosition = position;
	}

	public double getYSpeed() {
		return ySpeed;
	}

	public void setYSpeed(double speed) {
		ySpeed = speed;
	}

	public void move() {
		// Just use our speed to move, that's it, we're easy
		
		xPosition += xSpeed;
		yPosition += ySpeed;
	}

	public abstract void draw(Graphics g);
}

