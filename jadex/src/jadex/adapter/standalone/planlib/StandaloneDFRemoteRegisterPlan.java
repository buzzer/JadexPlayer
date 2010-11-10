package jadex.adapter.standalone.planlib;

import jadex.adapter.fipa.*;
import jadex.runtime.IGoal;
import jadex.runtime.Plan;

import java.util.Date;

/**
 *  Register on a remote platform.
 */
public class StandaloneDFRemoteRegisterPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
//		System.out.println("standalone register");
		DFRegister re = new DFRegister();
		AgentDescription desc = (AgentDescription)getParameter("description").getValue();
		Number lt = (Number)getParameter("leasetime").getValue();
		if(lt!=null)
			desc.setLeaseTime(new Date(System.currentTimeMillis()+lt.longValue()));
		re.setAgentDescription(desc);

		IGoal req = createGoal("rp_initiate");
		req.getParameter("receiver").setValue(getParameter("df").getValue());
		req.getParameter("action").setValue(re);
		dispatchSubgoalAndWait(req);

		getParameter("result").setValue(((DFRegister)((Done)req.getParameter("result").getValue()).getAction()).getResult());
	}
}
