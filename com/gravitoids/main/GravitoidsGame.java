package com.gravitoids.main;

import java.awt.Container;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import com.gravitoids.panel.GravitoidsPanel;

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

public class GravitoidsGame extends JFrame implements WindowListener {
	private static int DEFAULT_FPS = 60;

	private GravitoidsPanel gp;

	public GravitoidsGame(long period) {
		super("Gravitoids");
		makeGUI(period);

		addWindowListener(this);
		pack();
		setResizable(false);
		setVisible(true);
	}

	private void makeGUI(long period)  {
		Container c = getContentPane();

		gp = new GravitoidsPanel(this, period);
		c.add(gp, "Center");
	}	

	// ----------------------------------------------------

	public void windowActivated(WindowEvent e) {
		gp.resumeGame();
	}

	public void windowDeactivated(WindowEvent e) {
	//	gp.pauseGame();
	}

	public void windowDeiconified(WindowEvent e) {
		gp.resumeGame();
	}

	public void windowIconified(WindowEvent e) {
		gp.pauseGame(); 
	}

	public void windowClosing(WindowEvent e) {
		gp.stopGame();
	}

	public void windowClosed(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}

	// ----------------------------------------------------

	public static void main(String args[]) { 
		int fps = DEFAULT_FPS;
		if (args.length != 0)
			fps = Integer.parseInt(args[0]);

		long period = (long) 1000.0/fps;
		
		System.out.println("fps: " + fps + "; period: " + period + " ms");

		new GravitoidsGame(period*1000000L);		 // ms --> nanosecs 
	}
}


