package jadex.runtime.impl.agenda.goals.maintain;

import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.RMaintainGoal;
import jadex.util.SReflect;

/**
 *  The target action.
 */
public class TargetAction extends AbstractElementAgendaAction implements java.io.Serializable
{
	//-------- attributes --------

	/** The maintain goal. */
	private RMaintainGoal goal;

	//-------- constructors --------

	/**
	 *  Create a new action.
	 */
	public TargetAction(RMaintainGoal goal, IAgendaActionPrecondition precond)
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
//		String	processingstate	= goal.getProcessingState();
		//System.out.println("---- Target triggered: "+RMaintainGoal.this.getName());
		goal.changeProcessingState(RMaintainGoal.MAINTAIN_STATE_IDLE);

		goal.abortProcessGoals();
		// Dispatch a new info goal event.
		goal.throwInfoEvent();
		goal.clearExcludedCandidates();
		goal.getMaintainCondition().traceOnce();
		//System.out.println("target cond triggered of "+getScope()+"."+getName()+": State was "+processingstate+", new state is "+getProcessingState()+", history size is "+getHistoryEntries().size());
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
