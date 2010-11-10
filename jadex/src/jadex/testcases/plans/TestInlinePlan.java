package jadex.testcases.plans;

import jadex.planlib.TestReport;
import jadex.runtime.GoalFailureException;
import jadex.runtime.IGoal;
import jadex.runtime.Plan;

/**
 *  Create a goal and wait for the result.
 */
public class TestInlinePlan extends Plan
{
	public void body()
	{
		TestReport	report	= new TestReport("test_inline", "Dispatch a goal handled by the inline plan");
		try
		{
			IGoal	agoal	= createGoal("testgoal");
			dispatchSubgoalAndWait(agoal);
			report.setSucceeded(true);
		}
		catch(GoalFailureException gfe)
		{
			report.setReason(gfe.toString());
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
	}
}
