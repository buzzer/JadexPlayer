package jadex.runtime.impl.agenda.goals;

import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.RGoal;
import jadex.util.SReflect;

/**
 *  The retry action.
 */
public class RetryAction extends AbstractElementAgendaAction implements java.io.Serializable
{
	//-------- attributes --------

	/** The goal. */
	protected RGoal goal;

	//-------- constructors --------

	/**
	 *  Create a new action.
	 */
	public RetryAction(RGoal goal, IAgendaActionPrecondition precond)
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
		//System.out.println("Retry process event thrown");
		goal.throwProcessEvent();
		goal.setRetryEntry(null);
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
