/*	
 * 	ECSE211 - DPM Winter 2013
 *  Final Project - Main Class
 *  Group 08
 *  Wei-Di Chang 260524917
 *  Aidan Petit
 */

import lejos.nxt.*;
<<<<<<< HEAD
=======
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.pathfinding.Path;
//import lejos.robotics.localization.OdometryPoseProvider;
//import lejos.robotics.navigation.Navigator;
//import lejos.robotics.navigation.Waypoint;
//import lejos.robotics.pathfinding.Path;
>>>>>>> FETCH_HEAD
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;

/**
 * Main class for the final project, execution starts from here.
 *
 * @author Wei-Di
 * @version 3.0
 * @since 1.0
 */

public class FinalProject {
	private static Team08Robot myBot;
	private static LCDDisplay myLCD;

	public static void main(String[] args)  throws Exception{
<<<<<<< HEAD
		
		LCD.drawString("Press a button", 0, 0);
		Button.waitForAnyPress();
		
		myBot = new Team08Robot();				//	Initialize the robot along with everything it needs

		LCD.clearDisplay();						//	Clear the display
		myLCD = new LCDDisplay(myBot.getOdo());	//	Display the odometer on the LCD display

		myBot.getPilot().setTravelSpeed(15);	//	Set the travel speed of the robot
		myBot.getPilot().setRotateSpeed(60);	//	Set the rotating speed of the robot

		// Create all the needed behaviors
		Behavior b1 = new Travel(myBot);
		Behavior b2 = new Avoid(myBot);
		Behavior b3 = new Capture(myBot);
		Behavior b4 = new Search(myBot);
		
		
		// Do the Ultrasonic sensor localization
		USLocalization USLocalizer = new USLocalization(myBot);
		USLocalizer.doLocalization(myBot.getStartingCorner());

		// Do the Light sensor localization
		LightLocalization LightLocalizer = new LightLocalization(myBot);
		LightLocalizer.doLocalization(myBot.getStartingCorner());
		
		// Create the array of behaviors that will be passed on to the Arbitrator, ordered by priority
		Behavior[] behaviorList = {b1 ,b2 ,b3, b4};
		
		//Initialize the Arbitrator and start it
		Arbitrator arb = new Arbitrator(behaviorList);	
		arb.start();
=======
		int buttonChoice;

		do {
			// clear the display
			LCD.clear();

			LCD.drawString("< Left | Right >", 0, 0);
			LCD.drawString("       |        ", 0, 1);
			LCD.drawString(" Test  | Full ", 0, 2);
			LCD.drawString("       | Run", 0, 3);
			LCD.drawString("       |", 0, 4);


			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);

		if (buttonChoice == Button.ID_LEFT) {
			myBot = new Team08Robot();
			myLCD = new LCDDisplay(myBot.getOdo());
			Navigation myNav = myBot.getNav();
			
			USLocalization USLocalizer = new USLocalization(myBot);
			USLocalizer.doLocalization(3);	
			
			LightLocalization LightLocalizer = new LightLocalization(myBot);
			LightLocalizer.doLocalization(3);

			myBot.getPilot().setRotateSpeed(60);
			myBot.getPilot().setTravelSpeed(10);
			
			Waypoint test = new Waypoint(0.5*30.48,0);
			Waypoint t2 = new Waypoint(0.5*30.48,1.5*30.48);
			Waypoint t3 = new Waypoint(1.5*30.48,1.5*30.48);
			Waypoint t4 = new Waypoint(1.5*30.48,0);
			Waypoint t5 = new Waypoint(0,0);

			Path myPath = new Path();
			
			myPath.add(test);
			myPath.add(t2);
			myPath.add(t3);
			myPath.add(t4);
			myPath.add(t5);

			myNav.followPath(myPath);
			
			OdometerCorrection myCorrect = new OdometerCorrection(myBot);
			myBot.setOdometerCorrection(myCorrect);
			
			OdometerCorrection.enableCorrection();
			myCorrect.start();
						
			while(!myNav.pathCompleted()) {
				//do nothing
			}
			
			Sound.twoBeeps();


			
//			myBot.getPilot().rotateRight();
//			ColorSensor cs = myBot.getRightCS();
//			int current = cs.getNormalizedLightValue();
//			
//			while(true){
//				current = cs.getNormalizedLightValue();
//				LCD.clear(5);
//				LCD.drawString("CS: "+current, 0,5);
//
//				
//				if (current < 350){
//					myBot.getPilot().stop();
//					double angle = myBot.getOdo().getPose().getHeading();
//					LCD.clear(4);
//					LCD.drawString("angle: "+angle, 0,4);
//					break;
//				}
//			}
//			
//			try { Thread.sleep(15000); } catch (InterruptedException e) {}  
			
//			myBot.getPilot().rotate(360);
//			myBot.getPilot().rotate(360);
//			myBot.getPilot().rotate(360);
//			myBot.getPilot().rotate(360);						

		}
		else{
			LCD.clearDisplay();


			myBot = new Team08Robot();
			myLCD = new LCDDisplay(myBot.getOdo());

			USLocalization USLocalizer = new USLocalization(myBot);
			USLocalizer.doLocalization(1);

			LightLocalization LightLocalizer = new LightLocalization(myBot);
			LightLocalizer.doLocalization(1);

			myBot.getPilot().setTravelSpeed(20);
			myBot.getPilot().setRotateSpeed(60);

			Behavior b1=new Travel(myBot);
			Behavior b2=new Avoid(myBot);
			Behavior b3=new Capture(myBot);
			Behavior b4=new Search(myBot);

			Behavior[] behaviorList = {b1,b2,b3,b4};
			Arbitrator arb = new Arbitrator(behaviorList);

			arb.start();

		}

>>>>>>> FETCH_HEAD
	}
}

