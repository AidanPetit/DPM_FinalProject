import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.Pose;

/**
*
* Odometry Correction class, which uses two color sensors located at the rear of the robot
* to detect grid lines and correct the robots position
* 
* @param robot Team08Robot object used for this project
*
* @author Wei-Di Chang, Aidan Petit
* @version 3.0
* @since 1.0
*/
public class OdometerCorrection extends Thread{
	// objects this class requires
	private Team08Robot myBot;
	private OdometryPoseProvider myOdo;

	// lock object for Thread synchronization
	private Object myLock;	

	// count to restart correction if it has been too long without detecting a line
	int count;

	private static final long CORRECTION_PERIOD = 10;	// correction can occur every 10 ms
	
	//store the Pose from the odometer when a left or right line is detected
	private Pose leftPose;
	private Pose rightPose;
	
	//stores the current odometer pose
	private Pose currPose;
	
	//light values for sensors to detect grid lines
	double leftLight;
	double rightLight;

	public static final double COLORSENSORS_DISTANCE = 21; //this is correct

	//these values are not perfect
	public static final int LEFT_THRESHOLD = 60;   
	public static final int RIGHT_THRESHOLD = 60;
	
	//boolean to enable or disable correction
	private static boolean enabled;

	// left and right color sensors, facing down located at the rear of the robot
	ColorSensor leftCS;
	ColorSensor rightCS;

	//these are averaged values from 10 CS readings
	public double leftAverage;
	public double rightAverage;


/*
 * Constructor for Odometry Correction
 * 
 * Requires a Team08Robot object as a parameter and uses it to acquire 
 * the robot properties it needs, such as the Odometer and the color sensors
 */
	public OdometerCorrection (Team08Robot myBot) {
		this.myBot = myBot;
		this.myOdo = myBot.getOdo();
		this.leftCS = myBot.getLeftCS();
		this.rightCS = myBot.getRightCS();

		//correction is disabled by default
		enabled = false;	
	
		//initialize light averages and values to zero
		leftLight = 0;
		rightLight = 0;
		leftAverage = 0;
		rightAverage = 0;
		count = 0;
		
		//turn on both color sensors
		leftCS.setFloodlight(true);
		rightCS.setFloodlight(true);

		//initialize lock for synchronization
		myLock = new Object();
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}
	}

	/*
	 * The run method is what is executed when Odometry correction is started
	 * Correction only occurs when two grid lines, left and right, have been detected
	 */
	public void run() {
		
		
		//sensors calibrated each new run
		calibrateLightSensors();
		long correctionStart, correctionEnd;
		
		while(true) {
			
			correctionStart = System.currentTimeMillis();	// for timeout

			//integer indicating cardinal direction
			int heading = getHeadingNumber();

			
			count++;
			if (count == 100) {	//if 100 iterations occur without detecting both grid lines, reset everything
				count = 0;
				leftPose = null;
				rightPose = null;
			}
			if (!(myBot.isRotating())) {	//correction doesnt happen when the robot is rotating
				
				currPose = myOdo.getPose();

				//get new values from each color sensor
				leftLight = leftCS.getNormalizedLightValue();
				rightLight = rightCS.getNormalizedLightValue();
				
				//detect line under left CS
				if (leftLight < leftAverage - LEFT_THRESHOLD) {		
					leftPose = myOdo.getPose();
				}
				
				//detect line under right CS
				if (rightLight < rightAverage - RIGHT_THRESHOLD) {
					rightPose = myOdo.getPose();
				}

				//only apply correction when both sensors have detected a line
				if(leftPose != null && rightPose != null) {
					correctionCalculator(heading);
				}
			}

			correctionEnd = System.currentTimeMillis();

			// put the thread to sleep if the correction period hasn't expired yet
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD - (correctionEnd-correctionStart));
				} catch (InterruptedException e) {}
			}
		}
	}


	/*
	 * Return an integer indicating the robots rough heading based on the Odometer 
	 * Heading data. 
	 * 
	 * Returns:
	 *  0 - north
	 *  1 - east
	 *  2 - south 
	 *  3 - west
	 *  -1 - error, not close enough to one cardinal direction
	 */
	private int getHeadingNumber() {
		float angle = myOdo.getPose().getHeading();

		/*
		 * Using +/- 30% as a margin to determine rough cardinal direction
		 */
		if(angle>(0.7*90) && angle<(1.3*90)) {
			return 0;
		}
		else if(angle < 27 && angle > -27) {
			return 1;
		}
		else if(angle<(0.7*-90) && angle>(1.3*-90)) {
			return 2;
		}
		else if((angle > 153 && angle <= 180) || (angle < -153 && angle >= -180)) {
			return 3;
		}
		else{
			return -1;	//this means the robot is going diagonally
		}
	}

	/*
	 * This method calculates correction, based on which sensor detected a black line
	 * and which direction the robot is traveling in
	 */
	public void correctionCalculator(int currentHeading) {
		if(enabled) {		//only correct if enabled
			int heading = currentHeading;	
			double angle_rad;	
			double angle_deg;
			double positionCorrection;

			/*
			 * When a line is detected the Pose is stored in rightPose or leftPose.
			 * These are saved until the next correction period
			 * 
			 * Correction is applied only when both of these are set
			 * 
			 * Depending on the heading, correct the heading 
			 * and EITHER x or y position.
			 * 
			 */

			angle_rad = Math.atan((getDistance(leftPose,rightPose))/COLORSENSORS_DISTANCE); //angle correction
			angle_deg = angle_rad*(180/Math.PI);	//convert to degrees for the Odometer
			
			/*
			 * If the robot is traveling north or south, the positional correction 
			 * is in the Y axis
			 * 
			 * Otherwise correct for the X axis position
			 */
			if (heading== 0 || heading == 2){	//heading north or south, update Y position
				positionCorrection = Math.abs(leftPose.getY() - rightPose.getY())/2;
			}
			else {
				positionCorrection = Math.abs(leftPose.getX() - rightPose.getX())/2;
			}

			//change the sign of the correction to negative if left sensor is leading
			if (getDistance(leftPose,currPose) > getDistance(rightPose,currPose)) {
				angle_deg = -angle_deg;
			}


			/*
			 * switch block to properly apply correction to the heading
			 * and to either X or Y position
			 */
			switch(heading) {				
			case 0: correctTheta(90 + angle_deg);		//north +90 deg
			correctPositionY(positionCorrection);
			break;
			case 1: correctTheta(+angle_deg);			//east is 0 deg
			correctPositionX(positionCorrection);
			break;
			case 2: correctTheta(-90 + angle_deg);		//south is -90 deg
			correctPositionY(positionCorrection);
			break;
			case 3: correctTheta(180 + angle_deg);		//west +180 deg
			correctPositionX(positionCorrection);
			break;

			}
			calibrateLightSensors();		//recalibrate light sensors after each correction
		}
	}

	/*
	 * This method corrects the value of Theta 
	 */
	public void correctTheta(double degreeAngle) {
		
		LCD.clear(6);
		LCD.drawString("new H: "+degreeAngle, 0,6);
		
		Pose current = myOdo.getPose();
		current.setHeading((float)degreeAngle);

		synchronized (myLock) {
			myOdo.setPose(current);
			leftPose = null;
			rightPose = null;
			leftLight = 0;
			rightLight = 0;
		}
	}

	/*
	 * To correct Y position, take the current position and round to the 
	 * nearest 10. Then add the positionCorrection 
	 *
	 */
	public void correctPositionY(double positionCorrection) {	
		
		double currentvalue = myOdo.getPose().getY();
		double rounded = 10*Math.rint(currentvalue/10);
		rounded = rounded + ((rounded/30)*(0.48));
		positionCorrection = positionCorrection + rounded;

		//update odometer Y position
		Pose current = myOdo.getPose();	
		current.setLocation(current.getX(),(float) positionCorrection);
		myOdo.setPose(current);	
	}

	/*
	 * Nearly identical to the above method, but for X axis position instead of Y
	 */
	public void correctPositionX(double positionalcorrection) {
		double currentvalue = myOdo.getPose().getX();
		double rounded = 10*Math.rint(currentvalue/10);
		rounded = rounded + ((rounded/30)*(0.48));
		positionalcorrection = positionalcorrection + rounded;

		//update odometer X position
		Pose current = myOdo.getPose();	
		current.setLocation((float) positionalcorrection,current.getY());
		myOdo.setPose(current);		
	}

	/*
	 * Method that calculates and stores an average value for each color sensor separately
	 * Using an averaged value and a threshold, line detection can be made very robust and reliable even under various lighting conditions
	 */
	public void calibrateLightSensors() {
		
		double left = 0;
		double right = 0;

		for (int i = 0; i < 10; i++) {
			left = left + leftCS.getNormalizedLightValue();
			right = right + rightCS.getNormalizedLightValue();
		}

		leftAverage = left/10;
		rightAverage = right/10;

	}
	
	// Accessor to enable or diable correction
	public static void enableCorrection(){
		enabled = true;
	}
	
	public static void disableCorrection() {
		enabled = false;
	}
	
	//Helper method to find distance between two points
	public static double getDistance(Pose start, Pose end) {
		double x1 = start.getX();
		double x2 = end.getX();
		double y1 = start.getY();
		double y2 = end.getY();
		
		double result = Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
		
		return result;
	}

}
