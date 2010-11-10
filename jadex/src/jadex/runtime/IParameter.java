package jadex.runtime;

import jadex.model.IMParameter;

/**
 *  The interface for parameters.
 */
public interface IParameter extends IElement
{
	//-------- methods --------

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
	 *  Get the value class.
	 *  @return The valuec class.
	 */
	public Class	getClazz();
}
