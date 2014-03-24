/* DPM Final Project - Search Class
*  ECSE211-DPM	Group 08
*  Wei-Di Chang 260524917
*  Aidan Petit
*/
import lejos.robotics.subsumption.Behavior;

/**
*
* Search behavior class, first behavior in action, has highest priority.
*
*
* @author Wei-Di Chang
* @version 1.0
* @since 1.0
*/

public class Search implements Behavior{
	public static boolean suppressed;
	private static Team08Robot myBot;

	//Constructor
	public Search(Team08Robot robot) {
		myBot=robot;
	}

	@Override
	public boolean takeControl() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void action() {
		suppressed=false;

	}

	@Override
	public void suppress() {
		suppressed=true;
	}

}
