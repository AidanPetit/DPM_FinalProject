/*	DPM Final Project - Navigation Class
 *  ECSE211-DPM	Group 08
 *  Wei-Di Chang 260524917
 *  Aidan Petit
 */
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.MoveController;
import lejos.robotics.navigation.Navigator;

/**
 *
 *
 * Navigation class which extends Navigator, which takes care of following a path made of waypoints
 *
 * @param pilot MoveController of the two wheeled robot, the driver
 * @param poseProvider
 *
 * @author Wei-Di Chang
 * @version 1.0
 * @since 1.0
 */

public class Navigation extends Navigator {

	public Navigation(MoveController pilot, PoseProvider poseProvider) {
		super(pilot, poseProvider);
		// TODO Auto-generated constructor stub
	}


}
