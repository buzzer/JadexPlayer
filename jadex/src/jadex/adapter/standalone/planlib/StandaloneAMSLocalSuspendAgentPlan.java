package jadex.adapter.standalone.planlib;

import jadex.runtime.*;
import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.standalone.StandaloneAgentAdapter;

/**
 *  Plan for suspending a Jadex agent on the standalone platform.
 */
public class StandaloneAMSLocalSuspendAgentPlan extends Plan
{
	/**
	 *  Execute a plan.
	 */
	public void body()
	{
		AgentIdentifier	aid	= (AgentIdentifier)getParameter("agentidentifier").getValue();
		StandaloneAgentAdapter	adapter	= (StandaloneAgentAdapter)getScope().getPlatformAgent();
		getParameter("agentdescription").setValue(adapter.getPlatform().getAMS().getAgentDescription(aid));
		adapter.getPlatform().getAMS().suspendAgent(aid);
		getParameter("agentdescription").setValue(adapter.getPlatform().getAMS().getAgentDescription(aid));
	}
}
