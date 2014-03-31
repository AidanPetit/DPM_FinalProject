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
//		int buttonChoice;
		LCD.drawString("Press a button", 0, 0);
		Button.waitForAnyPress();
		
		myBot = new Team08Robot();

		LCD.clearDisplay();
		myLCD = new LCDDisplay(myBot.getOdo());

		USLocalization USLocalizer = new USLocalization(myBot);
		USLocalizer.doLocalization();

		LightLocalization LightLocalizer = new LightLocalization(myBot);
		LightLocalizer.doLocalization();

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

