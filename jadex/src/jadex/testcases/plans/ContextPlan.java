package jadex.testcases.plans;

import jadex.runtime.*;
import jadex.planlib.TestReport;

/**
 *  A plan that waits for the same condition as the goal 
 */
public class ContextPlan extends Plan
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
		tr = new TestReport("#1", "Test plan termination.");
		getLogger().info("Plan started, waiting for context.");
		// Waits internally on the same condition as the plan context.
		// This means the plan is immediately aborted when both conditions trigger.
		ICondition cond = createCondition("!$beliefbase.context");
		waitForCondition(cond);
		getLogger().info("Plan after condition??");
		tr.setReason("Plan is executed even though plan context is not valid.");
		getBeliefbase().getBeliefSet("reports").addFact(tr);
	}

	/**
	 *  Called on plan abortion.
	 */
	public void aborted()
	{
		tr.setSucceeded(true);
		getBeliefbase().getBeliefSet("reports").addFact(tr);
		getLogger().info("Plan aborted.");
	}
}
