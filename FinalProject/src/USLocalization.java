import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;


public class USLocalization {
	public static double ROTATION_SPEED = 30;

	private OdometryPoseProvider myOdo;	
	private ColorSensor myLS;
	private Navigator myNav;
	private Driver myPilot;
	private UltrasonicSensor myUS;


	public USLocalization(OdometryPoseProvider odo, UltrasonicSensor us, Navigator nav,ColorSensor ls,Driver pilot) {
		this.myOdo = odo;
		this.myUS = us;
		this.myLS = ls;
		this.myNav = nav;
		this.myPilot = pilot;

		// switch off the ultrasonic sensor
		myUS.off();
	}

	public void doLocalization() {
		/*
		 * Localize the robot using Rising Edge Detection, assumes robot is in bottom left hand of board ie near (0.0)
		 * 
		 * 
		 * Not tested
		 */
		double angleA, angleB;
		int wall = getFilteredData();
		// rotate the robot until it sees no wall
		while(wall<50){
			myPilot.rotateRight();
			wall = getFilteredData();
		}
		myPilot.stop();

		// rotate until the robot sees a wall, then latch the angle
		myPilot.rotateRight();
		wall = getFilteredData();

		while(wall>35){
			wall = getFilteredData();
			LCD.drawString("US: "+wall, 0, 6);	//for debugging
		}
		myPilot.stop();		
		angleA = myOdo.getPose().getHeading();
		Sound.beep();							//for debugging

		// switch direction and wait until it sees no wall
		myPilot.rotateLeft();
		wall = getFilteredData();

		while(wall!=60){
			wall = getFilteredData();
		}
		Sound.buzz();							//for debugging

		// keep rotating until the robot sees a wall, then latch the angle
		while(wall>35){
			wall = getFilteredData();
		}
		myPilot.stop();
		angleB = myOdo.getPose().getHeading();

		Sound.beep();
		
		// angleA is clockwise from angleB, so assume the average of the
		// angles to the right of angleB is 45 degrees past 'north'
		// 
		
		double startingAng = -(angleB-360-angleA)/2 + 45 - angleA;
		double delta=0;
		if(angleA<angleB){
			delta = 45-(angleA+(angleB-360))/2;
		}
		else if(angleA>angleB){
			delta = 225-(angleA+(angleB-360))/2;
		}
		Pose curr = myOdo.getPose();
		float X = curr.getX();
		float Y = curr.getY();
		float theta = (float) (180-(curr.getHeading()-delta)); 
		
		Pose newPose = new Pose(X,Y,theta);

		// update the odometer pose
		myOdo.setPose(newPose);
		
		try{Thread.sleep(3000);}
		catch(InterruptedException e){}
		
		myNav.goTo(0, 0);
		myNav.rotateTo(180);
	}

	private int getFilteredData() {
		int distance;

		// do a ping
		myUS.ping();

		// wait for the ping to complete
		try { Thread.sleep(50); } catch (InterruptedException e) {}

		// there will be a delay here
		distance = myUS.getDistance();

		//this filters out large values
		if(distance>60){
			distance = 60;
		}

		return distance;
	}
}
