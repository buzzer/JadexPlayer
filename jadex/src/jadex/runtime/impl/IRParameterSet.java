package jadex.runtime.impl;

/**
 *  The internal interface for parameters and references.
 */
public interface IRParameterSet extends IRReferenceableElement
{
	/**
	 *  Add a value to a parameter.
	 *  @param value The new value.
	 */
	public void addValue(Object value);

	/**
	 *  Remove a value to a parameter.
	 *  @param value The new value.
	 */
	public void removeValue(Object value);

	/**
	 *  Add values to a parameter set.
	 */
	public void addValues(Object[] values);

	/**
	 *  Remove all values from a parameter.
	 */
	public void removeValues();

	/**
	 *  Get a value equal to the given object.
	 *  @param oldval The old value.
	 *  @throw RuntimeException when the value was not found.
	 */
	public Object	getValue(Object oldval);

	/**
	 *  Test if a value is contained in a parameter.
	 *  @param value The value to test.
	 *  @return True, if value is contained.
	 */
	public boolean containsValue(Object value);

	/**
	 *  Get the values of a parameterset.
	 *  @return The values.
	 */
	public Object[]	getValues();

	/**
	 *  Update a value to a new value. Searches the old
	 *  value with equals, removes it and stores the new value.
	 *  @param newvalue The new value.
	 *  @throw RuntimeException when the value was not found.
	 */
	public void updateValue(Object newvalue);

	/**
	 *  Get the number of values currently
	 *  contained in this set.
	 *  @return The values count.
	 */
	public int size();

	/**
	 *  Update or add a value. When the value is already
	 *  contained it will be updated to the new value.
	 *  Otherwise the value will be added.
	 *  @param value The new or changed value.
	 * /
	public void updateOrAddValue(Object value);*/

	/**
	 *  Replace a value with another one.
	 *  @param oldvalue The old value.
	 *  @param newvalue The new value.
	 * /
	public void replaceValue(Object oldvalue, Object newvalue);*/

	/**
	 *  Was the typed element modified by setting a value.
	 *  @return True, if modified.
	 */
	public boolean isModified();

	/**
	 *  Get the value class.
	 *  Shortcut for getModelElement().getClazz().
	 *  @return The value class.
	 */
	public Class	getClazz();

	//-------- parameter protection methods --------
	
	/**
	 *  Check if this paramter can be accessed for read access.
	 */
	public void checkReadAccess();

	/**
	 *  Check if this paramter can be accessed for write access.
	 */
	public void checkWriteAccess();

	/**
	 *  Get the protection mode of this parameter.
	 *  @return The protection mode.
	 * /
	public String getProtectionMode();*/

	/**
	 *  Set the initial values.
	 *  May be default values (from model),
	 *  or initiual values provided on creation.
	 */
	public void	setInitialValues();

}
