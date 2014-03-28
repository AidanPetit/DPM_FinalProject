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
	private double ROTATE_SPEED = 25;

	private int startingAngle = 180;
	private int objectCount = 0;
	private int THRESHOLD = 40;

	private boolean Achecked = false;
	private boolean Bchecked = false;

	private double A1, A2, B1, B2;

	//Constructor
	public Search(Team08Robot robot) {
		myBot=robot;
		this.flagColor = myBot.getFlagColor();
		this.myCS = myBot.getFrontCS();
		this.myUS = myBot.getFrontUS();
		this.myPilot = myBot.getPilot();
		this.myNav = myBot.getNav();
	}

	@Override
	public boolean takeControl() {
		return (myBot.isAtFlagZone() && !myBot.getFlagRecognized());
	}

	@Override
	public void action() {
		suppressed=false;

		while(!suppressed){


			myPilot.setRotateSpeed(ROTATE_SPEED);
			myNav.rotateTo(startingAngle);

			myPilot.rotateRight();

			int distance = getFilteredData();
			
			boolean obj1 = false;

			while(objectCount==0){
				distance = getFilteredData();
				LCD.clear(5);
				LCD.drawString("US: "+ distance, 0, 5);

				if(distance < THRESHOLD){
					A1 = myBot.getOdo().getPose().getHeading();
					Sound.beep();

					obj1 = true;
				}
				if(distance > THRESHOLD && obj1){
					A2 = myBot.getOdo().getPose().getHeading();
					Sound.buzz();
					objectCount++;
				}
			}

			boolean obj2 = false;

			while(objectCount==1){
				distance = getFilteredData();
				LCD.clear(5);
				LCD.drawString("US: "+ distance, 0, 5);
				if(distance < THRESHOLD){
					B1 = myBot.getOdo().getPose().getHeading();
					Sound.beep();
					obj2 = true;
				}
				if(distance > THRESHOLD && obj2){
					B2 = myBot.getOdo().getPose().getHeading();
					Sound.buzz();
					objectCount++;
				}
			}
			
			if(objectCount==2){
				double A = (A1+A2)/2;
				double B = (B1+B2)/2;
				
				myNav.rotateTo(A);

				while(!Achecked){
					myPilot.setTravelSpeed(SLOW_SPEED);
					int dist = getFilteredData();
					myPilot.travel(dist);

					if(identifyBlock() == flagColor){
						Sound.twoBeeps();
						myBot.setFlagRecognized(true);

					}
					else{
						myPilot.travel(-dist);
					}
					Achecked=true;
				}
				if(Achecked && !Bchecked){
					
					myNav.rotateTo(B);
					int dist = getFilteredData();
					myPilot.setTravelSpeed(SLOW_SPEED);
					myPilot.travel(dist);

					if(identifyBlock() == flagColor){
						Sound.twoBeeps();
						myBot.setFlagRecognized(true);
					}
					else{
						myPilot.travel(-dist);
					}
					Bchecked=true;
				}
			}
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

			//			double ratioGR = intensityG/intensityR;
			//
			//			double ratioBR = intensityB/intensityR;
			//			double ratioBG = intensityB/intensityG;

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



	@Override
	public void suppress() {
		suppressed=true;
	}

	private int getFilteredData() {
		int distance;

		// do a ping
		myUS.ping();

		// wait for the ping to complete
		try { Thread.sleep(50); } catch (InterruptedException e) {}

		// there will be a delay here
		distance = myUS.getDistance();

		//this filters out large values
		if(distance>60){
			distance = 60;
		}

		return distance;
	}

}
