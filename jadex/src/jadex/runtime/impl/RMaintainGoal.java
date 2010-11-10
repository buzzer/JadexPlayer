package jadex.runtime.impl;

import jadex.model.*;
import jadex.util.SReflect;
import jadex.util.collection.MultiCollection;
import jadex.runtime.*;
import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.agenda.goals.*;
import jadex.runtime.impl.agenda.goals.maintain.*;
import jadex.runtime.impl.agenda.easydeliberation.*;

import java.util.List;
import java.util.Map;

/**
 *  The maintain goal class.
 */
public class RMaintainGoal extends RGoal
{
	//-------- constants --------

	/* The state types of a maintain goal. */

	/** The goal could not reestablish the maintain condition. */
	public static final String	MAINTAIN_STATE_UNMAINTAINABLE	= "unmaintainable";

	/** The goal state is unknown. */
	public static final String	MAINTAIN_STATE_UNKNOWN	= "unknown";
	
	/** The goal is actually processed. */
	public static final String	MAINTAIN_STATE_IN_PROCESS	= "in_process";
	
	/** The goal is idle. */
	public static final String	MAINTAIN_STATE_IDLE	= "idle";

	/** The goal is paused. */
	public static final String	MAINTAIN_STATE_PROCESSING_PAUSED	= "processing_paused";

	/** The allowed state tarnsitions (oldstate mapped to collection of new states). */
	protected static final MultiCollection	proc_transitions	= new MultiCollection();

	static
	{
		// Add allowed state transitions to transition table.
		proc_transitions.put(MAINTAIN_STATE_IDLE, MAINTAIN_STATE_IN_PROCESS);
		proc_transitions.put(MAINTAIN_STATE_IDLE, MAINTAIN_STATE_UNMAINTAINABLE);
		proc_transitions.put(MAINTAIN_STATE_IN_PROCESS, MAINTAIN_STATE_IN_PROCESS);
		proc_transitions.put(MAINTAIN_STATE_IN_PROCESS, MAINTAIN_STATE_IDLE);
		proc_transitions.put(MAINTAIN_STATE_IN_PROCESS, MAINTAIN_STATE_UNMAINTAINABLE);
		proc_transitions.put(MAINTAIN_STATE_IN_PROCESS, MAINTAIN_STATE_UNKNOWN);
		proc_transitions.put(MAINTAIN_STATE_IN_PROCESS, MAINTAIN_STATE_PROCESSING_PAUSED);
		proc_transitions.put(MAINTAIN_STATE_UNMAINTAINABLE, MAINTAIN_STATE_IDLE);
		proc_transitions.put(MAINTAIN_STATE_UNMAINTAINABLE, MAINTAIN_STATE_IN_PROCESS);
		proc_transitions.put(MAINTAIN_STATE_UNMAINTAINABLE, MAINTAIN_STATE_UNMAINTAINABLE);
		proc_transitions.put(MAINTAIN_STATE_UNKNOWN, MAINTAIN_STATE_IDLE);
		proc_transitions.put(MAINTAIN_STATE_UNKNOWN, MAINTAIN_STATE_IN_PROCESS);
		proc_transitions.put(MAINTAIN_STATE_PROCESSING_PAUSED, MAINTAIN_STATE_IDLE);
		proc_transitions.put(MAINTAIN_STATE_PROCESSING_PAUSED, MAINTAIN_STATE_IN_PROCESS);
		proc_transitions.put(MAINTAIN_STATE_PROCESSING_PAUSED, MAINTAIN_STATE_UNMAINTAINABLE);
	}

	//-------- attributes --------
	
	/** The maintain condition (not nullable). */
	protected RCondition maintain;

	/** The target condition. */
	protected RCondition target;

	/** The target action. */
	protected AbstractAgendaAction targetaction;

	/** The processing state. */
	protected String processingstate;

	/** The retry action. */
	public IAgendaAction retryaction;

	//-------- constructor --------

	/**
	 *  Create a new goal.
	 * @param name The name.
	 * @param goal The model element.
	 * @param config The configuration.
	 * @param owner The owner.
	 * @param binding The binding.
	 */
	protected RMaintainGoal(String name, IMMaintainGoal goal, IMConfigGoal config,
			RElement owner, RReferenceableElement creator, Map binding)
	{
		super(name, goal, config, owner, creator, binding);

		this.targetaction	= new TargetAction(this, new GoalLifecycleStatePrecondition(this, IGoal.LIFECYCLESTATE_ACTIVE));
		this.retryaction = new MaintainGoalRetryAction(this, new GoalProcessingStatePrecondition(this, MAINTAIN_STATE_PROCESSING_PAUSED));

		// Maintain goal starts in idle state.
		setProcessingState(MAINTAIN_STATE_IDLE);
		setMaintainCondition(goal.getMaintainCondition());
		setTargetCondition(goal.getTargetCondition());
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
		if(this.target != null)
		{
			this.target.cleanup();
		}
		if(this.maintain != null)
		{
			this.maintain.cleanup();
		}
	}

	//-------- accessors --------

	/**
	 *  Get the maintain condition.
	 *  @return The maintain condition.
	 */
	public RCondition getMaintainCondition()
	{
		return this.maintain;
	}

	/**
	 *  Set the goal's maintain condition.
	 *  @param mmaintain The maintain condition model.
	 */
	public void setMaintainCondition(IMCondition mmaintain)
	{
		// Stop tracing of previous condition (if any).
		if (this.maintain != null)
		{
			this.maintain.cleanup();
		}

		// Start tracing of new condition.
		this.maintain	= getScope().getExpressionbase().createInternalCondition(mmaintain,
			this, new MaintainAction(this, new GoalLifecycleStatePrecondition(this, IGoal.LIFECYCLESTATE_ACTIVE)), null);

		// Start tracing if necessary.
		if(isActive())
		{
			this.maintain.traceOnce();
		}

		throwSystemEvent(SystemEvent.GOAL_CHANGED);
	}

	/**
	 *  Get the target condition.
	 *  @return The target condition.
	 */
	public RCondition getTargetCondition()
	{
		return this.target;
	}

	/**
	 *  Set the goal's target condition.
	 *  @param mtarget The target condition model.
	 */
	public void setTargetCondition(IMCondition mtarget)
	{
		// Stop tracing of previous condition (if any).
		if(this.target != null)
		{
			this.target.cleanup();
		}

		// Start tracing of new condition (if any).
		if(mtarget != null)
		{
			//System.out.println("Tracing target: "+this.getName());
			this.target	= getScope().getExpressionbase()
				.createInternalCondition(mtarget, this, targetaction, null);
		}

		throwSystemEvent(SystemEvent.GOAL_CHANGED);
	}

	/**
	 *  Test if a goal is succeeded.
	 *  This has different meanings for the different goal types.
	 *  @return True, if goal is succeeded.
	 */
	public boolean isSucceeded()
	{
		return MAINTAIN_STATE_IDLE.equals(processingstate);
	}

	//-------- update semantics --------

	/**
	 *  Called when new process goals for this goal are created.
	 *  @param hists The list with the new history entries (including the process goals)
	 */
	public void processGoalsCreated(List hists)
	{
		super.processGoalsCreated(hists);

		// Handle goals without candidate, and failed goals without retry or posttoall.
		if(hists.size() == 0)
		{
//			//System.out.println("No candidate found, unmaintainable."+getTargetCondition().evaluate()
//			//	+" "+getTargetCondition().getTraceMode());
//			// If the 'achieve goal' fails the maintain goal is
//			// unmaintainable
//			changeProcessingState(MAINTAIN_STATE_UNMAINTAINABLE);
//			throwInfoEvent();
//			scheduleRecur();
//			getTargetCondition().setTraceMode(ICondition.TRACE_NEVER);
//			getMaintainCondition().traceOnce();

			// Set to unmaintainable, to let target condition decide on final state.
			// Precondition assures that action is only executed, if target condition has not triggered.
			getScope().getAgent().getInterpreter().addAgendaEntry(new MaintainGoalFinishedAction(this,
				new GoalProcessingStatePrecondition(this, getProcessingState())), this);
		}
		else
		{
			changeProcessingState(MAINTAIN_STATE_IN_PROCESS);
		}
	}

	/**
	 *  Called from a process goal
	 *  when it is finished.
	 *  Produces an info event and updates the state
	 *  of the proprietary goal.
	 *  Produces new process events according to the
	 *  goal semantics.
	 *  @param procgoal The process goal.
	 */
	protected void processGoalFinished(RProcessGoal procgoal)
	{
		super.processGoalFinished(procgoal);

		// Ignore process goal when not processing.
		if (ignoreProcessGoals())
		{
			return;
		}
		
		assert isActive() : this;

		//System.out.println("processGoal finished for: "+this.getName()+" "+procgoal.getName()
		//	+" "+getTargetCondition().evaluate()+" "+getTargetCondition().getTraceMode());

		// Update the goals state.
		// May trigger a target condition, if present.
		//copyContentFrom(procgoal);

		// Retry always to let the target condition decide when to stop processing.
		if(!isPostToAll() && isRetry())
		{
			if(getRetryDelay()>0)
			{
				changeProcessingState(MAINTAIN_STATE_PROCESSING_PAUSED);
				setRetryEntry(new TimetableData(getRetryDelay(), retryaction));
			 	getScope().getAgent().addTimetableEntry(getRetryEntry());
			}
			else
			{
				throwProcessEvent();
			}
		}

		// Goal finished (for post-to-all: last plan).
		else if(isProcessingFinished())
		{
			// Set to unmaintainable, to let target condition decide on final state.
			// Precondition assures that action is only executed, if target condition has not triggered.
			getScope().getAgent().getInterpreter().addAgendaEntry(new MaintainGoalFinishedAction(this,
				new GoalProcessingStatePrecondition(this, MAINTAIN_STATE_IN_PROCESS)), this);
		}
	}

	/**
	 *  Clear exclude candidates.
	 */
	// todo: remove
	public void clearExcludedCandidates()
	{
		if(apl!=null)
			apl.clearExcludeSet();
	}

	 //-------- handle the processing state --------

	/**
	 *  Get the goal processing state.
	 *  @return The processing state.
	 */
	public String 	getProcessingState()
	{
		assert processingstate!=null;

		return this.processingstate;
	}

	/**
	 *  Set a state for the goal.
	 *  @param processingstate The state.
	 */
	protected void	setProcessingState(String processingstate)
	{
		this.processingstate = processingstate;
		throwSystemEvent(SystemEvent.GOAL_CHANGED);
	}

	/**
	 *  Change the processing state.
	 */
	public void changeProcessingState(String newstate)
	{
		//System.err.println("Changing procesing state from "+ getProcessingState() + " to " + newstate + ": " + this.getName());
		assert proc_transitions.getCollection(getProcessingState()).contains(newstate)
 			:"Cannot change processing state from "
			+ getProcessingState() + " to " + newstate + ": " + this.getName();

		if (newstate.equals(getProcessingState()))
		{
			return;
		}

		String oldstate = getProcessingState();
		setProcessingState(newstate);

		// exit in_process state.
		if(oldstate.equals(MAINTAIN_STATE_IN_PROCESS))
		{
			if(DELIBERATION)
			{
				getScope().getAgent().getInterpreter().addAgendaEntry(new DeliberateInhibitedGoalsReactivationAction(
					new GoalProcessingStatePrecondition(this, MAINTAIN_STATE_IN_PROCESS, true), this), this);
			}
		}

		// enter in_process state
		else if(newstate.equals(MAINTAIN_STATE_IN_PROCESS))
		{
			if(DELIBERATION)
			{
				getScope().getAgent().getInterpreter().addAgendaEntry(new DeactivateInhibitedGoalsAction(
					new GoalProcessingStatePrecondition(this, MAINTAIN_STATE_IN_PROCESS), this), this);
			}
		}
	}

	//-------- lifecycle handling --------

	/**
	 *  Enter active state.
	 */
	protected void	enterActiveState()
	{
		// Activating adopted goal of an agent.
		// This means for the maintain goal that the
		// maintain condition is traced.
		this.maintain.traceOnce();

		super.enterActiveState();
	}

	/**
	 *  Exit active state.
	 */
	protected void	exitActiveState()
	{
		if (MAINTAIN_STATE_IN_PROCESS.equals(processingstate)
				|| MAINTAIN_STATE_PROCESSING_PAUSED.equals(processingstate))
		{
			setProcessingState(MAINTAIN_STATE_IDLE);
		}
		
		// Deactivating adopted goal of an agent.
		// This means for the maintain goal that the
		// maintain condition is no longer traced.
		this.maintain.setTraceMode(ICondition.TRACE_NEVER);
		this.maintain.reset();
		this.target.setTraceMode(ICondition.TRACE_NEVER);

		super.exitActiveState();
	}

	//-------- methods --------

	/**
	 *  Check if the process goals can be ignored.
	 *  @return True, if the goal is in some state, where process goals are ignored.
	 */
	public boolean ignoreProcessGoals()
	{
		return MAINTAIN_STATE_IDLE.equals(processingstate)
			|| MAINTAIN_STATE_UNKNOWN.equals(processingstate)
			|| MAINTAIN_STATE_UNMAINTAINABLE.equals(processingstate);
	}

	/**
	 *  Get the processing state.
	 *  @return True, if the goal is in process.
	 */
	public boolean	isInProcess()
	{
		return isActive() && MAINTAIN_STATE_IN_PROCESS.equals(processingstate);
	}

	/**
	 *  Test, if the goal is finished.
	 *  Only dropped maintain goals 
	 *  @return True, if the goal is finished.
	 * /
	public boolean	isFinished()
	{
		// Hack!!! Maintain goal has no final (processing) states.
		return dropping || IGoal.LIFECYCLESTATE_DROPPING.equals(getLifecycleState())
			 || IGoal.LIFECYCLESTATE_DROPPED.equals(getLifecycleState());
	}*/

	//-------- helper methods --------

	/**
	 *  Generate a property representation for encoding this element
	 *  (eg to SL).
	 *  @return A properties object representaing this element.
	 */
	public Map getEncodableRepresentation()
	{
		Map representation = super.getEncodableRepresentation();
		representation.put("processingstate", processingstate);
		representation.put("maintaincondition", maintain == null ? ""
				: ((IMCondition)maintain.getModelElement()).getExpressionText());
		representation.put("targetcondition", target == null ? ""
				: ((IMCondition)target.getModelElement()).getExpressionText());
		return representation;
	}


	/**
	 *  Get the string representation for a goal
	 *  including subgoals.
	 *  @return The string representation.
	 *  todo: adapt to actual BDI-flags
	 */
	protected String toString(int level)
	{
		StringBuffer	buf	= new StringBuffer();
		//buf.append(super.toString());
		for(int i=0; i<level; i++)
		{
			buf.append("  ");
		}
		buf.append(SReflect.getInnerClassName(this.getClass()));
		buf.append("(name=");
		buf.append(getName());
		buf.append(", type=");
		buf.append(getType());
		buf.append(", processingstate=");
		buf.append(processingstate);
		buf.append(", active=");
		buf.append(isActive());
		buf.append(")");
/*
		RAbstractGoal[] subgoals	= getChildren();
		for(int i=0; i<subgoals.length; i++)
		{
			buf.append("\n");
			buf.append(subgoals[i].toString(level+1));
		}*/

		return buf.toString();
	}
	
	/**
	 *  Create the process event precondition;
	 *  @return The process event precondition.
	 */
	protected IAgendaActionPrecondition createProcessEventPrecondition()
	{
		ComposedPrecondition cp = new ComposedPrecondition(new GoalNotFinishedPrecondition(this),
			new GoalLifecycleStatePrecondition(this, IGoal.LIFECYCLESTATE_ACTIVE));
		cp.addPrecondition(new IAgendaActionPrecondition()
		{
			public boolean check()
			{
				// Only process events, when maintain condition is violated.
				return !(maintain.getLastValue()!=null && maintain.getLastValue().booleanValue());
			}
		});
		return cp;
	}

	/**
	 *  Recur this goal.
	 */
	public void doRecur()
	{
		assert RMaintainGoal.MAINTAIN_STATE_UNKNOWN.equals(getProcessingState())
			|| RMaintainGoal.MAINTAIN_STATE_UNMAINTAINABLE.equals(getProcessingState())
			: getProcessingState();
	
		super.doRecur();
		getMaintainCondition().setTraceMode(ICondition.TRACE_NEVER);
		getTargetCondition().setTraceMode(ICondition.TRACE_ONCE);
	}
}
