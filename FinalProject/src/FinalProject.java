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
		Team08Robot myBot = new Team08Robot();


		do {
			LCD.clear();

			LCD.drawString("< Left   | Right >", 0, 0);
			LCD.drawString("         |    ", 0, 1);
			LCD.drawString("     US  | Comm ", 0, 2);
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
			//			myLCD = new LCDDisplay(myBot.getOdo());

			//			Behavior b1=new Search(myBot);
			//			Behavior b2=new Avoid(myBot);
			//			Behavior[] bArray = {b1,b2};
			//			Arbitrator arb = new Arbitrator(bArray);
			//			arb.start();

			RemoteNXT nxt = null;	
			int power = 0;
			int mode = 1;
			int motor = 0;
			String motorString = "Motor:";
			String modeString = "Mode:";
			String powerString = "Power:";
			String batteryString = "Battery:";
			String lightString = "Light:";
			String tachoString = "Tacho:";

	        // Get the type of communications to be used
//			String[] connectionStrings = new String[]{"Bluetooth", "USB", "RS485"};
//	        TextMenu connectionMenu = new TextMenu(connectionStrings, 1, "Connection");
//	        NXTCommConnector[] connectors = {Bluetooth.getConnector(), USB.getConnector(), RS485.getConnector()};
	        

	        // Now connect
	        try {
	            LCD.clear();
	            LCD.drawString("Connecting...",0,0);
	        	nxt = new RemoteNXT("NXT", RS485.getConnector());
	        	LCD.clear();
	            LCD.drawString("Type: RS485"  , 0, 0);
	            LCD.drawString("Connected",0,1);
	            try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
				}
	        } catch (IOException ioe) {
	        	LCD.clear();
	            LCD.drawString("Conn Failed",0,0);
	            try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
				}
	            System.exit(1);
	        }

	        LCD.clear();
			RemoteMotor[] motors = {nxt.A, nxt.B, nxt.C};
			LightSensor light = new LightSensor(nxt.S2);
			while (true) {
				// Get data from the remote NXT and display it
				LCD.drawString(motorString,0,0);
				LCD.drawInt(motor, 3, 10, 0);
				LCD.drawString(powerString,0,1);
				LCD.drawInt(power, 3, 10, 1);
				LCD.drawString(modeString,0,2);
				LCD.drawInt(mode, 3, 10, 2);
				LCD.drawString(tachoString,0,3);
				LCD.drawInt(motors[motor].getTachoCount(), 6,  7, 3);
				LCD.drawString(batteryString,0,4);
				LCD.drawInt(nxt.Battery.getVoltageMilliVolt(), 6,  7, 4);
				LCD.drawString(lightString,0,5);
				LCD.drawInt(light.readValue(), 6,  7, 5);
				LCD.drawString(nxt.getBrickName(), 0, 6);
				LCD.drawString(nxt.getFirmwareVersion(), 0, 7);
				LCD.drawString(nxt.getProtocolVersion(), 4, 7);
				LCD.drawInt(nxt.getFlashMemory(), 6, 8, 7);

	            // Do we have a button press?
				int key = Button.readButtons();
				if (key != 0)
	            {
	                // New command, work out what to do.
	                if (key == 1) { // ENTER
	                    power += 20;
	                    if (power > 100) power = 0;
	                } else if (key == 2) { // LEFT
	                    mode++;
	                    if (mode > 4) mode = 1;
	                } else if (key == 4) { // RIGHT
	                    motor++;
	                    if (motor > 2) motor = 0;
	                } else if (key == 8) { // ESCAPE
	                    LCD.clear();
	                    LCD.drawString("Closing...", 0, 0);
	                    for(int i = 0; i < motors.length; i++)
	                        motors[i].flt();
	                    nxt.close();
	                    try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
//							e.printStackTrace();
						}
	                    System.exit(0);
	                }

	                LCD.clear();
	                LCD.drawString("Setting power",0,0);
	                motors[motor].setPower(power);
	                LCD.drawString("Moving motor",0,1);
	                if (mode == 1) motors[motor].forward();
	                else if (mode == 2) motors[motor].backward();
	                else if (mode == 3) motors[motor].flt();
	                else if (mode == 4) motors[motor].stop();
	                // Wait for the button to be released...
	                while (Button.readButtons() != 0)
	                    Thread.yield();
	                LCD.clear();
	            }
			}

			// myBot.getPilot().travel(30);

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
