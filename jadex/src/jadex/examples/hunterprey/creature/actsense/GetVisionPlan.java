package jadex.examples.hunterprey.creature.actsense;

import jadex.examples.hunterprey.*;
import jadex.runtime.IGoal;
import jadex.adapter.fipa.Done;

/**
 *  Handles an get_vision goal.
 */
/*  @handles goal get_vision
 *  @requires goal df_search
 *  @requires goal request
 *  @requires belief myself
 *  @requires belief environmentagent
 */
public class GetVisionPlan extends RemoteActionPlan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
		RequestVision rv = new RequestVision((Creature)getBeliefbase().getBelief("my_self").getFact());
		IGoal	result	= requestAction(rv);
		//Done done = (Done)result.getGoal().getResult();
		Done done = (Done)result.getParameter("result").getValue();
		getParameter("vision").setValue(((RequestVision)done.getAction()).getVision());
	}
}
