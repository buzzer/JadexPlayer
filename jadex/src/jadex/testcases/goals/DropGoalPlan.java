package jadex.testcases.goals;

import jadex.planlib.TestReport;
import jadex.runtime.IGoal;
import jadex.runtime.Plan;
import jadex.runtime.TimeoutException;

/**
 *  Test waiting for a dropped goal.
 */
public class DropGoalPlan extends Plan
{
	public void body()
	{
		// Dispatch goal.
		IGoal	goal	= createGoal("testgoal");
		dispatchSubgoal(goal);
		
		// Wait a sec. and then drop the goal.
		waitFor(1000);
		goal.drop();
		
		// Wait for goal to be finished and check the result.
		TestReport	report	= new TestReport("drop_wait", "Test waiting for a dropped goal.");
		try
		{
			waitForSubgoal(goal, 1000);
			report.setSucceeded(true);
		}
		catch(TimeoutException te)
		{
			report.setFailed("Goal did not finish.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);		
	}
}
