package jadex.runtime.impl.agenda.goals.maintain;

import jadex.runtime.impl.agenda.IAgendaActionPrecondition;
import jadex.runtime.impl.RMaintainGoal;
import jadex.runtime.ICondition;

/**
 *  The retry action.
 */
public class MaintainGoalRetryAction extends jadex.runtime.impl.agenda.goals.RetryAction
{
	private RMaintainGoal goal;

	/**
	 *  Create a new action.
	 */
	public MaintainGoalRetryAction(RMaintainGoal goal, IAgendaActionPrecondition precond)
	{
		super(goal, precond);
		this.goal = goal;
	}

	/**
	 *  The action.
	 */
	public void execute()
	{
		assert RMaintainGoal.MAINTAIN_STATE_PROCESSING_PAUSED.equals(
			goal.getProcessingState()) : goal.getProcessingState();

		// Throw process event.
		super.execute();

		goal.getMaintainCondition().setTraceMode(ICondition.TRACE_NEVER);
		goal.getTargetCondition().setTraceMode(ICondition.TRACE_ONCE);
	}

	/**
	 * @return the "cause" of this action
	 * @see jadex.runtime.impl.agenda.IAgendaAction#getCause()
	 * /
	public Object getCause()
	{
		return getName();
	}*/
}
