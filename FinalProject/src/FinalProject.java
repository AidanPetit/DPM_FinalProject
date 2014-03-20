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
			
			
		}
		if(Button.waitForAnyPress() == Button.ID_ESCAPE) {
			System.exit(0);
		}
	}
}
