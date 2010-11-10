package jadex.model;


/**
 *  The container for properties.
 */
public interface IMPropertybase extends IMBase
{

	//-------- properties --------

	/**
	 *  Get all properties.
	 *  @return An array of the properties.
	 */
	public IMExpression[]	getProperties();

	/**
	 *  Get a property.
	 *  @param name The property name.
	 *  @return The property expression.
	 */
	public IMExpression	getProperty(String name);

	/**
	 *  Create a property.
	 *  @param name The property name.
	 *  @param expression	The expression string.
	 *  @return The new property.
	 */
	public IMExpression	createProperty(String name, String expression);

	/**
	 *  Delete the property.
	 */
	public void	deleteProperty(IMExpression property);
}
