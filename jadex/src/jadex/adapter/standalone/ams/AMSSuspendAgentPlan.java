package jadex.adapter.standalone.ams;

import jadex.runtime.*;
import jadex.adapter.fipa.*;

/**
 *  Suspend an agent.
 */
public class AMSSuspendAgentPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		AMSSuspendAgent sa = (AMSSuspendAgent)getParameter("action").getValue();

		IGoal sag = createGoal("ams_suspend_agent");
		sag.getParameter("agentidentifier").setValue(sa.getAgentIdentifier());
		dispatchSubgoalAndWait(sag);

		getParameter("result").setValue(new Done(sa));
	}
}
