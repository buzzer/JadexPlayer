package jadex.runtime.impl.agenda.plans;

import jadex.util.*;
import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.*;

/**
 *  The plan step action.
 */
public class ExecutePlanStepAction extends AbstractElementAgendaAction implements java.io.Serializable
{
	//-------- attributes --------

	/** The plan. */
	protected RPlan plan;

	//-------- constructors --------

	/**
	 *  Create a nwe process event action.
	 */
	public ExecutePlanStepAction(RPlan plan)
	{
		super(plan, new DefaultPlanActionPrecondition(plan));
		this.plan = plan;
	}

	//-------- methods --------

	/**
	 *  The action.
	 */
	public void execute()
	{
		assert plan.isAlive(): "Executing inactive plan: " + plan + ", " + plan.getRootGoal();

		// When executing body, context must be valid and root goal must be active
		Boolean	c;
		assert !plan.getState().equals(RPlan.STATE_BODY)
			|| (plan.getContextCondition()==null || (c=plan.getContextCondition().evaluate())!=null && c.booleanValue())
			&& !plan.getRootGoal().isFinished() && (plan.getRootGoal().getProprietaryGoal()==null || plan.getRootGoal().getProprietaryGoal().isActive()) : plan;
		
		plan.executePlanStep();
	}

	/**
	 *  Get the plan.
	 *  @return The plan.
	 */
	public RPlan getPlan()
	{
		return this.plan;
	}
  
	/**
	* @return the "cause" of this action
	* @see jadex.runtime.impl.agenda.IAgendaAction#getCause()
	* /
	public Object getCause()
	{
		return plan.getName();
	}*/
  

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public  String toString()
	{
		return SReflect.getInnerClassName(this.getClass())+"( plan="+plan.getName()+")";
	}
}
