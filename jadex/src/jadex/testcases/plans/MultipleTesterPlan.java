package jadex.testcases.plans;

import jadex.runtime.Plan;
import jadex.planlib.TestReport;

/**
 *  Test if a plan is triggered once.
 */
public class MultipleTesterPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		TestReport tr = new TestReport("#1", "Tests if a plan is triggered once.");
		getBeliefbase().getBelief("bel_a").setFact(new Integer(1));
		waitFor(100);
		if(((Integer)getBeliefbase().getBelief("plan_cnt").getFact()).intValue()==1)
		{
			tr.setSucceeded(true);
		}
		else
		{
			tr.setReason("More than one plan triggered.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);
	}
}
