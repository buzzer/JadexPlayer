package jadex.examples.cleanerworld.multi.cleaner;

import jadex.examples.cleanerworld.multi.Location;
import jadex.runtime.*;

/**
 *  Wander around randomly.
 */
public class RandomWalkPlan extends Plan
{

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public RandomWalkPlan()
	{
		getLogger().info("Created: "+this+" for goal "+getRootGoal());
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
