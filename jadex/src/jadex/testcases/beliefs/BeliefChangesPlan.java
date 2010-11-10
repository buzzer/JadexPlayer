package jadex.testcases.beliefs;

import jadex.runtime.Plan;
import jadex.planlib.TestReport;

/**
 *  Test different kinds of belief changes.
 */
public class BeliefChangesPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		// Hack?! First initial belief reaction plans should be executed :-(
		waitFor(300);

		TestReport tr= new TestReport("#1", "Belief value is changed and plan a is sensible to that.");
		getLogger().info("Test 1: Plan a should be created in response to setValue of bel_a.");
		getLogger().info("bel_a.setFact(\"aaaa\")");
		getBeliefbase().getBelief("bel_a").setFact("aaaa");
		waitFor(300);
		if(((Integer)getBeliefbase().getBelief("plan_a_executed").getFact()).intValue()==2
			&& ((Integer)getBeliefbase().getBelief("plan_b_executed").getFact()).intValue()==1
			&& ((Integer)getBeliefbase().getBelief("plan_c_executed").getFact()).intValue()==1)
			tr.setSucceeded(true);
		else
			tr.setReason("Plan a should be created in response to setValue of bel_a.");
		getBeliefbase().getBeliefSet("reports").addFact(tr);

		/*System.out.println(((Integer)getBeliefbase().getBelief("plan_a_executed").getFact()).intValue());
		System.out.println(((Integer)getBeliefbase().getBelief("plan_b_executed").getFact()).intValue());
		System.out.println(((Integer)getBeliefbase().getBelief("plan_c_executed").getFact()).intValue());
*/
		tr = new TestReport("#2", "Belief value is set the same value and no plan should react.");
		getLogger().info("\n\nTest 2: No plans should be created in response to setValue of same value.");
		getLogger().info("Test 2: bel_a.setFact(\"aaaa\")");
		getBeliefbase().getBelief("bel_a").setFact("aaaa");
		waitFor(300);
		if(((Integer)getBeliefbase().getBelief("plan_a_executed").getFact()).intValue()==2
			&& ((Integer)getBeliefbase().getBelief("plan_b_executed").getFact()).intValue()==1
			&& ((Integer)getBeliefbase().getBelief("plan_c_executed").getFact()).intValue()==1)
			tr.setSucceeded(true);
		else
			tr.setReason("No plans should be created in response to setValue of same value.");
		getBeliefbase().getBeliefSet("reports").addFact(tr);

		tr = new TestReport("#2", "Two dependent beliefs are changed and two plans monitor them.");
		getLogger().info("\n\nTest 3: Plan a and b should be created in response to a changed value " +
			"of bel_a which affects bel_b.");
		getLogger().info("bel_a.setFact(\"new_value\")");
		getBeliefbase().getBelief("bel_a").setFact("new_value");
		waitFor(300);
		if(((Integer)getBeliefbase().getBelief("plan_a_executed").getFact()).intValue()==3
			&& ((Integer)getBeliefbase().getBelief("plan_b_executed").getFact()).intValue()==2
			&& ((Integer)getBeliefbase().getBelief("plan_c_executed").getFact()).intValue()==1)
			tr.setSucceeded(true);
		else
			tr.setReason("Plan a and b should be created in response to a changed value of bel_a which affects bel_b.");
		getBeliefbase().getBeliefSet("reports").addFact(tr);

		tr = new TestReport("#2", "A deep bean change is done and a plan monitors that.");
		getLogger().info("\n\nTest 4:Plan should be created in response to a deep bean change.");
		getLogger().info("((TestBean)bel_c.getFact()).setName(\"new_name\")");
		((TestBean)getBeliefbase().getBelief("bel_c").getFact()).setName("new_name");
		waitFor(300);
		if(((Integer)getBeliefbase().getBelief("plan_a_executed").getFact()).intValue()==3
			&& ((Integer)getBeliefbase().getBelief("plan_b_executed").getFact()).intValue()==2
			&& ((Integer)getBeliefbase().getBelief("plan_c_executed").getFact()).intValue()==2)
			tr.setSucceeded(true);
		else
			tr.setReason("Plan should be created in response to a deep bean change.");
		getBeliefbase().getBeliefSet("reports").addFact(tr);
	}
}
