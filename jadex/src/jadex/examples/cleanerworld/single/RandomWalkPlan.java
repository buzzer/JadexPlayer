package jadex.examples.cleanerworld.single;

import jadex.runtime.*;

/**
 *  Wander around.
 */
public class RandomWalkPlan extends Plan
{

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public RandomWalkPlan()
	{
		getLogger().info("Created: "+this);
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		double x_dest = Math.random();
		double y_dest = Math.random();
		Location dest = new Location(x_dest, y_dest);
		IGoal moveto = createGoal("achievemoveto");
		moveto.getParameter("location").setValue(dest);
		dispatchSubgoalAndWait(moveto);
		getLogger().info("Reached point: "+dest);
	}
}
