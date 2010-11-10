package jadex.testcases.goals;

import jadex.planlib.TestReport;
import jadex.runtime.IGoal;
import jadex.runtime.Plan;

/**
 *  Test if uniqueness properties of goals are respected.
 */
public class UniqueGoalPlan extends Plan
{
	public void body()
	{
		// Dispatch first goal.
		TestReport	report	= new TestReport("first_goal", "The first goal should always be adopted.");
		IGoal	goal	= createGoal("testgoal");
		goal.getParameter("p_inc").setValue(new Integer(1));
		goal.getParameter("p_exc").setValue(new Integer(1));
		try
		{
			dispatchSubgoal(goal);
			report.setSucceeded(true);
		}
		catch(Exception e)
		{
			report.setReason("Unexpected exception: "+e);
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
		

		// Dispatch second goal.
		report	= new TestReport("second_goal", "The second goal differs and should also be adopted.");
		goal	= createGoal("testgoal");
		goal.getParameter("p_inc").setValue(new Integer(2));
		goal.getParameter("p_exc").setValue(new Integer(1));
		try
		{
			dispatchSubgoal(goal);
			report.setSucceeded(true);
		}
		catch(Exception e)
		{
			report.setReason("Unexpected exception: "+e);
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
		
	
		// Dispatch first goal.
		report	= new TestReport("third_goal", "The third goal is the same as the first and should not be adopted.");
		goal	= createGoal("testgoal");
		goal.getParameter("p_inc").setValue(new Integer(1));
		goal.getParameter("p_exc").setValue(new Integer(2));
		try
		{
			dispatchSubgoal(goal);
			report.setReason("Expected exception did not occur.");
		}
		catch(Exception e)
		{
			report.setSucceeded(true);
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
	}
}
