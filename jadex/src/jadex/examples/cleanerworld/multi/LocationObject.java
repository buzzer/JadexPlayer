package jadex.examples.cleanerworld.multi;


/**
 *  Editable Java class for concept LocationObject of cleaner-generated ontology.
 */
public abstract class LocationObject	extends LocationObjectData	implements Cloneable
{
	//-------- constructors --------

	/**
	 *  Create a new LocationObject.
	 */
	public LocationObject()
	{
		// Empty constructor required for JavaBeans (do not remove).
	}

	/**
	 *  Create a new LocationObject.
	 */
	public LocationObject(String id, Location location)
	{
		setId(id);
		setLocation(location);
	}

	//-------- custom code --------

	/**
	 *  Test if two instances are equal.
	 *  @return True, if equal.
	 */
	public boolean equals(Object o)
	{
		return o instanceof LocationObject && ((LocationObject)o).id.equals(id);
	}

	/**
	 *  Get the hashcode for this object.
	 *  @return The hashcode.
	 */
	public int hashCode()
	{
		return id.hashCode();
	}

	/**
	 *  Clone the object.
	 */
	public Object	clone()
	{
		try
		{
			LocationObject	clone	= (LocationObject)super.clone();
			clone.setLocation((Location)getLocation().clone());
			return clone;
		}
		catch(CloneNotSupportedException e)
		{
			assert false;
			throw new RuntimeException("Clone not supported");
		}
	}
}

