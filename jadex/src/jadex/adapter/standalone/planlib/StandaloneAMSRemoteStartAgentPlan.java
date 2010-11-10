package jadex.adapter.standalone.planlib;

import jadex.adapter.fipa.AMSStartAgent;
import jadex.adapter.fipa.AgentIdentifier;
import jadex.runtime.IGoal;
import jadex.runtime.Plan;

/**
 *  Start an agent on a remote ams.
 */
public class StandaloneAMSRemoteStartAgentPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		AMSStartAgent sa = new AMSStartAgent();
		sa.setAgentIdentifier((AgentIdentifier)getParameter("agentidentifier").getValue());

		IGoal req = createGoal("rp_initiate");
		req.getParameter("receiver").setValue(getParameter("ams").getValue());
		req.getParameter("action").setValue(sa);
		dispatchSubgoalAndWait(req);
	}
}
