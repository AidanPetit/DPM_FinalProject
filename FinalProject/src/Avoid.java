/*	DPM Final Project - Avoid Behavior Class
 *  ECSE211-DPM	Group 08
 *  Wei-Di Chang 260524917
 *  Aidan Petit
 */

import lejos.nxt.Sound;
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
	private static final float MAP_MIDPOINT = 90;
	public static boolean suppressed;
	private static Team08Robot myBot;

	//Constructor
	public Avoid(Team08Robot robot) {
		myBot=robot;
	}

	//takeControl defines (returns) the conditions for which the behavior should take over
	@Override
	public boolean takeControl() {
		return (myBot.isTooClose());
	}

	@Override
	public void action() {
		suppressed=false;
		Sound.buzz();
		while(!suppressed)
		{
			if(myBot.getOdo().getPose().getX()>=MAP_MIDPOINT)
			{
				myBot.getPilot().rotate(90);
				myBot.getPilot().travel(20);
//				myBot.getNav().goTo(myBot.getOdo().getPose().getX()-10, myBot.getOdo().getPose().getY()-20);
			}
			else
			{
				myBot.getPilot().rotate(-90);
				myBot.getPilot().travel(20);
//				myBot.getNav().goTo(myBot.getOdo().getPose().getX()+10, myBot.getOdo().getPose().getY()+20);
			}
		}


		myBot.setTooClose(false);


	}

	@Override
	public void suppress() {
		suppressed=true;
	}

}
