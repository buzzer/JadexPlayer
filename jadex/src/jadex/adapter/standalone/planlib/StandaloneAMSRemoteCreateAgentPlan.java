package jadex.adapter.standalone.planlib;

import java.util.Map;

import jadex.runtime.*;
import jadex.adapter.fipa.*;

/**
 *  Create an agent on a remote ams.
 */
public class StandaloneAMSRemoteCreateAgentPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		AMSCreateAgent ca = new AMSCreateAgent();
		ca.setType((String)getParameter("type").getValue());
		ca.setName((String)getParameter("name").getValue());
		ca.setConfiguration((String)getParameter("configuration").getValue());
		ca.setArguments((Map)getParameter("arguments").getValue());
		ca.setStart(((Boolean)getParameter("start").getValue()).booleanValue());


		IGoal req = createGoal("rp_initiate");
		req.getParameter("receiver").setValue(getParameter("ams").getValue());
		req.getParameter("action").setValue(ca);
		dispatchSubgoalAndWait(req);

		getParameter("agentidentifier").setValue(((AMSCreateAgent)((Done)req.getParameter("result").getValue()).getAction()).getAgentIdentifier());
	}
}
