package jadex.adapter.standalone.planlib;

import jadex.runtime.*;
import jadex.adapter.fipa.*;

/**
 *  Destroy an agent on a remote ams.
 */
public class StandaloneAMSRemoteDestroyAgentPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		AMSDestroyAgent da = new AMSDestroyAgent();
		da.setAgentIdentifier((AgentIdentifier)getParameter("agentidentifier").getValue());

		IGoal req = createGoal("rp_initiate");
		req.getParameter("receiver").setValue(getParameter("ams").getValue());
		req.getParameter("action").setValue(da);
		dispatchSubgoalAndWait(req);
	}
}
