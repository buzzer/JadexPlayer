package jadex.model;

/**
 *  Model element for a query goal.
 */
public interface IMQueryGoal extends IMGoal 
{
	//-------- target condition --------

	/**
	 *  Get the target condition of the goal.
	 *  @return The target condition (if any).
	 */
	public IMCondition	getTargetCondition();

	/**
	 *  Create a target condition for the goal.
	 *  @param expression	The expression string.
	 *  @return The new target condition.
	 */
	public IMCondition	createTargetCondition(String expression);

	/**
	 *  Delete the target condition of the goal.
	 */
	public void	deleteTargetCondition();


	//-------- failure condition --------

	/**
	 *  Get the failure condition of the goal.
	 *  @return The failure condition (if any).
	 */
	public IMCondition	getFailureCondition();

	/**
	 *  Create a failure condition for the goal.
	 *  @param expression	The expression string.
	 *  @return The new failure condition.
	 */
	public IMCondition	createFailureCondition(String expression);

	/**
	 *  Delete the failure condition of the goal.
	 */
	public void	deleteFailureCondition();
}
