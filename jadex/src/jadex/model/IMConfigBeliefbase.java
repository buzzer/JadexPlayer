package jadex.model;

/**
 *  Configuration settings for the belief base.
 */
public interface IMConfigBeliefbase extends IMConfigBase
{
	//-------- beliefs --------
	
	/**
	 *  Get all defined beliefs.
	 *  @return The beliefs.
	 */
	public IMConfigBelief[] getInitialBeliefs();

	/**
	 *  Create a new initial belief.
	 *  @param ref	The name of the referenced belief.
	 *  @param expression	The fact expression.
	 *  @param mode	The evaluation mode.
	 *  @return	The newly created initial belief.
	 */
	public IMConfigBelief	createInitialBelief(String ref, String expression, String mode);
	
	/**
	 *  Delete a belief.
	 *  @param belief	The belief to delete.
	 */
	public void	deleteInitialBelief(IMConfigBelief belief);

	
	//-------- belief sets --------
	
	/**
	 *  Get all defined belief sets.
	 *  @return The belief sets.
	 */
	public IMConfigBeliefSet[] getInitialBeliefSets();

	/**
	 *  Create a new belief set.
	 *  @param ref	The name of the referenced belief.
	 *  @param expression	The initial facts expression (if any).
	 *  @return	The newly created belief set.
	 */
	public IMConfigBeliefSet	createInitialBeliefSet(String ref, String expression);
	
	/**
	 *  Delete a belief set.
	 *  @param beliefset	The belief set to delete.
	 */
	public void	deleteInitialBeliefSet(IMConfigBeliefSet beliefset);


	//-------- not xml related --------
	
	/**
	 *  Get the initial configuration for a given belief.
	 *  @param belief	The belief.
	 *  @return	The initial belief configuration.
	 */
	public IMConfigBelief	getInitialBelief(IMBelief belief);

	/**
	 *  Get the initial configuration for a given belief reference.
	 *  @param beliefref	The belief reference.
	 *  @return	The initial belief reference configuration.
	 */
	public IMConfigBelief	getInitialBelief(IMBeliefReference beliefref);

	/**
	 *  Get the initial configuration for a given belief set.
	 *  @param beliefset	The belief set.
	 *  @return	The initial belief set configuration.
	 */
	public IMConfigBeliefSet	getInitialBeliefSet(IMBeliefSet beliefset);

	/**
	 *  Get the initial configuration for a given belief set reference.
	 *  @param beliefsetref	The belief set reference.
	 *  @return	The initial belief set reference configuration.
	 */
	public IMConfigBeliefSet	getInitialBeliefSet(IMBeliefSetReference beliefsetref);
}
