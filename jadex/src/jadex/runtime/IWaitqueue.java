package jadex.runtime;

/**
 *  Interface for the waitqueue of plans. The waitqueue makes
 *  a plan plan responsive to events permanently. 
 */
public interface IWaitqueue
{
	/**
	 *  Get all waitqueue events.
	 *  @return The suitable events.
	 */
	public IEvent[] getEvents();

	/**
	 *  Get all waitqueue events that match the filter.
	 *  Removes the matching events from the waitqueue.
	 *  @param filter The filter.
	 *  @return The suitable events.
	 */
	public IEvent[] getEvents(IFilter filter);

	/**
	 *  Add a message event.
	 *  @param type The type.
	 */
	public void addMessageEvent(String type);

	/**
	 * Add a message event reply.
	 * @param me The message event.
	 */
	public void addReply(IMessageEvent me);

	/**
	 *  Add an internal event.
	 *  @param type The type.
	 */
	public void addInternalEvent(String type);

	/**
	 *  Add a goal.
	 *  @param type The type.
	 */
	public void addGoal(String type);

	/**
	 *  Add a subgoal.
	 *  @param subgoal The subgoal.
	 */
	public void addSubgoal(IGoal subgoal);

	/**
	 *  Add a belief.
	 *  @param type The type.
	 * /
	// Not yet supported, refactoring of waitFor without events needed.
	public void addBelief(String type);*/
	
	/**
	 *  Add a belief.
	 *  @param bel The belief.
	 * /
	// Not yet supported, refactoring of waitFor without events needed.
	public void addBelief(IBelief bel);*/

	/**
	 *  Add a belief set.
	 *  @param type The type.
	 *  @param eventtype The event type.
	 * /
	// Not yet supported, refactoring of waitFor without events needed.
	public void addBeliefSet(String type, String eventtype[]);*/
	
	/**
	 *  Add a belief set.
	 *  @param belset The belief set.
	 *  @param eventtype The event type.
	 * /
	// Not yet supported, refactoring of waitFor without events needed.
	public void addBeliefSet(IBeliefSet belset, String eventtype[]); */

	/**
	 *  Add a user filter.
	 *  @param filter The user filter.
	 */
	public void addFilter(IFilter filter);

	/**
	 *  Add a condition.
	 *  @param condition The condition.
	 * /
	// Not yet supported, refactoring of waitFor without events needed.
	public void addCondition(String condition); */

	/**
	 *  Add a condition.
	 *  @param condition The condition.
	 */
	public void addCondition(ICondition condition);

	//-------- remover methods --------

	/**
	 *  Remove a message event.
	 *  @param type The type.
	 */
	public void removeMessageEvent(String type);

	/**
	 *  Remove a message event reply.
	 *  @param me The message event.
	 */
	public void removeReply(IMessageEvent me);

	/**
	 *  Remove an internal event.
	 *  @param type The type.
	 */
	public void removeInternalEvent(String type);

	/**
	 *  Remove a goal.
	 *  @param type The type.
	 */
	public void removeGoal(String type);

	/**
	 *  Remove a goal.
	 *  @param goal The goal.
	 * /
	public void removeGoal(IRGoal goal)
	{
		removeToComposedFilter(new GoalEventFilter(goal.getType(), goal.getName(), true));
	}*/

	/**
	 *  Remove a subgoal.
	 *  @param subgoal The subgoal.
	 */
	public void removeGoal(IGoal subgoal);

	/**
	 *  Remove a belief.
	 *  @param type The type.
	 * /
	// Not yet supported, refactoring of waitFor without events needed.
	public void removeBelief(String type); */

	/**
	 *  Remove a belief set.
	 *  @param type The type.
	 * /
	// Not yet supported, refactoring of waitFor without events needed.
	public void removeBeliefSet(String type);*/

	/**
	 *  Remove a user filter.
	 *  @param filter The user filter.
	 */
	public void removeFilter(IFilter filter);

	/**
	 *  Remove a condition.
	 *  @param condition The condition.
	 * /
	// Not yet supported, refactoring of waitFor without events needed.
	public void removeCondition(String condition);*/

	/**
	 *  Remove a condition.
	 *  @param condition The condition.
	 */
	public void removeCondition(ICondition condition);
	
	/**
	 *  Get the number of events in the waitqueue.
	 */
	public int	size();

	/**
	 *  Test if the waitqueue is empty.
	 */
	public boolean	isEmpty();
}
