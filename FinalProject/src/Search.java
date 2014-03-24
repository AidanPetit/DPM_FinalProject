import lejos.robotics.subsumption.Behavior;


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
