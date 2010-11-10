package jadex.examples.hunterprey;


/**
 *  Editable Java class for concept Location of hunterprey ontology.
 */
public class Location	extends LocationData
{
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
	public Location(int x, int y)
	{
		// Constructor using required slots (change if desired).
		setX(x);
		setY(y);
	}

	//-------- custom code --------

	/**
	 *  Test if two locations are equal.
	 */
	public boolean	equals(Object o)
	{
		return (o instanceof Location) &&
			((Location)o).getX()==getX() && ((Location)o).getY()==getY();
	}

	/**
	 *  The hashcode is the 16 bit shifted x position
	 *  plus the y position.
	 *  @return The hashcode.  
	 */
	public int hashCode()
	{
		return getX()<<16 + getY();
	}

}

