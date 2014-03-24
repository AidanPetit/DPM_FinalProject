import lejos.robotics.subsumption.Behavior;


public class Avoid implements Behavior{
	public static boolean suppressed;
	private static Team08Robot myBot;

	//Constructor
	public Avoid(Team08Robot robot) {
		myBot=robot;
	}
	
	//takeControl defines (returns) the conditions for which the behavior should take over
	@Override
	public boolean takeControl() {
		return (myBot.getFrontUS().getDistance()<15);
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
