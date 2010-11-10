package jadex.runtime;

/**
 *  The interface for external threads.
 */
public interface IExternalAccess extends ICapability
{
	//-------- goalbase shortcut methods --------

	/**
	 *  Dispatch a new top-level goal.
	 *  @param goal The new goal.
	 *  Note: plan step is interrupted after call.
	 */
	public void dispatchTopLevelGoal(IGoal goal);

	/**
	 *  Create a goal from a template goal.
	 *  To be processed, the goal has to be dispatched as subgoal
	 *  or adopted as top-level goal.
	 *  @param type	The template goal name as specified in the ADF.
	 *  @return The created goal.
	 */
	public IGoal createGoal(String type);

	//-------- eventbase shortcut methods --------

	/**
	 *  Send a message after some delay.
	 *  @param me	The message event.
	 *  @return The filter to wait for an answer.
	 */
	public IFilter	sendMessage(IMessageEvent me);

	/**
	 *  Dispatch an internal event.
	 *  @param event The event.
	 *  Note: plan step is interrupted after call.
	 */
	public void dispatchInternalEvent(IInternalEvent event);

	/**
	 *  Create a new message event.
	 *  @return The new message event.
	 */
	public IMessageEvent createMessageEvent(String type);

	/**
	 *  Create a new intenal event.
	 *  @return The new intenal event.
	 */
	public IInternalEvent createInternalEvent(String type);

	/**
	 *  Create a new intenal event.
	 *  @return The new intenal event.
	 *  @deprecated Convenience method for easy conversion to new explicit internal events.
	 *  Will be removed in later releases.
	 */
	public IInternalEvent createInternalEvent(String type, Object content);

	//-------- methods --------

	/**
	 *  Wait for a some time.
	 *  @param duration The duration.
	 */
	public void	waitFor(long duration);

	/**
	 *  Wait for a condition to be satisfied.
	 *  @param condition The condition.
	 */
	public void	waitForCondition(ICondition condition);

	/**
	 *  Wait for a condition or until the timeout occurs.
	 *  @param condition The condition.
	 *  @param timeout The timeout.
	 */
	public void waitForCondition(ICondition condition, long timeout);

	/**
	 *  Wait for a condition to be satisfied.
	 *  @param condition The condition.
	 */
	public void	waitForCondition(String condition);

	/**
	 *  Wait for a condition to be satisfied.
	 *  @param condition The condition.
	 */
	public void	waitForCondition(String condition, long timeout);

	/**
	 *  Wait for an internal event.
	 *  @param type The internal event type.
	 */
	public IInternalEvent waitForInternalEvent(String type);

	/**
	 *  Wait for an internal event.
	 *  @param type The internal event type.
	 *  @param timeout The timeout.
	 */
	public IInternalEvent waitForInternalEvent(String type, long timeout);

	/**
	 *  Send a message and wait for the answer.
	 *  @param me The message event.
	 *  @return The result event.
	 */
	public IMessageEvent sendMessageAndWait(IMessageEvent me);

	/**
	 *  Send a message and wait for the answer.
	 *  Adds a reply-with entry if not present, for tracking the conversation.
	 *  @param me The message event.
	 *  @param timeout The timeout.
	 *  @return The result event.
	 */
	public IMessageEvent sendMessageAndWait(IMessageEvent me, long timeout);

	/**
	 *  Wait for a message event.
	 *  @param type The message event type.
	 */
	public IMessageEvent waitForMessageEvent(String type);

	/**
	 *  Wait for a message event.
	 *  @param type The message event type.
	 *  @param timeout The timeout.
	 */
	public IMessageEvent waitForMessageEvent(String type, long timeout);

	/**
	 *  Wait for a message.
	 *  @param msgevent The message event.
	 */
	public IMessageEvent	waitForReply(IMessageEvent msgevent);

	/**
	 *  Wait for a message.
	 *  @param msgevent The message event.
	 */
	public IMessageEvent	waitForReply(IMessageEvent msgevent, long timeout);

	/**
	 *  Wait for a goal.
	 *  @param type The goal type.
	 */
	public void waitForGoal(String type);

	/**
	 *  Wait for a goal.
	 *  @param type The goal type.
	 *  @param timeout The timeout.
	 */
	public void waitForGoal(String type, long timeout);

	/**
	 *  Wait for a belief change.
	 *  @param type The internal event type.
	 *  @return The new fact.
	 */
	public Object waitForBeliefChange(String type);

	/**
	 *  Wait for a belief change.
	 *  @param type The belief type.
	 *  @param timeout The timeout.
	 */
	public Object waitForBeliefChange(String type, long timeout);

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 */
	public void waitForBeliefSetChange(String type);

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 */
	public void waitForBeliefSetChange(String type, long timeout);

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 */
	public Object waitForFactAddedOrRemoved(String type);

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  @param timeout The timeout.
	 *  todo: returns a condition triggered event? or new BeliefChanged event?
	 */
	public Object waitForFactAddedOrRemoved(String type, long timeout);

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  @return The fact that was added.
	 */
	public Object waitForFactAdded(String type);

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  @param timeout The timeout.
	 *  @return The fact that was added.
	 */
	public Object waitForFactAdded(String type, long timeout);

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  @return The fact that was added.
	 */
	public Object waitForFactRemoved(String type);

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  @param timeout The timeout.
	 *  @return The fact that was added.
	 */
	public Object waitForFactRemoved(String type, long timeout);

	/**
	 *  Dispatch a top level goal and wait for the result.
	 *  @param goal The goal.
	 */
	public void dispatchTopLevelGoalAndWait(IGoal goal);

	/**
	 *  Dispatch a top level goal and wait for the result.
	 *  @param goal The goal.
	 */
	public void dispatchTopLevelGoalAndWait(IGoal goal, long timeout);
	
	/**
	 *  Wait for the agent to terminate.
	 */
	public void waitForAgentTerminating();

	/**
	 *  Wait for the agent to terminate.
	 */
	public void waitForAgentTerminating(long timeout);
}
