/* DPM Final Project - Main Class
 *  ECSE211-DPM	Group 08
 *  Wei-Di Chang 260524917
 *  Aidan Petit
 */
import java.io.IOException;

import bluetooth.BluetoothConnection;
import bluetooth.PlayerRole;
import bluetooth.StartCorner;
import bluetooth.Transmission;

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
import lejos.robotics.navigation.Waypoint;



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
	private OdometerCorrection odoCorrect;
	private Navigation nav;
	private NXTCommConnector connector;
	private RemoteNXT slave;

	//Objective values
	private int objectiveXLL ;
	private int objectiveYLL ;
	private int objectiveXUR;
	private int objectiveYUR;
	private int redFlag;
	private StartCorner corner;

	//Behavior booleans
	private boolean tooClose;
	private boolean flagCaptured;
	private boolean flagRecognized;
	private boolean atFlagZone;
	private boolean atDropZone;

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
	private ColorSensor leftCS;		
	private ColorSensor rightCS;	//for localization


	//Testing feature detector
	private int MAX_DISTANCE = 50;
	private int DELAY=100;


	public Waypoint getObjectiveWaypoint()
	{
		return (new Waypoint(30.48*objectiveXLL,30.48*objectiveYLL));
	}


	public int getObjectiveYUR() {
		return (int) (30.48*objectiveYUR);
	}

	public int getObjectiveXUR() {
		return (int) (30.48*objectiveXUR);
	}

	public Team08Robot(){
		this.pilot=new Driver(leftWheelDiameter, rightWheelDiameter, width, leftMotor, rightMotor, false);
		this.odometer=new OdometryPoseProvider(pilot);
		this.nav=new Navigation(pilot, odometer);

		//Initialize all booleans
		this.tooClose = false;
		this.flagCaptured = false;
		this.flagRecognized = false;
		/*
		BluetoothConnection conn = new BluetoothConnection();

		// as of this point the bluetooth connection is closed again, and you can pair to another NXT (or PC) if you wish

		// example usage of Tranmission class
		Transmission t = conn.getTransmission();
		if (t == null) {
			LCD.drawString("Failed to read transmission", 0, 5);
		} else {
			PlayerRole role = t.role;
			corner = t.startingCorner;
			int greenZoneLL_X = t.greenZoneLL_X;
			int greenZoneLL_Y = t.greenZoneLL_Y;
			this.objectiveXLL = t.redZoneLL_X;
			this.objectiveYLL = t.redZoneLL_Y;
			this.objectiveXUR = t.redZoneUR_X;
			this.objectiveYUR = t.redZoneUR_Y;
			int greenDZone_X = t.greenDZone_X;
			int greenDZone_Y = t.greenDZone_Y;
			int redDZone_X = t.redDZone_X;
			int redDZone_Y = t.redDZone_Y;
			int greenFlag = t.greenFlag;
			redFlag=t.redFlag;



			LCD.drawString("All received",0,0);
			// print out the transmission information
			conn.printTransmission();
		}
		 */
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
			} 
			catch (InterruptedException e) {
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
		//		this.topTouch = new TouchSensor(slave.S1);
		this.frontUS = new UltrasonicSensor(slave.S2);

		//Initialize master sensors
		this.leftCS = new ColorSensor(SensorPort.S1);
		this.rightCS = new ColorSensor(SensorPort.S2);
		this.frontCS= new ColorSensor(SensorPort.S3);

		//stop tracks
		this.leftTrack.stop();
		this.rightTrack.stop();

	}

	//Boolean getters and setters

	public int getRedFlag() {
		return redFlag;
	}

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



	public boolean isAtFlagZone() {
		return atFlagZone;
	}

	public void setAtFlagZone(boolean atFlagZone) {
		this.atFlagZone = atFlagZone;
	}

	public boolean isAtDropZone() {
		return atDropZone;
	}

	public void setAtDropZone(boolean atDropZone) {
		this.atDropZone = atDropZone;
	}

	public OdometryPoseProvider getOdo(){
		return this.odometer;
	}

	public Navigation getNav(){
		return this.nav;
	}

	public Driver getPilot(){
		return this.pilot;
	}

	public UltrasonicSensor getFrontUS(){
		return this.frontUS;
	}



	public ColorSensor getFrontCS() {
		return this.frontCS;
	}

	public ColorSensor getRightCS() {
		return rightCS;
	}


	public ColorSensor getLeftCS() {
		return leftCS;
	}

	//
	//	public TouchSensor getTopTouch() {
	//		return this.topTouch;
	//	}


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

	public int getFilteredData() {
		int distance;

		// do a ping
		frontUS.ping();

		// wait for the ping to complete
		try { Thread.sleep(50); } catch (InterruptedException e) {}

		// there will be a delay here
		distance = frontUS.getDistance();

		//this filters out large values
		if(distance>60){
			distance = 60;
		}
		return distance;
	}


	public StartCorner getCorner() {
		return this.corner;
	}

	public boolean isRotating() {
		if(leftMotor.getRotationSpeed() > 0 && rightMotor.getRotationSpeed() < 0) {
			return true;
		}
		else if(leftMotor.getRotationSpeed() < 0 && rightMotor.getRotationSpeed() > 0){
			return true;
		}
		else{
			return false;
		}
	}


	public OdometerCorrection getMyOdoCorrect() {
		return odoCorrect;
	}
	
	public void setOdometerCorrection(OdometerCorrection correct) {
		this.odoCorrect = correct;
	}
}
