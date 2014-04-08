import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;

public class OdometerCorrection extends Thread{

	private Team08Robot myBot;

	private OdometryPoseProvider myOdo;
//	private Navigator myNav;
//	private Driver myPilot;
//	private UltrasonicSensor localizationUS;

	private Object lock;

	int countTime;

	private static final long CORRECTION_PERIOD = 10;

	//these store the X or Y coordinates of the odometer's position
	//when a black line is detected
	public double leftLineValue;
	public double rightLineValue;

	double leftLight;
	double rightLight;

	//this is correct
	public static final double COLORSENSORS_DISTANCE = 21;

	//need to experiment with these values
	public static final int ANGLE_TOLERANCE = 0; //this value needs to be experimented with!
	public static final int SENSOR_THRESHOLD = 70; //this value needs to be experimented with!


	private static boolean enabled;

	ColorSensor leftCS;
	ColorSensor rightCS;

	//this is an average of 10 readings from the color sensors
	//used to make the system less susceptible to changes in light conditions
	public double averageLight;



	public OdometerCorrection (Team08Robot myBot) {
		this.myBot = myBot;
		this.myOdo = myBot.getOdo();
		this.leftCS = myBot.getLeftCS();
		this.rightCS = myBot.getRightCS();

		enabled = false;
		averageLight = 0;
		leftLineValue = 0;
		rightLineValue = 0;
		leftLight = 0;
		rightLight = 0;
		countTime = 0;

		lock = new Object();
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}

	}

	public void run() {
		leftCS.setFloodlight(true);
		rightCS.setFloodlight(true);
		
		//sensors calibrated each new run
		calibrateLightSensors();

		long correctionStart, correctionEnd;

		while(true) {
			correctionStart = System.currentTimeMillis();

			//int indicating direction
			//0 - north, 1 - east, 2 - south, 3 - west
			int heading = getHeadingNumber();

			LCD.clear(4);
			LCD.drawString("heading: "+heading, 0,4);

			
			countTime++;
			if (countTime == 100) {
				countTime = 0;
				leftLineValue = 0;
				rightLineValue = 0;
			}
			if (!(myBot.isRotating())) {

				leftLight = leftCS.getNormalizedLightValue();
				rightLight = rightCS.getNormalizedLightValue();
				
//				LCD.drawString("L: "+leftLight, 0,3);
//				LCD.drawString("R: "+rightLight, 0,4);
//				LCD.drawString("AVG: "+averageLight, 0,7);

				//detect line under left CS
				if (leftLight < averageLight - SENSOR_THRESHOLD) {
					
					Sound.beep();
					
					if (heading == 0 || heading == 2) {
						leftLineValue = myOdo.getPose().getY();
					}
					else {
						leftLineValue = myOdo.getPose().getX();
					}
				}
				//detect line under right CS
				if (rightLight < averageLight - SENSOR_THRESHOLD) {
					
					Sound.buzz();
					
					if (heading == 0 || heading == 2) {
						rightLineValue = myOdo.getPose().getY();
					}
					else {
						rightLineValue = myOdo.getPose().getX();
					}
				}

				//only apply correction when both sensors have detected a line
				if (leftLineValue != 0 && rightLineValue != 0) {
					correctionCalculator(heading);
				}
			}

			correctionEnd = System.currentTimeMillis();

			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD - (correctionEnd-correctionStart));
				} catch (InterruptedException e) {}
			}
		}
	}


	/*
	 * Return an int indicating the robots heading based on the odometer's 
	 * Heading data. -1 indicates an error
	 *  0 - north
	 *  1 - east
	 *  2 - south 
	 *  3 - west
	 */
	private int getHeadingNumber() {
		float angle = myOdo.getPose().getHeading();

		if(angle>(0.7*90) && angle<(1.3*90)) {
			return 0;
		}
		else if((angle > 153 && angle <= 180) || (angle < -153 && angle >= -180)) {
			return 1;
		}
		else if(angle<(0.7*-90) && angle>(1.3*-90)) {
			return 2;
		}
		else if(angle < 27 && angle > -27) {
			return 3;
		}
		else{
			return -1;
		}
	}

	//this is the method which calculates correction, both based on which sensor detected black and which direction the robot is heading in
	public void correctionCalculator(int currentHeading) {
		if(enabled) {
			int heading = currentHeading;

			double angle_rad;
			double angle_deg;
			double positionCorrection;

			/*
			 * When a line is detected the light value reported by the colorsensor
			 * is stored in rightLineValue or leftLineValue. These values are saved until the next correction period
			 * 
			 * Correction is applied only when both of these are set
			 * 
			 * Depending on the heading, correct the heading 
			 * and EITHER x or y position.
			 * 
			 */

			angle_rad = Math.atan((rightLineValue - leftLineValue)/COLORSENSORS_DISTANCE); //angle correction
			angle_deg = angle_rad*(180/Math.PI);
			positionCorrection = Math.abs(rightLineValue - leftLineValue)/2;

			LCD.clear(5);
			LCD.drawString("deg: "+angle_deg, 0,5);

			
			switch(heading) {				
			case 0: correctTheta(90 - angle_deg);		//north +90 deg
			correctPositionY(positionCorrection);
			break;
			case 1: correctTheta(-angle_deg);			//east is 0 deg
			correctPositionX(positionCorrection);
			break;
			case 2: correctTheta(-90 - angle_deg);		//south is -90 deg
			correctPositionY(positionCorrection);
			break;
			case 3: correctTheta(180 - angle_deg);		//west +180 deg
			correctPositionX(positionCorrection);
			break;

			}
		}
	}

	public void correctTheta(double degreeAngle) {
		
		LCD.clear(6);
		LCD.drawString("new H: "+degreeAngle, 0,6);
		
		Pose current = myOdo.getPose();
		current.setHeading((float)degreeAngle);

		synchronized (lock) {
			myOdo.setPose(current);
			leftLineValue = 0;
			rightLineValue = 0;
			leftLight = 0;
			rightLight = 0;
		}
	}


	public void correctPositionY(double positionCorrection) {	
		/*
		 * To correct Y position, take the current position and round to the 
		 * nearest 10. Then add the positionCorrection 
		 *
		 */
		
		double currentvalue = myOdo.getPose().getY();
		double rounded = 10*Math.rint(currentvalue/10);
		rounded = rounded + ((rounded/30)*(0.48));
		positionCorrection = positionCorrection + rounded;

		//update odometer Y position
		Pose current = myOdo.getPose();	
		current.setLocation(current.getX(),(float) positionCorrection);
		myOdo.setPose(current);	
	}

	public void correctPositionX(double positionalcorrection) {
		//identical to above but for X values instead of Y
		
		double currentvalue = myOdo.getPose().getX();
		double rounded = 10*Math.rint(currentvalue/10);
		rounded = rounded + ((rounded/30)*(0.48));
		positionalcorrection = positionalcorrection + rounded;

		//update odometer X position
		Pose current = myOdo.getPose();	
		current.setLocation((float) positionalcorrection,current.getY());
		myOdo.setPose(current);		
	}

	public void calibrateLightSensors() {
		
		double result = 0;

		for (int i = 0; i < 5; i++) {
			result = result + leftCS.getNormalizedLightValue();
			result = result + rightCS.getNormalizedLightValue();
		}

		result = result/10;
		this.averageLight = result;

	}

	public static void enableCorrection(){
		enabled = true;
	}
	
	public static void disableCorrection() {
		enabled = false;
	}

}
