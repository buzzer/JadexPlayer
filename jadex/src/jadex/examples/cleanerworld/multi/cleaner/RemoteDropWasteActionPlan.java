package jadex.examples.cleanerworld.multi.cleaner;

import jadex.examples.cleanerworld.multi.*;
import jadex.runtime.PlanFailureException;

/**
 *  Pick up a piece of waste in the environment.
 */
public class RemoteDropWasteActionPlan extends RemoteActionPlan
{
	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		Waste waste = (Waste)getParameter("waste").getValue();
		Wastebin wastebin = (Wastebin)getParameter("wastebin").getValue();

		// Search and store the environment agent.
		if(getBeliefbase().getBelief("environmentagent").getFact()==null)
			searchEnvironmentAgent();
		if(getBeliefbase().getBelief("environmentagent").getFact()==null)
			throw new PlanFailureException();

		RequestDropWaste rd = new RequestDropWaste();
		rd.setWaste(waste);
		rd.setWastebinname(wastebin.getName());
		requestAction(rd);
	}
}
