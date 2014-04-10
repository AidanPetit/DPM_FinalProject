import lejos.robotics.subsumption.Behavior;

/**
 * Avoidance behavior class, which kicks in when an obstacle is detected less than 30cm ahead, third highest priority class.
 *
 * @author Wei-Di Chang
 * @version 3.0
 * @since 1.0
 */

public class Avoid implements Behavior{
	public static boolean suppressed;
	private static Team08Robot myBot;
	private final int BLOCK_AHEAD_DISTANCE = 31;


	//Constructor
	public Avoid(Team08Robot robot) {
		myBot = robot;		
	}

	//	takeControl defines (returns) the conditions for which the behavior should take over
	@Override
	public boolean takeControl() {
		return (myBot.getObstacle() == true);
	}


	//	action() is executed when the behavior is active, hence when it takes control
	@Override
	public void action() {
		suppressed = false;
		double startingAngle = myBot.getOdo().getPose().getHeading();
		myBot.getPilot().rotate(-90,false);						// Check left side
		if(myBot.getLeftFilteredData() > BLOCK_AHEAD_DISTANCE)	// If there is enough clearance, create a new path that way
		{														// And exit the behavior
			myBot.setMyPath(myBot.getNav().PathMaker(myBot.getOdo().getPose(), myBot.getObjectiveWaypoint()[0]));
			myBot.setObstacle(false);	
		}
		else 
		{
			myBot.getPilot().rotate(180, false);				// Check right side
			if(myBot.getLeftFilteredData()>BLOCK_AHEAD_DISTANCE)// If there is enough clearance, create a new path that way
			{													// And exit the behavior
				myBot.setMyPath(myBot.getNav().PathMaker(myBot.getOdo().getPose(), myBot.getObjectiveWaypoint()[0]));
				myBot.setObstacle(false);
			}
			else {
				myBot.getNav().rotateTo(startingAngle);			//Otherwise, rotate back to the starting angle and reverse
				myBot.getPilot().travel(-30.48);
				myBot.getPilot().rotate(90);					// Turn to a direction in the other axis, and generate a new path
				myBot.setMyPath(myBot.getNav().PathMaker(myBot.getOdo().getPose(), myBot.getObjectiveWaypoint()[0]));
				myBot.setObstacle(false);						// Exit the behavior
			}
		}
		myBot.setObstacle(false);	//Obstacle should be avoided, exit the Avoid behavior	
	}

	//	Suppress method suppresses the behavior, needs to be brief
	@Override
	public void suppress() {
		suppressed = true;
	}
	
}
