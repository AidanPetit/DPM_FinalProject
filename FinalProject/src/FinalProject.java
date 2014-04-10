/*	
 * 	ECSE211 - DPM Winter 2013
 *  Final Project - Main Class
 *  Group 08
 *  Wei-Di Chang 260524917
 *  Aidan Petit
 */

import lejos.nxt.*;
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
	}
}

