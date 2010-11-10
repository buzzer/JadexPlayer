package jadex.adapter.standalone.df;

import jadex.runtime.*;
import jadex.adapter.fipa.*;

/**
 *  The df modify plan has the task to receive a message
 *  andc reate a corresponding goal.
 */
public class DFModifyPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		DFModify mo = (DFModify)getParameter("action").getValue();

		IGoal reg = createGoal("df_modify");
		reg.getParameter("description").setValue(mo.getAgentDescription());
		dispatchSubgoalAndWait(reg);

		mo.setResult((AgentDescription)reg.getParameter("result").getValue());
		getParameter("result").setValue(new Done(mo));
	}
}
