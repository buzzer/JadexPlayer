package jadex.testcases.beliefs;

import jadex.runtime.Plan;
import jadex.runtime.TimeoutException;
import jadex.planlib.TestReport;

/**
 *  Test if auto updating beliefs work.
 */
public class UpdatingBeliefPlan extends Plan
{
	/**
	 *  The body method is called on the
	 *  instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		waitFor(100);
		TestReport tr = new TestReport("#1", "Test belief auto update.");
		try
		{
			waitForBeliefChange("time", 1000);
			getLogger().info("Test 1 succeeded.");
			tr.setSucceeded(true);
		}
		catch(TimeoutException e)
		{
			getLogger().info("Test 1 failed.");
			tr.setReason("No belief update detected.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);
	}
}
