package jadex.model;

/**
 *  Configuration settings for the goalbase.
 */
public interface IMConfigGoalbase extends IMConfigBase
{
	//-------- initial goals --------

	/**
	 *  Get all known initial goals.
	 *  @return The initial goals.
	 */
	public IMConfigGoal[] getInitialGoals();

	/**
	 *  Create a initial goal.
	 *  @param ref	The name of the referenced goal.
	 *  @return The initial goal.
	 */
	public IMConfigGoal	createInitialGoal(String ref);

	/**
	 *  Delete a initial goal.
	 *  @param goal	The initial goal.
	 */
	public void	deleteInitialGoal(IMConfigGoal goal);

	//-------- end goals --------

	/**
	 *  Get all known end goals.
	 *  @return The end goals.
	 */
	public IMConfigGoal[] getEndGoals();

	/**
	 *  Create a end goal.
	 *  @param ref	The name of the referenced goal.
	 *  @return The end goal.
	 */
	public IMConfigGoal	createEndGoal(String ref);

	/**
	 *  Delete a end goal.
	 *  @param goal	The end goal.
	 */
	public void	deleteEndGoal(IMConfigGoal goal);
}
