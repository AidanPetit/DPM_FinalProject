<<<<<<< HEAD
/* DPM Final Project - UltraSonic Localization Class
 *  ECSE211-DPM	Group 08
 *  Wei-Di Chang 260524917
 *  Aidan Petit
 */
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;

/**
 *
 * Ultrasonic Localization class. Used to determine initial heading of robot
 * just knowing the staring corner of the robot
 * 
 * @param robot Team08Robot object used for this project
 *
 * @author Aidan Petit
 * @version 3.0
 * @since 1.0
 */

public class USLocalization {
	private Team08Robot myBot;

	private OdometryPoseProvider myOdo;
	private Navigator myNav;
	private Driver myPilot;

	private double rotateSpeed = 80;	//degrees per second
	private int threshold = 35;
	private int margin = 3;

	int corner;

	public USLocalization(Team08Robot robot) {
		this.myBot = robot;

		this.myOdo = myBot.getOdo();
		this.myNav = myBot.getNav();
		this.myPilot = myBot.getPilot();

	}

	public void doLocalization(int StartCorner) {
		/*
		 * Localize the robot using Rising Edge Detection
		 * Achieves 0, 90, 180 or -90 degrees depending on 
		 * the starting corner.
		 * 
		 * Corner Numbers:
		 * 1 - bottom left
		 * 2 - bottom right
		 * 3 - top right
		 * 4 - top left
		 * 
		 */
		this.corner = StartCorner;

		double angleA, angleB;
		int wall = myBot.getFilteredData();

		// rotate the robot until it sees no wall

		myPilot.setRotateSpeed(rotateSpeed);

		myPilot.rotateRight();

		while(wall!=60){
			wall = myBot.getFilteredData();
		}

		myPilot.stop();

		// rotate until the robot sees a wall, then latch the angle
		myPilot.rotateRight();
		wall = myBot.getFilteredData();

		double A1=0;
		double A2=0;

		while(true){
			//use a margin to get two angle to detect the wall
			if(wall>threshold-margin){
				A1 = myOdo.getPose().getHeading();
				wall = myBot.getFilteredData();
			}
			if(wall<threshold+margin){
				A2 = myOdo.getPose().getHeading();
				wall = myBot.getFilteredData();
				break;
			}

		}
		myPilot.stop();

		//use the average of the two angles 
		angleA = (A1+A2)/2;



		// switch direction and wait until it sees no wall
		myPilot.rotateLeft();
		wall = myBot.getFilteredData();

		while(wall!=60){
			wall = myBot.getFilteredData();
		}

		double B1=0;
		double B2=0;

		// keep rotating until the robot sees a wall, then latch the angle
		while(true){
			//same as above for the second wall
			if(wall>threshold-margin){
				B1 = myOdo.getPose().getHeading();
				wall = myBot.getFilteredData();
			}
			if(wall<threshold+margin){
				B2 = myOdo.getPose().getHeading();
				wall = myBot.getFilteredData();
				break;
			}

		}

		myPilot.stop();
		//use the average of the two angles 
		angleB = (B1+B2)/2;


		// angleA is clockwise from angleB
		// the average of the angles to the right of angleB is 45 degrees past 'north'
		
		//calculate the angle to add to the current heading
		double delta = 0;
		if(angleA<angleB){
			delta = 45-(angleA+angleB)/2;
		}
		else if(angleA>angleB){
			delta = 225-(angleA+angleB)/2;
		}

		Pose curr = myOdo.getPose();

		float X = curr.getX();
		float Y = curr.getY();
		float theta = (float) (curr.getHeading()+delta); //add the calculated angle

		//correct the heading if not corner 1
		if (corner == 2){		
			theta = theta + 90;
		}
		else if (corner == 3){
			theta = theta + 180;
		}
		else if (corner == 4){
			theta = theta - 90;
		}

		// update the odometers pose
		Pose newPose = new Pose(X,Y,theta);
		myOdo.setPose(newPose);

		//rotate to a perpendicular heading depending on which corner the bot is in
		if (corner == 1){
			myNav.rotateTo(0);
		}
		else if (corner == 2){		
			myNav.rotateTo(90);
		}
		else if (corner == 3){
			myNav.rotateTo(180);
		}
		else if (corner == 4){
			myNav.rotateTo(-90);
		}
		myBot.getFrontUS().reset();	//resetting the US trying to fix blocking bug
		return ;
		
	}
}
=======
/* DPM Final Project - UltraSonic Localization Class
 *  ECSE211-DPM	Group 08
 *  Wei-Di Chang 260524917
 *  Aidan Petit
 */
import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;

/**
 *
 * Ultrasonic Localization class, which is used to determine initial heading
 * just knowing the staring corner of the robot
 * Corner Numbers:
 * 1 - bottom left
 * 2 - bottom right
 * 3 - top right
 * 4 - top left
 *
 * @author Aidan Petit
 * @version 2.0
 * @since 1.0
 */

public class USLocalization {
	private Team08Robot myBot;

	private OdometryPoseProvider myOdo;
	private Navigator myNav;
	private Driver myPilot;
	private UltrasonicSensor localizationUS;

	private double rotateSpeed = 80;	//degrees per second
	private int threshold = 35;
	private int margin = 3;

	int corner;

	public USLocalization(Team08Robot robot) {
		this.myBot = robot;

		this.myOdo = myBot.getOdo();
		this.myNav = myBot.getNav();
		this.myPilot = myBot.getPilot();

		this.localizationUS = myBot.getFrontUS();


	}

	public void doLocalization(int StartCorner) {
		/*
		 * Localize the robot using Rising Edge Detection
		 * Achieves 0, 90, 180 or -90 degrees depending on 
		 * the starting corner
		 * 
		 */
		this.corner = StartCorner;

		double angleA, angleB;
		int wall = getFilteredData();

		// rotate the robot until it sees no wall

		myPilot.setRotateSpeed(rotateSpeed);

		myPilot.rotateRight();

		while(wall!=60){
			wall = getFilteredData();
		}

		myPilot.stop();
		Sound.buzz();

		// rotate until the robot sees a wall, then latch the angle
		myPilot.rotateRight();
		wall = getFilteredData();

		double A1=0;
		double A2=0;

		while(true){

			if(wall>threshold-margin){
				A1 = myOdo.getPose().getHeading();
				wall = getFilteredData();
			}
			if(wall<threshold+margin){
				A2 = myOdo.getPose().getHeading();
				wall = getFilteredData();
				break;
			}

		}
		myPilot.stop();

		angleA = (A1+A2)/2;

//		LCD.drawString("angleA: "+angleA, 0, 4);	//for debugging

		Sound.beep();							//for debugging

		// switch direction and wait until it sees no wall
		myPilot.rotateLeft();
		wall = getFilteredData();

		while(wall!=60){
			wall = getFilteredData();
		}
		Sound.buzz();							//for debugging

		double B1=0;
		double B2=0;

		// keep rotating until the robot sees a wall, then latch the angle
		while(true){

			if(wall>threshold-margin){
				B1 = myOdo.getPose().getHeading();
				wall = getFilteredData();
			}
			if(wall<threshold+margin){
				B2 = myOdo.getPose().getHeading();
				wall = getFilteredData();
				break;
			}

		}

		myPilot.stop();
		angleB = (B1+B2)/2;
//		LCD.drawString("angleB: "+angleB, 0, 5);	//for debugging

		Sound.beep();

		// angleA is clockwise from angleB
		// 1 -the average of the angles to the right of angleB is 45 degrees past 'north'
		// 2 - 135
		// 3 - 225 (-135)
		// 4 - 315 (-45)

		//double startingAng = -(angleB-360-angleA)/2 + 45 - angleA;


		double delta = 0;
		if(angleA<angleB){
			delta = 45-(angleA+angleB)/2;
		}
		else if(angleA>angleB){
			delta = 225-(angleA+angleB)/2;
		}

		Pose curr = myOdo.getPose();

		float X = curr.getX();
		float Y = curr.getY();
		float theta = (float) (curr.getHeading()+delta);

		if (corner == 2){		
			theta = theta + 90;
		}
		else if (corner == 3){
			theta = theta + 180;
		}
		else if (corner == 4){
			theta = theta - 90;
		}

//		LCD.drawString("new Theta: "+theta, 0, 3);	//for debugging


		Pose newPose = new Pose(X,Y,theta);

		// update the odometer pose
		myOdo.setPose(newPose);

		if (corner == 1){
			myNav.rotateTo(0);

		}
		else if (corner == 2){		
			myNav.rotateTo(90);
		}
		else if (corner == 3){
			myNav.rotateTo(180);
		}
		else if (corner == 4){
			myNav.rotateTo(-90);
		}
	}

	private int getFilteredData() {
		int distance;

		// do a ping
		localizationUS.ping();

		// wait for the ping to complete
		try { Thread.sleep(50); } catch (InterruptedException e) {}

		// there will be a delay here
		distance = localizationUS.getDistance();

		//this filters out large values
		if(distance>60){
			distance = 60;
		}

		return distance;
	}
}
>>>>>>> FETCH_HEAD
