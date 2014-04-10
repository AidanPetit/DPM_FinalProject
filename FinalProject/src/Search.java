import lejos.nxt.ColorSensor;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.Color;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.subsumption.Behavior;

/**
 * Search behavior class, which takes care of finding the right block, has highest priority.
 *
 * @author Wei-Di Chang
 * @version 2.5
 * @since 1.0
 */

public class Search implements Behavior{

	public static boolean suppressed;
	private static Team08Robot myBot;
	private int flagColor;
	private ColorSensor myCS;
	private UltrasonicSensor myUS;
	private Driver myPilot;
	private Navigator myNav;

	private double SLOW_SPEED = 20;
	private final int ROTATE_SPEED = 80;

	public double DELTA_THRESHOLD = 13;	

	//Angle variables
	private float risingEdgeAngle = 0;
	private float currentAngle = 0;
	private float finalAngle;
	private int startAngle = 0;
	private float lastAngle;
	private final int SCAN_ANGLE = 90;

	//Distance variables
	private int currentDistance = 0;
	private int lastDistance = 0;
	private int deltaDistance = 0; 

	private int angleA, angleB,angle;
	private BlockPoint[] latchedAngles;



	//Four Corners
	private Waypoint[] corners;



	//Constructor
	public Search(Team08Robot robot) {
		myBot=robot;
		this.flagColor = myBot.getFlagColor();
		this.myCS = myBot.getFrontCS();
		this.myUS = myBot.getFrontUS();
		this.myPilot = myBot.getPilot();
		this.myNav = myBot.getNav();

		//Initialize four search corners
		corners = myBot.getObjectiveWaypoint();

		//Initialize angles array, used Waypoint object, with rising edge angle as X and falling edge, second angle as Y, and polled distance as heading
		//		latchedAngles=new Waypoint[6]; //Capacity of 6 objects
		latchedAngles=new BlockPoint[6];

	}

	//	takeControl defines (returns) the conditions for which the behavior should take over
	//	Search behavior becomes active when the flag is not yet recognized and the robot has arrived at the flag zone
	@Override
	public boolean takeControl() {
		return (myBot.isAtFlagZone() && !myBot.getFlagRecognized());
	}

	//	action() is executed when the behavior is active, hence when it takes control	
	@Override
	public void action() {
		suppressed=false;
		while (!suppressed)
		{
			int i=0;
			myPilot.setRotateSpeed(ROTATE_SPEED);
			myPilot.setTravelSpeed(SLOW_SPEED);
			while(i<4){
				myNav.goTo(corners[i].x, corners[i].y, startAngle);		//	Go to the "right" corner
				finalAngle = startAngle+120;							//	set angle at which to stop the scan
				if(finalAngle>360)
				{
					finalAngle-=360;
				}
				//	Initialize first measurements to avoid getting triggered on start
				currentDistance = myBot.getSearchFilteredData();		
				lastDistance = myBot.getSearchFilteredData();

				//	Initialize first angle values
				currentAngle = myBot.getOdo().getPose().getHeading();	
				lastAngle = myBot.getOdo().getPose().getHeading();
				int rotatedAngle = 0;
				int objectCount = 0;
				while(rotatedAngle < SCAN_ANGLE)	
				{
					//Rotate in increments of 10 until the set scan angle hasn't been reached
					myPilot.rotate(10, false);
					rotatedAngle+=10;

					//Get distance in the scanned direction
					currentDistance = myBot.getSearchFilteredData();

					//Compute difference ("derivative") between current and last distance measurements
					deltaDistance = currentDistance-lastDistance;

					//If distance variation is too high and rising edge :
					if(Math.abs(deltaDistance)>DELTA_THRESHOLD&&deltaDistance<0)	
					{
						risingEdgeAngle = myBot.getOdo().getPose().getHeading();	//Latch first angle
					}
					//If distance variation exceeds threshold and falling edge : 
					else if(Math.abs(deltaDistance)>DELTA_THRESHOLD&&deltaDistance>0)	
					{
						currentAngle = myBot.getOdo().getPose().getHeading();	//Latch second angle
						latchedAngles[objectCount]=new BlockPoint(risingEdgeAngle, currentAngle, lastDistance);	//After second angle has been latched, store all the data in an array of BlockPoint
						objectCount+=1;
					}
					lastDistance = currentDistance;
					lastAngle = currentDistance;
				}
				for(int j=0;j<6;j++)	//Go through all 6 stored objects and check each of them
				{					
					if(latchedAngles[j] != null)
					{	
						//Convert from -180|180 to 0|360
						angleA=(int)fixAngle(latchedAngles[j].getFirstAngle());
						angleB=(int)fixAngle(latchedAngles[j].getSecondAngle());


						//Use angleA as bigger angle, exchange the order if necessary
						if(angleB > angleA)
						{
							int temp = angleA;
							angleA = angleB;
							angleB = temp;
						}

						if((angleA-angleB) > 180)
						{
							angleA-=360;
						}

						// Compute the average of both angles
						angle = (angleA+angleB)/2;
						if(angle > 180)
						{
							angle-=360;
						}

						// Go forward to the object to try recognizing it
						myPilot.setTravelSpeed(SLOW_SPEED);
						myNav.rotateTo(fixAngle2(angle));
						myPilot.travel(latchedAngles[j].getDistance()-5);

						// Set a boolean if it is the flag, go back to original position otherwise
						if(identifyBlock() == flagColor){
							myBot.setFlagRecognized(true);
						}
						else{
							myPilot.travel(-latchedAngles[j].getDistance()+5);
						}

					}
				}

				// Increment i to go to the next corner
				i = i+1;

				//Increment starting angle so that the scan is always starting in the "right" direction
				startAngle+=90;	
			}
		}
	}

	// Method used to recognize each flag color, uses a combination of raw RGB values as well as ratios of these values to recognize each block
	// These values are hard coded but were obtained using data recorded by our testers
	private int identifyBlock() {
		/*
		 * identify between 5 flags
		 * 	
		 * 1 - light blue
		 * 2 - red
		 * 3 - yellow
		 * 4 - white
		 * 5 - dark blue
		 * 
		 * 0 - indicates error
		 */
		while(true){
			Color col = myCS.getColor();
			int intensityR = col.getRed();
			int intensityG = col.getGreen();
			int intensityB = col.getBlue();

			if(intensityR == 0){
				intensityR = 1;
			}
			if(intensityG == 0){
				intensityG = 1;
			}
			if(intensityB == 0){
				intensityB = 1;
			}

			double ratioRB = intensityR/intensityB;
			double ratioRG = intensityR/intensityG;
			double ratioGB = intensityG/intensityB;

			if(myCS.getColorID()!=7){
				if (intensityG > intensityB && intensityB > intensityR){
					return 1;
				}
				if(ratioRB > 3 && ratioRG > 3){
					return 2;
				}
				if(ratioRB > 2 && ratioRG <2 && ratioGB > 2){
					return 3;
				}
				if(intensityR > 150 && intensityG > 150 & intensityB > 200){
					return 4;
				}
				if(intensityB > intensityR && intensityB > intensityG){
					return 5;
				}
			}
			try { Thread.sleep(50); } catch (InterruptedException e) {}
		}
	}

	/*
	 * return an angle between 0 and 360
	 */
	public static double fixAngle(double angle) {
		angle = angle%360;
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

	//	Suppress() method suppresses the behavior, needs to be brief
	@Override
	public void suppress() {
		suppressed = true;
	}


}
