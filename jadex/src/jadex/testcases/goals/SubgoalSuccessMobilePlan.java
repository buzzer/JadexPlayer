package jadex.testcases.goals;

import jadex.planlib.TestReport;
import jadex.runtime.*;

/**
 *  Test subgoal success for mobile plans.
 */
public class SubgoalSuccessMobilePlan extends MobilePlan
{
	/** The test report (used by both methods).*/
	protected TestReport	report	= new TestReport("test_success", "Test if a subgoal succeeds.");

	/**
	 * The action methods is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void action(IEvent event)
	{
		if(event instanceof IGoalEvent)
		{
			if(((IGoalEvent)event).getGoal().isSucceeded())
			{
				report.setSucceeded(true);
			}
			else
			{
				report.setReason("Subgoal unexpectedly failed.");
			}
			getBeliefbase().getBeliefSet("reports").addFact(report);
		}
		else
		{
			IGoal sg = createGoal("success_goal");
			dispatchSubgoalAndWait(sg);
		}
	}

	/**
	 *  Called on BDI exception.
	 */
	public void	exception(Exception exception) throws Exception
	{
		report.setReason("Exception occurred: "+exception);
		getBeliefbase().getBeliefSet("reports").addFact(report);
	}
}
