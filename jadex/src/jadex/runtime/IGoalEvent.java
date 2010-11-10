package jadex.runtime;

/**
 *  The interface for all goal events (concrete and referenced).
 */
public interface IGoalEvent extends IEvent
{
	//-------- methods --------

	/**
	 *  Is the event an info (result) event.
	 *  @return True, if it is an info event.
	 */
	public boolean isInfo();

	/**
	 *  Get the goal.
	 *  @return The goal.
	 */
	public IGoal getGoal();
}
