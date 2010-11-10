package jadex.runtime;

import jadex.model.IMElement;

/**
 *	Base interface for all runtime elements.
 */
public interface IElement
{
	//-------- element methods ---------

	/**
	 *  Get the name.
	 *  @return The name.
	 */
	public String getName();

	/**
	 *  Get the model element.
	 *  @return The model element.
	 */
	public IMElement getModelElement();
}
