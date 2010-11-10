package jadex.examples.cleanerworld.multi.cleanermobile;

import jadex.adapter.fipa.AgentAction;
import jadex.examples.cleanerworld.multi.RequestPickUpWaste;
import jadex.examples.cleanerworld.multi.Waste;

/**
 *  Pick up a piece of waste in the environment.
 */
public class RemotePickUpWasteActionPlan extends RemoteActionPlan
{

	/**
	 *  Return the action to be requested.
	 */
	protected AgentAction	getAction()
	{
		Waste waste = (Waste)getParameter("waste").getValue();
		RequestPickUpWaste rp = new RequestPickUpWaste();
		rp.setWaste(waste);
		return rp;
	}
}
