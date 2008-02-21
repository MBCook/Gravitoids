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
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

import com.gravitoids.bean.GravitoidsCircleObject;
import com.gravitoids.bean.GravitoidsObject;
import com.gravitoids.bean.IntelligentGravitoidsShip;
import com.gravitoids.helper.GravityHelper;
import com.gravitoids.main.GravitoidsGame;

public class GravitoidsPanel extends JPanel implements Runnable, KeyListener {
	public static final int PANEL_WIDTH = 640;
	public static final int PANEL_HEIGHT = 480; 

	private static long MAX_STATS_INTERVAL = 1000000000L;	// Stats every second or so.

	// Number of uninterrupted runs before we force a break
	
	private static final int TIME_TO_YIELD = 16;
	private static final int MAX_FRAME_SKIPS = 5;		// Maximum number of frames to skip at once
	private static final int NUM_FPS = 10;		// How many FPS we keep for calculations

	private static final int NUM_SHIPS = 200;
	
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
	
	private long generation = 0;
	private int trial = 0;
	
	private final int MAX_TRIALS = 10;
	
	// Animation stuff

	private Thread animator;
	private boolean running = false;	 // used to stop the animation thread
	private boolean isPaused = false;

	private long period;			// period between drawing in ns

	// Game stuff

	private GravitoidsGame gg;

	private GravitoidsCircleObject universeObjects[];
	private List<IntelligentGravitoidsShip> ships = new ArrayList<IntelligentGravitoidsShip>();
	private List<IntelligentGravitoidsShip> deadShips = new ArrayList<IntelligentGravitoidsShip>();

	// Off screen rendering

	private Graphics dbg; 
	private Image dbImage = null;

	// And now.. methods!

	public GravitoidsPanel(GravitoidsGame thePG, long period) {
		gg = thePG;
		this.period = period;

		setBackground(Color.white);
		setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));

		setFocusable(true);
		requestFocus();		 	// the JPanel now has focus, so receives key events
		addKeyListener(this);	// Recieve key events

		// create game components
		
		prepareUniverse();
		prepareShips();
		
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

	private synchronized void prepareUniverse() {
		int numberToMake = ((int) (Math.random() * 4)) + 7;
		
		universeObjects = new GravitoidsCircleObject[numberToMake];
		
		for (int i = 0; i < numberToMake; i++) {
			GravitoidsCircleObject gco = new GravitoidsCircleObject();
			
			universeObjects[i] = gco;
			
			// Simple attributes
			
			gco.setColor(getAColorWeLike());					// Gets a color
			gco.setMass(100 + Math.random() * 900.0);				// Up to 1000 units of mass
			gco.setRadius(4.0 + Math.floor(Math.random() * 12.0));	// Up to 4 to 16 pixels radius
			
			if (i < 5) {
				gco.setMoveable(Math.random() >= 0.5);				// Random chance of movement
			} else {
				gco.setMoveable(true);								// Make sure there are always some
			}
			
			// Positioning
			
			gco.setXPosition(Math.random() * PANEL_WIDTH);		// Randomly positioned
			gco.setYPosition(Math.random() * PANEL_HEIGHT);
			
			// Now make sure they aren't in the "start box"
			
			while ((gco.getXPosition() >= 0.3 * PANEL_WIDTH) && (gco.getXPosition() <= 0.7 * PANEL_WIDTH)
					&& (gco.getYPosition() >= 0.3 * PANEL_HEIGHT) && (gco.getYPosition() <= 0.7 * PANEL_HEIGHT)) {
				// They're in the middle section, try again
				
				gco.setXPosition(Math.random() * PANEL_WIDTH);
				gco.setYPosition(Math.random() * PANEL_HEIGHT);
			}
			
			// Speed
			
			if (gco.isMoveable()) {
				if (Math.random() >= 0.5) {
					gco.setXSpeed(Math.random() * 20.0 - 10.0);	// -10 to 10
				}
				
				if (Math.random() >= 0.5) {
					gco.setYSpeed(Math.random() * 20.0 - 10.0);	// -10 to 10
				}
			} else {
				gco.setXSpeed(0.0);
				gco.setYSpeed(0.0);
			}
		}
	}
	
	private synchronized void prepareShips() {
		if (deadShips.size() == 0) {
			// Just generate random ships
		
			for (int i = 0; i < NUM_SHIPS; i++) {
				IntelligentGravitoidsShip igs = new IntelligentGravitoidsShip();
				
				igs.setName(igs.toString());
				
				igs.setRadius(5.0);
				igs.setMass(1.0);
				igs.setMoveable(true);
				igs.setThrust(0.0);
				igs.setXPosition(PANEL_WIDTH / 2);
				igs.setYPosition(PANEL_HEIGHT / 2);
				igs.setXSpeed(0.0);
				igs.setYSpeed(0.0);
				igs.setXThrustPortion(0.0);
				igs.setXThrustPortion(0.0);
				
				ships.add(igs);
			}
		} else {
			// We're going to breed. First sort the dead list by longest life
			
			Collections.sort(deadShips, new Comparator<IntelligentGravitoidsShip>() {
											public int compare(IntelligentGravitoidsShip one, IntelligentGravitoidsShip two) {
												return new Long(one.getAge()).compareTo(two.getAge());
											}
										});
			
			// Reverse it so it's in decending order, longest first
			
			Collections.reverse(deadShips);
			
			System.out.println("Oldest survived of generation " + generation + ": " + deadShips.get(0).getAge());
			
			saveDeadShips();
			
			generation++;
			
			// Copy the first ten entries over
			
			for (int i = 0; i < 10; i++) {
				IntelligentGravitoidsShip igs = new IntelligentGravitoidsShip(deadShips.get(i).getBrain());
				
				igs.setName(igs.toString());
				
				igs.setRadius(5.0);
				igs.setMass(1.0);
				igs.setMoveable(true);
				igs.setThrust(0.0);
				igs.setXPosition(PANEL_WIDTH / 2);
				igs.setYPosition(PANEL_HEIGHT / 2);
				igs.setXSpeed(0.0);
				igs.setYSpeed(0.0);
				igs.setXThrustPortion(0.0);
				igs.setXThrustPortion(0.0);
				
				ships.add(igs);
			}
			
			// Now make ~81 children children from the top 10 survivors
			
			for (int i = 0; i < 10; i++) {
				for (int j = 1; j < 10; j++) {
					if (i == j)
						continue;
					
					double[] brain = IntelligentGravitoidsShip.breed(deadShips.get(i), deadShips.get(j));
					
					IntelligentGravitoidsShip igs = new IntelligentGravitoidsShip(brain);
					
					igs.setName(igs.toString());
					
					igs.setRadius(5.0);
					igs.setMass(1.0);
					igs.setMoveable(true);
					igs.setThrust(0.0);
					igs.setXPosition(PANEL_WIDTH / 2);
					igs.setYPosition(PANEL_HEIGHT / 2);
					igs.setXSpeed(0.0);
					igs.setYSpeed(0.0);
					igs.setXThrustPortion(0.0);
					igs.setXThrustPortion(0.0);
					
					ships.add(igs);
				}
			}
			
			// Now do it agian for the absolute cream of the crop, so they can breed more
			
			for (int i = 0; i < 3; i++) {
				for (int j = 1; j < 5; j++) {
					if (i == j)
						continue;
					
					double[] brain = IntelligentGravitoidsShip.breed(deadShips.get(i), deadShips.get(j));
					
					IntelligentGravitoidsShip igs = new IntelligentGravitoidsShip(brain);
					
					igs.setName(igs.toString());
					
					igs.setRadius(5.0);
					igs.setMass(1.0);
					igs.setMoveable(true);
					igs.setThrust(0.0);
					igs.setXPosition(PANEL_WIDTH / 2);
					igs.setYPosition(PANEL_HEIGHT / 2);
					igs.setXSpeed(0.0);
					igs.setYSpeed(0.0);
					igs.setXThrustPortion(0.0);
					igs.setXThrustPortion(0.0);
					
					ships.add(igs);
				}
			}
			
			// Now we get the 10 worst ships from last time
			
			for (int i = NUM_SHIPS - 10; i < NUM_SHIPS; i++) {
				IntelligentGravitoidsShip igs = new IntelligentGravitoidsShip(deadShips.get(i).getBrain());
				
				igs.setName(igs.toString());
				
				igs.setRadius(5.0);
				igs.setMass(1.0);
				igs.setMoveable(true);
				igs.setThrust(0.0);
				igs.setXPosition(PANEL_WIDTH / 2);
				igs.setYPosition(PANEL_HEIGHT / 2);
				igs.setXSpeed(0.0);
				igs.setYSpeed(0.0);
				igs.setXThrustPortion(0.0);
				igs.setXThrustPortion(0.0);
				
				ships.add(igs);
			}
			
			// Now 10 random ships
			
			for (int i = 0; i < 10; i++) {
				IntelligentGravitoidsShip igs = new IntelligentGravitoidsShip(deadShips.get((int) (Math.random() * NUM_SHIPS)).getBrain());
				
				igs.setName(igs.toString());
				
				igs.setRadius(5.0);
				igs.setMass(1.0);
				igs.setMoveable(true);
				igs.setThrust(0.0);
				igs.setXPosition(PANEL_WIDTH / 2);
				igs.setYPosition(PANEL_HEIGHT / 2);
				igs.setXSpeed(0.0);
				igs.setYSpeed(0.0);
				igs.setXThrustPortion(0.0);
				igs.setXThrustPortion(0.0);
				
				ships.add(igs);
			}
			
			// Add random ships to fill things up
			
			while (ships.size() < NUM_SHIPS) {
				IntelligentGravitoidsShip igs = new IntelligentGravitoidsShip();
				
				igs.setName(igs.toString());
				
				igs.setRadius(5.0);
				igs.setMass(1.0);
				igs.setMoveable(true);
				igs.setThrust(0.0);
				igs.setXPosition(PANEL_WIDTH / 2);
				igs.setYPosition(PANEL_HEIGHT / 2);
				igs.setXSpeed(0.0);
				igs.setYSpeed(0.0);
				igs.setXThrustPortion(0.0);
				igs.setXThrustPortion(0.0);
				
				ships.add(igs);
			}
			
			// Clear the dead list
			
			deadShips.clear();
		}
	}
	
	private void saveDeadShips() {
		try {
			//FileWriter fw = new FileWriter("/Users/michael/desktop/graivtoids_ships.dat");
			FileWriter fw = new FileWriter("c:\\graivtoids_ships.dat");
			
			fw.append("Generation: " + generation + "\n");
			
			for (int i = 1; i <= NUM_SHIPS; i++) {
				IntelligentGravitoidsShip ship = deadShips.get(i - 1);
				
				fw.append("" + i + "," + ship.getAge());
				
				for (int j = 0; j < ship.getBrain().length; j++) {
					fw.append("," + ship.getBrain()[j]);
				}
				
				fw.append("\n");
			}
			
			fw.close();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);	// We'll just co-opt this
		}
	}
	
	private Color getAColorWeLike() {
		int c = (int) (Math.random() * 10.0);
		
		switch (c) {
			case 0:
				return Color.BLACK;
			case 1:
				return Color.CYAN;
			case 2:
				return Color.DARK_GRAY;
			case 3:
				return Color.GRAY;
			case 4:
				return Color.GREEN;
			case 5:
				return Color.LIGHT_GRAY;
			case 6:
				return Color.MAGENTA;
			case 7:
				return Color.ORANGE;
			case 8:
				return Color.PINK;
			case 9:
				return Color.RED;
			default:
				return null;
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
		} else if (keyCode == KeyEvent.VK_M) {
			IntelligentGravitoidsShip.setDrawMotivation(!IntelligentGravitoidsShip.isDrawMotivation());
		} else if (keyCode == KeyEvent.VK_T) {
			IntelligentGravitoidsShip.setDrawThrust(!IntelligentGravitoidsShip.isDrawThrust());
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


	private synchronized void gameUpdate() {
		if (!isPaused) {
			GravityHelper gh = GravityHelper.getInstance();
			
			// First on our main objects
			
			for (int i = 0; i < universeObjects.length - 1; i++) {
				for (int j = i + 1; j < universeObjects.length; j++) {
					gh.simulateGravity(universeObjects[i], universeObjects[j]);
				}
				
				for (int j = 0; j < ships.size(); j++) {
					gh.simulateGravityForOne(ships.get(j), universeObjects[i]);
				}
			}
			
			// Put them in the right spots
			
			for (int i = 0; i < universeObjects.length; i++) {
				universeObjects[i].move();
				checkBounds(universeObjects[i]);
			}
			
			// Now simulate our ships
			
			Iterator<IntelligentGravitoidsShip> it = ships.iterator();
			
			while (it.hasNext()) {
				IntelligentGravitoidsShip ship = it.next();
				
				ship.prepareMove(universeObjects);
				ship.move();
				
				// Now collision check
				
				boolean collided = false;
				
				collided = checkBounds(ship);
				
				if (!collided) {
					for (int i = 0; i < universeObjects.length; i++) {
						if (ship.hasCollided(universeObjects[i])) {
							collided = true;
							break;
						}
					}
				}
				
				// Handle any possible collisions
				
				if (collided) {
					it.remove();			// Remove us
					deadShips.add(ship);	// Add us to the dead ships
				} else {
					ship.incrementAge();	// Age us					
				}
			}
			
			// Reset things if all the ships are dead
			
			if (ships.size() == 0) {
				// Out of ships
				
				trial++;
				
				if (trial == MAX_TRIALS) {
					// Breed new ships, start a new trial
					
					prepareShips();
					prepareUniverse();
					
					trial = 0;
				} else {
					// We want things to keep going with the same ships, so move them to the live list
					
					ships.addAll(deadShips);
					deadShips.clear();
					
					// Reset all their locations and such, then get a new universe
					
					for (IntelligentGravitoidsShip ship : ships) {
						ship.setXPosition(PANEL_WIDTH / 2.0);
						ship.setYPosition(PANEL_HEIGHT / 2.0);
						ship.setXSpeed(0.0);
						ship.setYSpeed(0.0);
					}
					
					prepareUniverse();
				}				
			}
		}
	}

	private boolean checkBounds(GravitoidsObject object) {
		// Check the object against the bounds of (the) reality
		
		boolean atEdge = false;
		
		if (object.getXPosition() < 0) {
			object.setXPosition(0);
			object.setXSpeed(0);
			atEdge = true;
		} else if (object.getXPosition() > PANEL_WIDTH) {
			object.setXPosition(PANEL_WIDTH);
			object.setXSpeed(0);
			atEdge = true;
		}
		
		if (object.getYPosition() < 0) {
			object.setYPosition(0);
			object.setYSpeed(0);
			atEdge = true;
		} else if (object.getYPosition() > PANEL_HEIGHT) {
			object.setYPosition(PANEL_HEIGHT);
			object.setYSpeed(0);
			atEdge = true;
		}
		
		return atEdge;
	}
	
	private synchronized void gameRender() {
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
		
		for (int i = 0; i < universeObjects.length; i++) {
			universeObjects[i].draw(dbg);
		}
		
		for (int i = 0; i < ships.size(); i++) {
			ships.get(i).draw(dbg);
		}
		
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
