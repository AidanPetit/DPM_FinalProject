/*	DPM Final Project - Main Class
 *  ECSE211-DPM	Group 22
 * 	Wei-Di Chang 260524917
 *  Sok Heng Lim 260581435
 */
import lejos.nxt.*;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.Navigator;

public class FinalProject {
	private static double leftWheelDiameter=4.32;
	private static double rightWheelDiameter=4.32;
	private static double width=16;
	private static NXTRegulatedMotor leftMotor=Motor.A;
	private static NXTRegulatedMotor rightMotor=Motor.B;
	


	public static void main(String[] args) {
		Button.waitForAnyPress();
		Driver pilot=new Driver(leftWheelDiameter, rightWheelDiameter, width, leftMotor, rightMotor, false);
		OdometryPoseProvider odometer=new OdometryPoseProvider(pilot);
		Navigator nav=new Navigator(pilot, odometer);
		LCDDisplay display=new LCDDisplay(odometer);
		
		nav.rotateTo(90);

		Sound.beep();
		nav.goTo(30, 0);
		nav.goTo(30,30);
		nav.goTo(0,30 );
		nav.goTo(0,0);
		
		Button.waitForAnyPress();

	}
}
