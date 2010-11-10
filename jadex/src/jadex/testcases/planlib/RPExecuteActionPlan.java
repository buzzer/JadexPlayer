package jadex.testcases.planlib;

import jadex.runtime.Plan;

/**
 *  Execute an action issued by a request.
 */
public class RPExecuteActionPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		getParameter("result").setValue("request task executed.");
	}
}
