package jadex.model;

/**
 *  Model element of a maintain goal.
 */
public interface IMMaintainGoal extends IMGoal
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

	
	//-------- maintain condition --------

	/**
	 *  Get the maintain condition of the goal.
	 *  @return The maintain condition.
	 */
	public IMCondition	getMaintainCondition();

	/**
	 *  Create a maintain condition for the goal.
	 *  @param expression	The expression string.
	 *  @return The new maintain condition.
	 */
	public IMCondition	createMaintainCondition(String expression);

	/**
	 *  Delete the maintain condition of the goal.
	 */
	public void	deleteMaintainCondition();

}
