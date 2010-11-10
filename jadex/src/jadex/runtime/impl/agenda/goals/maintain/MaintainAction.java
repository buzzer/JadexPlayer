package jadex.runtime.impl.agenda.goals.maintain;

import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.RMaintainGoal;
import jadex.runtime.ICondition;
import jadex.util.SReflect;

/**
 *  The maintain action.
 */
public class MaintainAction extends AbstractElementAgendaAction implements java.io.Serializable
{
	//-------- attributes --------

	/** The maintain goal. */
	private RMaintainGoal goal;

	//-------- constructors --------

	/**
	 *  Create a new action.
	 */
	public MaintainAction(RMaintainGoal goal, IAgendaActionPrecondition precond)
	{
		super(goal, precond);
		this.goal = goal;
	}

	//-------- methods --------

	/**
	 *  The action.
	 */
	public void execute()
	{
		// Maintain condition must not trigger when in-process.
		assert !RMaintainGoal.MAINTAIN_STATE_IN_PROCESS.equals(goal.getProcessingState());

		// Start an 'achieve goal' when condition
		// is triggered and state is idle, unmaintainable or unknown.
		Boolean lastvalue = goal.getMaintainCondition().getLastValue();
		String	processingstate	= goal.getProcessingState();
		boolean	process	= false;
		assert lastvalue!=null;

		if(RMaintainGoal.MAINTAIN_STATE_IDLE.equals(processingstate))
		{
			// Maintain condition violated:
			// -> transition from idle->in process
			if(!lastvalue.booleanValue())
			{
				//System.out.println("idle->maintain  process event thrown "+lastvalue+" "
				//	+getName()+" "+getTargetCondition());

				// If already achieved, do not generate process event.
				if(goal.getTargetCondition().isTriggered())
				{
					goal.getTargetCondition().execute();
				}
				else
				{
					goal.throwProcessEvent();
					process	= true;
					goal.getTargetCondition().setTraceMode(ICondition.TRACE_ONCE);
				}
			}

			// False alarm, maintain condition still valid
			// -> keep monitoring.
			else
			{
				goal.getMaintainCondition().traceOnce();
			}
		}
		else if(RMaintainGoal.MAINTAIN_STATE_PROCESSING_PAUSED.equals(processingstate)
			|| RMaintainGoal.MAINTAIN_STATE_UNMAINTAINABLE.equals(processingstate)
			|| RMaintainGoal.MAINTAIN_STATE_UNKNOWN.equals(processingstate))
		{
			// Invalid condition has become valid again
			// -> just change state to idle.
			if(lastvalue.booleanValue())
			{
				// Remove obsolete retry entry.
				if(goal.isRetry() && RMaintainGoal.MAINTAIN_STATE_PROCESSING_PAUSED.equals(processingstate))
				{
					// Todo: remove entry from agenda when already scheduled.
					goal.getScope().getAgent().removeTimetableEntry(goal.getRetryEntry());
				}

				// Remove obsolete recur entry.
				else if(goal.isRecur() && !RMaintainGoal.MAINTAIN_STATE_PROCESSING_PAUSED.equals(processingstate))
				{
					// Todo: remove entry from agenda when already scheduled.
					goal.getScope().getAgent().removeTimetableEntry(goal.getRecurEntry());
				}

				goal.changeProcessingState(RMaintainGoal.MAINTAIN_STATE_IDLE);
			}

			// Keep monitoring in any case.
			goal.getMaintainCondition().traceOnce();
		}

		//System.out.println("maintain cond triggered of "+getScope()+"."+getName()+": State was "+processingstate+", condition is "+lastvalue+", new state is "+getProcessingState()+", history size is "+getHistoryEntries().size()+", processing is "+process);
		assert lastvalue.booleanValue() || !RMaintainGoal.MAINTAIN_STATE_IDLE.equals(goal.getProcessingState()) || process : this;
	}

	/**
	* @return the "cause" of this action
	* @see jadex.runtime.impl.agenda.IAgendaAction#getCause()
	* /
	public Object getCause()
	{
		return getName();
	}*/

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public  String toString()
	{
		return SReflect.getInnerClassName(this.getClass())+" (goal="+goal.getName()+")";
	}
}
