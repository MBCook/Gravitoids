package com.gravitoids.bean;


public abstract class GravitoidsAutonomousObject extends GravitoidsObject {
	private double xThrustPortion;
	private double yThrustPortion;
	private double thrust = 0.0;

	public void normalizeThrust() {
		double total = Math.sqrt(xThrustPortion * xThrustPortion + yThrustPortion * yThrustPortion);
		
		xThrustPortion = xThrustPortion / total;
		yThrustPortion = yThrustPortion / total;
	}
	
	public double getThrust() {
		return thrust;
	}

	public void setThrust(double thrust) {
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
		// Use our thrust to alter our speed
		
		setXSpeed(getXSpeed() + thrust * xThrustPortion * getSpeedFactor());
		setYSpeed(getYSpeed() + thrust * yThrustPortion * getSpeedFactor());

		// Now move us based on our speed,
		
		super.move();
	}
}

