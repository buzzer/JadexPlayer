package jadex.runtime.impl.agenda.goals;

import jadex.runtime.impl.IRGoal;
import jadex.runtime.impl.agenda.IAgendaActionPrecondition;

/**
 *  Verify that a goal is not finsihed.
 */
public class GoalNotFinishedPrecondition implements IAgendaActionPrecondition
{
	//-------- attributes --------

	/** The goal. */
	protected IRGoal goal;

	//-------- constructors --------

	 /**
	 *  Create a new precondition.
	 */
	public GoalNotFinishedPrecondition(IRGoal goal)
	{
		this.goal = goal;
	}

	//-------- methods --------

	/**
	 * Test, if the precondition is valid.
	 * @return True, if precondition is valid.
	 */
	public boolean check()
	{
		return !goal.isFinished();
	}
}
