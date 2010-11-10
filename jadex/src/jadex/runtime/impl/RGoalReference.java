package jadex.runtime.impl;

import jadex.model.IMCondition;
import jadex.model.IMDeliberation;
import jadex.model.IMGoalReference;
import jadex.model.IMConfigGoal;
import jadex.runtime.GoalEventFilter;
import jadex.runtime.IFilter;

import java.util.Map;

/**
 *  A goal implemented as a reference to another goal.
 */
public class RGoalReference extends RParameterElementReference implements IRGoal
{
	//-------- attributes --------

	/** The parent goal. */
	private RProcessGoal parent;

	//-------- constructor --------

	/**
	 *  Create a new belief.
	 *  @param name The name.
	 *  @param goal The goal model element.
	 *  @param owner The owner.
	 */
	protected RGoalReference(String name, IMGoalReference goal, IMConfigGoal state, RElement owner,
			RReferenceableElement creator)
	{
		super(name, goal, state, owner, creator);
	}
	
	/**
	 *  Perform any necessary clean up, when this element is
	 *  no longer used.
	 *  Subclasses may override this method to do their
	 *  cleanup operations. They must call super.cleanup
	 *  to ensure that the cleanedup property is set.
	 */
	public void	cleanup()
	{
		if(cleanedup)
			return;

		super.cleanup();

		// Hack!!! Should be removed already!?.
		// todo: remove
		RGoalbase	base	= (RGoalbase)getOwner();
		if(base.containsGoal(this))
			base.removeGoal(this);
	}

	//-------- event flags --------

	/**
	 *  Get the post-to-all flag.
	 */
	public boolean isPostToAll()
	{
		return ((IRGoal)getReferencedElement()).isPostToAll();
	}

	/**
	 *  Get the random selection flag.
	 */
	public boolean isRandomSelection()
	{
		return ((IRGoal)getReferencedElement()).isRandomSelection();
	}

	/**
	 *  Set the post-to-all flag.
	 *  @param flag The post-to-all flag.
	 */
	public void setPostToAll(boolean flag)
	{
		((IRGoal)getReferencedElement()).setPostToAll(flag);
	}

	/**
	 *  Set the random selection flag.
	 *  @param flag The random selection flag.
	 */
	public void setRandomSelection(boolean flag)
	{
		((IRGoal)getReferencedElement()).setRandomSelection(flag);
	}

	//-------- BDI flags --------

	/**
	 *  Get the retry flag.
	 */
	public boolean	isRetry()
	{
		return ((IRGoal)getReferencedElement()).isRetry();
	}

	/**
	 *  Set the retry flag.
	 *  @param flag The retry flag.
	 */
	public void	setRetry(boolean flag)
	{
		((IRGoal)getReferencedElement()).setRetry(flag);
	}

	/**
	 *  todo: rename ?!
	 *  Get the retry delay expression (if any).
	 *  @return The retry dely.
	 */
	public long	getRetryDelay()
	{
		return ((IRGoal)getReferencedElement()).getRetryDelay();
	}

	/**
	 *  Set the retry delay (in millis).
	 *  @param delay The delay.
	 */
	public void	setRetryDelay(long delay)
	{
		((IRGoal)getReferencedElement()).setRetryDelay(delay);
	}

	/**
	 *  Get the exclude mode.
	 *  @return The exclude mode.
	 */
	public String	getExcludeMode()
	{
		return ((IRGoal)getReferencedElement()).getExcludeMode();
	}

	/**
	 *  Set the exclude mode.
	 *  @param exclude The exclude mode.
	 */
	public void	setExcludeMode(String exclude)
	{
		((IRGoal)getReferencedElement()).setExcludeMode(exclude);
	}

	/**
	 *  Get the applicable candidates list (apl) recalculation mode.
	 *  @return True, if the apl is recalculated eaach time.
	 */
	public boolean 	isRecalculating()
	{
		return ((IRGoal)getReferencedElement()).isRecalculating();
	}

	/**
	 *  Set the applicable candidates recalculation mode.
	 *  @param recalculate The apl recalculation mode.
	 */
	public void	setRecalculating(boolean recalculate)
	{
		((IRGoal)getReferencedElement()).setRecalculating(recalculate);
	}

	/**
	 *  Get the recur flag.
	 */
	public boolean	isRecur()
	{
		return ((IRGoal)getReferencedElement()).isRecur();
	}

	/**
	 *  Get the recur delay expression (if any).
	 */
	public long	getRecurDelay()
	{
		return ((IRGoal)getReferencedElement()).getRecurDelay();
	}

	/**
	 *  Set the recur flag.
	 *  @param flag The recur flag.
	 */
	public void	setRecur(boolean flag)
	{
		((IRGoal)getReferencedElement()).setRecur(flag);
	}

	/**
	 *  Set the recur delay (in millis).
	 *  @param delay The delay.
	 */
	public void	setRecurDelay(long delay)
	{
		((IRGoal)getReferencedElement()).setRecurDelay(delay);
	}

	//-------- methods --------

	/**
	 *  Get the parent goal from this or other scopes.
	 *  @return The parent goal or null when top-level goal.
	 */
	public RProcessGoal getRealParent()
	{
		return ((IRGoal)getReferencedElement()).getRealParent();
	}

	/**
	 *  Get the parent goal.
	 *  @return The parent goal or null when top-level goal.
	 */
	public RProcessGoal	getParent()
	{
		return this.parent;
	}

	/**
	 *  Set the parent goal.
	 *  @param parent The parent.
	 */
	public void	setParent(RProcessGoal parent)
	{
		assert this.parent==null || parent==null;
		this.parent = parent;
		if(parent!=null)
			setSubgoal(true);

	}

	/**
	 *  Test if a goal is succeeded.
	 *  This has different meanings for the different goal types.
	 *  @return True, if goal is succeeded.
	 */
	public boolean isSucceeded()
	{
		return ((IRGoal)getReferencedElement()).isSucceeded();
	}

	/**
	 *  Test if a goal is failed. All goals that
	 *  have not succeeded (also in process goals) are
	 *  per definition not succeeded (=failed).
	 *  This has different meanings for the different goal types.
	 *  @return True, if goal has failed.
	 */
	public boolean isFailed()
	{
		return ((IRGoal)getReferencedElement()).isFailed();
	}

	/**
	 *  Test if a goal is finished.
	 *  @return True, if goal is finished.
	 */
	public boolean isFinished()
	{
		return ((IRGoal)getReferencedElement()).isFinished();
	}

	/**
	 *  Get the processing state.
	 *  @return True, if the goal is in process.
	 */
	public boolean	isInProcess()
	{
		return ((IRGoal)getReferencedElement()).isInProcess();
	}

	/**
	 *  Check if there have been process goals for this goal.
	 */
	// Todo: replace by APL handling.
	public boolean wasProcessed()
	{
		return ((IRGoal)getReferencedElement()).wasProcessed();
	}

	/**
	 *  Get a property of the goal, e.g.
	 *  flags or a return value.
	 *  @param key The property name.
	 *  @return The property value.
	 * /
	public Object	getAttributeValue(String key)
	{
		// Hack!!! Some properties have to be overriden.
		Object	ret;
		if(RElement.NAME.equals(key))
		{
			ret = getName();
		}
		else if(RElement.FULLNAME.equals(key))
		{
			ret = getFullName();
		}
		else
		{
			ret	= ref.getAttributeValue(key);
		}
		return ret;
	}*/

	/**
	 *  Get the activation state.
	 *  @return True, if the goal is active.
	 */
	public boolean	isActive()
	{
		return ((IRGoal)getReferencedElement()).isActive();
	}

	/**
	 *  Get the option state.
	 *  @return True, if the goal is an option.
	 */
	public boolean	isOption()
	{
		return ((IRGoal)getReferencedElement()).isOption();
	}

	/**
	 *  Check if goal is adopted
	 *  @return True, if the goal is adopted.
	 */
	public boolean	isAdopted()
	{
		return ((IRGoal)getReferencedElement()).isAdopted();
	}

	/**
	 *  Change the lifecycle state of the goal.
	 *  @param newstate	The new lifecycle state.
	 */
	public void	changeLifecycleState(String newstate)
	{
		((IRGoal)getReferencedElement()).changeLifecycleState(newstate);
	}

	/**
	 *  Get the lifecycle state.
	 *  @return The current lifecycle state (e.g. new, active, dropped).
	 */
	public String	getLifecycleState()
	{
		return ((IRGoal)getReferencedElement()).getLifecycleState();
	}

	/**
	 *  Get the processing state.
	 *  @return The current processing state (e.g. in_process, succeeded, ...).
	 */
	public String	getProcessingState()
	{
		return ((IRGoal)getReferencedElement()).getProcessingState();
	}

	/**
	 *  Set the goal's context condition.
	 *  @param mcontext The context condition model.
	 */
	public void setContextCondition(IMCondition mcontext)
	{
		((IRGoal)getReferencedElement()).setContextCondition(mcontext);
	}

	/**
	 *  Set the goal's drop condition.
	 *  @param mdel The drop condition model.
	 */
	public void setDropCondition(IMCondition mdel)
	{
		((IRGoal)getReferencedElement()).setDropCondition(mdel);
	}

	/**
	 *  Check if the goal is the same as another goal
	 *  with respect to uniqueness settings.
	 *  When two goals are the same this does not mean
	 *  the objects are equal() in the Java sense!
	 */
	public boolean	isSame(IRGoal goal)
	{
		return ((IRGoal)getReferencedElement()).isSame(goal);
	}

	/**
	 *  Set the apl.
	 *  @param apl The apl.
	 */
	public void setApplicableCandidateList(ApplicableCandidateList apl)
	{
		((IRGoal)getReferencedElement()).setApplicableCandidateList(apl);
	}

	/**
	 *  Get the apl.
	 *  @return The apl.
	 */
	public ApplicableCandidateList getApplicableCandidateList()
	{
		return ((IRGoal)getReferencedElement()).getApplicableCandidateList();
	}
	
	//-------- abstract goal methods --------

	/**
	 *  Set the result for the goal.
	 *  This is a convenience method, as the goal result
	 *  is stored as property.
	 *  @param result The result.
	 *  todo: replace by in/out parameters.
	 */
	public void	setResult(Object result)
	{
		((IRGoal)getReferencedElement()).setResult(result);
	}

	/**
	 *  Get the result of the goal.
	 *  This is a convenience method, as the goal result
	 *  is stored as property.
	 *  @return The result value.
	 *  todo: replace by in/out parameters.
	 */
	public Object	getResult()
	{
		return ((IRGoal)getReferencedElement()).getResult();
	}

	/**
	 *  Drop this goal.
	 *  Causes all associated process goals
	 *  and subgoals to be dropped.
	 */
	public void drop()
	{
		((IRGoal)getReferencedElement()).drop();
	}

	/**
	 *  Get the deliberation info for a goal.
	 *  @return The deliberation info.
	 */
	public IMDeliberation getDeliberationInfo()
	{
		return ((IMGoalReference)getModelElement()).getDeliberation();
	}

	/**
	 *  Test if this goal is a subgoal.
	 *  Resolves possibly indirect parent.
	 *  @return True, if goal is a subgoal.
	 *  Note, this is not the same as getParent().
	 */
	public boolean isSubgoal()
	{
		return ((IRGoal)getReferencedElement()).isSubgoal();
	}

	/**
	 *  Set if this goal is a subgoal.
	 */
	public void	setSubgoal(boolean subgoal)
	{
		((IRGoal)getReferencedElement()).setSubgoal(subgoal);
	}
	
	/**
	 *  Get the filter to wait for an info event.
	 *  @return The filter.
	 */
	public IFilter getFilter()
	{
		return new GoalEventFilter(getType(), getName(), true);
	}

	/**
	 *  Get the exception (if any).
	 */
	public Exception	getException()
	{
		return ((IRGoal)getReferencedElement()).getException();
	}

	/**
	 *  Generate a property representation for encoding this element
	 *  (eg to SL).
	 *  Method will be overridden by subclasses. When the method
	 *  is invoked it newly fetches several proporties.
	 *  @return A properties object representing this element.
	 */
	public Map getEncodableRepresentation()
	{
		Map rep = super.getEncodableRepresentation();
		//rep.put("content", content);

		if(parent!=null)
		{
			rep.put("parent", parent.getName());
		}

		return rep;
	}

	/**
	 *  Make the goal to an option.
	 */
	public void option()
	{
		((IRGoal)getReferencedElement()).option();
	}

	/**
	 *  Activate the goal.
	 */
	public void activate()
	{
		((IRGoal)getReferencedElement()).activate();
	}

	/**
	 *  Suspend the goal.
	 */
	public void suspend()
	{
		((IRGoal)getReferencedElement()).suspend();
	}
}
