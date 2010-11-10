package jadex.runtime.impl;

import jadex.model.*;

import jadex.util.SReflect;
import jadex.util.collection.MultiCollection;
import jadex.runtime.*;
import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.agenda.goals.*;
import jadex.runtime.impl.agenda.easydeliberation.*;
import java.util.List;
import java.util.Map;

/**
 *  An achieve goal encapsulates the special semantics for
 *  handling newly created process goals and finsished process
 *  goals.
 */
public class RAchieveGoal extends RGoal
{
	//-------- constants --------

	/* The state types of an achieve goal. */

	/** The goal is idle. */
	public static final String	ACHIEVE_STATE_START	= "start";

	/** The goal is retry. */
	public static final String	ACHIEVE_STATE_PROCESSING_PAUSED	= "processing_paused";

	/** The state is finished and it is not known if it was successful. */
	public static final String	ACHIEVE_STATE_UNKNOWN	= "unknown";

	/** The state is finished and succeeded. */
	public static final String	ACHIEVE_STATE_SUCCEEDED	= "succeeded";
	
	/** The state is finished and failed. */
	public static final String	ACHIEVE_STATE_FAILED	= "failed";
	
	/** The goal is actually processed. */
	public static final String	ACHIEVE_STATE_IN_PROCESS	= "in_process";

	/** The allowed state tarnsitions (oldstate mapped to collection of new states). */
	protected static final MultiCollection	proc_transitions	= new MultiCollection();

	static
	{
		// Add allowed state transitions to transition table.
		proc_transitions.put(ACHIEVE_STATE_START, ACHIEVE_STATE_IN_PROCESS);
		proc_transitions.put(ACHIEVE_STATE_START, ACHIEVE_STATE_SUCCEEDED);
		proc_transitions.put(ACHIEVE_STATE_START, ACHIEVE_STATE_FAILED);
		proc_transitions.put(ACHIEVE_STATE_START, ACHIEVE_STATE_PROCESSING_PAUSED);
		proc_transitions.put(ACHIEVE_STATE_IN_PROCESS, ACHIEVE_STATE_IN_PROCESS);
		proc_transitions.put(ACHIEVE_STATE_IN_PROCESS, ACHIEVE_STATE_PROCESSING_PAUSED);
		proc_transitions.put(ACHIEVE_STATE_IN_PROCESS, ACHIEVE_STATE_SUCCEEDED);
		proc_transitions.put(ACHIEVE_STATE_IN_PROCESS, ACHIEVE_STATE_UNKNOWN);
		proc_transitions.put(ACHIEVE_STATE_IN_PROCESS, ACHIEVE_STATE_FAILED);
		proc_transitions.put(ACHIEVE_STATE_PROCESSING_PAUSED, ACHIEVE_STATE_IN_PROCESS);
		proc_transitions.put(ACHIEVE_STATE_PROCESSING_PAUSED, ACHIEVE_STATE_SUCCEEDED);
		proc_transitions.put(ACHIEVE_STATE_PROCESSING_PAUSED, ACHIEVE_STATE_FAILED);
		//proc_transitions.put(ACHIEVE_STATE_PROCESSING_PAUSED, ACHIEVE_STATE_UNKNOWN); // ?
	}
	//-------- attributes --------

	/** The target condition. */
	protected RCondition target;

	/** The failure condition. */
	protected RCondition failure;

	/** The processing state. */
	protected String processingstate;

	/** The retry action. */
	public IAgendaAction retryaction;

	//-------- constructor --------

	/**
	 *  Create a new goal.
	 *  @param name The name.
	 *  @param goal The model element.
	 *  @param config The configuration.
	 *  @param owner The owner.
	 *  @param binding The binding.
	 */
	protected RAchieveGoal(String name, IMAchieveGoal goal, IMConfigGoal config,
			RElement owner, RReferenceableElement creator, Map binding)
	{
		super(name, goal, config, owner, creator, binding);
		this.retryaction = new RetryAction(this, new GoalProcessingStatePrecondition(this, ACHIEVE_STATE_PROCESSING_PAUSED));
		setProcessingState(ACHIEVE_STATE_START);
		setTargetCondition(goal.getTargetCondition());
		setFailureCondition(goal.getFailureCondition());
		//System.out.println("Created: "+this);
	}

	/**
	 *  Perform any necessary clean up, when this element is
	 *  no longer used.
	 *  Subclasses may override this method to do their
	 *  cleanup operations.
	 */
	public void	cleanup()
	{
		if(cleanedup)
			return;

		super.cleanup();

		// Cleanup conditions (if any).
		if(this.target != null)
			this.target.cleanup();
		if(this.failure != null)
			this.failure.cleanup();
	}

	//-------- accessors --------

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
			this.target	= getScope().getExpressionbase().createInternalCondition(mtarget, this,
				new GoalFinishedAction(this, prec_notfinished, ACHIEVE_STATE_SUCCEEDED), null);

			// Start tracing if necessary.
			if(isAdopted())
			{
				this.target.traceOnce();
			}
		}

		throwSystemEvent(SystemEvent.GOAL_CHANGED);
	}

	/**
	 *  Get the failure condition.
	 *  @return The failure condition.
	 */
	public RCondition getFailureCondition()
	{
		return this.failure;
	}

	/**
	 *  Set the goal's failure condition.
	 *  @param mfail The failure condition model.
	 */
	public void setFailureCondition(IMCondition mfail)
	{
		// Stop tracing of previous condition (if any).
		if(this.failure != null)
		{
			this.failure.cleanup();
		}

		// Start tracing of new condition (if any).
		if(mfail != null)
		{
			this.failure	= getScope().getExpressionbase().createInternalCondition(mfail, this,
				new GoalFinishedAction(this, prec_notfinished, ACHIEVE_STATE_FAILED), null);

			// Start tracing if necessary.
			if(isAdopted())
			{
				this.failure.traceOnce();
			}
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
		return ACHIEVE_STATE_SUCCEEDED.equals(processingstate);
	}

	/**
	 *  Get the processing state.
	 *  @return True, if the goal is in process.
	 */
	public boolean	isInProcess()
	{
		return isActive() && ACHIEVE_STATE_IN_PROCESS.equals(processingstate);
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
			// Schedule recur when no plans but recur is on.
			if(isRecur())
			{
				if(!ACHIEVE_STATE_PROCESSING_PAUSED.equals(getProcessingState()))
					changeProcessingState(ACHIEVE_STATE_PROCESSING_PAUSED);
				scheduleRecur();
			}
			
			// If a process goal cannot be processed set to finally failed.
			else
			{
				changeProcessingState(ACHIEVE_STATE_FAILED);
				drop();
			}
		}
		else
		{
			changeProcessingState(ACHIEVE_STATE_IN_PROCESS);
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

		// Ignore process goal when finished or (moving to) supended.
		if(isFinished() || ACHIEVE_STATE_START.equals(processingstate))
		//if(isFinished() || getLifecycleState().equals(IGoal.LIFECYCLESTATE_SUSPENDED))
			return;
		
		assert isActive() : this;

		//if(this.getName().indexOf("")!=-1)
		//System.out.println("processGoal finished for: "+this.getName()+" "+procgoal);

		// Update the goals state.
		// May trigger a target condition, if present.
		//copyContentFrom(procgoal);

		String procstate = procgoal.getProcessingState();
				
		// Retry a goal when the process goal is failed and retry is on.
		// When target condition is present retry the goal, even if a plan succeeds
		// to let the condition decide when to stop processing.
		if(!isPostToAll() && isRetry() && (getTargetCondition()!=null ||
			!RProcessGoal.PROCESS_STATE_SUCCEEDED.equals(procstate)))
		{
			if(getRetryDelay()>0)
			{
				changeProcessingState(ACHIEVE_STATE_PROCESSING_PAUSED);
				setRetryEntry(new TimetableData(getRetryDelay(), retryaction));
			 	getScope().getAgent().addTimetableEntry(getRetryEntry());
			}
			else
			{
				throwProcessEvent();
			}
		}

		// Goal finished (for post-to-all: last plan or plan succeeded).
		else if(isProcessingFinished() || RProcessGoal.PROCESS_STATE_SUCCEEDED.equals(procstate))
		{

			// Schedule recur when not succeeded but recur is on (if target triggers recur action will not be executed).
			if(!RProcessGoal.PROCESS_STATE_SUCCEEDED.equals(procstate) && isRecur())
			{
				changeProcessingState(ACHIEVE_STATE_PROCESSING_PAUSED);
				scheduleRecur();
			}
			
			// Goal is finished.
			else
			{
				// Determine new processing state.
				String newstate = null;
	
				// When goal has target condition, let condition decide on final state.
				if(getTargetCondition()!=null)
				{
					newstate	= ACHIEVE_STATE_FAILED;
				}
	
				// Otherwise, change goal state to state of the process goal.
				else if(RProcessGoal.PROCESS_STATE_SUCCEEDED.equals(procstate))
				{
					newstate	= ACHIEVE_STATE_SUCCEEDED;
				}
				else
				{
					assert RProcessGoal.PROCESS_STATE_FAILED.equals(procstate) || RProcessGoal.PROCESS_STATE_ABORTED_ON_FAILURE.equals(procstate): this;
					newstate	= ACHIEVE_STATE_FAILED;
				}

				// Precondition assures that action is only executed, if target condition has not triggered.
				getScope().getAgent().getInterpreter().addAgendaEntry(new GoalFinishedAction(this,
					new GoalProcessingStatePrecondition(this, ACHIEVE_STATE_IN_PROCESS), newstate), this);
			}
		}
	}

	/**
	 *  Check if the goal is in some finished state.
	 *  @return True, if the goal is finished.
	 * /
	public boolean isFinished()
	{
		return ACHIEVE_STATE_SUCCEEDED.equals(processingstate)
				|| ACHIEVE_STATE_UNKNOWN.equals(processingstate)
				|| ACHIEVE_STATE_FAILED.equals(processingstate);
	}*/

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
		assert proc_transitions.getCollection(getProcessingState()).contains(newstate)
 			:"Cannot change processing state from "
			+ getProcessingState() + " to " + newstate + ": " + this.getName();

		if(newstate.equals(getProcessingState()))
			return;
		
		String oldstate = getProcessingState();
		setProcessingState(newstate);


		// exit in_process state.
		if(oldstate.equals(ACHIEVE_STATE_IN_PROCESS))
		{
			if(DELIBERATION)
			{
				getScope().getAgent().getInterpreter().addAgendaEntry(new DeliberateInhibitedGoalsReactivationAction(
					new GoalProcessingStatePrecondition(this, ACHIEVE_STATE_IN_PROCESS, true), this), this);
			}
		}

		// enter in_process state
		else if(newstate.equals(ACHIEVE_STATE_IN_PROCESS))
		{
			if(DELIBERATION)
			{
				getScope().getAgent().getInterpreter().addAgendaEntry(new DeactivateInhibitedGoalsAction(
					new GoalProcessingStatePrecondition(this, ACHIEVE_STATE_IN_PROCESS), this), this);
			}
		}
	}

	//-------- lifecycle handling --------

	/**
	 *  Entry action for adopted state.
	 */
	protected void	enterAdoptedState()
	{
		//if(getScope().getPlatformAgent().getLocalName().indexOf("cleany")!=-1)
		//	System.out.println(getScope().getPlatformAgent().getLocalName()+" adopted achieve: "+RAchieveGoal.this.getName()+" "+parameters.get("waste"));
		super.enterAdoptedState();

		if(isAdopted())
		{
			// Trace target condition.
			// Hack!!! Explicit checks not necessary with agenda preconditions.
			if(this.target!=null)
			{
				if(target.isTriggered())
				{
					setProcessingState(ACHIEVE_STATE_SUCCEEDED);
					drop();
				}
				else
				{
					this.target.setTraceMode(ICondition.TRACE_ONCE);
				}
			}

			// Trace failure condition.
			// Hack!!! Explicit checks not necessary with agenda preconditions.
			if(isAdopted() && this.failure!=null)
			{
				if(failure.isTriggered())
				{
					setProcessingState(ACHIEVE_STATE_FAILED);
					drop();
				}
				else
				{
					this.failure.setTraceMode(ICondition.TRACE_ONCE);
				}
			}
		}
	}

	/**
	 *  Exit action for adopted state.
	 */
	protected void	exitAdoptedState()
	{
		// Stop tracing.
		if(target!=null)	target.setTraceMode(ICondition.TRACE_NEVER);
		if(failure!=null)	failure.setTraceMode(ICondition.TRACE_NEVER);

		super.exitAdoptedState();
	}

	/**
	 *  Exit action for dropping state.
	 */
	protected void exitDroppingState()
	{
		// Set final state (hack???)
		if(!isFinished())
			setProcessingState(ACHIEVE_STATE_UNKNOWN);

		super.exitDroppingState();
	}
	
	/**
	 *  Enter active state.
	 */
	protected void	enterActiveState()
	{
		// Goal not reached: Start processing.
		throwProcessEvent();

		super.enterActiveState();
	}

	/**
	 *  Exit action for active state.
	 */
	protected void	exitActiveState()
	{
		if(ACHIEVE_STATE_IN_PROCESS.equals(processingstate)
			|| ACHIEVE_STATE_PROCESSING_PAUSED.equals(processingstate))
			setProcessingState(ACHIEVE_STATE_START);

		super.exitActiveState();
	}

	//-------- helper methods --------

	/**
	 *  Generate a property representation for encoding this element
	 *  (eg to SL).
	 *  @return A properties object representaing this element.
	 */
	public Map getEncodableRepresentation()
	{
		Map representation = super.getEncodableRepresentation();
		representation.put("processingstate", processingstate.equals(ACHIEVE_STATE_START)? "" : processingstate);
		representation.put("targetcondition", target == null ? ""
				: ((IMCondition)target.getModelElement()).getExpressionText());
		representation.put("failurecondition", failure == null ? ""
				: ((IMCondition)failure.getModelElement()).getExpressionText());
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
		buf.append(", state=");
		buf.append(processingstate);
		/*buf.append(", active=");
		buf.append(isActive());
		buf.append(", retry=");
		buf.append(isRetry());
		buf.append(", exclude=");
		buf.append(isExclude());
		buf.append(", mlreasoning=");
		buf.append(isMetaLevelReasoning());
		buf.append(", posttoallabort=");
		buf.append(isPostToAllAbort());*/
		buf.append(")");

/*		RAbstractGoal[] subgoals	= getChildren();
		for(int i=0; i<subgoals.length; i++)
		{
			buf.append("\n");
			buf.append(subgoals[i].toString(level+1));
		}
*/
		return buf.toString();
	}

}
