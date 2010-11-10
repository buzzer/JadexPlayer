package jadex.runtime.impl.agenda.goals;

import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.*;
import jadex.util.SReflect;

/**
 *  The finished action.
 */
public class GoalFinishedAction extends AbstractElementAgendaAction implements java.io.Serializable
{
	//-------- attributes --------

	/** The goal. */
	protected RGoal goal;

	/** The state. */
	protected String state;

	//-------- constructors --------

	/**
	 *  Create a new action.
	 */
	public GoalFinishedAction(RGoal goal, IAgendaActionPrecondition precond, String state)
	{
		super(goal, precond);
		this.goal = goal;
		this.state = state;
	}

	//-------- methods --------

	/**
	 *  Execute the action.
	 */
	public void execute()
	{
		//if(getScope().getPlatformAgent().getLocalName().indexOf("cleany")!=-1)
		//	System.out.println(getScope().getPlatformAgent().getLocalName()+" achieved: "+RAchieveGoal.this.getName());
		goal.changeProcessingState(state);
		goal.drop();
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
		return SReflect.getInnerClassName(this.getClass())+" (goal="+goal.getName()+", state="+state+")";
	}
}
