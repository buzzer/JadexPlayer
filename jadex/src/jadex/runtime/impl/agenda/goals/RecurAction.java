package jadex.runtime.impl.agenda.goals;

import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.RGoal;
import jadex.util.SReflect;

/**
 *  The recur action.
 */
public class RecurAction extends AbstractElementAgendaAction implements java.io.Serializable
{
	//-------- attributes --------

	/** The goal. */
	protected RGoal goal;

	//-------- constructors --------

	/**
	 *  Create a new action.
	 */
	public RecurAction(RGoal goal, IAgendaActionPrecondition precond)
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
		goal.doRecur();
	}

	/**
	  *  Get the string representation.
	 *  @return The string representation.
	 */
	public  String toString()
	{
		return SReflect.getInnerClassName(this.getClass())+" (goal="+goal.getName()+")";
	}
}
