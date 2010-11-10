package jadex.adapter.standalone.planlib;

import jadex.runtime.*;
import jadex.adapter.fipa.*;

/**
 *  Search for agents on a remote platform.
 */
public class StandaloneAMSRemoteSearchAgentsPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		AMSSearchAgents sa = new AMSSearchAgents();
		sa.setAgentDescription((AMSAgentDescription)getParameter("description").getValue());
		sa.setSearchConstraints((SearchConstraints)getParameter("constraints").getValue());

		IGoal req = createGoal("rp_initiate");
		req.getParameter("receiver").setValue(getParameter("ams").getValue());
		req.getParameter("action").setValue(sa);
		dispatchSubgoalAndWait(req);

		getParameterSet("result").addValues(((AMSSearchAgents)((Done)req.getParameter("result")
			.getValue()).getAction()).getAgentDescriptions());
	}
}

