package jadex.model;

/**
 *  The beliefbase is the container for the agent's or capability's beliefs and belief sets.
 */
public interface IMBeliefbase extends IMBase
{
	
	//-------- beliefs --------
	
	/**
	 *  Get all defined beliefs.
	 *  @return The beliefs.
	 */
	public IMBelief[] getBeliefs();

	/**
	 *  Get a belief by name.
	 *  Searches the belief in direct subcapabilities,
	 *  when path notation is used (a.name).
	 *  @param name	The belief name.
	 *  @return The belief with that name (if any).
	 */
	public IMBelief	getBelief(String name);

	/**
	 *  Create a new belief.
	 *  @param name	The belief name.
	 *  @param clazz	The class for facts.
	 *  @param updaterate	The updaterate (or -1 for none).
	 *  @return	The newly created belief.
	 */
	public IMBelief	createBelief(String name, Class clazz, long updaterate, String exported);
	
	/**
	 *  Delete a belief.
	 *  @param belief	The belief to delete.
	 */
	public void	deleteBelief(IMBelief belief);

	
	//-------- belief sets --------
	
	/**
	 *  Get all defined belief sets.
	 *  @return The belief sets.
	 */
	public IMBeliefSet[] getBeliefSets();

	/**
	 *  Get a belief set by name.
	 *  Searches the belief in direct subcapabilities,
	 *  when path notation is used (a.name).
	 *  @param name	The belief set name.
	 *  @return The belief set with that name (if any).
	 */
	public IMBeliefSet	getBeliefSet(String name);

	/**
	 *  Create a new belief set.
	 *  @param name	The belief set name.
	 *  @param clazz	The class for facts.
	 *  @param updaterate	The updaterate (or -1 for none).
	 *  @param exported	Flag indicating if this belief set may be referenced from outside capabilities.
	 *  @return	The newly created belief set.
	 */
	public IMBeliefSet	createBeliefSet(String name, Class clazz, long updaterate, String exported);
	
	/**
	 *  Delete a belief set.
	 *  @param beliefset	The belief set to delete.
	 */
	public void	deleteBeliefSet(IMBeliefSet beliefset);

	
	//-------- belief references --------

	/**
	 *  Get all belief references.
	 *  @return The belief references.
	 */
	public IMBeliefReference[] getBeliefReferences();

	/**
	 *  Get a belief reference.
	 *  Searches the belief ref in direct subcapabilities,
	 *  when path notation is used (a.name).
	 *  @param name The name.
	 *  @return The belief reference.
	 */
	public IMBeliefReference getBeliefReference(String name);

	/**
	 *  Create a new belief reference.
	 *  @param name	The belief reference name.
	 *  @param clazz	The class for facts.
	 *  @param exported	Flag indicating if this belief reference may be referenced from outside capabilities.
	 *  @param ref	The referenced belief (or null for abstract).
	 *  @return	The newly created belief reference.
	 */
	public IMBeliefReference	createBeliefReference(String name, Class clazz, String exported, String ref);
	
	/**
	 *  Delete a belief reference.
	 *  @param beliefreference	The belief reference to delete.
	 */
	public void	deleteBeliefReference(IMBeliefReference beliefreference);

	
	//-------- belief set references --------

	/**
	 *  Get all defined belief set references.
	 *  @return The belief set references.
	 */
	public IMBeliefSetReference[] getBeliefSetReferences();

	/**
	 *  Get a belief set reference by name.
	 *  Searches the belief set ref in direct subcapabilities,
	 *  when path notation is used (a.name).
	 *  @param name	The belief set name.
	 *  @return The belief set with that name (if any).
	 */
	public IMBeliefSetReference	getBeliefSetReference(String name);

	/**
	 *  Create a new belief set reference.
	 *  @param name	The belief set reference name.
	 *  @param clazz	The class for facts.
	 *  @param exported	Flag indicating if this belief set reference may be referenced from outside capabilities.
	 *  @param ref	The referenced belief set (or null for abstract).
	 *  @return	The newly created belief set reference.
	 */
	public IMBeliefSetReference	createBeliefSetReference(String name, Class clazz, String exported, String ref);
	
	/**
	 *  Delete a belief set reference.
	 *  @param beliefsetreference	The belief set reference to delete.
	 */
	public void	deleteBeliefSetReference(IMBeliefSetReference beliefsetreference);
}
