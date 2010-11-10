package jadex.examples.garbagecollector;

import jadex.runtime.*;

/**
 *  Burn a piece of garbage.
 */
public class BurnPlan extends Plan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
		Environment env = (Environment)getBeliefbase().getBelief("env").getFact();

		// Pickup the garbarge.
		IGoal pickup = createGoal("pick");
		dispatchSubgoalAndWait(pickup);

		// Burn the waste.
		waitFor(100);
		env.burn(getAgentName());
	}
}
