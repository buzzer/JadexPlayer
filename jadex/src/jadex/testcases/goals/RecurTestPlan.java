package jadex.testcases.goals;

import jadex.planlib.TestReport;
import jadex.runtime.IGoal;
import jadex.runtime.Plan;

/**
 *  Test recur features of goals.
 */
public class RecurTestPlan extends Plan
{
	/**
	 *	Plan code. 
	 */
	public void body()
	{
		// Dispatch goals.
		IGoal	perf	= createGoal("perf");
		IGoal	achi	= createGoal("achi");
		IGoal	quer	= createGoal("quer");
		dispatchSubgoal(perf);
		dispatchSubgoal(achi);
		dispatchSubgoal(quer);
		
		
		// Check state of goals when no plan is available.
		waitFor(50);
		
		TestReport	report	= new TestReport("perform_paused", "Test if perform goal continues when no plan is found.");
		if(perf.isActive())
			report.setSucceeded(true);
		else
			report.setReason("Goal not active.");
		getBeliefbase().getBeliefSet("reports").addFact(report);
		
		report	= new TestReport("achieve_paused", "Test if achieve goal continues when no plan is found.");
		if(achi.isActive())
			report.setSucceeded(true);
		else
			report.setReason("Goal not active.");
		getBeliefbase().getBeliefSet("reports").addFact(report);

		report	= new TestReport("query_paused", "Test if query goal continues when no plan is found.");
		if(quer.isActive())
			report.setSucceeded(true);
		else
			report.setReason("Goal not active.");
		getBeliefbase().getBeliefSet("reports").addFact(report);

		
		// Check state when plans are applicable.
		getBeliefbase().getBelief("context").setFact(Boolean.TRUE);
		perf.setRecur(false);	// Necessary for perform goal to finish after plan execution.
		waitFor(250);

		report	= new TestReport("perform_succeeded", "Test if perform goal succeedes after plan is found.");
		if(perf.isSucceeded())
			report.setSucceeded(true);
		else
			report.setReason("Goal not succeeded.");
		getBeliefbase().getBeliefSet("reports").addFact(report);

		report	= new TestReport("achieve_succeeded", "Test if achieve goal succeedes after plan is found.");
		if(achi.isSucceeded())
			report.setSucceeded(true);
		else
			report.setReason("Goal not succeeded.");
		getBeliefbase().getBeliefSet("reports").addFact(report);

		report	= new TestReport("query_succeeded", "Test if query goal succeedes after plan is found.");
		if(quer.isSucceeded())
			report.setSucceeded(true);
		else
			report.setReason("Goal not succeeded.");
		getBeliefbase().getBeliefSet("reports").addFact(report);
	}
}
