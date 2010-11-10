package jadex.model;

/**
 *  Configuration settings for the planbase.
 */
public interface IMConfigPlanbase extends IMConfigBase
{
	//-------- initial plans --------

	/**
	 *  Get all known initial plans.
	 *  @return The initial plans.
	 */
	public IMConfigPlan[] getInitialPlans();

	/**
	 *  Create a initial plan.
	 *  @param ref	The name of the referenced plan.
	 *  @return The initial plan.
	 */
	public IMConfigPlan	createInitialPlan(String ref);

	/**
	 *  Delete a initial plan.
	 *  @param plan	The initial plan.
	 */
	public void	deleteInitialPlan(IMConfigPlan plan);

	//-------- end plans --------

	/**
	 *  Get all known end plans.
	 *  @return The end plans.
	 */
	public IMConfigPlan[] getEndPlans();

	/**
	 *  Create a end plan.
	 *  @param ref	The name of the referenced plan.
	 *  @return The end plan.
	 */
	public IMConfigPlan	createEndPlan(String ref);

	/**
	 *  Delete a end plan.
	 *  @param plan	The end plan.
	 */
	public void	deleteEndPlan(IMConfigPlan plan);
}
