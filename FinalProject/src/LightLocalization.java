/*	DPM Final Project - LighLocalization Class
 *  ECSE211-DPM	Group 08
 *  Wei-Di Chang 260524917
 *  Aidan Petit
 */
import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;

/**
 *
 * Light Localization provides a precise positional and angular localization 
 * after USLocalization has been used to find the robot's heading
 *
 * @param robot Team08Robot object used for this project
 *
 * @author Aidan Petit
 * @version 3.0
 * @since 1.0
 */

public class LightLocalization {
	private OdometryPoseProvider odo;
	private ColorSensor myCS;
	private Navigator myNav;
	private Driver myPilot;

	private double rotateSpeed = 60;	//units are degrees/second
	private double sensorOffset = 12; 	// in centimeters

	public int LS_THRESHOLD = 50; 	//this value was updated for the competition


	public LightLocalization(Team08Robot myBot) {
		/*
		 * Constructor for LightLocalization
		 * Acquires the objects it needs from Team08Robot object,
		 * such as the odometer, the navigation class and the Pilot object.
		 *
		 */
		this.odo = myBot.getOdo();
		this.myCS = myBot.getRightCS();
		this.myNav = myBot.getNav();
		this.myPilot = myBot.getPilot();
		myCS.setFloodlight(true);

	}

	public void doLocalization(int startingCorner) {
		/*
		 * This localization assumes the robot's heading is approximately
		 * correct (within ~10 degrees) and it's center of rotation is inside one of
		 * the starting corner boxes.
		 *
		 * Localization is performed by rotating in place to detect 4 grid lines
		 * then performing some basic trigonometry to calculate the robots X and Y position
		 * and an accurate value for the heading
		 */
		
		double averageLight = calibrateLS();	//use an averaged value as a threshold to detects lines

		boolean anglesClocked = false;
		int lockCount = 0;

		double[] xAngles = new double[2];	//holds the two angles when the light sensor detects the X axis
		double[] yAngles = new double[2];	//holds the two angles for Y axis when sensor detects the line

		double xAxisIntersectAngle = 0; 	//holds angle when lightsensor hits negative X axis
		double yAxisIntersectAngle = 0;
		

		// start rotating and clock all 4 gridlines
		// by rotating to 0 before begining the robot will detect the lines
		// in this order: -x, +y, +x, -y
		myPilot.setRotateSpeed(rotateSpeed);
		myNav.rotateTo(0);
		myPilot.rotateRight();

		while(!anglesClocked){ //keep rotating until all four angles have been clocked
			
			int currentReading = myCS.getNormalizedLightValue(); //get a new value for the light reading every iteration
				
			if(currentReading < averageLight - LS_THRESHOLD) {	//averaged value along with a threshold was used to detect lines
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

				if (lockCount == 4){	// after 4 lines have been clocked the loop terminates and the robot stops moving
					myPilot.stop();
					anglesClocked = true;
					break;
				}
				try { Thread.sleep(75); } catch (InterruptedException e) {}  //this delay is to make sure a line is not detected twice
			}
		}

		//fix angles
		xAngles[1]=fixAngle(xAngles[1]);
		xAngles[0]=fixAngle(xAngles[0]);

		yAngles[1]=fixAngle(yAngles[1]);
		yAngles[0]=fixAngle(yAngles[0]);

		yAxisIntersectAngle = fixAngle(yAxisIntersectAngle);
		xAxisIntersectAngle = fixAngle(xAxisIntersectAngle);


		// find the arc angle between the positive and negative axis for X and Y
		double thetaY = Math.abs(yAngles[1]-yAngles[0]);
		double thetaX = Math.abs(xAngles[1]-xAngles[0]);

		// do trig to compute (0,0) and 0 degrees
		double dX = sensorOffset*Math.cos((thetaY/2)*Math.PI/180);
		double dY = sensorOffset*Math.cos((thetaX/2)*Math.PI/180);
		
		
		//calculate your new positions using thetaX and thetaY
		double newHeadingY = 90 + (thetaX/2) - (xAxisIntersectAngle-180);


		/*
		 *  initialize a new Pose to update the Odometer
		 *  X and Y are cast from double to float, precision shouldn't be an issue
		 */
		double currentTheta = fixAngle(odo.getPose().getHeading());
		double newTheta = fixAngle2(currentTheta+(newHeadingY-56));	// this subtraction is to account for the light sensor being off center  

		/*
		 * Update the Odometer to reflect the new X, Y and Theta values	and travel
		 * to the nearest intersect of X and Y grid lines
		 * 
		 * The correction to X and Y position is added or subtracted accordingly
		 * from the coordinates of the grid line intersect near that starting corner	
		 */
		
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
			float newX = (float) (304.8+dX);
			float newY = (float) (304.8+dY);
			float newT = (float) newTheta;
			Pose newPos = new Pose(newX,newY,newT);
			odo.setPose(newPos);
			myNav.goTo((float)304.8,(float)304.8);

		}
		else if(startingCorner == 4) {
			float newX = (float) (0.0-dX);
			float newY = (float) (304.8+dY);
			float newT = (float) newTheta;
			Pose newPos = new Pose(newX,newY,newT);
			odo.setPose(newPos);
			myNav.goTo((float)0.0,(float)304.8);

		}

		while(myNav.isMoving()) {
			//wait until the robot moves to the grid lines intersection
		}

		//rotate to a perpendicular position depending on which corner the robot is in
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
	 * return an angle between -179 and 180
	 */
	public static double fixAngle2(double angle){
		angle = fixAngle(angle);
		if(angle>180) angle-=360;
		return angle;

	}
	
	/*
	 * this method averages 5 light values from the color sensor and 
	 * stores the average which is use as a threshold to detect gridlines
	 */
	public double calibrateLS() {
			
		double result = 0;

		for (int i = 0; i < 5; i++) {
			result += myCS.getNormalizedLightValue();
		}

		return result/5;	
	}
	
}
