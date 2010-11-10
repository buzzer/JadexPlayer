package jadex.adapter.standalone.planlib;

import jadex.adapter.fipa.*;
import jadex.adapter.standalone.StandaloneAgentAdapter;
import jadex.runtime.Plan;


/**
 *  Plan for searching for agents on the standalone platform.
 */
public class StandaloneAMSLocalSearchAgentsPlan extends Plan
{
	/**
	 *  Execute a plan.
	 */
	public void body()
	{
		AMSAgentDescription	desc	= (AMSAgentDescription)getParameter("description").getValue();
		SearchConstraints	constraints	= (SearchConstraints)getParameter("constraints").getValue();
		StandaloneAgentAdapter	adapter	= (StandaloneAgentAdapter)getScope().getPlatformAgent();
		AMSAgentDescription[]	result	= adapter.getPlatform().getAMS().searchAgents(desc, constraints);
		for(int i=0; i<result.length; i++)
			getParameterSet("result").addValue(result[i]);
	}
}
