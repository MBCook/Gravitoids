package com.gravitoids.bean;

import java.awt.Color;
import java.awt.Graphics;

public class GravitoidsCircleObject extends GravitoidsObject {
	private Color color;
	
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void draw(Graphics g) {
		// Figure out where we are
		
		int r = (int) getRadius();
		
		int drawX = (int) getXPosition() - r;
		int drawY = (int) getYPosition() - r;
		
		// Draw us
		
		g.setColor(color);
		g.fillOval(drawX, drawY, r * 2, r * 2);
	}
}

