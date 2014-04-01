/*	DPM Final Project - Navigation Class
 *  ECSE211-DPM	Group 08
 *  Wei-Di Chang 260524917
 *  Aidan Petit
 */

import lejos.nxt.Sound;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.mapping.NavEventListener;
import lejos.robotics.mapping.NavigationModel.NavEvent;
import lejos.robotics.navigation.MoveController;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.pathfinding.Path;

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
	
	public Path PathMaker(Pose start, Waypoint end){
		Path generatedPath = new Path();
		generatedPath.add(new Waypoint(start.getX(),end.getY()));
		generatedPath.add(new Waypoint(end.getX(),end.getY()));
		return generatedPath;

	}



	

}
