package jadex.planlib;

import jadex.runtime.IGoal;
import jadex.runtime.Plan;

/**
 *  Perform all testcases.
 */
public class PerformTestsPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		Testcase[] testcases = (Testcase[])getParameterSet("testcases").getValues();
		int no_failed = 0;

		for(int i=0; i<testcases.length; i++)
		{
			IGoal pt = createGoal("perform_test");
			pt.getParameter("testcase").setValue(testcases[i]);
			dispatchSubgoalAndWait(pt);
			if(!testcases[i].isSucceeded())
				no_failed++;
		}

		if(no_failed==0)
			getLogger().info("SUCCESS: All "+testcases.length+" testcases passed.\n");
		else
			getLogger().info("FAILURE: "+no_failed+" of "+testcases.length+" testcases failed.\n");

		getLogger().info("The detailed results are:");
		for(int i=0; i<testcases.length; i++)
			getLogger().info(""+testcases[i]);
	}
}
