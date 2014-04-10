import lejos.nxt.NXTRegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;


/**
 * Driver class which extends DifferentialPilot, only class which has access to the two motors
 *
 * @param leftWheelDiameter Diameter of the left wheel of the two wheeled robot
 * @param rightWheelDiameter Diameter of the right wheel of the two wheeled robot
 * @param trackWidth Length of the wheelbase of the two wheeled robot
 * @param leftMotor Left Motor of the two wheeled robot
 * @param rightMotor Right Motor of the two wheeled robot
 * @param reverse Boolean set if the robot is inverted, ie if the robot goes backwards to go forward
 *
 * @author Wei-Di
 * @version 1.0
 * @since 1.0
 */

public class Driver extends DifferentialPilot{
	private final double TRACKWIDTH=16.5;


	//	Superconstructor
	public Driver(double leftWheelDiameter, double rightWheelDiameter,double trackWidth, NXTRegulatedMotor leftMotor,NXTRegulatedMotor rightMotor, boolean reverse) {
		super(leftWheelDiameter, rightWheelDiameter, trackWidth, leftMotor, rightMotor,reverse);
		leftWheelDiameter = WHEEL_SIZE_NXT2;
		rightWheelDiameter =  WHEEL_SIZE_NXT2;
		trackWidth = this.TRACKWIDTH;
		reverse = false;


	}
}
