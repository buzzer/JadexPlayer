package jadex.examples.hunterprey;


/**
 *  Editable Java class for concept WorldObject of hunterprey ontology.
 */
public class WorldObject	extends WorldObjectData
{
	//-------- constructors --------

	/**
	 *  Create a new WorldObject.
	 */
	public WorldObject()
	{
		// Empty constructor required for JavaBeans (do not remove).
	}

	/**
	 *  Create a new WorldObject.
	 */
	public WorldObject(Location location)
	{
		// Constructor using required slots (change if desired).
		setLocation(location);
	}

	//-------- custom code --------

	/**
	 *  Test if two worldobjects are equal.
	 */
	public boolean	equals(Object o)
	{
		return o.getClass()==this.getClass()
			&& ((WorldObject)o).getLocation().equals(this.getLocation());
	}

	/**
	 *  Get the hash code of the world object.
	 */
	public int	hashCode()
	{
		return getClass().hashCode() ^ getLocation().hashCode();
	}
}

