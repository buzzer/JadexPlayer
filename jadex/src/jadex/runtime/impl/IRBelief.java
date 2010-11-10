package jadex.runtime.impl;

/**
 *  The internal interface for all beliefs (concrete and referenced).
 */
public interface IRBelief	extends IRReferenceableElement
{
	//-------- methods --------

	/**
	 *  Set a fact of a belief.
	 *  @param fact The new fact.
	 */
	public void setFact(Object fact);

	/**
	 *  Get the fact of a belief.
	 *  @return The fact.
	 */
	public Object	getFact();

	/**
	 *  Is this belief accessable.
	 *  @return False, if the belief cannot be accessed.
	 */
	public boolean isAccessible();

	/**
	 *  Refresh the value of the belief.
	 */
	public void	refresh();

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
}
