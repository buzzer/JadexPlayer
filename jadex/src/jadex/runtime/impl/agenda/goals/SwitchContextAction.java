package jadex.runtime.impl.agenda.goals;

import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.RGoal;
import jadex.runtime.ICondition;
import jadex.util.SReflect;

/**
 *  The action that will be executed when the context condition triggers.
 */
public class SwitchContextAction extends AbstractElementAgendaAction implements java.io.Serializable
{
	//-------- attributes --------

	/** The goal. */
	protected RGoal goal;

	//-------- constructors --------

	/**
	 *  Create a new action.
	 */
	public SwitchContextAction(RGoal goal, IAgendaActionPrecondition precond)
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
		//if(((IMCondition)context.getModelElement()).getExpressionText().startsWith("!$beliefbase.is_cleaning"))

		// Hack? Should allow user specified trigger conditions?!
		// Trigger flag is set in enter/exitSuspendedState
		if(goal.getContextCondition().getTrigger().equals(ICondition.TRIGGER_CHANGES_TO_FALSE))
		{
			//System.out.println("Suspending: "+getName()+" "+context.getTrigger());
			goal.suspend();
		}
		else
		{
			//System.out.println("Reactivating: "+getName()+" "+context.getTrigger());
			goal.option();
		}
	}

	/**
	 *  Get the cause for this action.
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
