package jadex.runtime.planwrapper;

import jadex.model.ISystemEventTypes;
import jadex.runtime.*;
import jadex.runtime.externalaccesswrapper.ElementWrapper.AgentInvocation;
import jadex.runtime.impl.*;
import jadex.util.Tuple;

/**
 *  The wrapper for all goal types (perform, achieve, query, maintain).
 */
public class GoalWrapper	extends ParameterElementWrapper implements IGoal
{
	//-------- attributes --------

	/** The original goal. */
	protected IRGoal goal;

	//-------- constructors --------

	/**
	 *  Create a new goal wrapper.
	 */
	public GoalWrapper(IRGoal goal)
	{
		super(goal);
		this.goal = goal;
	}

	//-------- methods --------

	/**
	 * Get the post-to-all flag.
	 */
	public boolean isPostToAll()
	{
		checkThreadAccess();
		return goal.isPostToAll();
	}

	/**
	 * Get the random selection flag.
	 */
	public boolean isRandomSelection()
	{
		checkThreadAccess();
		return goal.isRandomSelection();
	}

	/**
	 * Set the post-to-all flag.
	 * @param flag The post-to-all flag.
	 */
	public void setPostToAll(boolean flag)
	{
		checkThreadAccess();
		goal.setPostToAll(flag);
	}

	/**
	 * Set the random selection flag.
	 * @param flag The random selection flag.
	 */
	public void setRandomSelection(boolean flag)
	{
		checkThreadAccess();
		goal.setRandomSelection(flag);
	}

	/**
	 * Get the retry flag.
	 */
	public boolean isRetry()
	{
		checkThreadAccess();
		return goal.isRetry();
	}

	/**
	 * Get the retry delay expression (if any).
	 */
	public long getRetryDelay()
	{
		checkThreadAccess();
		return goal.getRetryDelay();
	}

	/**
	 * Get the exclude mode.
	 * @return The exclude mode.
	 */
	public String getExcludeMode()
	{
		checkThreadAccess();
		return goal.getExcludeMode();
	}

	/**
	 * Set the retry flag.
	 * @param flag The retry flag.
	 */
	public void setRetry(boolean flag)
	{
		checkThreadAccess();
		goal.setRetry(flag);
	}

	/**
	 * Set the retry delay (in millis).
	 * @param delay The delay.
	 */
	public void setRetryDelay(long delay)
	{
		checkThreadAccess();
		goal.setRetryDelay(delay);
	}

	/**
	 * Set the exclude mode.
	 * @param exclude The exclude mode.
	 * @see IGoal for constants.
	 */
	public void setExcludeMode(String exclude)
	{
		checkThreadAccess();
		goal.setExcludeMode(exclude);
	}

	/**
	 *  Get the recur flag.
	 */
	public boolean	isRecur()
	{
		checkThreadAccess();
		return goal.isRecur();
	}

	/**
	 *  Get the recur delay expression (if any).
	 */
	public long	getRecurDelay()
	{
		checkThreadAccess();
		return goal.getRecurDelay();
	}

	/**
	 *  Set the recur flag.
	 *  @param flag The recur flag.
	 */
	public void	setRecur(boolean flag)
	{
		checkThreadAccess();
		goal.setRecur(flag);
	}

	/**
	 *  Set the recur delay (in millis).
	 *  @param delay The delay.
	 */
	public void	setRecurDelay(long delay)
	{
		checkThreadAccess();
		goal.setRecurDelay(delay);
	}

	/**
	 * Get the activation state.
	 * @return True, if the goal is active.
	 */
	public boolean isActive()
	{
		checkThreadAccess();
		return goal.isActive();
	}

	/**
	 * Check if goal is adopted
	 * @return True, if the goal is adopted.
	 */
	public boolean isAdopted()
	{
		checkThreadAccess();
		return goal.isAdopted();
	}

	/**
	 * Get the lifecycle state.
	 * @return The current lifecycle state (e.g. new, active, dropped).
	 */
	public String getLifecycleState()
	{
		checkThreadAccess();
		return goal.getLifecycleState();
	}

	/**
	 *  Test if a goal is finished.
	 *  @return True, if goal is finished.
	 */
	public boolean isFinished()
	{
		checkThreadAccess();
		return goal.isFinished();
	}

	/**
	 *  Test if a goal is succeeded.
	 *  This has different meanings for the different goal types.
	 *  @return True, if goal is succeeded.
	 */
	public boolean isSucceeded()
	{
		checkThreadAccess();
		return goal.isSucceeded();
	}

	/**
	 *  Test if a goal is failed.
	 *  This has different meanings for the different goal types.
	 *  @return True, if goal has failed.
	 */
	public boolean isFailed()
	{
		checkThreadAccess();
		return goal.isFailed();
	}

	/**
	 *  Drop this goal.
	 *  Causes all associated process goals
	 *  and subgoals to be dropped.
	 */
	public void drop()
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{goal.drop();}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Get the exception (if any).
	 *  When the goal has failed, the exception can be inspected.
	 *  If more than one plan has been executed for a goal
	 *  only the last exception will be available.
	 */
	public Exception	getException()
	{
		checkThreadAccess();
		return goal.getException();
	}
	
	//-------- parameter handling --------

	/**
	 *  Get the result of the goal.
	 *  This is a convenience method, as the goal result
	 *  is stored as property.
	 *  @return The result value.
	 *  @deprecated
	 */
	public Object	getResult()
	{
		checkThreadAccess();
		return goal.getResult();
	}

	/**
	 *  Set the result for the goal.
	 *  This is a convenience method, as the goal result
	 *  is stored as property.
	 *  @param result The result.
	 *  @deprecated
	 */
	public void	setResult(Object result)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{goal.setResult(result);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Get the filter to wait for an info event.
	 *  @return The filter.
	 */
	public IFilter getFilter()
	{
		checkThreadAccess();
		return goal.getFilter();
	}

	//-------- abstract goal methods --------

	/**
	 *  Get the goal type.
	 *  @return The goal type.
	 */
	public String	getType()
	{
		checkThreadAccess();
		return goal.getType();
	}
	
	//-------- listeners --------
	
	/**
	 *  Add a goal listener.
	 *  @param listener The goal listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addGoalListener(IGoalListener userlistener, boolean async)
	{
		checkThreadAccess();
		IFilter filter = new SystemEventFilter(new String[]{ISystemEventTypes.GOAL_REMOVED}, unwrap());
		final AsynchronousSystemEventListener listener 
			= new AsynchronousSystemEventListener(userlistener, new Tuple(userlistener, goal));
		getCapability().addSystemEventListener(listener, filter, true, async);
	}
	
	/**
	 *  Remove a goal listener.
	 *  @param listener The goal listener.
	 */
	public void removeGoalListener(IGoalListener userlistener)
	{
		checkThreadAccess();
		Object	identifier	= new Tuple(userlistener, goal);
		ISystemEventListener[] listeners = getAgent().getSystemEventListeners();
		for(int i=0; i<listeners.length; i++)
		{
			if((listeners[i] instanceof AsynchronousSystemEventListener) 
				&& ((AsynchronousSystemEventListener)listeners[i]).getIdentifier().equals(identifier))
			{
				getCapability().removeSystemEventListener(listeners[i]);
				break;
			}
		}
	}
}
