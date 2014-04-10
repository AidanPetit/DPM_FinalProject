<<<<<<< HEAD
import java.io.IOException;

import bluetooth.BluetoothConnection;
import bluetooth.PlayerRole;
import bluetooth.StartCorner;
import bluetooth.Transmission;

import lejos.nxt.*;
import lejos.nxt.comm.NXTCommConnector;
import lejos.nxt.comm.RS485;
import lejos.nxt.remote.RemoteMotor;
import lejos.nxt.remote.RemoteNXT;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.pathfinding.Path;

/**
 * Team08Robot Class gives access to :
 * All the hardware properties of the robot: sensors, navigation, odometry, etc.
 * All the software properties of the robot
 *
 * @author Aidan
 * @version 3.0
 * @since 1.0
 */

public class Team08Robot {
	private Driver pilot;
	private OdometryPoseProvider odometer;
	private Navigation nav;
	private NXTCommConnector connector;
	private RemoteNXT slave;


	//Objective search values
	private PlayerRole role;
	private int objectiveXLL ;
	private int objectiveYLL ;
	private int objectiveXUR;
	private int objectiveYUR;
	private int redFlag;
	private Waypoint[] objective;
	private int sensorRange;
	private int startingCorner;
	private int greenDZone_Y, greenDZone_X, redDZone_X, redDZone_Y, greenFlag, redZoneLL_X, redZoneLL_Y, redZoneUR_X, redZoneUR_Y, greenZoneLL_X, greenZoneLL_Y, greenZoneUR_X, greenZoneUR_Y;
	private int flagColor;
	
	//Sensor Normal Range
	private final int SENSOR_RANGE = 60;

	//Behavior booleans
	private boolean flagCaptured;
	private boolean flagRecognized;
	private boolean atFlagZone;
	private boolean atDropZone;
	private boolean obstacle;
	private Path myPath;


	//Robot properties
	private static double leftWheelDiameter=4.32;		//these values are accurate
	private static double rightWheelDiameter=4.32;
	private static double width=16.5;

	//Motors
	private static NXTRegulatedMotor leftMotor=Motor.A;
	private static NXTRegulatedMotor rightMotor=Motor.B;

	//Slave motors
	private RemoteMotor leftTrack;
	private RemoteMotor rightTrack;

	//Sensors
	private UltrasonicSensor frontUS;
	private UltrasonicSensor leftUS;
	private ColorSensor frontCS;
	private ColorSensor leftCS;		
	private ColorSensor rightCS;	//for localization


	public Team08Robot(){
		
		//Initialize all used objects
		this.pilot = new Driver(leftWheelDiameter, rightWheelDiameter, width, leftMotor, rightMotor, false);
		this.odometer = new OdometryPoseProvider(pilot);
		this.nav = new Navigation(pilot, odometer);

		//Initialize all behavior booleans
		this.flagCaptured = false;
		this.flagRecognized = false;
		this.atFlagZone = false;		
		this.obstacle = false;


		//Connect to bluetooth server and store all received values
		BluetoothConnection conn = new BluetoothConnection();
		Transmission t = conn.getTransmission();
		if (t == null) {
			LCD.drawString("Failed to read transmission", 0, 5);
		} else {
			role = t.role;	//1-green, 2-red
			startingCorner = t.startingCorner.getId();
			greenZoneLL_X = t.greenZoneLL_X;
			greenZoneLL_Y = t.greenZoneLL_Y;
			greenZoneUR_X = t.greenZoneUR_X;
			greenZoneUR_Y = t.greenZoneUR_Y;

			redZoneLL_X = t.redZoneLL_X;
			redZoneLL_Y = t.redZoneLL_Y;
			redZoneUR_X = t.redZoneUR_X;
			redZoneUR_Y = t.redZoneUR_Y;
			greenDZone_X = t.greenDZone_X;
			greenDZone_Y = t.greenDZone_Y;
			redDZone_X = t.redDZone_X;
			redDZone_Y = t.redDZone_Y;
			greenFlag = t.greenFlag;
			redFlag=t.redFlag;
			LCD.drawString("All received",0,0);
			conn.printTransmission();
		}
		// Assign values according to player role
		if(role.getId()==1)	//green
		{
			objectiveXLL = redZoneLL_X;
			objectiveYLL = redZoneLL_Y;
			objectiveXUR = redZoneUR_X;
			objectiveXUR = redZoneUR_X;
			flagColor = greenFlag;
		}
		else {	//red
			
			objectiveXLL = greenZoneLL_X;
			objectiveYLL = greenZoneLL_Y;
			objectiveXUR = greenZoneUR_X;
			objectiveXUR = greenZoneUR_X;
			flagColor = redFlag;
		}
	
		//Scale sensorRange with respect to the objective zone, for the search scan. 
		//Anything beyond the longest side of the objective zone is not in the corner scan zone
		//As the robot should go through all 4 corners of the zone, the whole zone should get throroughly scanned.
		if((this.objectiveXUR-this.objectiveXLL)>(this.objectiveYUR-this.objectiveYLL))							
		{																				
			this.sensorRange = (int)30.48*((this.objectiveXUR-this.objectiveXLL));		
		}
		else {
			this.sensorRange = (int)30.48*((this.objectiveYUR-this.objectiveYLL));
		}

		//Initialize connection with slave
		LCD.clearDisplay();
		LCD.drawString("Connecting...",0,0);
		try{
			this.connector = RS485.getConnector();
			this.slave = new RemoteNXT("NXT", connector);
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
		this.leftUS = new UltrasonicSensor(slave.S1);

		//Initialize objective corners array
		//0 --> Lower left corner
		//1 --> Lower right corner 
		//2 -->	Upper left corner
		//3 -->	Upper right corner
		this.objective=new Waypoint[4];
		this.objective[0] = new Waypoint(30.48*objectiveXLL,30.48*objectiveYLL);
		this.objective[1] = new Waypoint(30.48*objectiveXUR,30.48*objectiveYLL);
		this.objective[2] = new Waypoint(30.48*objectiveXUR,30.48*objectiveYUR);
		this.objective[3] = new Waypoint(30.48*objectiveXLL,30.48*objectiveYUR);


		//Initialize master sensors
		this.rightCS = new ColorSensor(SensorPort.S2);
		this.frontCS= new ColorSensor(SensorPort.S3);
		this.frontUS = new UltrasonicSensor(SensorPort.S1);

		//Stop both claw motors to prevent them from dropping constantly
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

	public boolean getObstacle() {
		return obstacle;
	}

	public void setObstacle(boolean obstacle) {
		this.obstacle = obstacle;
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

	//Objective values getters/setters
	public int getFlagColor() {
		return flagColor;
	}

	public void setFlagColor(int flagColor) {
		this.flagColor = flagColor;
	}

	public Waypoint[] getObjectiveWaypoint()
	{
		return objective;
	}

	public int getObjectiveYUR() {
		return (int) (30.48*objectiveYUR);
	}

	public int getObjectiveXUR() {
		return (int) (30.48*objectiveXUR);
	}

	public int getStartingCorner() {
		return startingCorner;
	}

	public void setStartingCorner(int startingCorner) {
		this.startingCorner = startingCorner;
	}

	public int getSensorRange() {
		return sensorRange;
	}

	public void setSensorRange(int sensorRange) {
		this.sensorRange = sensorRange;
	}

	public Path getMyPath() {
		return myPath;
	}

	public void setMyPath(Path myPath) {
		this.myPath = myPath;
	}
	
	//Accessors for odometry, navigation, and pilot
	public OdometryPoseProvider getOdo(){
		return this.odometer;
	}

	public Navigation getNav(){
		return this.nav;
	}

	public Driver getPilot(){
		return this.pilot;
	}

	//Hardware accessors
	public UltrasonicSensor getFrontUS(){
		return this.frontUS;
	}

	public UltrasonicSensor getLeftUS(){
		return this.leftUS;
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

	//Polling methods for both Ultrasonic sensors
	public int getFilteredData() {
		int distance = 0;

		// do a ping
		frontUS.ping();

		// wait for the ping to complete
		try { Thread.sleep(50); } catch (InterruptedException e) {}

		// there will be a delay here
		distance = frontUS.getDistance();

		//this filters out large values
		if(distance>SENSOR_RANGE){	
			distance = SENSOR_RANGE;
		}
		return distance;
	}
	
	public int getLeftFilteredData() {
		int distance = 0;

		// do a ping
		frontUS.ping();

		// wait for the ping to complete
		try { Thread.sleep(50); } catch (InterruptedException e) {}

		// there will be a delay here
		distance = leftUS.getDistance();

		//this filters out large values
		if(distance>SENSOR_RANGE){	
			distance = SENSOR_RANGE;
		}
		Sound.buzz();
		return distance;
	}


	public int getSearchFilteredData() {
		int distance;

		// do a ping
		frontUS.ping();

		// wait for the ping to complete
		try { Thread.sleep(50); } catch (InterruptedException e) {}

		// there will be a delay here
		distance = frontUS.getDistance();

		//this filters out large values
		if(distance>sensorRange){	
			distance = sensorRange;
		}
		return distance;
	}
	
	//Returns true if the robot is rotating, false otherwise
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
}
=======
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
import lejos.robotics.pathfinding.Path;



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
	private Navigation nav;
	private NXTCommConnector connector;
	private RemoteNXT slave;
	private OdometerCorrection odoCorrect;

	//Objective search values
	private int objectiveXLL ;
	private int objectiveYLL ;
	private int objectiveXUR;
	private int objectiveYUR;
	private int redFlag;
	private Waypoint[] objective;
	private int sensorRange;
	private int startingCorner;
	
	//Sensor Normal Range
	private final int SENSOR_RANGE = 60;

	//Behavior booleans
	private boolean tooClose;
	private boolean flagCaptured;
	private boolean flagRecognized;
	private boolean atFlagZone;
	private boolean atDropZone;
//	private boolean check;	//For travel and avoid
	private boolean obstacle;
	private Path myPath;



	//Robot properties
	private static double leftWheelDiameter=4.32;		//these values are accurate
	private static double rightWheelDiameter=4.32;
	private static double width=16.5;
	
	//Motors
	private static NXTRegulatedMotor leftMotor=Motor.A;
	private static NXTRegulatedMotor rightMotor=Motor.B;
	
	//Slave motors
	private RemoteMotor leftTrack;
	private RemoteMotor rightTrack;
	
	//Sensors
	private UltrasonicSensor frontUS;
	private UltrasonicSensor leftUS;
	private UltrasonicSensor rightUS;

	private ColorSensor frontCS;
	private ColorSensor leftCS;		
	private ColorSensor rightCS;	//for localization

//	//Testing feature detector
//	private int MAX_DISTANCE = 50;
//	private int DELAY=100;





	public Team08Robot(){
		this.pilot=new Driver(leftWheelDiameter, rightWheelDiameter, width, leftMotor, rightMotor, false);
		this.odometer=new OdometryPoseProvider(pilot);
		this.nav=new Navigation(pilot, odometer);

		//Initialize all booleans
//		this.tooClose = false;
		this.flagCaptured = false;
		this.flagRecognized = false;
		this.atFlagZone = false;		//Change back to false
		this.setObstacle(false);
		


		//		BluetoothConnection conn = new BluetoothConnection();
		//
		//		// as of this point the bluetooth connection is closed again, and you can pair to another NXT (or PC) if you wish
		//
		//		// example usage of Tranmission class
		//		Transmission t = conn.getTransmission();
		//		if (t == null) {
		//			LCD.drawString("Failed to read transmission", 0, 5);
		//		} else {
		//			PlayerRole role = t.role;
		//			StartCorner corner = t.startingCorner;
		//			int greenZoneLL_X = t.greenZoneLL_X;
		//			int greenZoneLL_Y = t.greenZoneLL_Y;
		//			this.objectiveXLL = t.redZoneLL_X;
		//			this.objectiveYLL = t.redZoneLL_Y;
		//			this.objectiveXUR = t.redZoneUR_X;
		//			this.objectiveYUR = t.redZoneUR_Y;
		//			int greenDZone_X = t.greenDZone_X;
		//			int greenDZone_Y = t.greenDZone_Y;
		//			int redDZone_X = t.redDZone_X;
		//			int redDZone_Y = t.redDZone_Y;
		//			int greenFlag = t.greenFlag;
		//			redFlag=t.redFlag;
		//
		//
		//			LCD.drawString("All received",0,0);
		//			// print out the transmission information
		//			conn.printTransmission();
		//		}

		//For testing purpose :
		this.objectiveXLL=4;
		this.objectiveYLL=5;
		this.objectiveXUR=6;
		this.objectiveYUR=6;
		int greenFlag=3;
		startingCorner = 1;
		redFlag=3;
		
		
		if((this.objectiveXUR-this.objectiveXLL)>(this.objectiveYUR-this.objectiveYLL))	//Scale sensorRange with respect to the objective zone, for the search scan. 						
		{																				//Anything beyond the longest side of the objective zone is not in the corner scan zone
			this.sensorRange = (int)30.48*((this.objectiveXUR-this.objectiveXLL));		//As the robot should go through all 4 corners of the zone, the whole zone should get throroughly scanned.
		}
		else {
			this.sensorRange = (int)30.48*((this.objectiveYUR-this.objectiveYLL));
		}
		//--------------------

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
		this.leftUS = new UltrasonicSensor(slave.S1);
		this.rightUS = new UltrasonicSensor(slave.S3);

		//Initialize objective corners array
		this.objective=new Waypoint[4];
		this.objective[0] = new Waypoint(30.48*objectiveXLL,30.48*objectiveYLL);
		this.objective[1] = new Waypoint(30.48*objectiveXUR,30.48*objectiveYLL);
		this.objective[2] = new Waypoint(30.48*objectiveXUR,30.48*objectiveYUR);
		this.objective[3] = new Waypoint(30.48*objectiveXLL,30.48*objectiveYUR);


		//Initialize master sensors
		this.leftCS = new ColorSensor(SensorPort.S1);
		this.rightCS = new ColorSensor(SensorPort.S2);
		this.frontCS= new ColorSensor(SensorPort.S3);

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

	public boolean getObstacle() {
		return obstacle;
	}

	public void setObstacle(boolean obstacle) {
		this.obstacle = obstacle;
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

	//------------------------------------------
	//Objective getters/setters
	
	public Waypoint[] getObjectiveWaypoint()
	{
		return objective;
	}


	public int getObjectiveYUR() {
		return (int) (30.48*objectiveYUR);
	}

	public int getObjectiveXUR() {
		return (int) (30.48*objectiveXUR);
	}
	public int getStartingCorner() {
		return startingCorner;
	}

	public void setStartingCorner(int startingCorner) {
		this.startingCorner = startingCorner;
	}

	public int getSensorRange() {
		return sensorRange;
	}

	public void setSensorRange(int sensorRange) {
		this.sensorRange = sensorRange;
	}

	public Path getMyPath() {
		return myPath;
	}

	public void setMyPath(Path myPath) {
		this.myPath = myPath;
	}

	//--------------------------------------------
	public OdometryPoseProvider getOdo(){
		return this.odometer;
	}

	public Navigation getNav(){
		return this.nav;
	}

	public Driver getPilot(){
		return this.pilot;
	}
	
	//Ultrasonic Sensors getters
	public UltrasonicSensor getFrontUS(){
		return this.frontUS;
	}
	
	public UltrasonicSensor getLeftUS(){
		return this.leftUS;
	}	
	
	public UltrasonicSensor getRightUS(){
		return this.rightUS;
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
		if(distance>SENSOR_RANGE){	
			distance = SENSOR_RANGE;
		}
		return distance;
	}
	
	public int getSearchFilteredData() {
		int distance;

		// do a ping
		frontUS.ping();

		// wait for the ping to complete
		try { Thread.sleep(50); } catch (InterruptedException e) {}

		// there will be a delay here
		distance = frontUS.getDistance();

		//this filters out large values
		if(distance>sensorRange){	
			distance = sensorRange;
		}
		return distance;
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
		return this.odoCorrect;
	}
	
	public void setOdometerCorrection(OdometerCorrection correct) {
		this.odoCorrect = correct;
	}


}
>>>>>>> FETCH_HEAD
