package jadex.examples.cleanerworld.multi.cleanermobile;

import jadex.adapter.fipa.AgentAction;
import jadex.examples.cleanerworld.multi.RequestDropWaste;
import jadex.examples.cleanerworld.multi.Waste;
import jadex.examples.cleanerworld.multi.Wastebin;

/**
 *  Pick up a piece of waste in the environment.
 */
public class RemoteDropWasteActionPlan extends RemoteActionPlan
{
	//-------- methods --------

	/**
	 *  Return the action to be requested.
	 */
	protected AgentAction getAction()
	{
		Waste waste = (Waste)getParameter("waste").getValue();
		Wastebin wastebin = (Wastebin)getParameter("wastebin").getValue();

		RequestDropWaste rd = new RequestDropWaste();
		rd.setWaste(waste);
		rd.setWastebinname(wastebin.getName());
		return rd;
	}
}
