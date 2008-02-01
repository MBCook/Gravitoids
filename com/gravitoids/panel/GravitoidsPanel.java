package com.gravitoids.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

import javax.swing.JPanel;

import com.gravitoids.bean.GravitoidsCircleObject;
import com.gravitoids.helper.GravityHelper;
import com.gravitoids.main.GraitoidsGame;

public class GravitoidsPanel extends JPanel implements Runnable, KeyListener {
	private static final int PANEL_WIDTH = 640;
	private static final int PANEL_HEIGHT = 480; 

	private static long MAX_STATS_INTERVAL = 1000000000L;	// Stats every second or so.

	// Number of uninterrupted runs before we force a break
	private static final int TIME_TO_YIELD = 16;
	private static int MAX_FRAME_SKIPS = 5;		// Maximum number of frames to skip at once
	private static int NUM_FPS = 10;		// How many FPS we keep for calculations

	// Statistics stuff

	private long statsInterval = 0L;		// in ns
	private long prevStatsTime;		
	private long totalElapsedTime = 0L;
	private long gameStartTime;
	private int timeSpentInGame = 0;		// in seconds

	private long frameCount = 0;
	private double fpsStore[];
	private long statsCount = 0;
	private double averageFPS = 0.0;

	private long framesSkipped = 0L;
	private long totalFramesSkipped = 0L;
	private double upsStore[];
	private double averageUPS = 0.0;

	private DecimalFormat df = new DecimalFormat("0.##");		 // 2 dp
	private DecimalFormat timedf = new DecimalFormat("0.####");	 // 4 dp

	private Font font;
	private FontMetrics metrics;
	
	// Animation stuff

	private Thread animator;
	private boolean running = false;	 // used to stop the animation thread
	private boolean isPaused = false;

	private long period;			// period between drawing in ns

	// Game stuff

	private GraitoidsGame gg;

	private GravitoidsCircleObject objectOne;
	private GravitoidsCircleObject objectTwo;
	private GravitoidsCircleObject objectThree;

	// Off screen rendering

	private Graphics dbg; 
	private Image dbImage = null;

	// And now.. methods!

	public GravitoidsPanel(GraitoidsGame thePG, long period) {
		gg = thePG;
		this.period = period;

		setBackground(Color.white);
		setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));

		setFocusable(true);
		requestFocus();		 	// the JPanel now has focus, so receives key events
		addKeyListener(this);	// Recieve key events

		// create game components

		objectOne = new GravitoidsCircleObject();
		objectTwo = new GravitoidsCircleObject();
		objectThree = new GravitoidsCircleObject();
		
		objectOne.setColor(Color.DARK_GRAY);
		objectOne.setMass(100.0);
		objectOne.setRadius(16.0);
		objectOne.setXSpeed(0.0);
		objectOne.setYSpeed(0.0);
		objectOne.setXPosition(320.0);
		objectOne.setYPosition(340.0);

		objectTwo.setColor(Color.GRAY);
		objectTwo.setMass(100.0);
		objectTwo.setRadius(16.0);
		objectTwo.setXSpeed(0.0);
		objectTwo.setYSpeed(0.0);
		objectTwo.setXPosition(40.0);
		objectTwo.setYPosition(40.0);
		
		objectThree.setColor(Color.PINK);
		objectThree.setMass(100.0);
		objectThree.setRadius(16.0);
		objectThree.setXSpeed(0.0);
		objectThree.setYSpeed(0.0);
		objectThree.setXPosition(600.0);
		objectThree.setYPosition(240.0);
		
		// Set up the mouse

		addMouseListener(	new MouseAdapter() {
								public void mousePressed(MouseEvent e) {
									testPress(e.getX(), e.getY());
								}
							});

		// Setup our font
		
		font = new Font("SansSerif", Font.BOLD, 24);
		metrics = this.getFontMetrics(font);
		
		// Initialise timing elements

		fpsStore = new double[NUM_FPS];
		upsStore = new double[NUM_FPS];

		for (int i=0; i < NUM_FPS; i++) {
			fpsStore[i] = 0.0;
			upsStore[i] = 0.0;
		}
	}

	// ------- Key Stuff --------

	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();

		if ((keyCode == KeyEvent.VK_ESCAPE) ||
			(keyCode == KeyEvent.VK_Q) ||
			(keyCode == KeyEvent.VK_END) ||
			((keyCode == KeyEvent.VK_C) && e.isControlDown()) ) {

			running = false;
		}

		if ((keyCode == KeyEvent.VK_LEFT) || 
			(keyCode == KeyEvent.VK_RIGHT) || 
			(keyCode == KeyEvent.VK_UP) || 
			(keyCode == KeyEvent.VK_DOWN)) {

			//moveFrog(keyCode);
		}
	}

	public void keyReleased(KeyEvent e) {
		// We don't use this
	}

	public void keyTyped(KeyEvent e) {
		// We don't use this
	}

	// -------------------------

	public void addNotify() {
		// Wait for the JPanel to be added to the JFrame before starting
		super.addNotify();		// Creates the peer
		startGame();			// Start the thread
	}

	private void startGame() {
		// Initialise and start the thread 
		
		if (animator == null || !running) {
			animator = new Thread(this);
			animator.start();
		}
	}	

	// ------------- game life cycle methods ------------
	// Called by the JFrame's window listener methods

	public void resumeGame(){
		// Called when the JFrame is activated / deiconified
		isPaused = false;
	} 

	public void pauseGame() {
		// Called when the JFrame is deactivated / iconified
		isPaused = true;
	} 

	public void stopGame() {
		// Called when the JFrame is closing
		running = false;
	}

	// ----------------------------------------------

	private void testPress(int x, int y) {
		// Check to see where the user clicked
/*		if (!isPaused && !gameOver) {
			if (fred.nearHead(x,y)) {		// was mouse press near the head?
				gameOver = true;
				score =	100;	// For now, hack together a score
			} else {	 // add an obstacle if possible
				if (!fred.touchedAt(x,y))		// was the worm's body untouched?
					obs.add(x,y);
			}
		// We don't do anything here yet
		} */
	}

	public void run() {
		long beforeTime, afterTime, timeDiff, sleepTime;
		long overSleepTime = 0L;
		int noDelays = 0;
		long excess = 0L;

		gameStartTime = System.nanoTime();
		prevStatsTime = gameStartTime;
		beforeTime = gameStartTime;

		running = true;

		while(running) {
			gameUpdate();
			gameRender();
			paintScreen();

			afterTime = System.nanoTime();
			timeDiff = afterTime - beforeTime;
			sleepTime = (period - timeDiff) - overSleepTime;	

			if (sleepTime > 0) {	 // some time left in this cycle
				try {
					Thread.sleep(sleepTime/1000000L);	 // nano -> ms
				}
				catch(InterruptedException ex){}
				overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
			} else {		// sleepTime <= 0; the frame took longer than the period
				excess -= sleepTime;	// store excess time value
				overSleepTime = 0L;

				if (++noDelays >= TIME_TO_YIELD) {
					Thread.yield();		// give another thread a chance to run
					noDelays = 0;
				}
			}

			beforeTime = System.nanoTime();

			/* If frame animation is taking too long, update the game state
				 without rendering it, to get the updates/sec nearer to
				 the required FPS. */
			int skips = 0;

			while((excess > period) && (skips < MAX_FRAME_SKIPS)) {
				excess -= period;
				gameUpdate();		 // update state but don't render
				skips++;
			}

			framesSkipped += skips;
			storeStats();
		}

		printStats();
		System.exit(0);
	}


	private void gameUpdate() {
		if (!isPaused) {
			GravityHelper gh = GravityHelper.getInstance();
			
			gh.simulateGravity(objectOne, objectTwo);
			gh.simulateGravity(objectTwo, objectThree);
			gh.simulateGravity(objectOne, objectThree);
			
			objectOne.move();
			objectTwo.move();
			objectThree.move();
		}
	}

	private void gameRender() {
		// Time to draw everything. First we'll setup the double-buffering image if needed.
		
		if (dbImage == null) {
			dbImage = createImage(PANEL_WIDTH, PANEL_HEIGHT);

			if (dbImage == null) {
				System.out.println("dbImage is null");
				return;
			} else {
				dbg = dbImage.getGraphics();
			}
		}

		// Draw stuff

		double nt;
		double ts = System.nanoTime();

		dbg.setColor(Color.WHITE);
		
		dbg.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
		
		objectOne.draw(dbg);
		objectTwo.draw(dbg);
		objectThree.draw(dbg);
		
		if (isPaused) {
			drawPaused(dbg);
		}
	}

	private void drawPaused(Graphics g) {
		String msg = "Game Paused";

		int x = (PANEL_WIDTH - metrics.stringWidth(msg))/2; 
		int y = (PANEL_HEIGHT - metrics.getHeight())/2;

		g.setColor(Color.red);
		g.setFont(font);
		g.drawString(msg, x, y);
	}

	private void paintScreen() {
		// Use active rendering to put the buffered image on-screen

		Graphics g;

		try {
			g = this.getGraphics();

			if ((g != null) && (dbImage != null))
				g.drawImage(dbImage, 0, 0, null);

			g.dispose();
		} catch (Exception e) {
			System.out.println("Graphics context error: " + e);
		}
	}

	private void storeStats() {
		/* The statistics:
				 - the summed periods for all the iterations in this interval
					 (period is the amount of time a single frame iteration should take), 
					 the actual elapsed time in this interval, 
					 the error between these two numbers;

				 - the total frame count, which is the total number of calls to run();

				 - the frames skipped in this interval, the total number of frames
					 skipped. A frame skip is a game update without a corresponding render;

				 - the FPS (frames/sec) and UPS (updates/sec) for this interval, 
					 the average FPS & UPS over the last NUM_FPSs intervals.

			 The data is collected every MAX_STATS_INTERVAL	 (1 sec).
		*/

		frameCount++;
		statsInterval += period;

		if (statsInterval >= MAX_STATS_INTERVAL) {		 // record stats every MAX_STATS_INTERVAL
			long timeNow = System.nanoTime();
			timeSpentInGame = (int) ((timeNow - gameStartTime)/1000000000L);	// ns --> secs

			long realElapsedTime = timeNow - prevStatsTime;		// time since last stats collection
			totalElapsedTime += realElapsedTime;

			double timingError = 
				 ((double)(realElapsedTime - statsInterval) / statsInterval) * 100.0;

			totalFramesSkipped += framesSkipped;

			double actualFPS = 0;			// calculate the latest FPS and UPS
			double actualUPS = 0;
			if (totalElapsedTime > 0) {
				actualFPS = (((double)frameCount / totalElapsedTime) * 1000000000L);
				actualUPS = (((double)(frameCount + totalFramesSkipped) / totalElapsedTime) 
																														 * 1000000000L);
			}

			// store the latest FPS and UPS
			fpsStore[ (int)statsCount%NUM_FPS ] = actualFPS;
			upsStore[ (int)statsCount%NUM_FPS ] = actualUPS;
			statsCount = statsCount+1;

			double totalFPS = 0.0;		 // total the stored FPSs and UPSs
			double totalUPS = 0.0;
			for (int i=0; i < NUM_FPS; i++) {
				totalFPS += fpsStore[i];
				totalUPS += upsStore[i];
			}

			if (statsCount < NUM_FPS) { // obtain the average FPS and UPS
				averageFPS = totalFPS/statsCount;
				averageUPS = totalUPS/statsCount;
			}
			else {
				averageFPS = totalFPS/NUM_FPS;
				averageUPS = totalUPS/NUM_FPS;
			}
/*
			System.out.println(timedf.format( (double) statsInterval/1000000000L) + " " + 
										timedf.format((double) realElapsedTime/1000000000L) + "s " + 
							df.format(timingError) + "% " + 
										frameCount + "c " +
										framesSkipped + "/" + totalFramesSkipped + " skip; " +
										df.format(actualFPS) + " " + df.format(averageFPS) + " afps; " + 
										df.format(actualUPS) + " " + df.format(averageUPS) + " aups" );
*/
			framesSkipped = 0;
			prevStatsTime = timeNow;
			statsInterval = 0L;		// reset
		}
	}

	private void printStats() {
		System.out.println("Frame Count/Loss: " + frameCount + " / " + totalFramesSkipped);
		System.out.println("Average FPS: " + df.format(averageFPS));
		System.out.println("Average UPS: " + df.format(averageUPS));
		System.out.println("Time Spent: " + timeSpentInGame + " secs");
	}
}
