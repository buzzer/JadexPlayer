package jadex.testcases.goals;

import jadex.planlib.TestReport;
import jadex.runtime.*;

/**
 *  Test subgoal handling for standard plans.
 */
public class SubgoalHandlingPlan extends Plan
{
	/**
	 *  Plan body.
	 */
	public void body()
	{
		// Test success.
		TestReport	report	= new TestReport("test_success", "Test if a subgoal succeeds.");
		IGoal sg = createGoal("success_goal");
		try
		{
			dispatchSubgoalAndWait(sg);
			if(sg.isSucceeded())
				report.setSucceeded(true);
			else
				report.setReason("Should not continue execution after failed subgoal.");
		}
		catch(Exception e)
		{
			report.setReason("Unexpected exception: "+e);
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);

		// Test failure.
		report	= new TestReport("test_failure", "Test if a subgoal fails.");
		sg = createGoal("failure_goal");
		try
		{
			dispatchSubgoalAndWait(sg);
			if(sg.isSucceeded())
				report.setReason("Subgoal unexpectedly succeeded.");
			else
				report.setReason("Should not continue execution after failed subgoal.");
		}
		catch(Exception e)
		{
			if(e instanceof GoalFailureException)
				report.setSucceeded(true);
			else
				report.setReason("Wrong exception: "+e);
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);

		// Test timeout.
		report	= new TestReport("test_failure", "Test if a subgoal times out.");
		sg = createGoal("timeout_goal");
		try
		{
			dispatchSubgoalAndWait(sg, 100);
			if(sg.isSucceeded())
				report.setReason("Subgoal unexpectedly succeeded.");
			else
				report.setReason("Should not continue execution after failed subgoal.");
		}
		catch(Exception e)
		{
			if(e instanceof TimeoutException)
				report.setSucceeded(true);
			else
				report.setReason("Wrong exception: "+e);
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);

	}
}