package jadex.examples.cleanerworld.multi.cleanermobile;

import jadex.examples.cleanerworld.multi.Location;
import jadex.runtime.*;

/**
 *  Patrol along the patrol points.
 */
public class PatrolPlan extends MobilePlan
{
	//-------- attributes --------
	
	/** The next patrol point. */
	protected int nextpoint;

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public PatrolPlan()
	{
		getLogger().info("Created: "+this);
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void action(IEvent event)
	{
		Location[] loci = (Location[])getBeliefbase().getBeliefSet("patrolpoints").getFacts();

		if(nextpoint<loci.length)
		{
			IGoal moveto = createGoal("achievemoveto");
			moveto.getParameter("location").setValue(loci[nextpoint]);
			dispatchSubgoalAndWait(moveto);
			nextpoint++;
		}
	}
}
