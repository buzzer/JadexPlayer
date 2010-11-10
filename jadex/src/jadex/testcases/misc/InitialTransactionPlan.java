package jadex.testcases.misc;

import jadex.planlib.TestReport;
import jadex.runtime.Plan;

/**
 *  Test if initial transaction works. 
 */
public class InitialTransactionPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		waitFor(100);
		TestReport tr = new TestReport("#1", "Test if plan was executed.");
		if(getBeliefbase().getBelief("result").getFact().equals("Hello World!"))
		{
			tr.setSucceeded(true);
		}
		else
		{
			tr.setReason("Plan was not triggered.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);
	}
}
