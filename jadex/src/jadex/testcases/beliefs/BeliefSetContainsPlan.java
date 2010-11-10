package jadex.testcases.beliefs;

import jadex.planlib.TestReport;
import jadex.runtime.Plan;
import jadex.adapter.fipa.SFipa;

/**
 *  Test if equal elements can be contained more once in a beliefset.
 */
public class BeliefSetContainsPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		TestReport tr = new TestReport("#1", "Test if same fact can be added to belief set agentids.");
		int before = getBeliefbase().getBeliefSet("agentids").size();
		getLogger().info("Test 1: Test if same fact is added to belief set.");
		getBeliefbase().getBeliefSet("agentids").addFact(SFipa.DF);
		int after = getBeliefbase().getBeliefSet("agentids").size();
		if(before==after)
		{
			tr.setSucceeded(true);
			getLogger().info("Test 1: Succeeded.");
		}
		else
		{
			tr.setReason("Belief set element was added although it is equal.");
			getLogger().info("Test 1: Failed.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);


		tr = new TestReport("#2", "Test if same fact can be added to belief set.");
		before = getBeliefbase().getBeliefSet("strings").size();
		getLogger().info("Test 2: Test if same fact can be added to belief set strings.");
		getBeliefbase().getBeliefSet("strings").addFact("abc");
		after = getBeliefbase().getBeliefSet("strings").size();
		if(before==after)
		{
			tr.setSucceeded(true);
			getLogger().info("Test 2: Succeeded.");
		}
		else
		{
			tr.setReason("Belief set element was added although it is equal.");
			getLogger().info("Test 2: Failed.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);
	}
}
