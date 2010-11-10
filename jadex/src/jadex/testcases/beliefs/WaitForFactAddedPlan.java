package jadex.testcases.beliefs;

import jadex.runtime.Plan;
import jadex.runtime.TimeoutException;
import jadex.runtime.planwrapper.WaitqueueWrapper;
import jadex.model.ISystemEventTypes;
import jadex.planlib.TestReport;

/**
 *  Test if adding facts by two different plans is detected
 */
public class WaitForFactAddedPlan extends Plan
{
	/**
	 *  The body method is called on the
	 *  instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		TestReport tr = new TestReport("#1", "Test waitForFactAdded.");
		int counter = 0;
		int todetect = 5; // adding 5 facts
		long endtime = System.currentTimeMillis()+5000;
		((WaitqueueWrapper)getWaitqueue()).addBeliefSet("beliefSetToAddFacts", 
			new String[]{ISystemEventTypes.BSFACT_ADDED});
		try
		{
			while(counter < todetect)
			{
				getLogger().info("waiting for facts to be added");
				Object o = waitForFactAdded("beliefSetToAddFacts", 2000);
				getLogger().info("added fact detected: " + o);
				//System.out.println("added fact detected: " + o);
				counter++;
				// Waiting here causes further fact adds in the meantime so that
				// they cannot be detected without waitqueue
				waitFor(100); 
			}
			getLogger().info("Test 1 succeeded.");
			tr.setSucceeded(true);
		}
		catch(TimeoutException e)
		{
			getLogger().info("Test 1 failed.");
			tr.setReason("Not all added facts detected (" + counter + "/" + todetect + ").");
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);
	}
}
