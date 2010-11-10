package jadex.examples.cleanerworld.multi.cleaner;

import jadex.examples.cleanerworld.multi.Location;
import jadex.runtime.*;

/**
 *  Patrol along the patrol points.
 */
public class PatrolPlan extends Plan
{

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
	public void body()
	{
		Location[] loci = (Location[])getBeliefbase().getBeliefSet("patrolpoints").getFacts();

		for(int i=0; i<loci.length; i++)
		{
			IGoal moveto = createGoal("achievemoveto");
			moveto.getParameter("location").setValue(loci[i]);
			dispatchSubgoalAndWait(moveto);
		}
	}
}
