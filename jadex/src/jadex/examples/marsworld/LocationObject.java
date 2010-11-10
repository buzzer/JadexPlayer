package jadex.examples.marsworld;

import java.io.Serializable;

import nuggets.INugget;
import jadex.util.SReflect;

/**
 *  An object with a location.
 */
public class LocationObject	extends LocationObjectData	implements Serializable, INugget
{
	//-------- constructors --------

	/**
	 *  Create a new location object.
	 *  Empty bean contructor.
	 */
	public LocationObject()
	{
	}

	/**
	 *  Create a new location object.
	 *  @param id	The id.
	 *  @param location	The location.
	 */
	public LocationObject(String id, Location location)
	{
		this.id	= id;
		this.location = location;
	}

	//-------- methods --------

	/**
	 *  Test if two instances are equal.
	 *  @return True, if equal.
	 */
	public boolean equals(Object o)
	{
		return o instanceof LocationObject && ((LocationObject)o).id.equals(id);
	}

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public String toString()
	{
		return SReflect.getInnerClassName(this.getClass())+" loc: "+location;
	}

}
