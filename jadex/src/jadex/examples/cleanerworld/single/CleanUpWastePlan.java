package jadex.examples.cleanerworld.single;

import jadex.runtime.*;

/**
 *  Clean-up some waste.
 */
public class CleanUpWastePlan extends Plan
{

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public CleanUpWastePlan()
	{
		getLogger().info("Created: "+this);
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		// move to the waste position when necessary
		if(!((Boolean)getBeliefbase().getBelief("carrieswaste").getFact()).booleanValue())
		{
			getLogger().info("Moving to waste!");
			Location location = (Location)getParameter("waste_location").getValue();
			IGoal moveto = createGoal("achievemoveto");
			moveto.getParameter("location").setValue(location);
			dispatchSubgoalAndWait(moveto);
			if(!getBeliefbase().getBeliefSet("waste_locations").containsFact(location))
				fail();
			getLogger().info("Picking up-waste!");
			getBeliefbase().getBeliefSet("waste_locations").removeFact(location);
			getBeliefbase().getBelief("carrieswaste").setFact(new Boolean(true));
		}

		// move to the waste-bin
		Location location = (Location)getBeliefbase().getBelief("wastebin_location").getFact();
		IGoal moveto = createGoal("achievemoveto");
		moveto.getParameter("location").setValue(location);
		dispatchSubgoalAndWait(moveto);

		// drop waste to waste-bin
		getBeliefbase().getBelief("carrieswaste").setFact(new Boolean(false));
	}

}
