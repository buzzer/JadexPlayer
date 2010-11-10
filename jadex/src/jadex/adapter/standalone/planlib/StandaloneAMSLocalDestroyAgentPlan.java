package jadex.adapter.standalone.planlib;

import jadex.runtime.*;
import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.standalone.StandaloneAgentAdapter;


/**
 *  Plan for terminating a Jadex agent on the standalone platform.
 */
public class StandaloneAMSLocalDestroyAgentPlan extends Plan
{
	/**
	 *  Execute a plan.
	 */
	public void body()
	{	
		AgentIdentifier	aid	= (AgentIdentifier)getParameter("agentidentifier").getValue();
		StandaloneAgentAdapter	adapter	= (StandaloneAgentAdapter)getScope().getPlatformAgent();
		try
		{
			adapter.getPlatform().getAMS().destroyAgent(aid);
			ICondition cond = getCondition("agent_removed");
			cond.setParameter("aid", aid);
			waitForCondition(cond);
		}
		catch(Exception e)
		{
			//e.printStackTrace();
			fail(e);
		}
	}
}
