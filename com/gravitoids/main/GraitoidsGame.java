package com.gravitoids.main;

import java.awt.Container;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import com.gravitoids.panel.GravitoidsPanel;

public class GraitoidsGame extends JFrame implements WindowListener {
	private static int DEFAULT_FPS = 30;

	private GravitoidsPanel gp;

	public GraitoidsGame(long period) {
		super("The Pond Game");
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
		gp.pauseGame();
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

		new GraitoidsGame(period*1000000L);		 // ms --> nanosecs 
	}
}


