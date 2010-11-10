package jadex.runtime.impl.agenda.plans;

import jadex.runtime.SystemEvent;
import jadex.runtime.impl.*;
import jadex.runtime.impl.agenda.*;
import jadex.model.*;
import jadex.util.SReflect;

/**
 *  Action to generate execute plan events possibly with a binding.
 *  This action is used from creation consitions of plans.
 */
public class PlanCreationAction extends BindingAction
{
	//-------- attributes --------

	/** The planbase. */
	protected RPlanbase planbase;

	/** The plan. */
	protected IMPlan	plan;
	
	//-------- constructors --------

	/**
	 *  Create a plan binding action.
	 */
	public PlanCreationAction(RPlanbase planbase, IAgendaActionPrecondition precond, IMPlan plan)
	{
		super(precond);
		this.planbase = planbase;
		this.plan = plan;
	}

	//-------- methods --------

	/**
	 *  Execute the action.
	 */
	public void	execute()
	{
		// Create execute-plan event.
		RCapability	scope	= planbase.getScope();
		IMInternalEvent mevent = ((IMCapability)scope.getModelElement())
			.getEventbase().getInternalEvent(IMEventbase.TYPE_EXECUTEPLAN);
		RInternalEvent event = scope.getEventbase().createInternalEvent(mevent);
		PlanInfo	cand	= new PlanInfo(null, event, plan, getBinding());
		event.getParameter("candidate").setValue(cand);

		// Test if the plan conditions hold.
		if(planbase.checkPlanApplicability(cand, event, null))
		{
			// Dispatch event.
			// Todo: Use precondition of creation condition?
			//scope.dispatchEvent(event, new DefaultPrecondition(planbase));
			RPlan rplan = RPlanbase.scheduleCandidate(cand);
			
			// The cause of a conditional triggered action is the corresponding system event
			Object cause = scope.getAgent().getInterpreter().getCurrentAgendaEntry().getCause();
			if(cause instanceof SystemEvent)
			{
				// Adds the values for $addedfact / $removedfact to the expression parameters
				SystemEvent se = (SystemEvent)cause;
				if(se.getType().equals(ISystemEventTypes.BSFACT_ADDED))
					rplan.setExpressionParameter("$addedfact", cause);
				else if(se.getType().equals(ISystemEventTypes.BSFACT_REMOVED))
					rplan.setExpressionParameter("$removedfact", cause);
			}
		}
	}

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public  String toString()
	{
		return SReflect.getInnerClassName(this.getClass())+" (plan="+plan.getName()+")";
	}
}
