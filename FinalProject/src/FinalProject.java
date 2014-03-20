/*	DPM Final Project - Main Class
 *  ECSE211-DPM	Group 22
 * 	Wei-Di Chang 260524917
 *  Sok Heng Lim 260581435
 */
import lejos.nxt.*;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.Navigator;

public class FinalProject {
	private static Team08Robot myBot;

	public static void main(String[] args) {
		int buttonChoice;
		Team08Robot myBot = new Team08Robot();
		
		Navigator nav=myBot.getNav();

		do {
			LCD.clear();

			LCD.drawString("< Left   | Right >", 0, 0);
			LCD.drawString("         |    ", 0, 1);
			LCD.drawString("   US    | drive ", 0, 2);
			LCD.drawString("Localize | square", 0, 3);

			buttonChoice = Button.waitForAnyPress();

		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);

		if (buttonChoice == Button.ID_LEFT) {
			LCD.clearDisplay();
			
			USLocalization USLocalizer = new USLocalization(myBot);
			USLocalizer.doLocalization();

		}
		else if(buttonChoice == Button.ID_RIGHT) {
			LCD.clearDisplay();

			nav.rotateTo(90);

			Sound.beep();
			nav.goTo(30, 0);
			nav.goTo(30,30);
			nav.goTo(0,30 );
			nav.goTo(0,0);

		}
		else if(Button.waitForAnyPress() == Button.ID_ESCAPE) {
			System.exit(0);
		}
	}
}
