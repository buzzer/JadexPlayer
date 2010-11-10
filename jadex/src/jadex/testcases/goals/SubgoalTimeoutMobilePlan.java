package jadex.testcases.goals;

import jadex.planlib.TestReport;
import jadex.runtime.*;

/**
 *  Test subgoal timeout for mobile plans.
 */
public class SubgoalTimeoutMobilePlan extends MobilePlan
{
	/** The test report (used by both methods).*/
	protected TestReport	report	= new TestReport("test_timeout", "Test if a subgoal times out.");

	/**
	 * The action methods is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void action(IEvent event)
	{
		if(event instanceof IGoalEvent)
		{
			if(((IGoalEvent)event).getGoal().isFailed())
			{
				report.setReason("Should not call action with failed subgoal.");
			}
			else
			{
				report.setReason("Subgoal unexpectedly succeeded.");
			}
			getBeliefbase().getBeliefSet("reports").addFact(report);
		}
		else
		{
			IGoal sg = createGoal("timeout_goal");
			dispatchSubgoalAndWait(sg, 100);
		}
	}

	/**
	 *  Called on BDI exception.
	 */
	public void	exception(Exception exception) throws Exception
	{
		if(exception instanceof TimeoutException)
		{
			report.setSucceeded(true);
		}
		else
		{
			report.setReason("Wrong exception: "+exception);
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
	}
}
