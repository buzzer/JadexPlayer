package jadex.runtime.impl;

import jadex.model.IMConfigGoal;
import jadex.model.IMPerformGoal;
import jadex.runtime.SystemEvent;
import jadex.runtime.impl.agenda.IAgendaAction;
import jadex.runtime.impl.agenda.easydeliberation.DeactivateInhibitedGoalsAction;
import jadex.runtime.impl.agenda.easydeliberation.DeliberateInhibitedGoalsReactivationAction;
import jadex.runtime.impl.agenda.goals.GoalFinishedAction;
import jadex.runtime.impl.agenda.goals.GoalProcessingStatePrecondition;
import jadex.runtime.impl.agenda.goals.RetryAction;
import jadex.util.SReflect;
import jadex.util.collection.MultiCollection;

import java.util.List;
import java.util.Map;

/**
 *  A perform goal is associated to the action of doing
 *  something. Therefore a perform goal is not associated
 *  with a target condition and has only a finished state,
 *  when the action is performed.
 */
public class RPerformGoal extends RGoal
{
	//-------- constants --------

	/* The state types of an achieve goal. */

	/** The goal is idle. */
	public static final String	PERFORM_STATE_START	= "start";

	/** The goal is in processing paused. */
	public static final String	PERFORM_STATE_PROCESSING_PAUSED	= "processing_paused";

	/** The goal is actually processed. */
	public static final String	PERFORM_STATE_IN_PROCESS	= "in_process";
	
	/** The goal is succeeded. */
	public static final String	PERFORM_STATE_SUCCEEDED	= "succeeded";
	
	/** The goal is failed. */
	public static final String	PERFORM_STATE_FAILED	= "failed";

	//-------- internal constants --------

	/** The allowed state tarnsitions (oldstate mapped to collection of new states). */
	protected static final MultiCollection	proc_transitions	= new MultiCollection();

	static
	{
		// Add allowed state transitions to transition table.
		proc_transitions.put(PERFORM_STATE_START, PERFORM_STATE_IN_PROCESS);
		proc_transitions.put(PERFORM_STATE_START, PERFORM_STATE_FAILED);
		proc_transitions.put(PERFORM_STATE_START, PERFORM_STATE_PROCESSING_PAUSED);
		proc_transitions.put(PERFORM_STATE_IN_PROCESS, PERFORM_STATE_IN_PROCESS);
		proc_transitions.put(PERFORM_STATE_IN_PROCESS, PERFORM_STATE_PROCESSING_PAUSED);
		proc_transitions.put(PERFORM_STATE_IN_PROCESS, PERFORM_STATE_SUCCEEDED);
		proc_transitions.put(PERFORM_STATE_IN_PROCESS, PERFORM_STATE_FAILED);
		proc_transitions.put(PERFORM_STATE_PROCESSING_PAUSED, PERFORM_STATE_IN_PROCESS);
		proc_transitions.put(PERFORM_STATE_PROCESSING_PAUSED, PERFORM_STATE_SUCCEEDED);
		proc_transitions.put(PERFORM_STATE_PROCESSING_PAUSED, PERFORM_STATE_FAILED);
	}

	//-------- attributes --------

	/** The processing state. */
	protected String processingstate;

	/** The retry action. */
	public IAgendaAction retryaction;

	/** The succeeded flag (set to true when the first plan completes successfully). */
	protected boolean	succeeded;
	
	//-------- constructors --------

	/**
	 *  Create a new goal.
	 * @param name The name.
	 * @param goal The model element.
	 * @param config The configuration.
	 * @param owner The owner.
	 * @param binding The binding.
	 */
	protected RPerformGoal(String name, IMPerformGoal goal, IMConfigGoal config,
			RElement owner, RReferenceableElement creator, Map binding)
	{
		super(name, goal, config, owner, creator, binding);
		this.retryaction = new RetryAction(this, new GoalProcessingStatePrecondition(this, PERFORM_STATE_PROCESSING_PAUSED));
		setProcessingState(PERFORM_STATE_START);
	}

	//-------- methods --------

	/**
	 *  Check if the goal is in some finished state.
	 *  @return True, if the goal is finished.
	 * /
	public boolean isFinished()
	{
		return PERFORM_STATE_SUCCEEDED.equals(processingstate)
			|| PERFORM_STATE_FAILED.equals(processingstate);
	}*/

	/**
	 *  Test if a goal is succeeded.
	 *  This has different meanings for the different goal types.
	 *  @return True, if goal is succeeded.
	 */
	public boolean isSucceeded()
	{
		return PERFORM_STATE_SUCCEEDED.equals(processingstate);
	}

	/**
	 *  Get the processing state.
	 *  @return True, if the goal is in process.
	 */
	public boolean	isInProcess()
	{
		return isActive() && PERFORM_STATE_IN_PROCESS.equals(processingstate);
	}

	//-------- update semantics --------

	/**
	 *  Called when new process goals for this goal are created.
	 *  @param pgoals The list with the new process goals.
	 */
	public void processGoalsCreated(List pgoals)
	{
		super.processGoalsCreated(pgoals);

		// When candidates found, goal is in process.
		if(!pgoals.isEmpty())
		{
			changeProcessingState(PERFORM_STATE_IN_PROCESS);
		}
		
		// When no candidates are found, but recur is set, recur the goal.
		else if(isRecur())
		{
			if(!PERFORM_STATE_PROCESSING_PAUSED.equals(getProcessingState()))
				changeProcessingState(PERFORM_STATE_PROCESSING_PAUSED);
			scheduleRecur();
		}

		// Handle goals without candidate and recur.
		else //if(pgoals.isEmpty())
		{
			changeProcessingState(succeeded ? PERFORM_STATE_SUCCEEDED : PERFORM_STATE_FAILED);
			drop();
		}
	}

	/**
	 *  Called from a process goal when it is finished.
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
		//if(isFinished() || getLifecycleState().equals(IGoal.LIFECYCLESTATE_SUSPENDED))
		if(isFinished() || PERFORM_STATE_START.equals(processingstate))
			return;
		
		assert isActive() : this;

		//if(this.getName().indexOf("")!=-1)
		//System.out.println("processGoal finished for: "+this.getName()+" "+procgoal);

		// Update the goal state.
		//copyContentFrom(procgoal);
		succeeded	= succeeded || RProcessGoal.PROCESS_STATE_SUCCEEDED.equals(procgoal.getProcessingState());
				
		// Retry the goal.
		if(!isPostToAll() && isRetry())
		{
			if(getRetryDelay()>0)
			{
				changeProcessingState(PERFORM_STATE_PROCESSING_PAUSED);
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
			if(isRecur())
			{
				changeProcessingState(PERFORM_STATE_PROCESSING_PAUSED);
				scheduleRecur();
			}
			else
			{
				// Determine new processing state.
				String newstate = succeeded ? PERFORM_STATE_SUCCEEDED : PERFORM_STATE_FAILED;
	
				// Precondition assures that action is only executed, if target condition has not triggered.
				getScope().getAgent().getInterpreter().addAgendaEntry(new GoalFinishedAction(this,
					new GoalProcessingStatePrecondition(this, PERFORM_STATE_IN_PROCESS), newstate), this);
			}
		}
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
		assert proc_transitions.getCollection(getProcessingState()).contains(newstate)
 			:"Cannot change processing state from "
			+ getProcessingState() + " to " + newstate + ": " + this.getName();

		if(newstate.equals(getProcessingState()))
			return;

		String oldstate = getProcessingState();
		setProcessingState(newstate);

		// exit in_process state.
		if(oldstate.equals(PERFORM_STATE_IN_PROCESS))
		{
			if(DELIBERATION)
			{
				getScope().getAgent().getInterpreter().addAgendaEntry(new DeliberateInhibitedGoalsReactivationAction(
					new GoalProcessingStatePrecondition(this, PERFORM_STATE_IN_PROCESS, true), this), this);
			}
		}

		// enter in_process state
		else if(newstate.equals(PERFORM_STATE_IN_PROCESS))
		{
			if(DELIBERATION)
			{
				getScope().getAgent().getInterpreter().addAgendaEntry(new DeactivateInhibitedGoalsAction(
					new GoalProcessingStatePrecondition(this, PERFORM_STATE_IN_PROCESS), this), this);
			}
		}
	}

	//-------- lifecycle handling --------

	/**
	 *  Enter active state.
	 */
	protected void	enterActiveState()
	{
		// Start processing.
		throwProcessEvent();

		super.enterActiveState();
	}

	/**
	 *  Exit action for active state.
	 */
	protected void	exitActiveState()
	{
		if(PERFORM_STATE_IN_PROCESS.equals(processingstate))
			setProcessingState(PERFORM_STATE_START);

		super.exitActiveState();
	}

	/**
	 *  Exit action for dropping state.
	 */
	protected void exitDroppingState()
	{
		// Set final state (hack???)
		if(!isFinished())
			setProcessingState(PERFORM_STATE_FAILED);

		super.exitDroppingState();
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
		representation.put("processingstate", processingstate.equals(PERFORM_STATE_START)?"":processingstate);
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
