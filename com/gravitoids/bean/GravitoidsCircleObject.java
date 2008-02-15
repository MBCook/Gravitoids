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
		int drawX = (int) getXPosition();
		int drawY = (int) getYPosition();
		int r = (int) getRadius();
		
		g.setColor(color);
		g.fillOval(drawX - (r / 2), drawY - (r / 2), r * 2, r * 2);
	}
}

