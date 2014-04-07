/* DPM Final Project - Search Class
 *  ECSE211-DPM	Group 08
 *  Wei-Di Chang 260524917
 *  Aidan Petit
 */
import lejos.nxt.Sound;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.pathfinding.Path;
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
	private final int BLOCK_AHEAD_DISTANCE = 35;
	private Path myPath;
	
	//Constructor
	public Travel(Team08Robot robot) {
		myBot = robot;
		myPath = myBot.getNav().PathMaker(myBot.getOdo().getPose(), myBot.getObjectiveWaypoint()[0]);
	}

	@Override
	public boolean takeControl() {
		return true;
	}

	@Override
	public void action() {
		suppressed=false;		
		while(!suppressed)
		{
			if(myBot.getFilteredData()>BLOCK_AHEAD_DISTANCE)
			{
				myBot.getNav().followPath(myPath);
			}
			else {
				myBot.setObstacle(true);
			}
		}
		

	}
	


	@Override
	public void suppress() {
		suppressed=true;
	}

}
