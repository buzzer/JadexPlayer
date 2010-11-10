package jadex.adapter.standalone.planlib;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.standalone.StandaloneAgentAdapter;
import jadex.runtime.Plan;

/**
 *  Plan for resuming a Jadex agent on the standalone platform.
 */
public class StandaloneAMSLocalResumeAgentPlan extends Plan
{
	/**
	 *  Execute a plan.
	 */
	public void body()
	{
		AgentIdentifier	aid	= (AgentIdentifier)getParameter("agentidentifier").getValue();
		StandaloneAgentAdapter	adapter	= (StandaloneAgentAdapter)getScope().getPlatformAgent();
		getParameter("agentdescription").setValue(adapter.getPlatform().getAMS().getAgentDescription(aid));
		adapter.getPlatform().getAMS().resumeAgent(aid);
		getParameter("agentdescription").setValue(adapter.getPlatform().getAMS().getAgentDescription(aid));
	}
}
