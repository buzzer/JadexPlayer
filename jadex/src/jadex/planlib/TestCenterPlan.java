package jadex.planlib;

import jadex.runtime.Plan;
import jadex.runtime.IGoal;

/**
 *  Register the test center at the df and then perform tests.
 */
public class TestCenterPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		IGoal dfkr = createGoal("df_keep_registered");
		dfkr.getParameter("description").setValue(getPropertybase()
			.getProperty("fipa.agentdescription.test_center"));
		dispatchSubgoalAndWait(dfkr);

		IGoal perform = createGoal("perform_tests");
		dispatchSubgoalAndWait(perform);

		killAgent();
	}
}
