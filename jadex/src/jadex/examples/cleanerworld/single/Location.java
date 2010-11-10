package jadex.examples.cleanerworld.single;

/**
 *  The location represents a location on the world map.
 */
public class Location
{
	//-------- constants --------

	/** Distance, when two locations are considered near. */
	public static final double	DEFAULT_TOLERANCE	= 0.001;

	//-------- attributes --------

	/** The x coordinate. */
	public double	x;

	/** The y coordinate. */
	public double	y;

	//-------- constructors --------

	/**
	 *  Create a new location.
	 *  @param x	The x coordinate.
	 *  @param y	The y coordinate.
	 */
	public Location(double x, double y)
	{
		this.x	= x;
		this.y	= y;
	}

	//-------- methods --------

	/**
	 *  Caculate is a location is near this location.
	 *  @return The distance.
	 */
	public double getDistance(Location other)
	{
		return Math.sqrt((other.y-this.y)*(other.y-this.y)+(other.x-this.x)*(other.x-this.x));
	}

	/**
	 *  Check, if two locations are near to each other
	 *  using the default tolerance.
	 *  @return True, if two locations are near to each other.
	 */
	public boolean	isNear(Location other)
	{
		return isNear(other, DEFAULT_TOLERANCE);
	}

	/**
	 *  Check, if two locations are near to each other.
	 *  @param tolerance	The distance, when two locations are considered near.
	 *  @return True, if two locations are near to each other.
	 */
	public boolean	isNear(Location other, double tolerance)
	{
		return getDistance(other) <= tolerance;
	}

	/**
	 *  Convert the location to a string representation.
	 */
	public String	toString()
	{
		return "(" + x + ", " + y + ")";
	}
}

