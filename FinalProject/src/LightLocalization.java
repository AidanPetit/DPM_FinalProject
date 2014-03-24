/*	DPM Final Project - LighLocalization Class
 *  ECSE211-DPM	Group 08
 *  Wei-Di Chang 260524917
 *  Aidan Petit
 */
import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;

/**
 *
 * Light Localization class which takes care of initial localization
 *
 *
 * @author Aidan Petit
 * @version 1.0
 * @since 1.0
 */

public class LightLocalization {
	/*
	 * This will probable be fairly buggy, I need to be in the lab to debug
	 */

	//private Team08Robot robot;	??
	private OdometryPoseProvider odo;
	private ColorSensor myLS;
	private Navigator myNav;
	private Driver myPilot;

	private double sensorOffset = 12; // in cm, not accurate needs to be measured

	public LightLocalization(OdometryPoseProvider odo, ColorSensor ls,Navigator nav, Driver driver) {
		/*
		 * Constructor for LightLocalization
		 *
		 * Use nav or pilot to control movement of the bot
		 * and odometer to get position info
		 */

		this.odo = odo;
		this.myLS = ls;
		this.myNav = nav;
		this.myPilot = driver;
	}

	public void doLocalization() {
		/*
		 * This localization assumes the robot is at roughly a 0 degrees heading
		 * and is located inside the bottom-left box.
		 *
		 * Localization is performed by rotating in place to detect 4 grid lines
		 * then performing some basic trigonometry to calculate the robots X and Y position
		 * and a more accurate value for the heading
		 */

		boolean anglesClocked = false;
		int lockCount = 0;

		// need to figure out how to find desired position
		// for now this is the coordinates used for team24 in lab 4

		myNav.goTo(7,8);
		myNav.rotateTo(90);

		double[] xAngles = new double[2];	//holds the two angles when the light sensor detects the X axis
		double[] yAngles = new double[2];	//holds the two angles for Y axis when sensor detects the line
		double xAxisIntersectAngle = 0; 	//holds angle when lightsensor hits negative X axis

		// start rotating and clock all 4 gridlines
		//rotate in place to clock angles
		myLS.setFloodlight(true);

		myPilot.rotateRight();

		while(!anglesClocked){ //keep rotating until all four angles have been clocked

			int currentReading = myLS.getNormalizedLightValue(); //get a new value for the light reading every iteration
			Pose currentPose = odo.getPose();

			LCD.drawString("count: "+ lockCount, 0, 6); //output for debugging, might need to disable LCDDisplay to use

			/*
			 * this uses a threshold. this is a bad method we should use a
			 * differential method to detect the change in light instead.
			 * Juan and I never implemented this
			 */

			if (currentReading<300) {
				double currentTheta = currentPose.getHeading();
				if (lockCount==0){ // negative x axis
					xAngles[0] = currentTheta;
					xAxisIntersectAngle = currentTheta; //save the negative x axis intersection angle for future reference
					lockCount++;
				}
				else if (lockCount==1){ //positive y
					yAngles[1] = currentTheta;
					lockCount++;
				}
				else if (lockCount==2){ //positive x
					xAngles[1] = currentTheta;
					lockCount++;
				}
				else if (lockCount==3){ //negative y
					yAngles[0] = currentTheta;
					lockCount++;
				}

				if (lockCount == 4){	// after 4 lines have been clocked the loop terminates and the robot stops moving
					myPilot.stop();
					anglesClocked = true;
					break;
				}
				try { Thread.sleep(100); } catch (InterruptedException e) {}  //this delay is to make sure a line is not detected twice
			}
		}

		/*
		//Some screen output for debugging
		LCD.clear();

		LCD.drawString("xAxis: " + xAxisIntersectAngle, 0, 2);
		LCD.drawString("yAxis: " + yAxisIntersectAngle, 0, 3);
		LCD.drawString("x1: " + xAngles[0], 0, 4);
		LCD.drawString("x2: " + xAngles[1], 0, 5);
		LCD.drawString("y1: " + yAngles[0], 0, 6);
		LCD.drawString("y2: " + yAngles[1], 0, 7);
		try { Thread.sleep(2000); } catch (InterruptedException e) {}
		 */


		// do trig to compute (0,0) and 0 degrees
		double thetaY = yAngles[0]-yAngles[1];
		double thetaX = xAngles[0]-xAngles[1];
		//thetaX/Y is the measure of the arc the robot travels between the positive and negative X/Y axis

		double X = -sensorOffset*Math.cos((thetaY/2)*Math.PI/180);
		double Y = -sensorOffset*Math.cos((thetaX/2)*Math.PI/180);
		//calculate your new positions using thetaX and thetaY

		double newHeadingY = 90 + (thetaX/2) - (xAxisIntersectAngle-180);

		// only the heading with respect to the Y axis was used
		//double newHeadingX = (thetaY/2) - (yAxisIntersectAngle-180);
		//double newHeading = (newHeadingX+newHeadingY)/2;

		/*
		 *  initialize a new Pose to update the Odometer
		 *  X and Y are cast to float, precision shouldnt be an issue
		 */
		float newTheta = (float) (odo.getPose().getHeading()+newHeadingY);
		float newX = (float) X;
		float newY = (float) Y;

		Pose pos = new Pose(newX,newY,newTheta);

		//set the odometer to reflect the new X, Y and theta values
		odo.setPose(pos);


		// when done travel to (0,0) and turn to 0 degrees
		myNav.goTo(0,0);
		myNav.rotateTo(0);

		//Sound sequence to indicate termination
		Sound.beepSequenceUp();
	}
}
