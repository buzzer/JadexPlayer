package jadex.runtime.impl.agenda.plans;

import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.RPlan;

/**
 *  The plan termination action, triggered by the plan context condition.
 */
public class PlanTerminationAction extends AbstractElementAgendaAction implements java.io.Serializable
{
	//-------- attributes --------

	/** The plan. */
	protected RPlan plan;

	//-------- constructors --------

	/**
	 *  Create a new action.
	 */
	public PlanTerminationAction(RPlan plan, IAgendaActionPrecondition precond)
	{
		super(plan, precond);
		this.plan = plan;
	}

	//-------- methods --------

	/**
	 *  The action.
	 */
	public void execute()
	{
		plan.getRootGoal().fail(null);
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
