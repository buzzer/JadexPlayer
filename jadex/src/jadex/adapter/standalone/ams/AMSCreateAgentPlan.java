package jadex.adapter.standalone.ams;

import jadex.runtime.*;
import jadex.adapter.fipa.*;

/**
 *  Create an agent.
 */
public class AMSCreateAgentPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		AMSCreateAgent ca = (AMSCreateAgent)getParameter("action").getValue();

		IGoal cag = createGoal("ams_create_agent");
		cag.getParameter("name").setValue(ca.getName());
		cag.getParameter("type").setValue(ca.getType());
		cag.getParameter("configuration").setValue(ca.getConfiguration());
		cag.getParameter("arguments").setValue(ca.getArguments());
		dispatchSubgoalAndWait(cag);

		ca.setAgentIdentifier((AgentIdentifier)cag.getParameter("agentidentifier").getValue());
		getParameter("result").setValue(new Done(ca));
	}
}
