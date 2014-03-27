/* DPM Final Project - Search Class
 *  ECSE211-DPM	Group 08
 *  Wei-Di Chang 260524917
 *  Aidan Petit
 */
import lejos.nxt.Sound;
import lejos.robotics.subsumption.Behavior;

/**
 *
 * Search behavior class, first behavior in action, has lowest priority.
 *
 *
 * @author Wei-Di Chang
 * @version 1.0
 * @since 1.0
 */

public class Travel implements Behavior{
	public static boolean suppressed;
	private static Team08Robot myBot;

	//Constructor
	public Travel(Team08Robot robot) {
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
		myBot.getNav().goTo(60, 60);
		while(!suppressed)
		{
			Sound.beep();
			myBot.getFrontUS().ping();
			try {
				Thread.sleep(50); 
			}	
			catch (Exception e) { 

			}
			if(myBot.getFrontUS().getDistance()<30)
			{
				myBot.setTooClose(true);

			}
			else{
				myBot.setTooClose(false);
			}
		}

	}

	@Override
	public void suppress() {
		suppressed=true;
	}

}
