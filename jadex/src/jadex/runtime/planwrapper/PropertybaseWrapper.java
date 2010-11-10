package jadex.runtime.planwrapper;

import jadex.runtime.IPropertybase;
import jadex.runtime.impl.*;

/**
 *  The propertybase wrapper accessible from other threads (e.g. gui).
 */
public class PropertybaseWrapper extends ElementWrapper	implements IPropertybase
{
	//-------- attributes --------

	/** The original propertybase base. */
	protected RPropertybase propertybase;

	//-------- constructors --------

	/**
	 *  Create a new beliefbase wrapper.
	 */
	protected PropertybaseWrapper(RPropertybase propertybase)
	{
		super(propertybase);
		this.propertybase = propertybase;
	}

	//-------- methods --------

	/**
	 *  Get a property.
	 *  @param name The property name.
	 *  @return The property value.
	 */
	public Object getProperty(final String name)
	{
		checkThreadAccess();
		return propertybase.getProperty(name);
	}

	/**
	 *  Get all properties that start with a start string.
	 *  @param start The start string.
	 *  @return An array of the matching property names.
	 */
	public String[] getPropertyNames(final String start)
	{
		checkThreadAccess();
		return propertybase.getPropertyNames(start);
	}
}
