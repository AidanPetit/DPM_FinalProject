import lejos.robotics.subsumption.Behavior;

/**
 *
 * Search behavior class, first behavior in action, has lowest priority.
 * This means that the robot will always fall back to 'Travel' unless another behavior 
 * has been allowed to take control
 * 
 * @param robot Team08Robot object used for this project
 *
 * @author Wei-Di Chang, Aidan Petit
 * @version 3.0
 * @since 1.0
 */

public class Travel implements Behavior{
	public static boolean suppressed;
	private static Team08Robot myBot;
	private final int BLOCK_AHEAD_DISTANCE = 31;	// forward distance to detect obstacle

	//Constructor
	public Travel(Team08Robot robot) {
		myBot = robot;	
		myBot.setMyPath(myBot.getNav().PathMaker(myBot.getOdo().getPose(), myBot.getObjectiveWaypoint()[0]));
	}

	@Override
	public boolean takeControl() {
		/*
		 *  This method will always take control unless another behavior should to occur
		 */
		return (true);			}

	@Override
	public void action() {
		/*
		 * This method is what runs when this behavior has control and is active.
		 */
		suppressed=false;	
		while(!suppressed) {
			// get new values from both Ultrasonic sensors
			if(myBot.getLeftFilteredData()>BLOCK_AHEAD_DISTANCE||myBot.getFilteredData()>BLOCK_AHEAD_DISTANCE)
			{	// no object in front of the robot
				myBot.setObstacle(false);						// set Obstacle to false
				myBot.getNav().followPath(myBot.getMyPath());	// follow the Path to the next node
				myBot.getNav().waitForStop();					// wait for travel to be complete
			}
			else {	//there is an obstacle in front of the robot
				myBot.setObstacle(true);
			}
		}
	}



	@Override
	public void suppress() {
	/*
	 * This method is called by the Arbitrator when any other behavior wants 
	 * to take control. It must end the action() method 
	 */
		suppressed=true;
	}

}
