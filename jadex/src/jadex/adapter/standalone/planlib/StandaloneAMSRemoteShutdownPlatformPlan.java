package jadex.adapter.standalone.planlib;

import jadex.runtime.*;
import jadex.adapter.fipa.*;

/**
 *  Shutdown a remote platform.
 */
public class StandaloneAMSRemoteShutdownPlatformPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		AMSShutdownPlatform sp = new AMSShutdownPlatform();

		IGoal req = createGoal("rp_initiate");
		req.getParameter("receiver").setValue(getParameter("ams").getValue());
		req.getParameter("action").setValue(sp);
		dispatchSubgoalAndWait(req);
	}
}

