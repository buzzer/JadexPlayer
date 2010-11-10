package jadex.runtime;

import jadex.model.IMGoal;

/**
 *  The supertype for all goals (concrete and referenced)
 *  and all goal types (perform, achieve, get, maintain).
 */
public interface IGoal	extends IParameterElement
{
	//-------- constants --------

	/** Never exclude plan candidates from apl. */
	public static final Object EXCLUDE_NEVER = IMGoal.EXCLUDE_NEVER;

	/** Exclude failed plan candidates from apl. */
	public static final Object EXCLUDE_WHEN_FAILED = IMGoal.EXCLUDE_WHEN_FAILED;

	/** Exclude succeeded plan candidates from apl. */
	public static final Object EXCLUDE_WHEN_SUCCEEDED = IMGoal.EXCLUDE_WHEN_SUCCEEDED;

	/** Exclude tried plan candidates from apl. */
	public static final Object EXCLUDE_WHEN_TRIED = IMGoal.EXCLUDE_WHEN_TRIED;

	/* The lifecycle states. */
	
	/** The lifecycle state "new" (just created). */
	public static final String	LIFECYCLESTATE_NEW	= "new";

	/** The lifecycle state "adopted" (adopted, but not active). */
	public static final String	LIFECYCLESTATE_ADOPTED	= "adopted";

	/** The lifecycle state "option" (adopted, but not active). */
	public static final String	LIFECYCLESTATE_OPTION	= "option";

	/** The lifecycle state "active" (adopted and processed or monitored). */
	public static final String	LIFECYCLESTATE_ACTIVE	= "active";

	/** The lifecycle state "active" (adopted and processed or monitored). */
	public static final String	LIFECYCLESTATE_SUSPENDED	= "suspended";

	/** The lifecycle state "dropping" (just before finished, but still dropping its subgoals). */
	public static final String	LIFECYCLESTATE_DROPPING	= "dropping";

	/** The lifecycle state "dropped" (goal and all subgoals finished). */
	public static final String	LIFECYCLESTATE_DROPPED	= "dropped";

	//-------- event flags --------

	/**
	 *  Get the post-to-all flag.
	 */
	public boolean isPostToAll();

	/**
	 *  Get the random selection flag.
	 */
	public boolean isRandomSelection();

	/**
	 *  Set the post-to-all flag.
	 *  @param flag The post-to-all flag.
	 */
	public void setPostToAll(boolean flag);

	/**
	 *  Set the random selection flag.
	 *  @param flag The random selection flag.
	 */
	public void setRandomSelection(boolean flag);

	//-------- BDI flags --------

	/**
	 *  Get the retry flag.
	 */
	public boolean	isRetry();

	/**
	 *  Get the retry delay expression (if any).
	 */
	public long	getRetryDelay();

	/**
	 *  Get the exclude mode.
	 *  @return The exclude mode.
	 */
	public String	getExcludeMode();

	/**
	 *  Get the recur flag.
	 */
	public boolean	isRecur();

	/**
	 *  Get the recur delay expression (if any).
	 */
	public long	getRecurDelay();

	/**
	 *  Set the retry flag.
	 *  @param flag The retry flag.
	 */
	public void	setRetry(boolean flag);

	/**
	 *  Set the retry delay (in millis).
	 *  @param delay The delay.
	 */
	public void	setRetryDelay(long delay);

	/**
	 *  Set the exclude mode.
	 *  @param exclude The exclude mode.
	 */
	public void	setExcludeMode(String exclude);

	/**
	 *  Set the recur flag.
	 *  @param flag The recur flag.
	 */
	public void	setRecur(boolean flag);

	/**
	 *  Set the recur delay (in millis).
	 *  @param delay The delay.
	 */
	public void	setRecurDelay(long delay);

	//-------- methods --------

	/**
	 *  Get the activation state.
	 *  @return True, if the goal is active.
	 */
	public boolean	isActive();

	/**
	 *  Check if goal is adopted
	 *  @return True, if the goal is adopted.
	 */
	public boolean	isAdopted();

	/**
	 *  Get the lifecycle state.
	 *  @return The current lifecycle state (e.g. new, active, dropped).
	 */
	public String	getLifecycleState();

	/**
	 *  Test if a goal is finished.
	 *  @return True, if goal is finished.
	 */
	public boolean isFinished();

	/**
	 *  Test if a goal is succeeded.
	 *  This has different meanings for the different goal types.
	 *  @return True, if goal is succeeded.
	 */
	public boolean isSucceeded();

	/**
	 *  Test if a goal is failed.
	 *  This has different meanings for the different goal types.
	 *  @return True, if goal has failed.
	 */
	public boolean isFailed();

	/**
	 *  Drop this goal.
	 *  Causes all associated process goals
	 *  and subgoals to be dropped.
	 */
	public void drop();

	/**
	 *  Get the goal type.
	 *  @return The goal type.
	 */
	public String	getType();

	/**
	 *  Get the exception (if any).
	 *  When the goal has failed, the exception can be inspected.
	 *  If more than one plan has been executed for a goal
	 *  only the last exception will be available.
	 */
	public Exception	getException();
	
	//-------- parameter handling --------

	/**
	 *  Set the result for the goal.
	 *  This is a convenience method, as the goal result
	 *  is stored as property.
	 *  @param result The result.
	 *  @deprecated
	 */
	public void	setResult(Object result);

	/**
	 *  Get the result of the goal.
	 *  This is a convenience method, as the goal result
	 *  is stored as property.
	 *  @return The result value.
	 *  @deprecated
	 */
	public Object	getResult();

	/**
	 *  Get the filter to wait for an info event.
	 *  @return The filter.
	 */
	public IFilter getFilter();
	
	//-------- listeners --------
	
	/**
	 *  Add a goal listener.
	 *  @param listener The goal listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addGoalListener(IGoalListener listener, boolean async);
	
	/**
	 *  Remove a goal listener.
	 *  @param listener The goal listener.
	 */
	public void removeGoalListener(IGoalListener listener);
}
