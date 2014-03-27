/*	DPM Final Project - Main Class
 *  ECSE211-DPM	Group 08
 *  Wei-Di Chang 260524917
 *  Aidan Petit
 */
import java.io.*;

import lejos.nxt.*;
//import lejos.robotics.localization.OdometryPoseProvider;
//import lejos.robotics.navigation.Navigator;
//import lejos.robotics.navigation.Waypoint;
//import lejos.robotics.pathfinding.Path;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import lejos.nxt.comm.*;
import lejos.nxt.remote.*;
import lejos.util.TextMenu;

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
		Button.waitForAnyPress();
		myBot = new Team08Robot();

		do {
			LCD.clear();

			LCD.drawString("< Left   | Right >", 0, 0);
			LCD.drawString("         |    ", 0, 1);
			LCD.drawString("     US  | Behavior ", 0, 2);
			LCD.drawString("Localize | Test", 0, 3);

			buttonChoice = Button.waitForAnyPress();

		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);

		if (buttonChoice == Button.ID_LEFT) {
			LCD.clearDisplay();
			myLCD = new LCDDisplay(myBot.getOdo());

			USLocalization USLocalizer = new USLocalization(myBot);
			USLocalizer.doLocalization();

		}
		else if(buttonChoice == Button.ID_RIGHT) {
			LCD.clearDisplay();
			myLCD = new LCDDisplay(myBot.getOdo());
			Behavior b1=new Travel(myBot);
			Behavior b2=new Avoid(myBot);
			Behavior b3=new Capture(myBot);
			Behavior b4=new Search(myBot);

			Behavior[] behaviorList = {b1,b2,b3,b4};
			Arbitrator arb = new Arbitrator(behaviorList);
			arb.start();


//			 if(myBot.getTopTouch().isPressed())
//			 {
//				 Sound.beep();
//			 }

			/*
			Waypoint w1 = new Waypoint(0,30);
			Waypoint w2 = new Waypoint(30,60);
			Waypoint w3 = new Waypoint(60,90);
			Waypoint w4 = new Waypoint(45,45);
			Waypoint w5 = new Waypoint(0,0);

			Path path = new Path();
			path.add(w1);
			path.add(w2);
			path.add(w3);
			path.add(w4);
			path.add(w5);

			myBot.getNav().followPath(path);
			 */

		}

	}
}
