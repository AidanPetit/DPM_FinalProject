<<<<<<< HEAD
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.MoveController;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.pathfinding.Path;

/**
 * Navigation class which extends Navigator, which takes care of following a path made of waypoints as well as generating this path.
 *
 * @param pilot MoveController of the two wheeled robot, the driver
 * @param poseProvider
 *
 * @author Wei-Di Chang
 * @version 2.5
 * @since 1.0
 */

public class Navigation extends Navigator {

	final double SPACING = 30.48;		//	Constant to hold the spacing between each parallel gridline
	private Path nodePath = new Path();	//	The path is a property of navigation

	public Navigation(MoveController pilot, PoseProvider poseProvider) {
		super(pilot, poseProvider);
		this.singleStep(true);			//	Enable single step, waypoint by waypoint navigation
	}

	public Path PathMaker(Pose start, Waypoint end){
		nodePath.clear();				//Clear any previously existing path
		int xCounter = (int) Math.abs((end.getX()-start.getX())/SPACING);	//	Compute the number of waypoints to create on the X-axis
		int yCounter = (int) Math.abs((end.getY()-start.getY())/SPACING);	//	Compute the number of waypoints to create on the Y-axis

		//	Store starting position
		double xCoord = start.getX();	
		double yCoord = start.getY();

		//	Compute distance from start to end point, on each axis
		double yDelta = end.getY()-start.getY();	
		double xDelta = end.getX()-start.getX();	

		//	Initialize two counter variables
		int i = 0;
		int j = 0;

		// If the robot is facing a direction in the Y-Axis, generate a path which starts by travelling on the Y-axis
		if((start.getHeading()>45 && start.getHeading()<135) || (start.getHeading()<-45 && start.getHeading()>-135))
		{
			while(i != yCounter)
			{						
				//Increment or decrement the y coordinate and store it as a Waypoint
				if(yDelta > 0)
				{
					yCoord+=SPACING;
				}
				else {
					yCoord-=SPACING;
				}
				nodePath.add(new Waypoint(start.getX(),yCoord));

				i+=1;
			}			
			while(j != xCounter)
			{
				//Increment or decrement the x coordinate and store it as a Waypoint

				nodePath.add(new Waypoint(xCoord,yCoord));
				if(xDelta>0)
				{
					xCoord+=SPACING;
				}
				else {
					xCoord-=SPACING;
				}			
				j+=1;
			}
		}
		
		// If the robot is facing a direction in the X-Axis, generate a path which starts by travelling on the X-axis
		else {
			while(j != xCounter)
			{
				//Increment or decrement the x coordinate and store it as a Waypoint
				if(xDelta>0)
				{
					xCoord+=SPACING;
				}
				else {
					xCoord-=SPACING;
				}		
				nodePath.add(new Waypoint(xCoord,start.getY()));					
				j+=1;
			}
			while(i != yCounter)
			{				
				//Increment or decrement the y coordinate and store it as a Waypoint
				nodePath.add(new Waypoint(xCoord,yCoord));
				if(yDelta>0)
				{
					yCoord+=SPACING;
				}
				else {
					yCoord-=SPACING;
				}
				i+=1;
			}
		}

		//Return the generated path
		return nodePath;
	}
}
=======
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

	final double SPACING = 30.48;
	private Waypoint previousWaypoint;	
	private Waypoint destinationWaypoint;
	private Path nodePath = new Path();
	private String yDirection, xDirection;

	public Navigation(MoveController pilot, PoseProvider poseProvider) {
		super(pilot, poseProvider);
		// TODO Auto-generated constructor stub
		this.singleStep(true);
	}

	public Path PathMaker(Pose start, Waypoint end){
//		Path generatedPath = new Path();
		nodePath.clear();
		int xCounter = (int) Math.abs((end.getX()-start.getX())/SPACING);
		int yCounter = (int) Math.abs((end.getY()-start.getY())/SPACING);
		double xCoord = start.getX();
		double yCoord = start.getY();
		double yDelta = end.getY()-start.getY();
		double xDelta = end.getX()-start.getX();
		int i = 0;
		int j = 0;
		if((start.getHeading()>45&&start.getHeading()<135)||(start.getHeading()<-45&&start.getHeading()>-135))
		{

			while(i < yCounter)
			{							
				nodePath.add(new Waypoint(start.getX(),yCoord));
				if(yDelta>0)
				{
					yCoord+=SPACING;
				}
				else {
					yCoord-=SPACING;
				}
				i+=1;
			}
			while(j < xCounter)
			{
				nodePath.add(new Waypoint(xCoord,end.getY()));
				if(xDelta>0)
				{
					xCoord+=SPACING;
				}
				else {
					xCoord-=SPACING;
				}			
				j+=1;
			}
		}
		else {
			while(j < xCounter)
			{
				nodePath.add(new Waypoint(xCoord,start.getY()));
				if(xDelta>0)
				{
					xCoord+=SPACING;
				}
				else {
					xCoord-=SPACING;
				}			
				j+=1;
			}
			while(i < yCounter)
			{							
				nodePath.add(new Waypoint(end.getX(),yCoord));
				if(yDelta>0)
				{
					yCoord+=SPACING;
				}
				else {
					yCoord-=SPACING;
				}
				i+=1;
			}
		}
		return nodePath;

	}

	//NodePathMaker Method : 

	//	public Waypoint NextYWaypointMaker(Pose start, Waypoint end, boolean obstacle)
	//	{
	//		double yDelta = end.getY()-start.getY();
	//		if(yDelta>0)
	//		{
	//		yDirection = "North";
	//		}
	//		else if(yDelta<0)
	//		{
	//			yDirection = "South";
	//		}
	//		
	//		if(!obstacle)
	//		{
	//			if(start.getY() == end.getY())
	//			{
	//				return (NextXWaypointMaker(start, end, ))
	//			}
	//		}
	//		else {
	//			
	//		}
	//	}
	//	public Waypoint NextXWaypointMaker(Pose start, Waypoint end, boolean obstacle)
	//	{	
	//		double xDelta = end.getX()-start.getX();		
	//		if (xDelta>0)
	//		{
	//			xDirection = "East";
	//		}
	//		else if(xDelta<0)
	//		{
	//			xDirection = "West";
	//		}
	//		
	//		
	//		else {
	//			
	//		}
	//		if(!obstacle)
	//		{
	//			
	//		}
	//		else 
	//		{
	//			
	//		}
	//		
	//		Sound.beepSequenceUp();
	//		return end;	//Should never be reached
	//	}

	public String getxDirection() {
		return xDirection;
	}

	public void setxDirection(String xDirection) {
		this.xDirection = xDirection;
	}

	public String getyDirection() {
		return yDirection;
	}

	public void setyDirection(String yDirection) {
		this.yDirection = yDirection;
	}





}
>>>>>>> FETCH_HEAD
