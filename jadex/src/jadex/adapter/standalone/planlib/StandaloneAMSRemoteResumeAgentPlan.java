package jadex.adapter.standalone.planlib;

import jadex.runtime.*;
import jadex.adapter.fipa.*;

/**
 *  Suspend an agent on a remote ams.
 */
public class StandaloneAMSRemoteResumeAgentPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		AMSResumeAgent ra = new AMSResumeAgent();
		ra.setAgentIdentifier((AgentIdentifier)getParameter("agentidentifier").getValue());

		IGoal req = createGoal("rp_initiate");
		req.getParameter("receiver").setValue(getParameter("ams").getValue());
		req.getParameter("action").setValue(ra);
		dispatchSubgoalAndWait(req);
	}
}
