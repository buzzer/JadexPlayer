package jadex.adapter.standalone.planlib;

import jadex.adapter.fipa.*;
import jadex.runtime.*;

import java.util.Date;

/**
 *  Modify df entry on a remote platform.
 */
public class StandaloneDFRemoteModifyPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		DFModify mo = new DFModify();
		AgentDescription desc = (AgentDescription)getParameter("description").getValue();
		Number lt = (Number)getParameter("leasetime").getValue();
		if(lt!=null)
			desc.setLeaseTime(new Date(System.currentTimeMillis()+lt.longValue()));
		mo.setAgentDescription(desc);

		IGoal req = createGoal("rp_initiate");
		req.getParameter("receiver").setValue(getParameter("df").getValue());
		req.getParameter("action").setValue(mo);
		dispatchSubgoalAndWait(req);

		getParameter("result").setValue(((DFModify)((Done)req.getParameter("result").getValue()).getAction()).getResult());
	}
}
