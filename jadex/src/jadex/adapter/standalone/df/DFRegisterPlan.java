package jadex.adapter.standalone.df;

import jadex.runtime.*;
import jadex.adapter.fipa.*;

/**
 *  The df register plan has the task to receive a message
 *  and create a corresponding goal.
 */
public class DFRegisterPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		DFRegister re = (DFRegister)getParameter("action").getValue();

		IGoal reg = createGoal("df_register");
		reg.getParameter("description").setValue(re.getAgentDescription());
		dispatchSubgoalAndWait(reg);

		re.setResult((AgentDescription)reg.getParameter("result").getValue());
		getParameter("result").setValue(new Done(re));
	}
}