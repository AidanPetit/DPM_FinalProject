import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;

public class OdometerCorrection {

	private Team08Robot myBot;

	private OdometryPoseProvider myOdo;
	private Navigator myNav;
	private Driver myPilot;
	private UltrasonicSensor localizationUS;

	private Object lock;

	int countTime;

	private static final long CORRECTION_PERIOD = 10;

	//these store the X or Y coordinates of the odometer's position
	//when a black line is detected
	public double leftDetected;
	public double rightDetected;
	
	double leftlight;
	double rightlight;

	//needs to be measured
	public static final double COLORSENSORS_DISTANCE = 9;
	
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
		
		enabled = true;
		averageLight = 0;
		leftDetected = 0;
		rightDetected = 0;
		leftlight = 0;
		rightlight = 0;
		countTime = 0;
		
		lock = new Object();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}
			
	}

	public void run() {
		//sensors calibrated each new run
		calibrateLightSensors();
		
		long correctionStart, correctionEnd;
		
		while(true) {
			correctionStart = System.currentTimeMillis();

			//int indicating direction
			//0 - north, 1 - east, 2 - south, 3 - west
			
			int heading = getHeadingNumber();
			
			countTime++;
			if (countTime == 100) {
				countTime = 0;
				leftDetected = 0;
				rightDetected = 0;
				//Sound.beep();
			}
			if (!(myBot.isRotating())) {
				
				leftlight = leftCS.getNormalizedLightValue();
				rightlight = rightCS.getNormalizedLightValue();
			
				if (leftlight < averageLight - SENSOR_THRESHOLD) {
					if (heading == 0 || heading == 2)
						leftDetected = myOdo.getPose().getY();
					else
						leftDetected = myOdo.getPose().getX();
				}
			
				if (rightlight < averageLight - SENSOR_THRESHOLD) {
					if (heading == 0 || heading == 2)
						rightDetected = myOdo.getPose().getY();
					else
						rightDetected = myOdo.getPose().getX();
				}

				if (leftDetected != 0 && rightDetected != 0) {
					correctionCalculator(heading);
				}
			}

			correctionEnd = System.currentTimeMillis();

			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD - (correctionEnd-correctionStart));
				} catch (InterruptedException e) {Sound.beepSequenceUp();}
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
		
		if(angle>(0.8*90) && angle<(1.2*90)) {
			return 0;
		}
		else if((angle > 165 && angle <= 180) || (angle < -165 && angle >= -179)) {
			return 1;
		}
		else if(angle<(0.8*-90) && angle>(1.2*90)) {
			return 2;
		}
		else if(angle < 15 && angle > -15) {
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
			double angle_degree;
			double positionalcorrection;
			
			/*
			 * When a line is detected the value reported by the colorsensor
			 * is stored in rightDetected or leftDetected. These values are saved until the next iteration 
			 * 
			 * Correction is applied only when both of these are set
			 * 
			 * Depending on the heading, correct the heading 
			 * and EITHER x or y position.
			 * 
			 */
				
				angle_rad = Math.atan((rightDetected - leftDetected)/COLORSENSORS_DISTANCE); //angular correction
				angle_degree = angle_rad*(180/Math.PI);
				positionalcorrection = Math.abs(rightDetected - leftDetected)/2;
				
				switch(heading) {				
					case 0: correctTheta(angle_rad);
							correctPositionY(positionalcorrection);
						break;
					case 1: correctTheta(angle_rad + ((Math.PI)/2));
							correctPositionX(positionalcorrection);
						break;
					case 2: correctTheta(Math.PI - angle_rad);
							correctPositionY(positionalcorrection);
						break;
					case 3: correctTheta(((3*Math.PI)/2) - angle_rad);
							correctPositionX(positionalcorrection);
						break;
						
				}
				calibrateLightSensors();
		}


	}
			
	public void correctTheta(double angle) {
		Pose current = myOdo.getPose();
		
		current.setHeading((float)Math.toDegrees(angle));
		
		myOdo.setPose(current);
		
		synchronized (lock) {
		leftDetected = 0;
		rightDetected = 0;
		leftlight = 0;
		rightlight = 0;
		}
				
	}


	public void correctPositionY(double positionalcorrection) {	
		//the next 4 lines take the current odo position, round it to the nearest 10, 
		//and add the positionalcorrection found by taking the difference between two sensors' position
		double currentvalue = myOdo.getPose().getY();
		double rounded = 10*Math.rint(currentvalue/10);
		rounded = rounded + ((rounded/30)*(0.48));
		positionalcorrection = positionalcorrection + rounded;
		
		//update odometer Y position
		Pose current = myOdo.getPose();	
		current.setLocation(current.getX(),(float) positionalcorrection);
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


}