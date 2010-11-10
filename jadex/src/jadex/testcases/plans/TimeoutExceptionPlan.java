package jadex.testcases.plans;

import jadex.runtime.*;
import jadex.planlib.TestReport;

/**
 *  Test time waitFor() methods.
 */
public class TimeoutExceptionPlan extends Plan
{
	//-------- attributes --------

	/** The test report. */
	protected TestReport tr;

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		TestReport tr = new TestReport("#1", "Test normal timed wait.");
		long start = System.currentTimeMillis();
		long test = 200;
		waitFor(test);
		long dur = System.currentTimeMillis()-start;
		double diff = ((double)Math.abs(dur-test))/((double)test)*100;
		if(diff<5)
		{
			tr.setSucceeded(true);
		}
		else
		{
			tr.setReason("Difference greater than 5 percent: "+diff);
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);

		tr = new TestReport("#2", "Testing timeout with catch.");
		try
		{
			waitFor(IFilter.NEVER, 200);
			tr.setReason("No timeout exception occurred.");
		}
		catch(TimeoutException e)
		{
			tr.setSucceeded(true);
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);

		this.tr = new TestReport("#3", "Testing timeout without catch.");
		waitFor(IFilter.NEVER, 200);
		this.tr.setReason("No timeout exception occurred.");
	}

	/**
	 *  Called when plan failed.
	 */
	public void failed()
	{
		tr.setSucceeded(true);
		getBeliefbase().getBeliefSet("reports").addFact(tr);
	}
}
