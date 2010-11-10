package jadex.examples.cleanerworld.multi;


/**
 *  Editable Java class for concept Location of cleaner-generated ontology.
 */
public class Location	extends LocationData	implements Cloneable
{
	//-------- constants --------

	/** Distance, when two locations are considered near. */
	public static final double	DEFAULT_TOLERANCE	= 0.001;

	//-------- constructors --------

	/**
	 *  Create a new Location.
	 */
	public Location()
	{
		// Empty constructor required for JavaBeans (do not remove).
	}

	/**
	 *  Create a new Location.
	 */
	public Location(double x, double y)
	{
		setX(x);
		setY(y);
	}

	//-------- custom code --------

	/**
	 *  Caculate is a location is near this location.
	 *  @return The distance.
	 */
	public double getDistance(Location other)
	{
		assert other!=null;
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
	 *  Test if two instances are equal.
	 *  @return True, if equal.
	 */
	public boolean equals(Object o)
	{
		boolean ret = false;
		if(o instanceof Location)
		{
			Location loc = (Location)o;
			if(loc.x==x && loc.y==y)
				ret = true;
		}
		return ret;
	}

	/**
	 *  Clone the object.
	 */
	public Object	clone()
	{
		try
		{
			return super.clone();
		}
		catch(CloneNotSupportedException e)
		{
			assert false;
			throw new RuntimeException("Clone not supported");
		}
	}
}

