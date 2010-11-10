package jadex.runtime.impl;

import jadex.model.*;
import jadex.util.*;
import jadex.util.collection.*;
import jadex.runtime.*;
import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.agenda.goals.*;
import jadex.runtime.impl.agenda.easydeliberation.*;
import java.util.*;

/**
 *  This is the runtime object for a goal, that
 *  represents the goal creators goal view.
 *  There are several ways bring goals into life:<ul>
 *  <li> creation condition.
 *  <li> dispatch a top-level goal.<br>
 *  <li> dispatch a subgoal.<br>
 *  <li> initial goal (addTopLevelGoal() is called from goalbase.init).<br>
 *  </ul>
 *
 *  The goals lifecycle is as follows:
 *  new -> adopted (option | suspended | active) -> dropped
 */
public abstract class RGoal extends RParameterElement implements IRGoal
{
	//-------- internal constants --------

	/** Flag to indicate if deliberation is enabled. */
	protected static final boolean	DELIBERATION	= true;

	/** The allowed state transitions (oldstate mapped to collection of new states). */
	protected static final MultiCollection	transitions	= new MultiCollection();

	static
	{
		// Add allowed state transitions to transition table.
		transitions.put(IGoal.LIFECYCLESTATE_NEW,	IGoal.LIFECYCLESTATE_ADOPTED);
		transitions.put(IGoal.LIFECYCLESTATE_ADOPTED,	IGoal.LIFECYCLESTATE_OPTION);
		transitions.put(IGoal.LIFECYCLESTATE_ADOPTED,	IGoal.LIFECYCLESTATE_SUSPENDED);
		transitions.put(IGoal.LIFECYCLESTATE_ADOPTED,	IGoal.LIFECYCLESTATE_DROPPING);
		transitions.put(IGoal.LIFECYCLESTATE_OPTION,	IGoal.LIFECYCLESTATE_ACTIVE);
		transitions.put(IGoal.LIFECYCLESTATE_OPTION,	IGoal.LIFECYCLESTATE_SUSPENDED);
		transitions.put(IGoal.LIFECYCLESTATE_OPTION,	IGoal.LIFECYCLESTATE_DROPPING);
		transitions.put(IGoal.LIFECYCLESTATE_ACTIVE,	IGoal.LIFECYCLESTATE_OPTION);
		transitions.put(IGoal.LIFECYCLESTATE_ACTIVE,	IGoal.LIFECYCLESTATE_SUSPENDED);
		transitions.put(IGoal.LIFECYCLESTATE_ACTIVE,	IGoal.LIFECYCLESTATE_DROPPING);
		transitions.put(IGoal.LIFECYCLESTATE_SUSPENDED,	IGoal.LIFECYCLESTATE_OPTION);
		transitions.put(IGoal.LIFECYCLESTATE_SUSPENDED,	IGoal.LIFECYCLESTATE_DROPPING);
		transitions.put(IGoal.LIFECYCLESTATE_DROPPING,	IGoal.LIFECYCLESTATE_DROPPED);
	}

	//-------- attributes --------

	/** The parent goal. */
	protected RProcessGoal parent;
  
	/**  The element (plan) that created this goal */
	protected RElement cause;

	/** The set of currently active process goals. */
	protected Set	processgoals;

	/** The context condition. */
	protected RCondition context;

	/** The drop condition. */
	protected RCondition drop;

	/** The lifecycle state (e.g. new, active, dropped). */
	protected String	lifecyclestate;

	/** True, when the goal is dropping. */
	protected boolean dropping;

	/** True, when the goal is (or was) a subgoal. */
	protected boolean subgoal;

	/** The actual time table entry. */
	protected TimetableData retryentry;

	/** The exception (if any). */
	protected Exception exception;

	/** The APL. todo: where should the apl be stored? */
	protected ApplicableCandidateList apl;
	
	//-------- properties (also exhibited by events) --------

	/** Allow parallel dispatching to many plans. */
	protected Boolean posttoall;

	/** Allow random candidate selection. */
	protected Boolean random;

	//-------- BDI flags --------

	/** Retry to achieve the goal, when a plan fails.
	  todo: rename. iteration? */
	protected Boolean	retry;

	/** Wait for the given time (in millis) before retrying the goal
	  * after is has failed. */
	/* todo: rename?. */
	protected Long	retrydelay;

	/** Exclude failed plan when chosing the next candidate. */
	protected String	excludemode;

	/** The recalculate apl state. */
	protected Boolean recalculate;

	/** The recur flag. */
	protected Boolean recur;

	/** Wait for the given time (in millis) before recurring the goal
	  * after is has failed. */
	protected Long	recurdelay;

	//-------- additional things --------

	/** Precondition for testing if finished. */
	protected IAgendaActionPrecondition prec_notfinished;

	/** The recur time table entry. */
	private TimetableData recurentry;

	//-------- constructor --------

	/**
	 *  Create a new goal.
	 * @param name The name.
	 * @param goal The model element.
	 * @param config The configuration.
	 * @param owner The owner.
	 * @param binding The binding.
	 */
	protected RGoal(String name, IMGoal goal, IMConfigGoal config, RElement owner,
			RReferenceableElement creator, Map binding)
	{
		super(name, goal, config, owner, creator, binding);
		//System.out.println("Created: "+this);

		prec_notfinished = new GoalNotFinishedPrecondition(this);

		//this.excludeset = SCollection.createHashSet();
		this.lifecyclestate	= IGoal.LIFECYCLESTATE_NEW;

		// Make goal accessible for expressions.
		this.setExpressionParameter("$goal", this);
		
		setContextCondition(goal.getContextCondition());
		setDropCondition(goal.getDropCondition());

		setParameterProtectionMode(ACCESS_PROTECTION_INIT);
    
		setCause(getScope().getAgent().getCurrentPlan());
	}

	/**
	 *  Perform any necessary clean up, when this element is
	 *  no longer used.
	 *  Subclasses may override this method to do their
	 *  cleanup operations.
	 */
	public void	cleanup()
	{
		if (cleanedup)
		{
			return;
		}

		super.cleanup();

		// Cleanup conditions (if any).
		if (this.drop != null)
		{
			this.drop.cleanup();
		}
		if (this.context != null)
		{
			this.context.cleanup();
		}
	}

	//-------- event flags --------

	/**
	 *  Get the post-to-all flag.
	 */
	public boolean isPostToAll()
	{
		return posttoall != null ? posttoall.booleanValue()
				: ((IMGoal)getModelElement()).isPostToAll();
	}

	/**
	 *  Get the random selection flag.
	 */
	public boolean isRandomSelection()
	{
		return random != null ? random.booleanValue()
				: ((IMGoal)getModelElement()).isRandomSelection();
	}

	/**
	 *  Set the post-to-all flag.
	 *  @param flag The post-to-all flag.
	 */
	public void setPostToAll(boolean flag)
	{
		this.posttoall = flag ? Boolean.TRUE : Boolean.FALSE;	// Boolean.valueOf(flag);	// since 1.4
	}

	/**
	 *  Set the random selection flag.
	 *  @param flag The random selection flag.
	 */
	public void setRandomSelection(boolean flag)
	{
		this.random = flag ? Boolean.TRUE : Boolean.FALSE;	// Boolean.valueOf(flag);	// since 1.4
	}

	//-------- BDI flags --------

	/**
	 *  Get the retry flag.
	 */
	public boolean	isRetry()
	{
		return retry!=null ? retry.booleanValue()
			: ((IMGoal)getModelElement()).isRetry();
	}

	/**
	 *  Set the retry flag.
	 *  @param flag The retry flag.
	 */
	public void	setRetry(boolean flag)
	{
		this.retry	= flag ? Boolean.TRUE : Boolean.FALSE;	// Boolean.valueOf(flag);	// since 1.4
	}

	/**
	 *  Get the retry delay expression (if any).
	 *  todo: rename ?!
	 */
	public long	getRetryDelay()
	{
		return retrydelay!=null? retrydelay.longValue(): ((IMGoal)getModelElement()).getRetryDelay();
	}

	/**
	 *  Set the retry delay (in millis).
	 *  @param delay The delay.
	 */
	public void	setRetryDelay(long delay)
	{
		this.retrydelay	= new Long(delay);
	}

	/**
	 *  Get the exclude mode.
	 *  @return The exclude mode.
	 *  @see IGoal for constants.
	 */
	public String	getExcludeMode()
	{
		return excludemode!=null ? excludemode
			: ((IMGoal)getModelElement()).getExcludeMode();
	}

	/**
	 *  Set the exclude mode.
	 *  @param exclude The exclude mode.
	 */
	public void	setExcludeMode(String exclude)
	{
		// todo: check if mode is known?!
		this.excludemode	= exclude;
	}

	/**
	 *  Get the applicable candidates list (apl) recalculation mode.
	 *  @return True, if the apl is recalculated eaach time.
	 */
	public boolean 	isRecalculating()
	{
		return recalculate!=null ? recalculate.booleanValue()
			: ((IMGoal)getModelElement()).isRecalculating();
	}

	/**
	 *  Set the applicable candidates recalculation mode.
	 *  @param recalculate The apl recalculation mode.
	 */
	public void	setRecalculating(boolean recalculate)
	{
		this.recalculate	= recalculate ? Boolean.TRUE : Boolean.FALSE;	// Boolean.valueOf(recalculate);	// since 1.4
	}


	/**
	 *  Get the recur flag.
	 */
	public boolean	isRecur()
	{
		return recur!=null ? recur.booleanValue()
			: ((IMGoal)getModelElement()).isRecur();
	}

	/**
	 *  Get the recur delay expression (if any).
	 */
	public long	getRecurDelay()
	{
		return recurdelay!=null? recurdelay.longValue(): ((IMGoal)getModelElement()).getRecurDelay();
	}

	/**
	 *  Set the recur flag.
	 *  @param flag The recur flag.
	 */
	public void	setRecur(boolean flag)
	{
		this.recur	= flag ? Boolean.TRUE : Boolean.FALSE;	// Boolean.valueOf(recalculate);	// since 1.4
	}

	/**
	 *  Set the recur delay (in millis).
	 *  @param delay The delay.
	 */
	public void	setRecurDelay(long delay)
	{
		this.recurdelay	= new Long(delay);
	}

	//-------- methods --------

	/**
	 *  Get the real parent goal (also considering parent from other scope).
	 *  @return The parent goal or null when top-level goal.
	 */
	public RProcessGoal	getRealParent()
	{
		RProcessGoal	realparent	= null;
		List occs = getAllOccurrences();
		for(int i=0; realparent==null && i<occs.size(); i++)
		{
			IRGoal occ = (IRGoal)occs.get(i);
			realparent	= occ.getParent();
		}
		return realparent;
	}

	/**
	 *  Get the parent goal.
	 *  @return The parent goal or null when top-level goal or parent has different scope.
	 */
	public RProcessGoal	getParent()
	{
		return this.parent;
	}

	/**
	 *  Set the parent.
	 *  Do not use directly, use parent.addChild() instead.	
	 * @param parent The new parent of this goal.
	 */
	public void setParent(RProcessGoal parent)
	{
		this.parent = parent;
		if (parent != null)
		{
			setSubgoal(true);
		}
	}
  
	/**
	 *  Get the cause.
	 *  @return The cause.
	 */
	public RElement getCause()
	{
		return cause;
	}

	/**
	 *  Sets the element this goal was created by
	 *  @param cause The cause.
	 */
	public void setCause(RElement cause)
	{
		this.cause = cause;
	}

	/**
	 *  Get the activation state.
	 *  @return True, if the goal is active.
	 */
	public boolean	isActive()
	{
		return IGoal.LIFECYCLESTATE_ACTIVE.equals(this.lifecyclestate);
	}

	/**
	 *  Get the processing state.
	 *  @return True, if the goal is in process.
	 */
	public abstract boolean	isInProcess();

	/**
	 *  Test, if the goal is finished.
	 *  @return True, if the goal is finished.
	 */
//	public abstract boolean	isFinished();
	public boolean	isFinished()
	{
		return IGoal.LIFECYCLESTATE_DROPPED.equals(lifecyclestate);
	}

	/**
	 *  Test if a goal is succeeded.
	 *  This has different meanings for the different goal types.
	 *  @return True, if goal is succeeded.
	 */
	public abstract boolean isSucceeded();

	/**
	 *  Test if a goal is failed. All goals that
	 *  have not succeeded (also in process goals)
	 *  and are finished are per definition not succeeded (=failed).
	 *  This has different meanings for the different goal types.
	 *  @return True, if goal has failed.
	 */
	public boolean isFailed()
	{
		return isFinished() && !isSucceeded();
	}

	/**
	 *  Get the option state.
	 *  @return True, if the goal is an option.
	 */
	public boolean	isOption()
	{
		return IGoal.LIFECYCLESTATE_OPTION.equals(this.lifecyclestate);
	}

	/**
	 *  Check if goal is adopted
	 *  @return True, if the goal is adopted.
	 */
	public boolean	isAdopted()
	{
		return IGoal.LIFECYCLESTATE_ADOPTED.equals(this.lifecyclestate)
				|| IGoal.LIFECYCLESTATE_ACTIVE.equals(this.lifecyclestate)
				|| IGoal.LIFECYCLESTATE_OPTION.equals(this.lifecyclestate)
				|| IGoal.LIFECYCLESTATE_SUSPENDED.equals(this.lifecyclestate);
	}

	/**
	 *  Get the lifecycle state.
	 *  @return The current lifecycle state (e.g. new, active, dropped).
	 */
	public String	getLifecycleState()
	{
		return this.lifecyclestate;
	}

	/**
	 *  Set the goal's context condition.
	 *  @param mcontext The context condition model.
	 */
	public void setContextCondition(IMCondition mcontext)
	{
		// Stop tracing of previous condition (if any).
		if(this.context != null)
		{
			this.context.cleanup();
		}

		// Start tracing of new condition (if any).
		if(mcontext != null)
		{
			SwitchContextAction sca = new SwitchContextAction(this,
					new GoalLifecycleStatePrecondition(this, IGoal.LIFECYCLESTATE_ADOPTED));
			// Add a precondition that ensures that the switch context action
			// is only performed when the state of the context is as it was when
			// the condition triggered.
			sca.addPrecondition(new IAgendaActionPrecondition()
			{
				public boolean check()
				{
					Boolean valid_context = context.evaluate();
					boolean trigger_true = context.getTrigger().equals(ICondition.TRIGGER_CHANGES_TO_TRUE);
					return trigger_true && (valid_context!=null && valid_context.booleanValue())
						|| !trigger_true && (valid_context!=null && !valid_context.booleanValue());
				}
			});
			this.context	= getScope().getExpressionbase()
				.createInternalCondition(mcontext, this, sca, null);

			// Start tracing.
			if(isAdopted())
			{
				this.context.traceAlways();
			}
		}
		throwSystemEvent(SystemEvent.GOAL_CHANGED);
	}

	/**
	 *  Get the context condition.
	 *  @return The context condition.
	 */
	public RCondition getContextCondition()
	{
		return context;
	}

	/**
	 *  Set the goal's drop condition.
	 *  @param mdel The drop condition model.
	 */
	public void setDropCondition(IMCondition mdel)
	{
		// Stop tracing of previous condition (if any).
		if(this.drop != null)
		{
			this.drop.cleanup();
		}

		// Start tracing of new condition (if any).
		if(mdel != null)
		{
			this.drop	= getScope().getExpressionbase().createInternalCondition(
				mdel, this,	new DropGoalAction(new GoalLifecycleStatePrecondition(
				this, IGoal.LIFECYCLESTATE_ADOPTED), this), null);

			// Start tracing.
			if(isAdopted())
			{
				this.drop.traceOnce();
			}
		}
		throwSystemEvent(SystemEvent.GOAL_CHANGED);
	}

	/**
	 *  Check if the goal is the same as another goal
	 *  with respect to uniqueness settings.
	 *  When two goals are the same this does not mean
	 *  the objects are equal() in the Java sense!
	 */
	public boolean	isSame(IRGoal goal)
	{
		// Goals are only the same when they are of same type.
		boolean	same	= getType().equals(goal.getType());

		if(same)
		{
			// Check parameter correspondence of goal.
			IMGoal	mgoal	= (IMGoal)goal.getModelElement();

			// Compare parameter values.
			IMParameter[]	params	= mgoal.getRelevantParameters();
			for(int i=0; same && i<params.length; i++)
			{
				// Todo: Catch exceptions on parameter access?
				Object	val1	= this.getParameter(params[i].getName()).getValue();
				Object	val2	= goal.getParameter(params[i].getName()).getValue();
				same	= val1==val2 || val1!=null && val1.equals(val2);
			}

			// Compare parameter set values.
			IMParameterSet[]	paramsets	= mgoal.getRelevantParameterSets();
			for(int i=0; same && i<paramsets.length; i++)
			{
				// Todo: Catch exceptions on parameter set access?
				Object[]	vals1	= this.getParameterSet(paramsets[i].getName()).getValues();
				Object[]	vals2	= goal.getParameterSet(paramsets[i].getName()).getValues();
				same	= vals1.length==vals2.length;
				for (int j = 0; same && j < vals1.length; j++)
				{
					same = vals1[j] == vals2[j] || vals1[j] != null && vals1[j].equals(vals2[j]);
				}
			}
		}

		return same;
	}

	/**
	 *  Get the apl.
	 *  @return The apl.
	 */
	public ApplicableCandidateList getApplicableCandidateList()
	{
		return apl;
	}

	/**
	 *  Set the apl.
	 *  @param apl The apl.
	 */
	public void setApplicableCandidateList(ApplicableCandidateList apl)
	{
		this.apl = apl;
	}

	//-------- methods for goal semantics --------
	// More semantics in setActive(), drop()?, createProcessGoal()?,
	// setDropCondition()?
	// + dispatcher.MLReasoning?, planbase.generateAppCandList?, ...?

	/**
	 *  Called when new process goals for this goal are created.
	 *  @param pgoals The list with the new process goals.
	 */
	// Hack ! Should be replaced by above methods???
	public void processGoalsCreated(List pgoals)
	{
		if (processgoals == null && !pgoals.isEmpty())
		{
			processgoals = SCollection.createHashSet();
		}

		//System.out.println("processGoalCreated for: "+this.getName()+" "+hists.size());
		// Add the history entries to the goals history.
		for(int i=0; i<pgoals.size(); i++)
		{
			processgoals.add(pgoals.get(i));
			if(getExcludeMode().equals(IGoal.EXCLUDE_WHEN_TRIED))
			{
				apl.addExcludeCandidate(((RProcessGoal)pgoals.get(i)).getCandidateInfo());
			}
		}
	}

	/**
	 *  Called from a process goal when it is finished.
	 *  @param procgoal The process goal.
	 */
	protected void processGoalFinished(RProcessGoal procgoal)
	{
		// Assert fails, because isAdopted relies on lifecyclestate, which is already set before states are exited.
//		assert isAdopted() : this;
		
		assert processgoals!=null : this;
		assert processgoals.contains(procgoal) : this;
		processgoals.remove(procgoal);
		
		// todo: add when_aborted
		if(getExcludeMode().equals(IGoal.EXCLUDE_WHEN_SUCCEEDED) && procgoal.isSucceeded()
			|| getExcludeMode().equals(IGoal.EXCLUDE_WHEN_FAILED) && procgoal.isFailed())
		{
			apl.addExcludeCandidate(procgoal.getCandidateInfo());
		}

		// Ignore process goal when finished or (moving to) supended.
		if (!isFinished() && !getLifecycleState().equals(IGoal.LIFECYCLESTATE_SUSPENDED))
		{
			copyContentFrom(procgoal);
		}
		
		// Move from dropping to dropped state when last process goal is finished.
		if(IGoal.LIFECYCLESTATE_DROPPING.equals(lifecyclestate) && processgoals.isEmpty())
		{
//			System.out.println("processGoalFinished "+this);
			changeLifecycleState(IGoal.LIFECYCLESTATE_DROPPED);
		}
	}

	/**
	 *  Method to be called, when a recur needs to be scheduled.
	 */
	public void	scheduleRecur()
	{
		assert isRecur() && !isFinished(); 
		
		//System.out.println("No cand found: recur "+getRecurDelay());
		recurentry = new TimetableData(getRecurDelay(),
			new RecurAction(this, new GoalProcessingStatePrecondition(this, this.getProcessingState())));

		getScope().getAgent().addTimetableEntry(recurentry);
	}

	//-------- lifecycle methods --------

	/**
	 *  Change the lifecycle state of the goal.
	 *  @param newstate	The new lifecycle state.
	 */
	public void	changeLifecycleState(String newstate)
	{
//		if(getName().indexOf("test")!=-1)
//		{
//		    System.err.println("State change of: "+this.getName()+": "+lifecyclestate+" "+newstate);
//		    Thread.dumpStack();
//		}

		// Check if state transition is allowed.
		if(!transitions.getCollection(getLifecycleState()).contains(newstate))
			throw new RuntimeException("Cannot change lifecycle state from "+ getLifecycleState() + " to "
			+ newstate + ": " + this.getName());

		String	oldstate	= this.lifecyclestate;

		// Change lifecycle state.
		this.lifecyclestate	= newstate;

		// Exit current state.
		if(oldstate.equals(IGoal.LIFECYCLESTATE_NEW))
		{
			exitNewState();
		}
		else if(oldstate.equals(IGoal.LIFECYCLESTATE_OPTION))
		{
			exitOptionState();
		}
		else if(oldstate.equals(IGoal.LIFECYCLESTATE_ACTIVE))
		{
			exitActiveState();
		}
		else if(oldstate.equals(IGoal.LIFECYCLESTATE_SUSPENDED))
		{
			exitSuspendedState();
		}
		else if(oldstate.equals(IGoal.LIFECYCLESTATE_DROPPING))
		{
			exitDroppingState();
		}

		// Enter new current state.
		if(newstate.equals(IGoal.LIFECYCLESTATE_ADOPTED))
		{
			enterAdoptedState();
		}
		else if(newstate.equals(IGoal.LIFECYCLESTATE_OPTION))
		{
			enterOptionState();
		}
		else if(newstate.equals(IGoal.LIFECYCLESTATE_ACTIVE))
		{
			enterActiveState();
		}
		else if(newstate.equals(IGoal.LIFECYCLESTATE_SUSPENDED))
		{
			enterSuspendedState();
		}
		else if(newstate.equals(IGoal.LIFECYCLESTATE_DROPPING))
		{
			// Exit adopted state when dropping (hack?).
			if(IGoal.LIFECYCLESTATE_ADOPTED.equals(oldstate) || IGoal.LIFECYCLESTATE_ACTIVE.equals(oldstate)
				|| IGoal.LIFECYCLESTATE_OPTION.equals(oldstate) || IGoal.LIFECYCLESTATE_SUSPENDED.equals(oldstate))
			{
				exitAdoptedState();
			}
			enterDroppingState();
		}
		else if(newstate.equals(IGoal.LIFECYCLESTATE_DROPPED))
		{
			enterDroppedState();
		}

		// todo: repair false event propagation when moving to more than one state
		// e.g. new-adopted -> adopted-suspended | adopted-option-active
		// Therefore 2-3 events are thrown, but all of the last type (suspended or active)
		// Generate internal change notification.
		throwSystemEvent(SystemEvent.GOAL_CHANGED);
	}

	//-------- lifecycle state entry actions --------
	/**
	 *  Entry action for adopted state.
	 */
	protected void	enterAdoptedState()
	{
		// Check if all required parameters are set.
		IRParameter[] params = getParameters();
		for(int i=0; i<params.length; i++)
		{
			IMParameter	mparam	= (IMParameter)params[i].getModelElement();
			if(!mparam.isOptional()
				&& (mparam.getDirection().equals(IMParameter.DIRECTION_IN)
					|| mparam.getDirection().equals(IMParameter.DIRECTION_INOUT))
				&& params[i].getValue()==null)
			{
				throw new RuntimeException("Goal parameter is required and nulls: "
					+params[i]+" "+this+" "+parameters);
			}
		}
		IRParameterSet[] paramsets = getParameterSets();
		for(int i=0; i<paramsets.length; i++)
		{
			IMParameterSet	mparam	= (IMParameterSet)paramsets[i].getModelElement();
			if(!mparam.isOptional()
				&& (mparam.getDirection().equals(IMParameterSet.DIRECTION_IN)
					|| mparam.getDirection().equals(IMParameterSet.DIRECTION_INOUT))
				&& paramsets[i].size()==0)
			{
				throw new RuntimeException("Goal parameter set is required and nulls: "
					+paramsets[i]+" "+this+" "+parameters);
			}
		}

		//if(getName().indexOf("analyse_target")!=-1)
		//	System.out.println("new anaylse");
		setParameterProtectionMode(ACCESS_PROTECTION_PROCESSING);
		throwSystemEvent(new SystemEvent(SystemEvent.GOAL_ADDED, this));
    
		// Trace the drop condition.
		if(drop!=null)
		{
			if(drop.isTriggered())
			{
				drop();
			}
			else
			{
				drop.setTraceMode(ICondition.TRACE_ONCE);
			}
		}

		if(isAdopted())
		{
			// Trace the context condition.
			// This may lead to a state change to the suspended state.
			// Otherwise  move to default state (option).
			boolean suspend = false;
			if(context!=null)
			{
				// Todo: Should null-context indicate invalid context?
				Boolean valid_context = context.evaluate();
				suspend = valid_context!=null && !valid_context.booleanValue();
				context.setTraceMode(ICondition.TRACE_ALWAYS);
			}

			changeLifecycleState(suspend ? IGoal.LIFECYCLESTATE_SUSPENDED
				: IGoal.LIFECYCLESTATE_OPTION);
		}
	}

	/**
	 *  Entry action for option state.
	 */
	protected void	enterOptionState()
	{
		if(DELIBERATION)
		{
			// todo: if not was active in last state
			getScope().getAgent().getInterpreter().addAgendaEntry(
				new DeliberateGoalActivationAction(new GoalLifecycleStatePrecondition(this, IGoal.LIFECYCLESTATE_OPTION), this), this);
		}
		else
		{
			changeLifecycleState(IGoal.LIFECYCLESTATE_ACTIVE);
		}
	}

	/**
	 *  Entry action for active state.
	 */
	protected void	enterActiveState()
	{
		if(DELIBERATION)
		{
			getScope().getAgent().getInterpreter().addAgendaEntry(
				new DeactivateInhibitedGoalsAction(new GoalLifecycleStatePrecondition(this, IGoal.LIFECYCLESTATE_ACTIVE), this), this);
		}
	}

	/**
	 *  Get the deliberation info for a goal.
	 *  @return The deliberation info.
	 */
	public IMDeliberation getDeliberationInfo()
	{
		return ((IMGoal)getModelElement()).getDeliberation();
	}

	/**
	 *  Entry action for suspended state.
	 */
	protected void	enterSuspendedState()
	{
		// Little Hack! Watch for unsuspend condition.
		if (context != null)
		{
			this.context.setTrigger(ICondition.TRIGGER_CHANGES_TO_TRUE);
		}

		if(DELIBERATION)
		{
			getScope().getAgent().getInterpreter().addAgendaEntry(
				new DeliberateInhibitedGoalsReactivationAction(
				new GoalLifecycleStatePrecondition(this, IGoal.LIFECYCLESTATE_ACTIVE, true), this), this);
		}
	}

	
//protected boolean waittest;	// Used for debugging (remove).
	/**
	 *  Entry action for dropping state.
	 */
	protected void	enterDroppingState()
	{
//		if(getName().indexOf("rp_initiate")!=-1)
//			System.out.println("enterDropping "+this);
		
		if(processgoals==null || processgoals.isEmpty())
		{
			changeLifecycleState(IGoal.LIFECYCLESTATE_DROPPED);
		}
//		else
//		{
//			waittest	= true;
//			System.out.println("Goal waiting for process goals: "+this);
//		}
			
	}

	/**
	 *  Entry action for dropped state.
	 */
	protected void	enterDroppedState()
	{
//		if(waittest)
//			System.out.println("Process goals for goal finished: "+this);

		
/*		if(nested>20)
		{
			System.out.println("nested drop: "+this);
			System.out.println(SUtil.arrayToString(getParameters()));
			System.out.println(SUtil.arrayToString(getScope().getBeliefbase().getBeliefSet("wastes").getFacts()));
			System.out.println(SUtil.arrayToString(getScope().getBeliefbase().getBeliefSet("wastebins").getFacts()));
		}
		nested++;
*/
		// Dispatch event for terminated goal.
		throwInfoEvent();

		// Remove this goal and its references.
		List occs = getAllOccurrences();
		for(int i=0; i<occs.size(); i++)
		{
			IRGoal occ = (IRGoal)occs.get(i);
			
//			if(waittest)
//				System.out.println("Removing goal occurrence: "+occ+", parent="+occ.getParent());
			
			if (occ.getParent() != null)
			{
				occ.getParent().removeSubgoal(occ);
			}
			if(occ.getScope().getGoalbase().containsGoal(occ))
				occ.getScope().getGoalbase().removeGoal(occ);
		}

		// Throw system event.
		throwSystemEvent(new SystemEvent(SystemEvent.GOAL_REMOVED, this));

		// Cleanup the goal and its contained elements (e.g. conditions).
		cleanup();

//		nested--;
	}

	//-------- lifecycle state exit actions --------

	/**
	 *  Exit action for new state.
	 */
	protected void	exitNewState()
	{
	}

	/**
	 *  Exit action for adopted state.
	 */
	protected void	exitAdoptedState()
	{
//		assert isFinished() : this;

		// Stop tracing.
		if (context != null)
		{
			context.setTraceMode(ICondition.TRACE_NEVER);
		}
		if (drop != null)
		{
			drop.setTraceMode(ICondition.TRACE_NEVER);
		}

		if(DELIBERATION)
		{
			getScope().getAgent().getInterpreter().addAgendaEntry(new DeliberateInhibitedGoalsReactivationAction(null, this), this);
		}
	}

	/**
	 *  Exit action for option state.
	 */
	protected void	exitOptionState()
	{
	}

	/**
	 *  Exit action for active state.
	 */
	protected void	exitActiveState()
	{
		// Remove retry entry.
		if(retryentry!=null)
		{
			getScope().getAgent().removeTimetableEntry(retryentry);
		}

		// Remove recur entry.
		if(recurentry!=null)
		{
			getScope().getAgent().removeTimetableEntry(recurentry);
		}

		// Abort all process goals.
		abortProcessGoals();
	}

	/**
	 *  Exit action for suspended state.
	 */
	protected void	exitSuspendedState()
	{
		// Little Hack! Watch for context condition.
		if (context != null)
		{
			context.setTrigger(ICondition.TRIGGER_CHANGES_TO_FALSE);
		}
	}
	
	/**
	 *  Exit action for dropping state.
	 */
	protected void	exitDroppingState()
	{
	}

	/**
	 *  Get the retry entry.
	 *  @return The retry entry.
	 */
	public TimetableData getRetryEntry()
	{
		return retryentry;
	}

	/**
	 *  Set the retry entry.
	 *  @param retryentry The retry entry.
	 */
	public void setRetryEntry(TimetableData retryentry)
	{
		this.retryentry = retryentry;
	}

	/**
	 *  Get the recur entry.
	 *  @return The recur entry.
	 */
	// Todo: remove
	public TimetableData getRecurEntry()
	{
		return recurentry;
	}

	/**
	 *  Set the recur entry.
	 * /
	public void setRecurEntry(TimetableData data)
	{
		this.recurentry	= data;
	}*/

	//-------- legacy --------

	/**
	 *  Adopt the goal.
	 */
	protected void adopt()
	{
		changeLifecycleState(IGoal.LIFECYCLESTATE_ADOPTED);
	}

	/**
	 *  Make the goal to an option.
	 */
	public void option()
	{
		changeLifecycleState(IGoal.LIFECYCLESTATE_OPTION);
	}

	/**
	 *  Activate the goal.
	 */
	public void activate()
	{
		changeLifecycleState(IGoal.LIFECYCLESTATE_ACTIVE);
	}

	/**
	 *  Suspend the goal.
	 */
	public void suspend()
	{
		changeLifecycleState(IGoal.LIFECYCLESTATE_SUSPENDED);
	}

	protected RuntimeException ex;
	/**
	 *  Drop this goal.
	 *  Causes all associated process goals
	 *  and subgoals to be dropped.
	 */
	public void drop()
	{
		// Ignore drop on goals already dropped or dropping (hack)!?
		if(IGoal.LIFECYCLESTATE_DROPPING.equals(lifecyclestate))
			return;
			
		if(!isAdopted())
		{
			throw new RuntimeException("Cannot drop a goal wich is not (any longer) adopted: "+this);
		}
		
		// Dropping leads to abortion of process goals which may call drop again.
		if (dropping)
		{
			return;
		}

		boolean	debug	= false;
		assert	debug=true;	// Sets debug to true, if asserts are enabled.
		if(debug)
		{
			// Debug code, for printing both stack traces, when a goal was dropped twice
			if(ex==null)
			{
				// Remember stacktrace, when goal was dropped first time.
				try
				{
					throw new RuntimeException("First stack trace: "+getName()+", "+getProcessingState()+", "+getLifecycleState());
				}
				catch(RuntimeException e)
				{
					this.ex	= e;
				}
			}
			else
			{
				// Goal dropped twice.
				ex.printStackTrace();
				try
				{
					throw new RuntimeException("Second stack trace: "+getName()+", "+getProcessingState()+", "+getLifecycleState());
				}
				catch(RuntimeException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		// Set the state to dropping to avoid
		// event in the processGoalFinished() method. (hack)
		this.dropping = true;

		changeLifecycleState(IGoal.LIFECYCLESTATE_DROPPING);

		this.dropping=false;
	}

	//-------- internal methods --------

	/**
	 *  Set the exception for the goal.
	 *  This is a convenience method, as the goal exception
	 *  is stored as property.
	 *  @param exception The exception.
	 */
	public void	setException(Exception exception)
	{
		this.exception = exception;
	}

	/**
	 *  Get the exception of the goal.
	 *  @return The exception value.
	 */
	public Exception	getException()
	{
		return this.exception;
	}

	/**
	 *  Set the result for the goal.
	 *  This is a convenience method, as the goal result
	 *  is stored as property.
	 *  @param result The result.
	 */
	public void	setResult(Object result)
	{
		//System.out.println("::: "+result+" "+this);
		findResultParameter().setValue(result);
		throwSystemEvent(SystemEvent.GOAL_CHANGED);	// Hack???
	}

	/**
	 *  Get the result of the goal.
	 *  This is a convenience method, as the goal result
	 *  is stored as property.
	 *  @return The result value.
	 */
	public Object	getResult()
	{
		return findResultParameter().getValue();
	}

	/**
	 *  Find result parameter.
	 *  The result parameter is a unique parameter of direction "out".
	 *  @throws RuntimeException	When no unique out parameter found.
	 */
	protected IRParameter	findResultParameter()
	{
		IMParameter	mret	= null;
		IMParameter[]	mparams	= ((IMGoal)getModelElement()).getParameters();
		for(int i=0; i<mparams.length; i++)
		{
			if(mparams[i].getDirection().equals(IMParameter.DIRECTION_OUT)
				|| mparams[i].getDirection().equals(IMParameter.DIRECTION_INOUT))
			{
				if(mret==null)
				{
					mret	= mparams[i];
				}
				else
				{
					throw new RuntimeException("No unique out parameter: "+this);
				}
			}
		}
		if(mret==null)
		{
			throw new RuntimeException("No out parameter found: "+this);
		}
		return getParameter(mret.getName());
	}

	/**
	 *  Test if this goal is a (or was) subgoal.
	 */
	public boolean isSubgoal()
	{
		return this.subgoal;
	}

	/**
	 *  Set if this goal is a subgoal.
	 */
	public void	setSubgoal(boolean subgoal)
	{
		this.subgoal	= subgoal;
	}

	//-------- helper methods --------

	/**
	 *  Get the string representation for a goal
	 *  including subgoals.
	 *  @return The string representation.
	 */
	protected String toString(int level)
	{
		StringBuffer buf = new StringBuffer();
		//buf.append(super.toString());
		for(int i = 0; i < level; i++)
		{
			buf.append("  ");
		}
		buf.append(SReflect.getInnerClassName(this.getClass()));
		buf.append("(name=");
		buf.append(getName());
		buf.append(", type=");
		buf.append(getType());
		buf.append(", active=");
		buf.append(isActive());
		buf.append(")");

/*		RAbstractGoal[] subgoals = getChildren();
		for(int i = 0; i < subgoals.length; i++)
		{
			buf.append("\n");
			buf.append(subgoals[i].toString(level + 1));
		}
*/
		return buf.toString();
	}
	
	/**
	 *  Recur this goal.
	 */
	public void doRecur()
	{
		if(apl!=null)
			apl.clearExcludeSet();

		//System.out.println("Recur process event thrown");
		throwProcessEvent();
	}

	// Cached for speed
	protected IAgendaActionPrecondition precond;
	/**
	 *  Throw an event to process a goal.
	 */
	public void throwProcessEvent()
	{
		if(!dropping)
		{
			if(precond==null)
				this.precond = createProcessEventPrecondition();
			
			IRGoalEvent event = getScope().getEventbase().createGoalEvent(this, false);
			getScope().dispatchEvent(event, precond);
		}
	}
	
	/**
	 *  Create the process event precondition;
	 *  @return The process event precondition.
	 */
	protected IAgendaActionPrecondition createProcessEventPrecondition()
	{
		return new ComposedPrecondition(new GoalNotFinishedPrecondition(this),
			new GoalLifecycleStatePrecondition(this, IGoal.LIFECYCLESTATE_ACTIVE));
	}

	/**
	 *  Throw an event to inform about the (final) state of a goal.
	 */
	// Hack!!! shouldnt be public
	public void throwInfoEvent()
	{
		IRGoalEvent event = getScope().getEventbase().createGoalEvent(this, true);//, RInternalEvent.TYPE_GOALINFO, this));
		getScope().dispatchEvent(event, new IAgendaActionPrecondition()
		{
			public boolean check()
			{
				// Ignore subgoal info events, when subgoal has been removed (i.e. parent was reset to null).
				return !isSubgoal() || getRealParent().isAdopted();
			}
		});
	}

	protected SystemEvent	goalchanged	= new SystemEvent(SystemEvent.GOAL_CHANGED, this);
	/**
	 *  Generate a change event for this element.
	 *  @param event The event type.
	 */
	// Overridden to avoid creation of new system events.
	public void throwSystemEvent(String event)
	{
		assert SystemEvent.GOAL_CHANGED.equals(event);
		throwSystemEvent(goalchanged);
	}

	/**
	 *  Generate a property representation for encoding this element (eg to SL).
	 *  @return A properties object representaing this element.
	 */
	public Map	getEncodableRepresentation()
	{
		Map representation = super.getEncodableRepresentation();
		representation.put("contextcondition", context == null ? ""
				: ((IMCondition)context.getModelElement()).getExpressionText());
		representation.put("dropcondition", drop == null ? ""
				: ((IMCondition)drop.getModelElement()).getExpressionText());
		representation.put("lifecyclestate", getLifecycleState());

		if(parent!=null)
		{
			representation.put("parent", parent.getName());
		}
		RProcessGoal	realparent	= getRealParent();
		if(realparent!=null)
		{
			representation.put("realparent", realparent.getName());
			representation.put("realparentscope", realparent.getScope().getDetailName());
		}

		// Add goal kind (hack???).
		representation.put("kind", getGoalKind());
		
		return representation;
	}

	/**
	 *  Get the goal kind (e.g. achieve, maintain...).
	 */
	// Hack???
	public String getGoalKind()
	{
		String	kind	= SReflect.getInnerClassName(getClass());
		kind	= kind.substring(1, kind.length()-4);	// Strip R...Goal
		return kind.toLowerCase();
	}

	/**
	 *  Check if goal processing is finished.
	 *  Checks all process goals.
	 *  @return True, when no more goals are in process.
	 */
	protected boolean isProcessingFinished()
	{
		return processgoals==null || processgoals.isEmpty();
	}

	/**
	 *  Get the goal processing state.
	 *  @return The processing state.
	 */
	public abstract String 	getProcessingState();

	/**
	 *  Change the processing state.
	 */
	public abstract void changeProcessingState(String newstate);

	/**
	 *  Abort all still active process goals.
	 */
	public void abortProcessGoals()
	{
		// Drop process goals that are still executing.
		boolean abort_on_success = isSucceeded();
		if(processgoals!=null)
		{
			RProcessGoal[] goals = (RProcessGoal[])processgoals.toArray(new RProcessGoal[processgoals.size()]);
			for(int i=0; i<goals.length; i++)
			{
				final RProcessGoal	goal	= goals[i];
				
				// Dropping of other goal is decoupled. This ensures that e.g.
				// goal lifecycle changes are completed before more
				// lifecycle changes are triggered to to finished subgoals.
				getScope().getAgent().getInterpreter().addAgendaEntry(
					new AbortProcessGoalAction(new IAgendaActionPrecondition()
				{
					public boolean check()
					{
						return !goal.isFinished();
					}
				}, goals[i], abort_on_success), null); // todo: cause?
			}
		}
	}

	/**
	 *  Check if there have been process goals for this goal.
	 */
	// Todo: replace by APL handling.
	public boolean wasProcessed()
	{
		// Hack???
		return processgoals!=null;
	}

	/**
	 *  Copy the content from the rgoal (proprietary goal) to this goal.
	 *  @param pgoal The pgoal.
	 */
	protected void copyContentFrom(RProcessGoal pgoal)
	{
		setException(pgoal.getException());
		try
		{
			pgoal.copyContent();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 *  Get the filter to wait for an info event.
	 *  @return The filter.
	 */
	public IFilter getFilter()
	{
		return new GoalEventFilter(getType(), getName(), true);
	}
}
