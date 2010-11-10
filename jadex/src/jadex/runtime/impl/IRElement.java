package jadex.runtime.impl;

import jadex.model.IMElement;

/**
 *	Base interface for all runtime elements.
 */
public interface IRElement extends ICleanable
{
	//-------- element methods ---------

	/**
	 *  Get the name.
	 *  @return The name.
	 */
	public String getName();

	/**
	 *  Get the fully qualified name with respect its owner.
	 *  @return The fully qualified name.
	 */
	public String getDetailName();

	/**
	 *  Get the model element.
	 */
	public IMElement getModelElement();

	/**
	 *  Get the owner.
	 *  @return The owner.
	 */
	public RElement	getOwner();

	/**
	 *  Get the scope.
	 *  @return The scope.
	 */
	public RCapability	getScope();

	/**
	 *  Get the type of this runtime element.
	 *  @return The type.
	 */
	public String getType();

	/**
	 *  Test, if the element is cleanedup.
	 *  @return True, if cleaned up.
	 */
	public boolean isCleanedup();
}
