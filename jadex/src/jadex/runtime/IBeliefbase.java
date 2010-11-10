package jadex.runtime;

import jadex.model.*;


/**
 *  The beliefbase contains the beliefs and beliefsets
 *  of an agent or capability.
 */
public interface IBeliefbase	extends IElement
{
	//-------- methods concerning beliefs --------

    /**
	 *  Get a belief for a name.
	 *  @param name	The belief name.
	 */
	public IBelief getBelief(String name);

	/**
	 *  Get a belief set for a name.
	 *  @param name	The belief set name.
	 */
	public IBeliefSet getBeliefSet(String name);

	/**
	 *  Returns <tt>true</tt> if this beliefbase contains a belief with the
	 *  specified name.
	 *  @param name the name of a belief.
	 *  @return <code>true</code> if contained, <code>false</code> is not contained, or
	 *          the specified name refer to a belief set.
	 *  @see #containsBeliefSet(java.lang.String)
	 */
	public boolean containsBelief(String name);

	/**
	 *  Returns <tt>true</tt> if this beliefbase contains a belief set with the
	 *  specified name.
	 *  @param name the name of a belief set.
	 *  @return <code>true</code> if contained, <code>false</code> is not contained, or
	 *          the specified name refer to a belief.
	 *  @see #containsBelief(java.lang.String)
	 */
	public boolean containsBeliefSet(String name);

	/**
	 *  Returns the names of all beliefs.
	 *  @return the names of all beliefs.
	 */
	public String[] getBeliefNames();

	/**
	 *  Returns the names of all belief sets.
	 *  @return the names of all belief sets.
	 */
	public String[] getBeliefSetNames();

	/**
	 *  Create a belief with given key and class.
	 *  @param key The key identifying the belief.
	 *  @param clazz The class.
	 *  @deprecated
	 */
	public void createBelief(String key, Class clazz, int update);

	/**
	 *  Create a belief with given key and class.
	 *  @param key The key identifying the belief.
	 *  @param clazz The class.
	 *  @deprecated
	 */
	public void createBeliefSet(String key, Class clazz, int update);

	/**
	 *  Delete a belief with given key.
	 *  @param key The key identifying the belief.
	 *  @deprecated
	 */
	public void deleteBelief(String key);

	/**
	 *  Delete a belief with given key.
	 *  @param key The key identifying the belief.
	 *  @deprecated
	 */
	public void deleteBeliefSet(String key);

	/**
	 *  Register a new belief.
	 *  @param mbelief The belief model.
	 */
	public void registerBelief(IMBelief mbelief);

	/**
	 *  Register a new beliefset model.
	 *  @param mbeliefset The beliefset model.
	 */
	public void registerBeliefSet(IMBeliefSet mbeliefset);

	/**
	 *  Register a new belief reference.
	 *  @param mbeliefref The belief reference model.
	 */
	public void registerBeliefReference(IMBeliefReference mbeliefref);

	/**
	 *  Register a new beliefset reference model.
	 *  @param mbeliefsetref The beliefset reference model.
	 */
	public void registerBeliefSetReference(IMBeliefSetReference mbeliefsetref);

	/**
	 *  Deregister a belief model.
	 *  @param mbelief The belief model.
	 */
	public void deregisterBelief(IMBelief mbelief);

	/**
	 *  Deregister a beliefset model.
	 *  @param mbeliefset The beliefset model.
	 */
	public void deregisterBeliefSet(IMBeliefSet mbeliefset);

	/**
	 *  Deregister a belief reference model.
	 *  @param mbeliefref The belief reference model.
	 */
	public void deregisterBeliefReference(IMBeliefReference mbeliefref);

	/**
	 *  Deregister a beliefset reference model.
	 *  @param mbeliefsetref The beliefset reference model.
	 */
	public void deregisterBeliefSetReference(IMBeliefSetReference mbeliefsetref);

	//-------- methods concerning facts --------

	/**
	 *  Add a fact to a beliefset.
	 *  @param name The name identifying the belief set.
	 *  @param fact The new fact.
	 * /
	public void addFact(String name, Object fact);

	/**
	 *  Add a fact to a beliefset.
	 *  @param name The name identifying the belief set.
	 *  @param fact The fact to remove.
	 * /
	public void removeFact(String name, Object fact);

	/**
	 *  Get all facts corresponding to one beliefset.
	 *  @param name The name identifiying the beliefset.
	 *  @return The facts.
	 * /
	public Object[] getFacts(String name);

	/**
	 *  Get the number of values currently
	 *  contained in a set.
	 *  @param name The belief set name.
	 *  @return The values count.
	 * /
	public int size(String name);

	/**
	 *  Add a fact to a belief set.
	 *  @param name The name identifying the belief.
	 * /
	public void removeFacts(String name);

	/**
	 *  Test if a fact is contained in a belief set.
	 *  @param name The belief set identifier.
	 *  @param fact The fact to test.
	 *  @return True, if fact is contained.
	 * /
	public boolean containsFact(String name, Object fact);

	/**
	 *  Update or add a fact. When the fact is already
	 *  contained it will be updated to the new fact.
	 *  Otherwise the value will be added.
	 *  @param name The belief set identifier.
	 *  @param fact The new or changed fact.
	 * /
	public void updateOrAddFact(String name, Object fact);

	/**
	 *  Update a fact to a new fact. Searches the old
	 *  value with equals, removes it and stores the new fact.
	 *  @param name The belief set identifier.
	 *  @param newfact The new fact.
	 * /
	public void updateFact(String name, Object newfact);

	/**
	 *  Replace a fact with another one.
	 *  @param name The belief set identifier.
	 *  @param oldfact The old fact.
	 *  @param newfact The new fact.
	 * /
	public void replaceFact(String name, Object oldfact, Object newfact);

	/**
	 *  Get a fact equal to the given object.
	 *  @param name The belief set identifier.
	 *  @param oldfact The old fact.
	 * /
	public Object getFact(String name, Object oldfact);

	/**
	 *  Get a fact corresponding to a belief.
	 *  @param name The name identifiying the belief.
	 *  @return The fact.
	 * /
	public Object getFact(String name);

	/**
	 *  Set a fact of a believe.
	 *  @param name The name identifying the belief.
	 *  @param fact The new fact.
	 * /
	public void setFact(String name, Object fact);*/
}