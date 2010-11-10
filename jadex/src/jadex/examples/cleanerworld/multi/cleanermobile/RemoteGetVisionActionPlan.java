package jadex.examples.cleanerworld.multi.cleanermobile;

import jadex.adapter.fipa.AgentAction;
import jadex.adapter.fipa.Done;
import jadex.examples.cleanerworld.multi.*;
import jadex.runtime.IGoal;


/**
 *  Get the vision.
 */
public class RemoteGetVisionActionPlan extends RemoteActionPlan
{
	/**
	 *  Return the action to be requested.
	 */
	protected AgentAction	getAction()
	{
		Cleaner cl = new Cleaner((Location)getBeliefbase().getBelief("my_location").getFact(), getAgentName(),
			(Waste)getBeliefbase().getBelief("carriedwaste").getFact(),
			((Number)getBeliefbase().getBelief("my_vision").getFact()).doubleValue(),
			((Number)getBeliefbase().getBelief("my_chargestate").getFact()).doubleValue());

		RequestVision rv = new RequestVision();
		rv.setCleaner(cl);
		return rv;
	}
	
	/**
	 *  Handle the result (if any).
	 */
	protected void handleResult(Object result)
	{
		Vision vision = ((RequestVision)(((Done)result).getAction())).getVision();
		getParameter("vision").setValue(vision);
	}
}
