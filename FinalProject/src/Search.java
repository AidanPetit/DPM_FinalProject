/* DPM Final Project - Search Class
 *  ECSE211-DPM	Group 08
 *  Wei-Di Chang 260524917
 *  Aidan Petit
 */
import lejos.nxt.ColorSensor;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.Color;
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

	private double SLOW_SPEED = 20;


	//Constructor
	public Search(Team08Robot robot) {
		myBot=robot;
		this.flagColor = myBot.getFlagColor();
		this.myCS = myBot.getFrontCS();
		this.myUS = myBot.getFrontUS();
		this.myPilot = myBot.getPilot();
	}

	@Override
	public boolean takeControl() {
		return myBot.isAtFlagZone();
	}

	@Override
	public void action() {
		suppressed=false;

		int startingAngle = 180;

		while(!suppressed){
			myPilot.rotate(startingAngle);

			int distance = getFilteredData();
			if(distance < 30){
				myPilot.setTravelSpeed(SLOW_SPEED);
				myPilot.travel(distance);

				if(identifyBlock() == flagColor){
					myBot.setFlagRecognized(true);
				}
				else{
					myPilot.travel(-distance);
				}
			}		
			startingAngle = startingAngle + 5;
		}


	}

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

			double ratioGR = intensityG/intensityR;
			double ratioGB = intensityG/intensityB;

			double ratioBR = intensityB/intensityR;
			double ratioBG = intensityB/intensityG;

			if(myCS.getColorID()!=7){

				if (intensityG > intensityB && intensityB > intensityR){
					return 1;
				}
				if(ratioRB > 3 && ratioRG > 3){
					return 2;
				}
				if(ratioRB > 3 && ratioRG <2){
					return 3;
				}
				if (intensityG > intensityR && intensityR > intensityB){
					return 4;
				}
				if(intensityB > (intensityG+intensityR)){
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
