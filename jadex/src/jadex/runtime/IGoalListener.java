package jadex.runtime;

/**
 *  Listener for observing goals.
 */
public interface IGoalListener
{
	/**
	 *  Invoked when a new goal has been added.
	 *  @param ae The agent event.
	 */
	public void goalAdded(AgentEvent ae);
	
	/**
	 *  Invoked when a goal has been finished.
	 *  @param ae The agent event.
	 */
	public void goalFinished(AgentEvent ae);
	
	// todo: goal state changed? goal created, i.e. adopted
}
