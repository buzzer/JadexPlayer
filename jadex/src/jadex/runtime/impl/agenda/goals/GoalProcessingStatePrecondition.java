package jadex.runtime.impl.agenda.goals;

import jadex.runtime.impl.agenda.IAgendaActionPrecondition;
import jadex.runtime.impl.IRGoal;

/**
 *  Test the processing state.
 */
public class GoalProcessingStatePrecondition implements IAgendaActionPrecondition
{
	//-------- attributes --------

	/** The goal. */
	private IRGoal goal;

	/** The state. */
	protected String state;

	/** True for negating the precondition. */
	protected boolean not;

	//-------- constructors --------

	/**
	 *  Create a new precondition.
	 */
	public GoalProcessingStatePrecondition(IRGoal goal, String state)
	{
		this(goal, state, false);
	}

	/**
	 *  Create a new precondition.
	 */
	public GoalProcessingStatePrecondition(IRGoal goal, String state, boolean not)
	{
		this.goal = goal;
		this.state = state;
		this.not = not;
	}

	//-------- methods --------

	/**
	 * Test, if the precondition is valid.
	 * @return True, if precondition is valid.
	 */
	public boolean check()
	{
		return not? goal.isActive() && !goal.getProcessingState().equals(state)
			: goal.isActive() && goal.getProcessingState().equals(state);
	}
}
