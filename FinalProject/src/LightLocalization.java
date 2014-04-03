/*	DPM Final Project - LighLocalization Class
 *  ECSE211-DPM	Group 08
 *  Wei-Di Chang 260524917
 *  Aidan Petit
 */
import lejos.nxt.ColorSensor;
import lejos.nxt.Sound;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;

/**
 *
 * Light Localization class which takes care of initial precise localization
 * Corner Numbers:
 * 1 - bottom left
 * 2 - bottom right
 * 3 - top right
 * 4 - top left
 *
 * @author Aidan Petit
 * @version 2.0
 * @since 1.0
 */

public class LightLocalization {
	private OdometryPoseProvider odo;
	private ColorSensor myLS;
	private Navigator myNav;
	private Driver myPilot;

	private double rotateSpeed = 60;
	private double sensorOffset = 12; // in cm, not accurate needs to be measured


	public LightLocalization(Team08Robot myBot) {
		/*
		 * Constructor for LightLocalization
		 *
		 */

		this.odo = myBot.getOdo();
		this.myLS = myBot.getRightCS();
		this.myNav = myBot.getNav();
		this.myPilot = myBot.getPilot();
	}

	public void doLocalization(int startingCorner) {
		/*
		 * This localization assumes the robot's heading is approximately
		 * correct and it's center of rotation is inside one of
		 * the starting corner boxes.
		 *
		 * Localization is performed by rotating in place to detect 4 grid lines
		 * then performing some basic trigonometry to calculate the robots X and Y position
		 * and a more accurate value for the heading
		 */

		boolean anglesClocked = false;
		int lockCount = 0;

		double[] xAngles = new double[2];	//holds the two angles when the light sensor detects the X axis
		double[] yAngles = new double[2];	//holds the two angles for Y axis when sensor detects the line

		double xAxisIntersectAngle = 0; 	//holds angle when lightsensor hits negative X axis
		double yAxisIntersectAngle = 0;


		// start rotating and clock all 4 gridlines

		myNav.rotateTo(0);
		myLS.setFloodlight(true);

		myPilot.setRotateSpeed(rotateSpeed);
		myPilot.rotateRight();

		while(!anglesClocked){ //keep rotating until all four angles have been clocked

			int currentReading = myLS.getNormalizedLightValue(); //get a new value for the light reading every iteration

			//			LCD.drawString("LS: "+ currentReading, 0, 6); //output for debugging, might need to disable LCDDisplay to use
			//			LCD.drawString("count: "+ lockCount, 0, 7); //output for debugging, might need to disable LCDDisplay to use

			/*
			 * this uses a threshold. this is a bad method we should use a
			 * differential method to detect the change in light instead.
			 * Juan and I never implemented this
			 */

			if (currentReading<470) {
				double currentTheta = odo.getPose().getHeading();
				if (lockCount==0){ // negative x axis
					xAngles[0] = currentTheta;
					xAxisIntersectAngle = currentTheta; //save the negative x axis intersection angle for future reference
					lockCount++;
				}
				else if (lockCount==1){ //positive y
					yAngles[0] = currentTheta;
					lockCount++;
				}
				else if (lockCount==2){ //positive x
					xAngles[1] = currentTheta;
					lockCount++;
				}
				else if (lockCount==3){ //negative y
					yAngles[1] = currentTheta;
					yAxisIntersectAngle = currentTheta;
					lockCount++;
				}

				Sound.beep();

				if (lockCount == 4){	// after 4 lines have been clocked the loop terminates and the robot stops moving
					myPilot.stop();
					anglesClocked = true;
					break;
				}
				try { Thread.sleep(75); } catch (InterruptedException e) {}  //this delay is to make sure a line is not detected twice
			}
		}

		/*
		//Some screen output for debugging
		LCD.clear();

		LCD.drawString("x1: " + xAngles[0], 0, 3);
		LCD.drawString("x2: " + xAngles[1], 0, 4);
		LCD.drawString("y1: " + yAngles[0], 0, 5);
		LCD.drawString("y2: " + yAngles[1], 0, 6);
		try { Thread.sleep(2000); } catch (InterruptedException e) {}
		 */

		//fix angles
		xAngles[1]=fixAngle(xAngles[1]);
		xAngles[0]=fixAngle(xAngles[0]);

		yAngles[1]=fixAngle(yAngles[1]);
		yAngles[0]=fixAngle(yAngles[0]);

		yAxisIntersectAngle = fixAngle(yAxisIntersectAngle);
		xAxisIntersectAngle = fixAngle(xAxisIntersectAngle);


		// do trig to compute (0,0) and 0 degrees
		double thetaY = Math.abs(yAngles[1]-yAngles[0]);
		double thetaX = Math.abs(xAngles[1]-xAngles[0]);


		double dX = sensorOffset*Math.cos((thetaY/2)*Math.PI/180);
		double dY = sensorOffset*Math.cos((thetaX/2)*Math.PI/180);
		//calculate your new positions using thetaX and thetaY

		double newHeadingY = 90 + (thetaX/2) - (xAxisIntersectAngle-180);
		//double newHeadingX = 90 + (thetaY/2) - (yAxisIntersectAngle-180);


		/*
		 *  initialize a new Pose to update the Odometer
		 *  X and Y are cast to float, precision shouldnt be an issue
		 */
		double currentTheta = fixAngle(odo.getPose().getHeading());
		double newTheta = fixAngle2(currentTheta+(newHeadingY-52.5));

		//set the odometer to reflect the new X, Y and theta values		
		if(startingCorner == 1) {
			float newX = (float) (0.0-dX);
			float newY = (float) (0.0-dY);
			float newT = (float) newTheta;
			Pose newPos = new Pose(newX,newY,newT);
			odo.setPose(newPos);
			myNav.goTo(0,0);


		}
		else if(startingCorner == 2) {
			float newX = (float) (304.8+dX);
			float newY = (float) (0.0-dY);
			float newT = (float) newTheta;
			Pose newPos = new Pose(newX,newY,newT);
			odo.setPose(newPos);
			myNav.goTo((float)304.8,(float)0.0);

		}
		else if(startingCorner == 3) {
			float newX = (float) (304.8-dX);
			float newY = (float) (304.8-dY);
			float newT = (float) newTheta;
			Pose newPos = new Pose(newX,newY,newT);
			odo.setPose(newPos);
			myNav.goTo((float)304.8,(float)304.8);

		}
		else if(startingCorner == 4) {
			float newX = (float) (0.0-dX);
			float newY = (float) (304.8-dY);
			float newT = (float) newTheta;
			Pose newPos = new Pose(newX,newY,newT);
			odo.setPose(newPos);
			myNav.goTo((float)0.0,(float)304.8);

		}

		while(myNav.isMoving()) {
			//do nothing
		}

		if(startingCorner == 1) {
			myNav.rotateTo(0);
		}
		else if(startingCorner == 2){
			myNav.rotateTo(90);
		}
		else if(startingCorner == 3){
			myNav.rotateTo(180);

		}
		else if(startingCorner == 4){
			myNav.rotateTo(-90);
		}

		try { Thread.sleep(15000); } catch (InterruptedException e) {}  //this delay is to make sure a line is not detected twice


	}

	/*
	 * return an angle between 0 and 360
	 */
	public static double fixAngle(double angle) {
		angle=angle%360;
		if(angle<0) angle+=360;
		return angle;
	}

	/*
	 * return an angle between -180 and 180
	 */
	public static double fixAngle2(double angle){
		angle = fixAngle(angle);
		if(angle>180) angle-=360;
		return angle;

	}

}
