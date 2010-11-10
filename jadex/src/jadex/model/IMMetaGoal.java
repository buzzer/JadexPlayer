package jadex.model;

/**
 *  A meta goal is instantiated to select among applicable plans.
 */
public interface IMMetaGoal extends IMQueryGoal
{
	//-------- trigger --------

	/**
	 *  Get the trigger of the goal.
	 *  @return The trigger.
	 */
	public IMMetaGoalTrigger	getTrigger();

	/**
	 *  Create new the trigger for the goal.
	 *  @return The trigger.
	 */
	public IMMetaGoalTrigger	createTrigger();

	/**
	 *  Delete the trigger of the goal.
	 */
	public void	deleteTrigger();
}
