package jadex.examples.cleanerworld.single;

import jadex.runtime.Plan;

/**
 *  Move to a point.
 */
public class MoveToLocationPlan extends Plan
{

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public MoveToLocationPlan()
	{
		getLogger().info("Created: "+this);
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		Location target = (Location)getParameter("location").getValue();
		Location myloc = (Location)getBeliefbase().getBelief("my_location").getFact();
		while(!myloc.isNear(target))
		{
			// calculate the new position offset.
			double speed = ((Double)getBeliefbase().getBelief("speed").getFact()).doubleValue();
			double d = myloc.getDistance(target);
			double r = speed*0.1;
			double dx = target.x-myloc.x;
			double dy = target.y-myloc.y;

			// When radius smaller than distance, just move a step.
			double rx = r<d ? r*dx/d : dx;
			double ry = r<d ? r*dy/d : dy;
			getBeliefbase().getBelief("my_location").setFact(new Location(myloc.x+rx, myloc.y+ry));

			// alter the charge state
			double	charge	= ((Double)getBeliefbase().getBelief("chargestate").getFact()).doubleValue();
			charge	-= r*0.075;
			getBeliefbase().getBelief("chargestate").setFact(new Double(charge));

			waitFor(100); // wait for 0.1 seconds

			// Ceck if location has changed in mean time.
			myloc = (Location)getBeliefbase().getBelief("my_location").getFact();
		}
	}

}
