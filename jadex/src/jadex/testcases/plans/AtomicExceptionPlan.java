package jadex.testcases.plans;

import jadex.runtime.*;
import jadex.planlib.TestReport;

/**
 *  Testing atomic operation in conjunction with exceptions.
 */
public class AtomicExceptionPlan extends Plan
{
	/**
	 *  The body method.
	 */
	public void body()
	{
		// Test if second run
		TestReport tr = (TestReport)getBeliefbase().getBelief("report").getFact();
		if(tr!=null)
		{
			tr.setSucceeded(true);
			getBeliefbase().getBeliefSet("reports").addFact(tr);
			return;
		}

		tr = new TestReport("#1", "Testing waitFor in atomic mode.");
		getLogger().info("Testing waitFor in atomic mode. Should produce exception.");
		startAtomic();
		Exception exception	= null;
		try
		{
			waitFor(500);
		}
		catch(Exception e)
		{
			exception	= e;
		}
		endAtomic();
		if(exception!=null)
		{
			getLogger().info("Success. Exception was:");
			//exception.printStackTrace(System.out);
			tr.setSucceeded(true);
		}
		else
		{
			getLogger().info("Fail! No exception occurred :-(.");
			tr.setReason("No exception occurred.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);

		waitFor(500);
		tr = new TestReport("#2", "Testing exception while in atomic mode");
		getBeliefbase().getBelief("report").setFact(tr);
		getLogger().info("\nTesting exception while in atomic mode. Should be reset and trigger other plan.");
		startAtomic();
		getBeliefbase().getBelief("a").setFact(new Boolean(true));
		throw new PlanFailureException();
	}
}
