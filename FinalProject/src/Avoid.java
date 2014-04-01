/*	DPM Final Project - Avoid Behavior Class
 *  ECSE211-DPM	Group 08
 *  Wei-Di Chang 260524917
 *  Aidan Petit
 */

import lejos.nxt.Sound;
import lejos.robotics.objectdetection.Feature;
import lejos.robotics.objectdetection.FeatureDetector;
import lejos.robotics.objectdetection.FeatureListener;
import lejos.robotics.subsumption.Behavior;

/**
 *
 * Avoidance behavior class, which kicks in when an obstacle is detected less than 30cm ahead, third highest priority class.
 *
 *
 * @author Wei-Di Chang
 * @version 1.0
 * @since 1.0
 */

public class Avoid implements Behavior{
//	private static final float MAP_MIDPOINT = 90;
	public static boolean suppressed;
	private static Team08Robot myBot;

	//Constructor
	public Avoid(Team08Robot robot) {
		myBot=robot;		
	}

	//takeControl defines (returns) the conditions for which the behavior should take over
	@Override
	public boolean takeControl() {
		return (myBot.getFilteredData()<35);
	}

	@Override
	public void action() {
		suppressed=false;

		myBot.getNav().clearPath();
		myBot.getPilot().rotate(90);
		if(myBot.getFilteredData()>35)
		{
			myBot.getPilot().travel(25);
			myBot.getPilot().rotate(-90);
		}
		else{
			myBot.getPilot().rotate(180);	
			if(myBot.getFilteredData()>35)
			{
				myBot.getPilot().travel(25);
				myBot.getPilot().rotate(90);
			}
			else {
				myBot.getPilot().rotate(-90);
				myBot.getPilot().travel(25);
			}
		}
		
		

	}

	@Override
	public void suppress() {
		suppressed=true;
	}



}
