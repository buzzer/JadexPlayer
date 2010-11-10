package jadex.examples.garbagecollector;

import jadex.runtime.*;

/**
 *  Try to pickup some piece of garbage.
 */
public class PickUpPlan extends Plan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
		Environment env = (Environment)getBeliefbase().getBelief("env").getFact();

		//System.out.println("Calling pickup: "+getAgentName()+" "+getRootGoal());
		IGoal[] gs = getGoalbase().getGoals("take");
		if(!env.pickup(getAgentName()))
			fail();
	}
}
