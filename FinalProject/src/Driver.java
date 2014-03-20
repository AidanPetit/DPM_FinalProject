import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;

/*
 * This comment serves as a test if github is working
 * 
 */

public class Driver extends DifferentialPilot{
	private final double LEFTWHEELDIAMETER=4.32;
	private final double RIGHTWHEELDIAMETER=4.32;
	private final double TRACKWIDTH=12.1;
	

	
	public Driver(double leftWheelDiameter, double rightWheelDiameter,double trackWidth, NXTRegulatedMotor leftMotor,NXTRegulatedMotor rightMotor, boolean reverse) {
		super(leftWheelDiameter, rightWheelDiameter, trackWidth, leftMotor, rightMotor,reverse);
		leftWheelDiameter=this.WHEEL_SIZE_NXT2;
		rightWheelDiameter=this.WHEEL_SIZE_NXT2;
		trackWidth=this.TRACKWIDTH;
		reverse=false;


	}
}
