package jadex.adapter.standalone.planlib;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.standalone.StandaloneAgentAdapter;
import jadex.runtime.Plan;

import java.util.Map;

/**
 *  Plan for creating a Jadex agent on the standalone platform.
 */
public class StandaloneAMSLocalCreateAgentPlan extends Plan
{
	/**
	 *  Execute a plan.
	 */
	public void body()
	{
		String	type	= (String)getParameter("type").getValue();
		String	name	= (String)getParameter("name").getValue();
		String	config	= (String)getParameter("configuration").getValue();
		Map	args	= (Map)getParameter("arguments").getValue();
		boolean	start	= ((Boolean)getParameter("start").getValue()).booleanValue();

		StandaloneAgentAdapter	adapter	= (StandaloneAgentAdapter)getScope().getPlatformAgent();

		try
		{
			AgentIdentifier	aid	= adapter.getPlatform().getAMS().createAgent(name, type, config, args);
			getParameter("agentidentifier").setValue(aid);
			if(start)
				adapter.getPlatform().getAMS().startAgent(aid);
		}
		catch(Exception e)
		{
			//e.printStackTrace();
			fail(e); // Do not show exception on console. 
		}
	}
}
