package jadex.examples.cleanerworld.multi.cleanermobile;

import jadex.examples.cleanerworld.multi.Location;
import jadex.runtime.*;

/**
 *  Wander around randomly.
 */
public class RandomWalkPlan extends MobilePlan
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
	public void action(IEvent event)
	{
		if(event instanceof IGoalEvent && !((IGoalEvent)event).isInfo())
		{
			double x_dest = Math.random();
			double y_dest = Math.random();
			Location dest = new Location(x_dest, y_dest);
			IGoal moveto = createGoal("achievemoveto");
			moveto.getParameter("location").setValue(dest);
			dispatchSubgoalAndWait(moveto);
		}
	}
}
