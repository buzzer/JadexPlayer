package jadex.runtime;

/**
 *  The interface for accessing properties.
 */
public interface IPropertybase extends IElement
{
	/**
	 *  Get a property.
	 *  @param name The property name.
	 *  @return The property value.
	 */
	public Object getProperty(String name);

	/**
	 *  Get all properties that start with a start string.
	 *  @param start The start string.
	 *  @return An array of the matching property names.
	 */
	public String[] getPropertyNames(String start);
}
