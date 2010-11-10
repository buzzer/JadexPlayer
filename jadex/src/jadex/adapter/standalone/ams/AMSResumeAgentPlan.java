package jadex.adapter.standalone.ams;

import jadex.runtime.*;
import jadex.adapter.fipa.*;

/**
 *  Resume an agent.
 */
public class AMSResumeAgentPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		AMSResumeAgent ra = (AMSResumeAgent)getParameter("action").getValue();

		IGoal rag = createGoal("ams_suspend_agent");
		rag.getParameter("agentidentifier").setValue(ra.getAgentIdentifier());
		dispatchSubgoalAndWait(rag);

		getParameter("result").setValue(new Done(ra));
	}
}