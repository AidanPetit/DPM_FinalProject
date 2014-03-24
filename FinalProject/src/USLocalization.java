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
* Ultrasonic Localization class, which is used to determine initial 0 heading
*
* @author Aidan Petit
* @version 1.0
* @since 1.0
*/

public class USLocalization {
	private Team08Robot myBot;

	private OdometryPoseProvider myOdo;
	private Navigator myNav;
	private Driver myPilot;
	private UltrasonicSensor localizationUS;

	private double rotateSpeed = 100;	//degrees per second
	private int threshold = 35;
	private int margin = 3;

	public USLocalization(Team08Robot robot) {
		this.myBot = robot;

		this.myOdo = myBot.getOdo();
		this.myNav = myBot.getNav();
		this.myPilot = myBot.getPilot();

		this.localizationUS = myBot.getFrontUS();

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

		LCD.drawString("angleA: "+angleA, 0, 4);	//for debugging

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
		LCD.drawString("angleB: "+angleB, 0, 5);	//for debugging

		Sound.beep();

		// angleA is clockwise from angleB, so assume the average of the
		// angles to the right of angleB is 45 degrees past 'north'
		//

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

		LCD.drawString("new Theta: "+theta, 0, 3);	//for debugging


		Pose newPose = new Pose(X,Y,theta);

		// update the odometer pose
		myOdo.setPose(newPose);


		try{Thread.sleep(2000);}
		catch(InterruptedException e){}

		myNav.rotateTo(0);

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
