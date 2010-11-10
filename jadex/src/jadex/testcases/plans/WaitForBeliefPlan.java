package jadex.testcases.plans;

import jadex.runtime.Plan;
import jadex.planlib.TestReport;

/**
 *  Wait endlessly for belief changes.
 */
public class WaitForBeliefPlan extends Plan
{
	//-------- attributes --------

	/** The test report. */
	protected TestReport tr;

	//-------- constructors --------

	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		for(int i=0; i<3; i++)
		{
			tr = new TestReport("#"+i, "Tests if a beliefchange is detected.");
			getLogger().info("Now waiting for a change of belief 'some_number': ");
			waitForBeliefChange("some_number", 3000);
			tr.setSucceeded(true);
			getLogger().info("Belief changed: "+getBeliefbase().getBelief("some_number").getFact());
			getBeliefbase().getBeliefSet("reports").addFact(tr);
		}
	}

	/**
	 *  Called on plan failure.
	 */
	public void failed()
	{
		getLogger().info("No belief update detected. Plan failed.");
		if(tr==null)
			tr = new TestReport("", "Plan failure occurred before test was created.");
		tr.setReason("No belief update detected");
		getBeliefbase().getBeliefSet("reports").addFact(tr);
	}
}
