package com.gravitoids.bean;


public abstract class GravitoidsAutonomousObject extends GravitoidsObject {
	private double xThurstPortion;
	private double yThrustPortion;
	private double thrust;

	public void normalizeThrust() {
		double total = Math.sqrt(xThurstPortion * xThurstPortion + yThrustPortion * yThrustPortion);
		
		xThurstPortion = xThurstPortion / total;
		yThrustPortion = yThrustPortion / total;
	}
	
	public double getThrust() {
		return thrust;
	}

	public void setThrust(double thrust) {
		this.thrust = thrust;
	}

	public double getXThurstPortion() {
		return xThurstPortion;
	}

	public void setXThurstPortion(double thurstPortion) {
		xThurstPortion = thurstPortion;
	}

	public double getYThrustPortion() {
		return yThrustPortion;
	}

	public void setYThrustPortion(double thrustPortion) {
		yThrustPortion = thrustPortion;
	}

	public void move() {
		// Use our thrust to alter our speed
		
		setXSpeed(getXSpeed() + thrust * xThurstPortion);
		setYSpeed(getYSpeed() + thrust * yThrustPortion);

		// Now move us based on our speed,
		
		super.move();
	}
}

