package jadex.runtime.impl;

/**
 *  The internal interface for belief sets and belief set references.
 */
public interface IRBeliefSet	extends IRReferenceableElement
{
	/**
	 *  Add a fact to a belief.
	 *  @param fact The new fact.
	 */
	public void addFact(Object fact);

	/**
	 *  Remove a fact to a belief.
	 *  @param fact The new fact.
	 */
	public void removeFact(Object fact);

	/**
	 *  Add facts to a parameter set.
	 */
	public void addFacts(Object[] values);

	/**
	 *  Remove all facts from a belief.
	 */
	public void removeFacts();

	/**
	 *  Get a value equal to the given object.
	 *  @param oldval The old value.
	 */
	public Object	getFact(Object oldval);

	/**
	 *  Test if a fact is contained in a belief.
	 *  @param fact The fact to test.
	 *  @return True, if fact is contained.
	 */
	public boolean containsFact(Object fact);

	/**
	 *  Get the facts of a beliefset.
	 *  @return The facts.
	 */
	public Object[]	getFacts();

	/**
	 *  Update a fact to a new fact. Searches the old
	 *  value with equals, removes it and stores the new fact.
	 *  @param newfact The new fact.
	 */
	public void updateFact(Object newfact);

	/**
	 *  Get the number of values currently
	 *  contained in this set.
	 *  @return The values count.
	 */
	public int size();

	/**
	 *  Update or add a fact. When the fact is already
	 *  contained it will be updated to the new fact.
	 *  Otherwise the value will be added.
	 *  @param fact The new or changed fact.
	 * /
	public void updateOrAddFact(Object fact);*/

	/**
	 *  Replace a fact with another one.
	 *  @param oldfact The old fact.
	 *  @param newfact The new fact.
	 * /
	public void replaceFact(Object oldfact, Object newfact);*/

	/**
	 *  Is this belief accessable.
	 *  @return False, if the belief cannot be accessed.
	 */
	public boolean isAccessible();

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

	/**
	 *  Internal method to get the inivals expression.
	 */
	public IRExpression internalGetInivals();
}
