package jadex.planlib;

import jadex.runtime.IGoal;
import jadex.runtime.Plan;

/**
 *  This plan has the purpose to map incoming
 *  initial protocol message (e.g. a request or CFP)
 *  to a new top-level interaction goal.
 */
public class InteractionGoalCreationPlan extends Plan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
		IGoal	igoal	= createGoal((String)getParameter("goaltype").getValue());
		igoal.getParameter("message").setValue(getInitialEvent());
		dispatchTopLevelGoal(igoal);
	}
}
