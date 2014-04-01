/*	DPM Final Project - Avoid Behavior Class
 *  ECSE211-DPM	Group 08
 *  Wei-Di Chang 260524917
 *  Aidan Petit
 */

import lejos.robotics.subsumption.Behavior;

/**
 *
 * Capture behavior class, which kicks in when the flag has been found, second highest priority class.
 *
 *
 * @author Wei-Di Chang
 * @version 1.0
 * @since 1.0
 */

public class Capture implements Behavior{
	public static boolean suppressed;
	private static Team08Robot myBot;

	//Constructor
	public Capture(Team08Robot robot) {
		myBot=robot;
	}

	//takeControl defines (returns) the conditions for which the behavior should take over
	@Override
	public boolean takeControl() {
		return myBot.getFlagRecognized();
	}

	@Override
	public void action() {
		suppressed=false;
		myBot.getLeftTrack().setPower(+100);
		myBot.getRightTrack().setPower(+100);
		myBot.getLeftTrack().setAcceleration(999999);	//Making sure acceleration is at maximum
		myBot.getRightTrack().setAcceleration(999999);

		while(!suppressed&&!myBot.getFlagCaptured())
		{
			myBot.getLeftTrack().forward();
			myBot.getRightTrack().forward();
		}
		



	}

	@Override
	public void suppress() {
		suppressed=true;
	}

}
