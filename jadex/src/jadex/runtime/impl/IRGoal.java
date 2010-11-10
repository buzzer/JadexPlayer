package jadex.runtime.impl;

import jadex.model.*;
import jadex.runtime.IFilter;

/**
 *  The supertype for all goals (concrete and referenced)
 *  and all goal types (perform, achieve, get, maintain).
 */
public interface IRGoal extends IRParameterElement
{
	//-------- constants --------

	/*  The constants for accessing attributes via the IAttributeAccess interface.
	    The keys are intentionally set to long names to prevent name conflicts
	    with user properties. */

	/** The goal kind (achieve, maintain, perform). */
	public static final String KIND 	= "kind";
	
	/** The goal type (modelname of the goal). */
	public static final String TYPE 	= "type";
	
	/** The goal parent. */
	public static final String PARENT 	= "parent";
	
	/** The goal finished state. */
	public static final String FINISHED 	= "finished";
	
	/** The goal class. */
	public static final String CLASS 	= "class";


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
	 *  Set the retry flag.
	 *  @param flag The retry flag.
	 */
	public void	setRetry(boolean flag);

	/**
	 *  todo: rename ?!
	 *  Get the retry delay expression (if any).
	 */
	public long	getRetryDelay();

	/**
	 *  Set the retry delay (in millis).
	 *  @param delay The delay.
	 */
	public void	setRetryDelay(long delay);

	/**
	 *  Get the exclude mode.
	 *  @return The exclude mode.
	 */
	public String	getExcludeMode();

	/**
	 *  Set the exclude mode.
	 *  @param exclude The exclude mode.
	 */
	public void	setExcludeMode(String exclude);

	/**
	 *  Get the applicable candidates list (apl) recalculation mode.
	 *  @return True, if the apl is recalculated eaach time.
	 */
	public boolean 	isRecalculating();

	/**
	 *  Set the applicable candidates recalculation mode.
	 *  @param recalculate The apl recalculation mode.
	 */
	public void	setRecalculating(boolean recalculate);


	/**
	 *  Get the recur flag.
	 */
	public boolean	isRecur();

	/**
	 *  Get the recur delay expression (if any).
	 */
	public long	getRecurDelay();

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
	 *  Get the parent goal from this or other scopes.
	 *  @return The parent goal or null when top-level goal.
	 */
	public RProcessGoal	getRealParent();

	/**
	 *  Get the parent goal.
	 *  @return The parent goal or null when no parent in this capability.
	 */
	public RProcessGoal	getParent();

	/**
	 *  Set the parent goal.
	 *  @param parent The parent.
	 */
	public void	setParent(RProcessGoal parent);
 
	/**
	 *  Get the activation state.
	 *  @return True, if the goal is active.
	 */
	public boolean	isActive();

	/**
	 *  Get the option state.
	 *  @return True, if the goal is an option.
	 */
	public boolean	isOption();

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
	 *  Get the processing state.
	 *  @return The current processing state (e.g. in_process, succeeded, ...).
	 */
	public String	getProcessingState();

	/**
	 *  Change the lifecycle state of the goal.
	 *  @param newstate	The new lifecycle state.
	 */
	public void	changeLifecycleState(String newstate);

	/**
	 *  Set the goal's context condition.
	 *  @param mcontext The context condition model.
	 */
	public void setContextCondition(IMCondition mcontext);

	/**
	 *  Set the goal's drop condition.
	 *  @param mdel The drop condition model.
	 */
	public void setDropCondition(IMCondition mdel);

	/**
	 *  Drop this goal.
	 *  Causes all associated process goals
	 *  and subgoals to be dropped.
	 */
	public void drop();

	/**
	 *  Test if a goal is succeeded.
	 *  This has different meanings for the different goal types.
	 *  @return True, if goal is succeeded.
	 */
	public boolean isSucceeded();

	/**
	 *  Test if a goal is finished.
	 *  @return True, if goal is finished.
	 */
	public boolean isFinished();

	/**
	 *  Test if a goal is failed.
	 *  This has different meanings for the different goal types.
	 *  @return True, if goal has failed.
	 */
	public boolean isFailed();

	/**
	 *  Get the processing state.
	 *  @return True, if the goal is in process.
	 */
	public boolean	isInProcess();

	/**
	 *  Check if the goal is the same as another goal
	 *  with respect to uniqueness settings.
	 *  When two goals are the same this does not mean
	 *  the objects are equal() in the Java sense!
	 */
	public boolean	isSame(IRGoal goal);

//	/**
//	 *  Check if the goal inhibits another goal.
//	 *  @param when_active	Test if the goal inhibits another goal when active.
//	 *  @param when_process	Test if the goal inhibits another goal when in process.
//	 */
//	public boolean	inhibits(IRGoal goal, boolean when_active, boolean when_process);

	//-------- abstract goal methods --------

	/**
	 *  Get the goal type.
	 *  @return The goal type.
	 */
	public String	getType();

	//-------- parameter handling --------

	/**
	 *  Set the result for the goal.
	 *  This is a convenience method, as the goal result
	 *  is stored as property.
	 *  @param result The result.
	 *  todo: remove?
	 */
	public void	setResult(Object result);

	/**
	 *  Get the result of the goal.
	 *  This is a convenience method, as the goal result
	 *  is stored as property.
	 *  @return The result value.
	 *  todo: remove?
	 */
	public Object	getResult();

	/**
	 *  Get the deliberation info for a goal.
	 *  @return The deliberation info.
	 */
	public IMDeliberation getDeliberationInfo();

	/**
	 *  Test if this goal is (or was) a subgoal.
	 */
	public boolean isSubgoal();

	/**
	 *  Set if this goal is a subgoal.
	 */
	public void	setSubgoal(boolean subgoal);

	/**
	 *  Get the filter to wait for an info event.
	 *  @return The filter.
	 */
	public IFilter getFilter();

	/**
	 *  Get the exception (if any).
	 */
	public Exception	getException();

	/**
	 *  Check if there have been process goals for this goal.
	 */
	// Todo: replace by APL handling.
	public boolean wasProcessed();

	/**
	 *  Set the apl.
	 *  @param apl The apl.
	 */
	public void setApplicableCandidateList(ApplicableCandidateList apl);

	/**
	 *  Get the apl.
	 *  @return The apl.
	 */
	public ApplicableCandidateList getApplicableCandidateList();

	/**
	 *  Make the goal to an option.
	 */
	public void option();

	/**
	 *  Activate the goal.
	 */
	public void activate();

	/**
	 *  Suspend the goal.
	 */
	public void suspend();
}
