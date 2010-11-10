package jadex.adapter.standalone.ams;

import jadex.runtime.*;
import jadex.adapter.fipa.*;

/**
 *  Search for agents.
 */
public class AMSSearchAgentsPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		AMSSearchAgents sa = (AMSSearchAgents)getParameter("action").getValue();

		IGoal sag = createGoal("ams_search_agents");
		sag.getParameter("description").setValue(sa.getAgentDescription());
		sag.getParameter("constraints").setValue(sa.getSearchConstraints());
		dispatchSubgoalAndWait(sag);

		sa.setAgentDescriptions((AMSAgentDescription[])sag.getParameterSet("result").getValues());
		getParameter("result").setValue(new Done(sa));
	}
}
