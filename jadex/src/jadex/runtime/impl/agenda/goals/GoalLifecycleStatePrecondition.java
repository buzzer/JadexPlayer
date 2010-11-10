package jadex.runtime.impl.agenda.goals;

import java.util.HashSet;

import jadex.runtime.IGoal;
import jadex.runtime.impl.agenda.IAgendaActionPrecondition;
import jadex.runtime.impl.IRGoal;
import jadex.util.collection.SCollection;

/**
 *  Test the lifecycle state.
 */
public class GoalLifecycleStatePrecondition implements IAgendaActionPrecondition
{
	//-------- attributes --------

	/** The goal. */
	protected IRGoal goal;

	/** The states. */
	protected HashSet states;

	/** True for negating the precondition. */
	protected boolean not;

	//-------- constructors --------

 	/**
	 *  Create a new precondition.
	 */
	public GoalLifecycleStatePrecondition(IRGoal goal, String state)
	{
		this(goal, state, false);
	}

	/**
	 *  Create a new precondition.
	 */
	public GoalLifecycleStatePrecondition(IRGoal goal, String state, boolean not)
	{
		this.goal = goal;
		this.not = not;
		this.states = SCollection.createHashSet();
		states.add(state);
		if(state.equals(IGoal.LIFECYCLESTATE_ADOPTED))
		{
			states.add(IGoal.LIFECYCLESTATE_OPTION);
			states.add(IGoal.LIFECYCLESTATE_ACTIVE);
			states.add(IGoal.LIFECYCLESTATE_SUSPENDED);
		}
	}

	//-------- methods --------

	/**
	 * Test, if the precondition is valid.
	 * @return True, if precondition is valid.
	 */
	public boolean check()
	{
		return not? !states.contains(goal.getLifecycleState()): states.contains(goal.getLifecycleState());
	}
}
