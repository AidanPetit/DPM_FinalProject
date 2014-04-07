/* DPM Final Project - Search Class
 *  ECSE211-DPM	Group 08
 *  Wei-Di Chang 260524917
 *  Aidan Petit
 */
import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.Color;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.subsumption.Behavior;

/**
 *
 * Search behavior class, which takes care of finding the right block, has highest priority.
 *
 *
 * @author Wei-Di Chang
 * @version 1.0
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

	private int startAngle = 0;
	private int objectCount = 0;
	public double DELTA_THRESHOLD = 13;	
	//Angle variables
	private float risingEdgeAngle=0;
	private float currentAngle = 0;
	private float finalAngle;
	private float lastAngle;
	private final int SCAN_ANGLE=90;
	//Distance variables
	private int currentDistance = 0;
	private int lastDistance = 0;
	private int deltaDistance = 0; 

	private int angleA, angleB,angle;
//	private Waypoint[] latchedAngles;
	private BlockPoint[] latchedAngles;



	//Four Corners
	private Waypoint[] corners;

	

	//Constructor
	public Search(Team08Robot robot) {
		myBot=robot;
		this.flagColor = myBot.getRedFlag();
		this.myCS = myBot.getFrontCS();
		this.myUS = myBot.getFrontUS();
		this.myPilot = myBot.getPilot();
		this.myNav = myBot.getNav();

		//Initialize four search corners
		corners=myBot.getObjectiveWaypoint();

		//Initialize angles array, used Waypoint object, with rising edge angle as X and falling edge, second angle as Y, and polled distance as heading
//		latchedAngles=new Waypoint[6]; //Capacity of 6 objects
		latchedAngles=new BlockPoint[6];

	}

	@Override
	public boolean takeControl() {
		return (myBot.isAtFlagZone() && !myBot.getFlagRecognized());
	}

	@Override
	public void action() {
		suppressed=false;
		int i=0;
		myPilot.setRotateSpeed(ROTATE_SPEED);
		myPilot.setTravelSpeed(SLOW_SPEED);
		while(i<4){
							 				
				myNav.goTo(corners[i].x, corners[i].y, startAngle);	//Go to the "right" corner		//<----To be uncommented
				finalAngle = startAngle+120;				//set angle at which to stop the scan
				if(finalAngle>360)
				{
					finalAngle-=360;
				}
				currentDistance = myBot.getSearchFilteredData();	//initialize first measurements to avoid getting triggered on start
				lastDistance = myBot.getSearchFilteredData();
				currentAngle = myBot.getOdo().getPose().getHeading();	//initialize first angle values;
				lastAngle = myBot.getOdo().getPose().getHeading();
				int rotatedAngle=0;
				int objectCount = 0;
				while(rotatedAngle < SCAN_ANGLE)	//Rotate
				{

					myPilot.rotate(10, false);
					rotatedAngle+=10;

					currentDistance = myBot.getSearchFilteredData();
					deltaDistance = currentDistance-lastDistance;	//Compute difference ("derivative") between current and last distance measurements
					if(Math.abs(deltaDistance)>DELTA_THRESHOLD&&deltaDistance<0)	//If distance variation is too high and rising edge :
					{
						Sound.beep();
						risingEdgeAngle=myBot.getOdo().getPose().getHeading();	//Latch first angle
					}
					else if(Math.abs(deltaDistance)>DELTA_THRESHOLD&&deltaDistance>0)	//If distance variation exceeds threshold and falling edge : 
					{
						Sound.buzz();
						currentAngle = myBot.getOdo().getPose().getHeading();	//Latch second angle
						latchedAngles[objectCount]=new BlockPoint(risingEdgeAngle, currentAngle, lastDistance);
						objectCount+=1;
					}
					lastDistance = currentDistance;
					lastAngle = currentDistance;
				}
				for(int j=0;j<6;j++)	//Go through 6 objects
				{					
					if(latchedAngles[j]!=null)
					{	
						//Convert from -180|180 to 0|360
						angleA=(int)fixAngle(latchedAngles[j].getFirstAngle());
						angleB=(int)fixAngle(latchedAngles[j].getSecondAngle());


						//Use angleA as bigger angle
						if(angleB>angleA)
						{
							int temp=angleA;
							angleA=angleB;
							angleB=temp;
						}

						if((angleA-angleB)>180)
						{
							angleA-=360;
						}
						angle = (angleA+angleB)/2;
						if(angle>180)
						{
							angle-=360;
						}

						myPilot.setTravelSpeed(SLOW_SPEED);
						myNav.rotateTo(fixAngle2(angle));
						myPilot.travel(latchedAngles[j].getDistance()-5);

						if(identifyBlock() == flagColor){
							Sound.buzz();
							myBot.setFlagRecognized(true);
						}
						else{
							myPilot.travel(-latchedAngles[j].getDistance()+5);
						}

					}
				}

				i=i+1;
				startAngle+=90;	//Increment starting angle so that scan is always starting in right direction
			}



		}

	



	//			if(distance < THRESHOLD){
	//				myPilot.setTravelSpeed(SLOW_SPEED);
	//				myPilot.travel(distance);
	//
	//				if(identifyBlock() == flagColor){
	//					Sound.buzz();
	//					myBot.setFlagRecognized(true);
	//
	//				}
	//				else{
	//					myPilot.travel(-distance);
	//				}
	//			}		
	//			startingAngle = startingAngle - 10;
	//		}



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

	@Override
	public void suppress() {
		suppressed=true;
	}


}
