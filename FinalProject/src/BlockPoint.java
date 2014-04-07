
public class BlockPoint {

	private double firstAngle,secondAngle;
	private int distance;
	public BlockPoint(double firstAngle, double secondAngle, int distance)
	{
		this.firstAngle = firstAngle;
		this.secondAngle = secondAngle;
		this.distance = distance;
	}
	
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
