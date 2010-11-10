package jadex.model;

/**
 *  The planbase behaviour interface.
 */
public interface IMPlanbase extends IMBase
{

	//-------- plans --------

	/**
	 *  Get all known plans.
	 *  @return The plans.
	 */
	public IMPlan[] getPlans();

	/**
	 *  Get a plan by name.
	 *  @param name	The plan name.
	 *  @return The plan with that name (if any).
	 */
	public IMPlan	getPlan(String name);

	/**
	 *  Create a new plan.
	 *  @param name	The plan name.
	 *  @param priority	The plan priority.
	 *  @param body The body expression.
	 *  @param type	The body type.
	 *  @return The new plan.
	 */
	public IMPlan	createPlan(String name, int priority, String body, String type);

	/**
	 *  Delete a plan.
	 *  @param plan	The plan.
	 */
	public void	deletePlan(IMPlan plan);


	//-------- plan references (todo) --------

//	/**
//	 *  Get a plan reference.
//	 *  @return The plan reference.
//	 */
//	public IMPlanReference[] getPlanReferences();
//
//	/**
//	 *  Get a plan reference.
//	 *  @param name The name.
//	 *  @return The plan reference.
//	 */
//	public IMPlanReference getPlanReference(String name);
}
