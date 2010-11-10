package jadex.testcases.planlib;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.planlib.InteractionState;
import jadex.planlib.TestReport;
import jadex.runtime.IGoal;
import jadex.runtime.Plan;
import jadex.runtime.TimeoutException;

/**
 *  Test different cases of protocol cancellation.
 */
public class CMTestPlan extends Plan
{
	/**
	 *  Plan body performs the tests.
	 */
	public void	body()
	{
		testInitiatorCancel();
		
		testReceiverAbort();
	}
	
	/**
	 *  Test cancellation of interaction from initiator side (this side).
	 */
	public void testInitiatorCancel()
	{
		// Create receiver agent.
		String	agenttype	= "jadex.testcases.planlib.CMReceiver";
		IGoal	ca	= createGoal("ams_create_agent");
		ca.getParameter("type").setValue(agenttype);
		dispatchSubgoalAndWait(ca);
		AgentIdentifier	receiver	= (AgentIdentifier)ca.getParameter("agentidentifier").getValue();

		// Dispatch request goal.
		IGoal	request	= createGoal("rp_initiate");
		request.getParameter("action").setValue("dummy request");
		request.getParameter("receiver").setValue(receiver);
		dispatchSubgoal(request);
		
		// Wait a sec. and then drop the interaction goal.
		waitFor(1000);
		request.drop();
		
		// Wait for goal to be finished and check the result.
		waitForSubgoal(request);
		InteractionState	state	= (InteractionState)request.getParameter("interaction_state").getValue();
		TestReport	report	= new TestReport("test_cancel", "Test if interaction can be cancelled on initiator side.");
		if(InteractionState.CANCELLATION_SUCCEEDED.equals(state.getCancelResponse(receiver)))
		{
			report.setSucceeded(true);
		}
		else
		{
			report.setFailed("Wrong result. Expected '"+InteractionState.CANCELLATION_SUCCEEDED+"' but was '"+state.getCancelResponse(receiver)+"'.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
		
		// Destroy receiver agent.
		IGoal	da	= createGoal("ams_destroy_agent");
		da.getParameter("agentidentifier").setValue(receiver);
		dispatchSubgoalAndWait(da);
	}
	
	/**
	 *  Test abortion of interaction by receiver side (other side).
	 */
	public void testReceiverAbort()
	{
		// Create receiver agent.
		String	agenttype	= "jadex.testcases.planlib.CMReceiver";
		IGoal	ca	= createGoal("ams_create_agent");
		ca.getParameter("type").setValue(agenttype);
		dispatchSubgoalAndWait(ca);
		AgentIdentifier	receiver	= (AgentIdentifier)ca.getParameter("agentidentifier").getValue();

		// Dispatch request goal.
		IGoal	request	= createGoal("rp_initiate");
		request.getParameter("action").setValue("dummy request");
		request.getParameter("receiver").setValue(receiver);
		dispatchSubgoal(request);
		
		// Wait a sec. and then kill the receiver agent (should abort interaction in its end state).
		waitFor(1000);
		IGoal	da	= createGoal("ams_destroy_agent");
		da.getParameter("agentidentifier").setValue(receiver);
		dispatchSubgoalAndWait(da);
		
		// Check if goal finishes. (todo: check result).
		TestReport	report	= new TestReport("test_abort", "Test if interaction can be aborted on receiver side.");
		try
		{
			waitForSubgoal(request, 3000);
			if(request.isFailed())
			{
				report.setSucceeded(true);
			}
			else
			{
				report.setFailed("Goal unexpectedly succeeded.");
			}
		}
		catch(TimeoutException e)
		{
			report.setFailed("Goal did not finish.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
	}
}
