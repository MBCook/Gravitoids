package com.gravitoids.bean;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class GravitoidsImageObject extends GravitoidsObject {
	private BufferedImage image;
	
	private int imageWidth;
	private int imageHeight;
	
	private String imagePath;

	public void setImage(BufferedImage source) {
		image = source;
	}

	public BufferedImage getImage() {
		return image;
	}

	public int getImageHeight() {
		return imageHeight;
	}

	public void setImageHeight(int imageHeight) {
		this.imageHeight = imageHeight;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public int getImageWidth() {
		return imageWidth;
	}

	public void setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
	}

	public boolean loadImage() {
		try {
			URL file = this.getClass().getResource(imagePath);
			
			image = ImageIO.read(file);

			int transparency = image.getColorModel().getTransparency();

			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();

			BufferedImage copy = gc.createCompatibleImage(imageWidth, imageHeight, transparency);

			// Now that we've loaded the image and created a place for it, copy it there.

			Graphics2D g2d = copy.createGraphics();
	
			g2d.drawImage(image, 0, 0, null);
			g2d.dispose();

			image = copy;
			
			return true;
		} catch(IOException e) {
			System.out.println("Loading of image '" + imagePath + "' failed: " + e);
			return false;
		}
	}

	public void draw(Graphics g) {
		int drawX = (int) getXPosition();
		int drawY = (int) getYPosition();
		
		g.drawImage(image, drawX - (imageWidth / 2), drawY - (imageWidth / 2), null);
	}
}

