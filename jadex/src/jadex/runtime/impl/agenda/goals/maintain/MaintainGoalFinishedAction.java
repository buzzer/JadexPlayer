package jadex.runtime.impl.agenda.goals.maintain;

import jadex.runtime.ICondition;
import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.agenda.goals.GoalProcessingStatePrecondition;
import jadex.runtime.impl.agenda.goals.RecurAction;
import jadex.runtime.impl.*;
import jadex.util.SReflect;

/**
 *  The finished action.
 */
public class MaintainGoalFinishedAction extends AbstractElementAgendaAction implements java.io.Serializable
{
	//-------- attributes --------

	/** The goal. */
	protected RMaintainGoal goal;

	//-------- constructors --------

	/**
	 *  Create a new action.
	 */
	public MaintainGoalFinishedAction(RMaintainGoal goal, IAgendaActionPrecondition precond)
	{
		super(goal, precond);
		this.goal = goal;
	}

	//-------- methods --------

	/**
	 *  Execute the action.
	 */
	public void execute()
	{
		//if(getScope().getPlatformAgent().getLocalName().indexOf("cleany")!=-1)
		//	System.out.println(getScope().getPlatformAgent().getLocalName()+" achieved: "+RAchieveGoal.this.getName());
		goal.changeProcessingState(RMaintainGoal.MAINTAIN_STATE_UNMAINTAINABLE);
		goal.throwInfoEvent();
		if(goal.isRecur())
			goal.scheduleRecur();
		goal.getTargetCondition().setTraceMode(ICondition.TRACE_NEVER);
		goal.getMaintainCondition().traceOnce();
	}

	/**
	 *  Get the cause of this action.
	 *  @return the "cause" of this action
	 *  @see jadex.runtime.impl.agenda.IAgendaAction#getCause()
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
