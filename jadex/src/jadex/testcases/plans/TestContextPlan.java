package jadex.testcases.plans;

import jadex.planlib.TestReport;
import jadex.runtime.Plan;

/**
 *  This plan just waits until it is aborted.
 */
public class TestContextPlan extends Plan
{
	private TestReport	tr;

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		tr	= new TestReport("#1", "Tests plan abortion due to invalid context.");
		getLogger().info("Waiting: "+this);
		waitFor(1000);
		getLogger().info("Continued: "+this);
		tr.setReason("Plan was not aborted.");
		getBeliefbase().getBeliefSet("reports").addFact(tr);
	}

	/**
	 *  Called when the plan is aborted.
	 */
	public void aborted()
	{
		getLogger().info("Plan aborting...");
		tr.setSucceeded(true);
		getBeliefbase().getBeliefSet("reports").addFact(tr);
	}
}
