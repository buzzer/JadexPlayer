package jadex.adapter.standalone.planlib;

import jadex.adapter.fipa.*;
import jadex.runtime.*;

/**
 *  Register on a remote platform.
 */
public class StandaloneDFRemoteDeregisterPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
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

		DFDeregister dre = new DFDeregister();
		dre.setAgentDescription(desc);
		IGoal req = createGoal("rp_initiate");
		req.getParameter("receiver").setValue(getParameter("df").getValue());
		req.getParameter("action").setValue(dre);
		dispatchSubgoalAndWait(req);
	}
}
