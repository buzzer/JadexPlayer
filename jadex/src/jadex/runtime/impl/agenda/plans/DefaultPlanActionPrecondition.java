package jadex.runtime.impl.agenda.plans;

import jadex.runtime.impl.RPlan;
import jadex.runtime.impl.agenda.IAgendaActionPrecondition;


/**
 *  Default condition for plan related meta actions is
 *  that the plan step (plan state) is still valid,
 *  e.g. the plan was not aborted in meantime.
 */
public class DefaultPlanActionPrecondition implements IAgendaActionPrecondition
{
	//-------- attributes --------

	/** The plan. */
	protected RPlan plan;

	/** The plan state. */
	protected String state;

	//-------- constructors --------
	
	/**
	 *  Create a default condition for a given plan.
	 */
	public DefaultPlanActionPrecondition(RPlan plan)
	{
		this.plan = plan;
		this.state	= plan.getState();
	}
	
	//-------- IAgendaActionPrecondition interface --------

	/**
	 *  Test, if the precondition is valid.
	 *  @return True, if precondition is valid.
	 */
	public boolean check()
	{
		// State must be the same (e.g. plan not aborted in meantime).
		return state.equals(plan.getState());
	}
}


