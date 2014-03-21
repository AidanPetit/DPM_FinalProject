
import lejos.nxt.*;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.comm.NXTCommConnector;
import lejos.nxt.comm.NXTConnection;
import lejos.nxt.comm.USB;
import lejos.nxt.comm.USBConnection;
import lejos.nxt.remote.NXTComm;
import lejos.nxt.remote.RemoteMotor;
import lejos.nxt.remote.RemoteNXT;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.Navigator;


public class Team08Robot {
	private Driver pilot;
	private OdometryPoseProvider odometer;
	private Navigator nav;
	//private NXTCommConnector connector; 
	//private RemoteNXT slave;

	private static double leftWheelDiameter=4.32;		//these values are accurate
	private static double rightWheelDiameter=4.32;
	private static double width=16;
	
	private static NXTRegulatedMotor leftMotor=Motor.A;
	private static NXTRegulatedMotor rightMotor=Motor.B;

	private RemoteMotor leftTrack;
	private RemoteMotor rightTrack;


	private UltrasonicSensor frontUS;

	private ColorSensor frontCS;		//for object detection
	private ColorSensor rearCS;			//for localization



	public Team08Robot(){
		this.pilot=new Driver(leftWheelDiameter, rightWheelDiameter, width, leftMotor, rightMotor, false);
		this.odometer=new OdometryPoseProvider(pilot);
		this.nav=new Navigator(pilot, odometer);
		
		/*
		try{
			this.connector = Bluetooth.getConnector();
			this.slave = new RemoteNXT("NXT", connector);  //name needs to be changed to 'TEAM08-2'
		}
		catch (IOException ioe) {
			  LCD.clear();
			  LCD.drawString("Connection ", 0, 0);
			  LCD.drawString(" Failed ", 0, 1);

			  Button.waitForAnyPress();
			  System.exit(0);
		}
		
		
		this.leftTrack = slave.A;
		this.rightTrack = slave.B;
		*/
		this.frontUS = new UltrasonicSensor(SensorPort.S1);
		//this.frontCS = new ColorSensor(SensorPort.S2);
		//this.rearCS = new ColorSensor(SensorPort.S3);
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

