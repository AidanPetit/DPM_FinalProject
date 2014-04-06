/*	DPM Final Project - Main Class
 *  ECSE211-DPM	Group 08
 *  Wei-Di Chang 260524917
 *  Aidan Petit
 */
import java.io.*;

import bluetooth.BluetoothConnection;
import bluetooth.PlayerRole;
import bluetooth.StartCorner;
import bluetooth.Transmission;
import lejos.nxt.*;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.pathfinding.Path;
//import lejos.robotics.localization.OdometryPoseProvider;
//import lejos.robotics.navigation.Navigator;
//import lejos.robotics.navigation.Waypoint;
//import lejos.robotics.pathfinding.Path;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import lejos.nxt.comm.*;
import lejos.nxt.remote.*;

/**
 *
 * Main class for the final project, execution starts from here.
 *
 *
 * @author Wei-Di
 * @version 1.0
 * @since 1.0
 */

public class FinalProject {
	private static Team08Robot myBot;
	private static LCDDisplay myLCD;

	public static void main(String[] args)  throws Exception{
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
			USLocalizer.doLocalization(1);	
			
			LightLocalization LightLocalizer = new LightLocalization(myBot);
			LightLocalizer.doLocalization(1);


			Waypoint test = new Waypoint(60,60);

			Path myPath = myNav.PathMaker(myBot.getOdo().getPose(),test);

			myNav.followPath(myPath);

			OdometerCorrection myCorrect = new OdometerCorrection(myBot);
			myBot.setOdometerCorrection(myCorrect);
			
			OdometerCorrection.enableCorrection();
			myCorrect.start();

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

	}

}

