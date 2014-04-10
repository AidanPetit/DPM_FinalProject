/**
 * BlockPoint Object class, which is used to save two angles and the distance to that object.
 * The first angle is used to latch the first detected, usually rising edge of the object, and the second is the second detected edge, usually a falling edge.
 *
 * @author Wei-Di Chang
 * @version 2.5
 * @since 2.0
 */

public class BlockPoint {

	private double firstAngle,secondAngle;
	private int distance;
	public BlockPoint(double firstAngle, double secondAngle, int distance)
	{
		this.firstAngle = firstAngle;
		this.secondAngle = secondAngle;
		this.distance = distance;
	}
	
	//Accessors
	public double getSecondAngle() {
		return secondAngle;
	}

	public double getFirstAngle() {
		return firstAngle;
	}
	
	public int getDistance() {
		return distance;
	}
}
