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

	private OdometryPoseProvider odo;	
	private ColorSensor myCS;
	//private Team08Robot robot;	??
	private ColorSensor myLS;
	private Navigator myNav;
	private Driver myPilot;
	private boolean onLine;
	private boolean lineLocked;

	private double sensorOffset = 12; // in cm, not accurate needs to be measured


	public LightLocalization(Team08Robot bot) {
		/*
		 * Constructor for LightLocalization
		 *
		 * Use nav or pilot to control movement of the bot
		 * and odometer to get position info
		 */

		this.odo = bot.getOdo();
		this.myCS = bot.getRightCS();
		this.myNav = bot.getNav();
		this.myPilot = bot.getPilot();
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
		myPilot.setRotateSpeed(75);
		// need to figure out how to find desired position
		// for now this is the coordinates used for team24 in lab 4

		myNav.rotateTo(180);
		myNav.goTo(7,8);
		myNav.rotateTo(90);

		double[] xAngles = new double[2];	//holds the two angles when the light sensor detects the X axis
		double[] yAngles = new double[2];	//holds the two angles for Y axis when sensor detects the line
		double xAxisIntersectAngle = 0; 	//holds angle when lightsensor hits negative X axis

		// start rotating and clock all 4 gridlines
		//rotate in place to clock angles
		myCS.setFloodlight(true);

		myPilot.rotateRight();

		while(!anglesClocked){ //keep rotating until all four angles have been clocked

			Pose currentPose = odo.getPose();
			LCD.drawString("count: "+ lockCount, 0, 6); //output for debugging, might need to disable LCDDisplay to use

			/*
<<<<<<< HEAD
			 * This is attempting to implement a discrete derivative filter
			 * to determine when the a line is detected
=======
			 * this uses a threshold. this is a bad method we should use a
			 * differential method to detect the change in light instead.
			 * Juan and I never implemented this
			 */

			detectLine();
			lineLocked = false;

			if (onLine && !lineLocked) {	
				double currentTheta = currentPose.getHeading();	
				if (lockCount==0){ // positive x axis

					if (currentReading<300) {
						double currentTheta = currentPose.getHeading();
						if (lockCount==0){ // negative x axis
							xAngles[0] = currentTheta;
							lockCount++;
							lineLocked = true;
						}
						else if (lockCount==1){ //negative y
							yAngles[1] = currentTheta;
							lockCount++;
							lineLocked = true;
						}
						else if (lockCount==2){ //negative x
							xAngles[1] = currentTheta;
							xAxisIntersectAngle = currentTheta; //save the negative x axis intersection angle for future reference
							lockCount++;
							lineLocked = true;
						}
						else if (lockCount==3){ //positive y
							yAngles[0] = currentTheta;
							lockCount++;
							lineLocked = true;
						}

						if (lockCount == 4){	// after 4 lines have been clocked the loop terminates and the robot stops moving
							myPilot.stop();
							anglesClocked = true;
							break;
						}
					}
					try{Thread.sleep(200);}
					catch(InterruptedException e){}
				}
			}
		

				/*
		//Some screen output for debugging
		LCD.clear(4);
		LCD.clear(5);
		LCD.clear(6);
		LCD.clear(7);

		LCD.drawString("x1: " + xAngles[0], 0, 4);
		LCD.drawString("x2: " + xAngles[1], 0, 5);
		LCD.drawString("y1: " + yAngles[0], 0, 6);
		LCD.drawString("y2: " + yAngles[1], 0, 7);

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

				float newTheta = (float) ((odo.getPose().getHeading()+newHeadingY));
				float newX = (float) X;
				float newY = (float) Y;

				Pose pos = new Pose(newX,newY,newTheta);



				LCD.clear(4);
				LCD.clear(5);
				LCD.clear(6);

				LCD.drawString("new X: " + newX, 0, 4);
				LCD.drawString("new Y: " + newY, 0, 5);
				LCD.drawString("new T: " + newTheta, 0, 6);



				//set the odometer to reflect the new X, Y and theta values
				odo.setPose(pos);


				// when done travel to (0,0) and turn to 0 degrees
				myNav.goTo(0,0);


			}
	}

			public void detectLine(){
				int dPrev = 0;
				int dNow;

				int xLast, xNext;

				xLast = myCS.getRawLightValue();

				while(true){
					xNext = myCS.getRawLightValue();
					//LCD.drawString("CS: " + xNext, 0, 3);


					if (xNext < 500){
						onLine = true;
						Sound.beep();
						break;
					}
					else{
						onLine = false;
					}
					/*
			dNow = xLast - xNext;	

			if(dPrev > 0 && dNow < 0){
				onLine = true;
				Sound.beep();
				break;
			}
			else{
				onLine = false;		
			}
			xLast = xNext;				
			dPrev = dNow;	
					 */
				}
			}
		}
