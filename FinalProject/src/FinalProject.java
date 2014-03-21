/*	DPM Final Project - Main Class
 *  ECSE211-DPM	Group 22
 * 	Wei-Di Chang 260524917
 *  Sok Heng Lim 260581435
 */
import lejos.nxt.*;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.pathfinding.Path;

public class FinalProject {
	private static Team08Robot myBot;
	private static LCDDisplay myLCD;

	public static void main(String[] args) {
		int buttonChoice;
		Team08Robot myBot = new Team08Robot();

		do {
			LCD.clear();

			LCD.drawString("< Left   | Right >", 0, 0);
			LCD.drawString("         |    ", 0, 1);
			LCD.drawString("   US    | drive ", 0, 2);
			LCD.drawString("Localize | ", 0, 3);

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
			
			myBot.getPilot().travel(30);
			
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
		if(Button.waitForAnyPress() == Button.ID_ESCAPE) {
			System.exit(0);
		}
	}
}
