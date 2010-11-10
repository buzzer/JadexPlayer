package jadex.runtime;

/**
 *  Listener for observing belief sets.
 */
public interface IBeliefSetListener
{
	/**
	 *  Invoked when a belief set has been changed completely.
	 *  @param ae The agent event.
	 */ 
	public void beliefSetChanged(AgentEvent ae);

	/**
	 *  Invoked when a fact has been added.
	 *  The new fact is contained in the agent event.
	 *  @param ae The agent event.
	 */
	public void factAdded(AgentEvent ae);

	/**
	 *  Invoked when a fact has been removed.
	 *  The removed fact is contained in the agent event.
	 *  @param ae The agent event.
	 */
	public void factRemoved(AgentEvent ae);
}
