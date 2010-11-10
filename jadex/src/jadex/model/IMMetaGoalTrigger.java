package jadex.model;


/**
 *  The meta goal trigger identifier elements which trigger activation of a meta goal.
 */
public interface IMMetaGoalTrigger extends IMTrigger
{
	//-------- goals --------

	/**
	 *  Get the goals.
	 *  @return The goals.
	 */
	public IMReference[] getGoals();

	/**
	 *  Create a goal.
	 *  @param ref	The referenced goal.
	 *  @return The new goal.
	 */
	public IMReference createGoal(String ref);

	/**
	 *  Delete a goal.
	 *  @param ref	The goal.
	 */
	public void deleteGoal(IMReference ref);
	
}
