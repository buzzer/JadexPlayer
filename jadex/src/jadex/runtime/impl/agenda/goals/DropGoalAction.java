package jadex.runtime.impl.agenda.goals;

import jadex.util.SReflect;
import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.*;

/**
 *  The drop a goal action.
 */
public class DropGoalAction extends AbstractElementAgendaAction implements java.io.Serializable
{
	//-------- attributes --------

	/** The goal to drop. */
	protected IRGoal goal;

	//-------- constructors --------

	/**
	 *  Create a new action.
	 */
	public DropGoalAction(IAgendaActionPrecondition precond, IRGoal goal)
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
		assert !goal.isFinished();
		goal.drop();
	}

	/**
	 *  Get the goal.
	 */
	protected IRGoal getGoal()
	{
		return goal;
	}

	/**
	 * @return the "cause" of this action
	 * @see jadex.runtime.impl.agenda.IAgendaAction#getCause()
	 * /
	public Object getCause()
	{
		return goal.getName();
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

