package jadex.adapter.standalone.planlib;

import jadex.adapter.fipa.AgentDescription;
import jadex.adapter.fipa.SearchConstraints;
import jadex.adapter.standalone.StandaloneAgentAdapter;
import jadex.runtime.Plan;


/**
 *  Plan to register at the standalone df.
 */
public class StandaloneDFLocalSearchPlan extends Plan
{
	/**
	 *  Plan body.
	 */
	public void body()
	{
		// Todo: support other parameters!?
		AgentDescription desc = (AgentDescription)getParameter("description").getValue();
		SearchConstraints	con	= (SearchConstraints)getParameter("constraints").getValue();
		StandaloneAgentAdapter	agent	= (StandaloneAgentAdapter)getCapability().getPlatformAgent();
		AgentDescription[]	result	= agent.getPlatform().getDF().search(desc, con);
		getParameterSet("result").addValues(result);
	}
}
