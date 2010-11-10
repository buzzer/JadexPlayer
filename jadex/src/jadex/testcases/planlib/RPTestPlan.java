package jadex.testcases.planlib;

import jadex.planlib.TestReport;
import jadex.runtime.GoalFailureException;
import jadex.runtime.IGoal;
import jadex.testcases.AbstractMultipleAgentsPlan;

import java.util.List;
import java.util.Map;

/**
 *  Test the request protocol execution.
 */
public class RPTestPlan extends AbstractMultipleAgentsPlan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
		// Create 1 participants
		Map[] args = new Map[1];
		List agents = createAgents("jadex.testcases.planlib.RPReceiver", args);	

		TestReport tr = new TestReport("#1", "Test request protocol.");
		if(assureTest(tr))
		{
			try
			{
				IGoal request = createGoal("rp_initiate");
				request.getParameter("action").setValue("Request a task.");
				request.getParameter("receiver").setValue(agents.get(0));
				dispatchSubgoalAndWait(request);
				getLogger().info("Request result:"+request.getParameter("result").getValue());
				tr.setSucceeded(true);
			}
			catch(GoalFailureException e)
			{
				tr.setFailed("Exception occurred: "+e);
			}
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);
	}
}
