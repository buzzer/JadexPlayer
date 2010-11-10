package jadex.runtime.impl;



/**
 *  The interface for parameters.
 */
public interface IRParameter extends IRReferenceableElement
{
	/**
	 *  Set a value of a parameter.
	 *  @param value The new value.
	 */
	public void setValue(Object value);

	/**
	 *  Get the value of a parameter.
	 *  @return The value.
	 */
	public Object	getValue();

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
	 */
	//public String getProtectionMode();

	/**
	 *  Set the initial value.
	 *  May be a default value (from model),
	 *  or an initiual value provided on creation.
	 */
	public void	setInitialValue();

}