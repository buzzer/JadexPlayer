package jadex.model;

/**
 *  The trigger references events or goals.
 */
public interface IMTrigger	extends IMElement
{
	//-------- internal events --------

	/**
	 *  Get the internal events.
	 *  @return The internal events.
	 */
	public IMReference[] getInternalEvents();

	/**
	 *  Create an internal event.
	 *  @param ref	The referenced internal event.
	 *  @return The new internal event.
	 */
	public IMReference createInternalEvent(String ref);

	/**
	 *  Delete an internal event.
	 *  @param ref	The internal event.
	 */
	public void deleteInternalEvent(IMReference ref);


	//-------- goals --------

	/**
	 *  Get the goals.
	 *  @return The goals.
	 */
	public IMReference[] getGoalFinisheds();

	/**
	 *  Create a goal.
	 *  @param ref	The referenced goal.
	 *  @return The new goal.
	 */
	public IMReference createGoalFinished(String ref);

	/**
	 *  Delete a goal.
	 *  @param ref	The goal.
	 */
	public void deleteGoalFinished(IMReference ref);
	

	//-------- message events --------

	/**
	 *  Get the message events.
	 *  @return The message events.
	 */
	public IMReference[] getMessageEvents();

	/**
	 *  Create an message event.
	 *  @param ref	The referenced message event.
	 *  @return The new message event.
	 */
	public IMReference createMessageEvent(String ref);

	/**
	 *  Delete an message event.
	 *  @param ref	The message event.
	 */
	public void deleteMessageEvent(IMReference ref);

	
	//-------- filter --------

	/**
	 *  Get the filter.
	 *  @return The filter.
	 */
	public IMExpression	getFilter();

	/**
	 *  Create the filter.
	 *  @param expression	The filter expression.
	 *  @return The new filter.
	 */
	public IMExpression	createFilter(String expression);

	/**
	 *  Delete the filter.
	 */
	public void deleteFilter();
}
