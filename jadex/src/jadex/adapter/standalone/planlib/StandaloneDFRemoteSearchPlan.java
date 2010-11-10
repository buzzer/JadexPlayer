package jadex.adapter.standalone.planlib;

import jadex.runtime.*;
import jadex.adapter.fipa.*;

/**
 *  Search at a remote DF.
 */
public class StandaloneDFRemoteSearchPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		DFSearch se = new DFSearch();
		se.setAgentDescription((AgentDescription)getParameter("description").getValue());
		se.setSearchConstraints((SearchConstraints)getParameter("constraints").getValue());

		IGoal req = createGoal("rp_initiate");
		req.getParameter("receiver").setValue(getParameter("df").getValue());
		req.getParameter("action").setValue(se);
		dispatchSubgoalAndWait(req);

		getParameterSet("result").addValues(((DFSearch)((Done)req.getParameter("result").getValue()).getAction()).getResults());
	}
}
