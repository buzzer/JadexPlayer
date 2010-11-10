package jadex.testcases.goals;

import jadex.planlib.TestReport;
import jadex.runtime.*;

/**
 *  Test subgoal failure for mobile plans.
 */
public class SubgoalFailureMobilePlan extends MobilePlan
{
	/** The test report (used by both methods).*/
	protected TestReport	report	= new TestReport("test_failure", "Test if a subgoal fails.");

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
			IGoal sg = createGoal("failure_goal");
			dispatchSubgoalAndWait(sg);
		}
	}

	/**
	 *  Called on BDI exception.
	 */
	public void	exception(Exception exception) throws Exception
	{
		if(exception instanceof GoalFailureException)
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
