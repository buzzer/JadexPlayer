package jadex.adapter.standalone.ams;

import jadex.runtime.*;
import jadex.adapter.fipa.*;

/**
 *  Isuue a platform shutdown.
 */
public class AMSShutdownPlatformPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		AMSShutdownPlatform sd = (AMSShutdownPlatform)getParameter("action").getValue();
		dispatchSubgoalAndWait(createGoal("ams_shutdown_platform"));
		getParameter("result").setValue(new Done(sd));
	}
}
