package jadex.adapter.standalone.df;

import jadex.runtime.*;
import jadex.adapter.fipa.*;

/**
 *  The df deregister plan has the task to receive a message 
 *  and create a corresponding goal.
 */
public class DFDeregisterPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		DFDeregister de = (DFDeregister)getParameter("action").getValue();

		IGoal dreg = createGoal("df_deregister");
		dreg.getParameter("description").setValue(de.getAgentDescription());
		dispatchSubgoalAndWait(dreg);

		getParameter("result").setValue(new Done(de));
	}
}
