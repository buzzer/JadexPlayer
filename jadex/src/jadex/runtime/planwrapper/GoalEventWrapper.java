package jadex.runtime.planwrapper;

import jadex.runtime.planwrapper.EventWrapper;
import jadex.runtime.*;
import jadex.runtime.impl.*;


/**
 *  The user level view on a goal event.
 *  Methods can only be called from plans,
 *  otherwise exceptions are thrown.
 */
public class GoalEventWrapper	extends EventWrapper	implements IGoalEvent
{
	//--------attributes --------

	/** The original event. */
	protected IRGoalEvent	event;

	//-------- constructors --------

	/**
	 *  Create a new GoalEventWrapper.
	 *  @param event	The original event.
	 */
	protected GoalEventWrapper(IRGoalEvent event)
	{
		super(event);
		this.event	= event;
	}

	//-------- methods --------

	/**
	 *  Is the event an info (result) event.
	 *  @return True, if it is an info event.
	 */
	public boolean isInfo()
	{
		checkThreadAccess();
		return event.isInfo();
	}

	/**
	 *  Get the goal.
	 *  @return The goal.
	 */
	public IGoal getGoal()
	{
		checkThreadAccess();
		return new GoalWrapper(event.getGoal());
	}
}
