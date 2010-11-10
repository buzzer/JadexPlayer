package jadex.runtime.impl.agenda.goals;

import jadex.util.SReflect;
import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.*;

/**
 *  The drop a goal action.
 */
public class AbortProcessGoalAction extends AbstractElementAgendaAction implements java.io.Serializable
{
	//-------- attributes --------

	/** The goal to drop. */
	protected RProcessGoal goal;
	
	/** The flag to indicate that the goal is aborted due to succeeded original goal. */
	protected boolean abort_on_success;

	//-------- constructors --------

	/**
	 *  Create a new action.
	 */
	public AbortProcessGoalAction(IAgendaActionPrecondition precond, RProcessGoal goal, boolean aborted_on_success)
	{
		super(goal, precond);
		this.goal = goal;
		this.abort_on_success	= aborted_on_success;
	}

	//-------- methods --------

	/**
	 *  The action.
	 */
	public void execute()
	{
		assert !goal.isFinished();

		goal.abort(abort_on_success);
	}

	/**
	 *  Get the goal.
	 */
	protected RProcessGoal getGoal()
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

