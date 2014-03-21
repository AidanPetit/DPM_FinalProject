import lejos.util.Timer;
import lejos.util.TimerListener;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.MoveProvider;

/**
 * 
 * 
 * PoseProvider (odometry) class which extends OdometryPoseProvider. 
 * This odometer follows math conventions.
 * Hence the 0 angle is oriented east, with positive rotations anticlockwise, and negative rotation clockwise
 * 
 *
 * @param mp MoveProvider of the two wheeled robot, the driver
 * 
 * 
 * @author Wei-Di Chang
 * @version 1.0
 * @since 1.0
 */
 
public class PoseProvider extends OdometryPoseProvider {

	public PoseProvider(MoveProvider mp) {
		super(mp);
		// TODO Auto-generated constructor stub
	}
	
}
