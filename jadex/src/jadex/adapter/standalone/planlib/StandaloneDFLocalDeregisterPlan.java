package jadex.adapter.standalone.planlib;

import jadex.runtime.BasicAgentIdentifier;
import jadex.runtime.Plan;
import jadex.adapter.fipa.AgentDescription;
import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.standalone.StandaloneAgentAdapter;


/**
 *  Plan to deregister at the standalone df.
 */
public class StandaloneDFLocalDeregisterPlan extends Plan
{
	/**
	 *  Plan body.
	 */
	public void body()
	{
		// In case of a remote request the agent description is already
		// set via the remote deregister plan.
		AgentDescription desc = (AgentDescription)getParameter("description").getValue();
		if(desc==null)
		{
			desc	= new AgentDescription();
		}
		if(desc.getName()==null)
		{
			BasicAgentIdentifier	bid	= getScope().getAgentIdentifier();
			desc.setName(bid instanceof AgentIdentifier ? (AgentIdentifier)bid
				: new AgentIdentifier(bid.getName()));
		}

		StandaloneAgentAdapter	agent	= (StandaloneAgentAdapter)getCapability().getPlatformAgent();
		try
		{
			agent.getPlatform().getDF().deregister(desc);
		}
		catch(Exception e)
		{
			fail();
		}
	}
}
