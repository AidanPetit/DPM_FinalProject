/* DPM Final Project - Main Class
*  ECSE211-DPM	Group 08
*  Wei-Di Chang 260524917
*  Aidan Petit
*/
import java.io.IOException;

import lejos.nxt.*;
import lejos.nxt.comm.NXTCommConnector;
import lejos.nxt.comm.NXTConnection;
import lejos.nxt.comm.RS485;
import lejos.nxt.remote.NXTComm;
import lejos.nxt.remote.RemoteMotor;
import lejos.nxt.remote.RemoteNXT;
import lejos.nxt.remote.RemoteSensorPort;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.Navigator;

/**
*
* Team08Robot Class gives access to all the sensors, navigation, odometry, etc.
*
*
* @author Aidan
* @version 1.0
* @since 1.0
*/

public class Team08Robot {
	private Driver pilot;
	private OdometryPoseProvider odometer;
	private Navigator nav;
	private NXTCommConnector connector;
	private RemoteNXT slave;
	
	//Behavior booleans
	private boolean tooClose;
	private boolean flagCaptured;
	private boolean flagRecognized;

	private static double leftWheelDiameter=4.32;		//these values are accurate
	private static double rightWheelDiameter=4.32;
	private static double width=16;

	private static NXTRegulatedMotor leftMotor=Motor.A;
	private static NXTRegulatedMotor rightMotor=Motor.B;

	private RemoteMotor leftTrack;
	private RemoteMotor rightTrack;

	private TouchSensor topTouch;
	private UltrasonicSensor frontUS;

	private ColorSensor frontCS;		//for object detection, changed to RemoteSensorPort to accomodate RS485 connection, untested
	private ColorSensor rearCS;			//for localization



	public Team08Robot(){
		this.pilot=new Driver(leftWheelDiameter, rightWheelDiameter, width, leftMotor, rightMotor, false);
		this.odometer=new OdometryPoseProvider(pilot);
		this.nav=new Navigator(pilot, odometer);
		
		//Initialize all booleans
		this.tooClose=false;
		this.setFlagCaptured(false);
		
		//initialize connection with slave
		LCD.clearDisplay();
        LCD.drawString("Connecting...",0,0);
		try{
			this.connector = RS485.getConnector();
			this.slave = new RemoteNXT("NXT", connector);  //name needs to be changed to 'TEAM08-2'
			LCD.clear();
            LCD.drawString("Connected",0,0);
            try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
		}
		catch (IOException ioe) {
			  LCD.clear();
			  LCD.drawString("Connection ", 0, 0);
			  LCD.drawString(" Failed ", 0, 1);

			  Button.waitForAnyPress();
			  System.exit(1);
		}

		//Initialize slave motors and sensors
		this.leftTrack = slave.A;
		this.rightTrack = slave.B;
		this.topTouch=new TouchSensor(slave.S1);
		this.frontUS = new UltrasonicSensor(slave.S2);

		
//		this.frontCS=new RemoteSensorPort(slave.S1,); // int id as second argument ?
//		ColorSensor color=new ColorSensor();
		
		//Initialize master sensors
		this.frontCS= new ColorSensor(SensorPort.S2);

		//this.frontCS = new ColorSensor(SensorPort.S2);
		//this.rearCS = new ColorSensor(SensorPort.S3);
		this.leftTrack.stop();
		this.rightTrack.stop();
		
	}

	//Boolean getters and setters

	public boolean getFlagCaptured() {
		return flagCaptured;
	}

	public void setFlagCaptured(boolean flagCaptured) {
		this.flagCaptured = flagCaptured;
	}

	public boolean getFlagRecognized() {
		return flagRecognized;
	}
	
	public void setFlagRecognized(boolean flagRecognized) {
		this.flagRecognized = flagRecognized;
	}

	public boolean isTooClose() {
		return tooClose;
	}

	public void setTooClose(boolean tooClose) {
		this.tooClose = tooClose;
	}

	public OdometryPoseProvider getOdo(){
		return this.odometer;
	}

	public Navigator getNav(){
		return this.nav;
	}

	public Driver getPilot(){
		return this.pilot;
	}

	public UltrasonicSensor getFrontUS(){
		return this.frontUS;
	}

	public ColorSensor getRearCS() {
		return this.rearCS;
	}

	public ColorSensor getFrontCS() {
		return this.frontCS;
	}

	public TouchSensor getTopTouch() {
		return this.topTouch;
	}


	public RemoteMotor getLeftTrack() {
		return this.leftTrack;
	}

	public RemoteMotor getRightTrack() {
		return this.rightTrack;
	}

	public NXTRegulatedMotor getLeftMotor() {
		return leftMotor;
	}

	public NXTRegulatedMotor getRightMotor() {
		return rightMotor;
	}
}
