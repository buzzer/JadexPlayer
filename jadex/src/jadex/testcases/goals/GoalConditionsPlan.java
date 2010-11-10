package jadex.testcases.goals;

import jadex.planlib.TestReport;
import jadex.runtime.Plan;

/**
 *  Plan to test goal conditions.
 */
public class GoalConditionsPlan	extends Plan
{

	public void	body()
	{
		// Initially there should be no goal and no plan (except this one).
		TestReport	report	= new TestReport("test_setup", "No goal and plan should exist at start", true, null);
		if(getGoalbase().getGoals("test").length!=0)
		{
			report.setFailed("Goal already exists");
		}
		else if(getPlanbase().getPlans().length!=1)
		{
			report.setFailed("Wrong planbase contents");
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
		
		
		// Now triggering goal creation (still no plan due to invalid context).
		getBeliefbase().getBelief("creation").setFact(new Boolean(true));
		report	= new TestReport("trigger_creation", "Triggering goal creation", true, null);
		if(getGoalbase().getGoals("test").length!=1)
		{
			report.setFailed("Goal does not exist");
		}
		else if(getPlanbase().getPlans().length!=1)
		{
			report.setFailed("Wrong planbase contents");
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
		
		
		// Now triggering goal context to start plan.
		getBeliefbase().getBelief("context").setFact(new Boolean(true));
		report	= new TestReport("trigger_context", "Triggering goal context", true, null);
		if(getGoalbase().getGoals("test").length!=1)
		{
			report.setFailed("Goal does not exist");
		}
		else if(getPlanbase().getPlans().length!=2)
		{
			report.setFailed("Wrong planbase contents");
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);

		
		// Now triggering goal drop condition (goal and plan will be removed).
		getBeliefbase().getBelief("drop").setFact(new Boolean(true));
		report	= new TestReport("trigger_drop", "Triggering goal drop condition", true, null);
		if(getGoalbase().getGoals("test").length!=0)
		{
			report.setFailed("Goal still exists");
		}
		else if(getPlanbase().getPlans().length!=1)
		{
			report.setFailed("Wrong planbase contents");
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
		
		

		// Now triggering goal creation again (plan will also be created).
		getBeliefbase().getBelief("drop").setFact(new Boolean(false));
		getBeliefbase().getBelief("creation").setFact(new Boolean(false));
		getBeliefbase().getBelief("creation").setFact(new Boolean(true));
		report	= new TestReport("trigger_creation2", "Triggering goal creation again", true, null);
		if(getGoalbase().getGoals("test").length!=1)
		{
			report.setFailed("Goal does not exist");
		}
		else if(getPlanbase().getPlans().length!=2)
		{
			report.setFailed("Wrong planbase contents");
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
		
		
		// Now invalidating goal context to abort plan.
		getBeliefbase().getBelief("context").setFact(new Boolean(false));
		report	= new TestReport("trigger_context2", "Invalidating goal context", true, null);
		if(getGoalbase().getGoals("test").length!=1)
		{
			report.setFailed("Goal does not exist");
		}
		else if(getPlanbase().getPlans().length!=1)
		{
			report.setFailed("Wrong planbase contents");
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);

		// Now triggering goal context again to restart plan.
		getBeliefbase().getBelief("context").setFact(new Boolean(true));
		report	= new TestReport("trigger_context3", "Triggering goal context again", true, null);
		if(getGoalbase().getGoals("test").length!=1)
		{
			report.setFailed("Goal does not exist");
		}
		else if(getPlanbase().getPlans().length!=2)
		{
			report.setFailed("Wrong planbase contents");
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
	}
}
