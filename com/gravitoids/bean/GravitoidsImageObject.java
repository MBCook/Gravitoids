package com.gravitoids.bean;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 * Copyright (c) 2008, Michael Cook
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Michael Cook nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Cook ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Michael Cook BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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

