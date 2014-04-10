/*	DPM Final Project - Display Class
 *  ECSE211-DPM	Group 08
 *  Wei-Di Chang 260524917
 *  Aidan Petit
 */
import lejos.nxt.LCD;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.util.Timer;
import lejos.util.TimerListener;

/**
 *
 * LCD Display class displays Odometer information and allows 
 * the display to run as a separate timed thread.
 * 
 * Only lines 0,1,and 2 are used to allow developers 
 * to use LCD as a debugging tool
 * 
 * @param odometer OdometryPoseProvider which tracks the robots Pose (position and heading) 
 * 
 * @author Wei-Di Chang, Aidan Petit
 * @version 3.0
 * @since 1.0
 */

public class LCDDisplay implements TimerListener{
	public static final int LCD_REFRESH = 100;	//updates display every 100 ms
	private OdometryPoseProvider odo;			
	private Timer lcdTimer;						// times to control updating the display
	private double xPos,yPos, heading;			// doubles to hold X, Y and theta values

	public LCDDisplay(OdometryPoseProvider odo) {
		this.odo = odo;
		this.lcdTimer = new Timer(LCD_REFRESH, this);


		// start the timer
		lcdTimer.start();
	}

	/*
	 * This method executes when the timer runs out every 100 ms and 
	 * updates the robot's display
	 */
	public void timedOut() {	
		xPos=odo.getPose().getX();
		yPos=odo.getPose().getY();
		heading=odo.getPose().getHeading();

		//clear lines 0, 1, and 2
		LCD.clear(0);
		LCD.clear(1);
		LCD.clear(2);

		// print the new information to the screen
		LCD.drawString("X: ", 0, 0);
		LCD.drawString("Y: ", 0, 1);
		LCD.drawString("H: ", 0, 2);
		LCD.drawInt((int)(xPos ), 3, 0);	// coordinates and angles are cast to integers for ease of display
		LCD.drawInt((int)(yPos), 3, 1);
		LCD.drawInt((int)heading, 3, 2);
	}
}
