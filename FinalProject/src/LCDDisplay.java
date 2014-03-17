import lejos.nxt.LCD;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class LCDDisplay implements TimerListener{
	public static final int LCD_REFRESH = 100;
	private OdometryPoseProvider odo;
	private Timer lcdTimer;
	private double xPos,yPos, heading;
	
	// arrays for displaying data
	private double [] pos;
	
	public LCDDisplay(OdometryPoseProvider odo) {
		this.odo = odo;
		this.lcdTimer = new Timer(LCD_REFRESH, this);
		
		// initialise the arrays for displaying data
		
		// start the timer
		lcdTimer.start();
	}
	
	public void timedOut() { 
		xPos=odo.getPose().getX();
		yPos=odo.getPose().getY();
		heading=odo.getPose().getHeading();

		LCD.clear();
		LCD.drawString("X: ", 0, 0);
		LCD.drawString("Y: ", 0, 1);
		LCD.drawString("H: ", 0, 2);
		LCD.drawInt((int)(xPos ), 3, 0);
		LCD.drawInt((int)(yPos), 3, 1);
		LCD.drawInt((int)heading, 3, 2);
	}
}
