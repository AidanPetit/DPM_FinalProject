import lejos.robotics.subsumption.Behavior;


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
		return (myBot.getFrontUS().getDistance()<15);
	}

	@Override
	public void action() {
		suppressed=false;
		if(myBot.getOdo().getPose().getX()>MAP_MIDPOINT)
		{
//			myBot.getPilot().
		}
		else
		{
			
		}
		
	}

	@Override
	public void suppress() {
		suppressed=true;		
	}

}
