package jadex.adapter.standalone.planlib;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.standalone.StandaloneAgentAdapter;
import jadex.runtime.Plan;

/**
 *  Plan for starting a Jadex agent on the standalone platform.
 */
public class StandaloneAMSLocalStartAgentPlan extends Plan
{
	/**
	 *  Execute a plan.
	 */
	public void body()
	{
		AgentIdentifier	agentidentifier	= (AgentIdentifier)getParameter("agentidentifier").getValue();
		StandaloneAgentAdapter	adapter	= (StandaloneAgentAdapter)getScope().getPlatformAgent();

		try
		{
			adapter.getPlatform().getAMS().startAgent(agentidentifier);
		}
		catch(Exception e)
		{
			//e.printStackTrace();
			fail(e); // Do not show exception on console. 
		}
	}
}
