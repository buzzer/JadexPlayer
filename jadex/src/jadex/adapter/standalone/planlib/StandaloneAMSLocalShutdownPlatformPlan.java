package jadex.adapter.standalone.planlib;

import jadex.adapter.standalone.StandaloneAgentAdapter;
import jadex.runtime.Plan;

/**
 *  Shutdown the platform.
 */
public class StandaloneAMSLocalShutdownPlatformPlan extends Plan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
		StandaloneAgentAdapter	adapter	= (StandaloneAgentAdapter)getScope().getPlatformAgent();
		adapter.getPlatform().shutdown();
	}
}
