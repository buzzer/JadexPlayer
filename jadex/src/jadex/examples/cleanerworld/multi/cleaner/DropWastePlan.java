package jadex.examples.cleanerworld.multi.cleaner;

import jadex.examples.cleanerworld.multi.*;
import jadex.runtime.*;

/**
 *  Clean-up some waste.
 */
public class DropWastePlan extends Plan
{

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public DropWastePlan()
	{
		getLogger().info("Created: "+this);
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		Waste waste = (Waste)getBeliefbase().getBelief("carriedwaste").getFact();

		// Move to a not full waste-bin
		Wastebin wastebin = (Wastebin)getParameter("wastebin").getValue();
		if(wastebin==null)
			throw new PlanFailureException();

		Location location = wastebin.getLocation();
		IGoal moveto = createGoal("achievemoveto");
		moveto.getParameter("location").setValue(location);
		dispatchSubgoalAndWait(moveto);

		// Drop waste to waste-bin.
		//IEnvironment env = (IEnvironment)getBeliefbase().getBelief("environment").getFact();
		//boolean success = env.dropWasteInWastebin(waste, wastebin);
		IGoal dg = createGoal("drop_waste_action");
		dg.getParameter("waste").setValue(waste);
		dg.getParameter("wastebin").setValue(wastebin);
		dispatchSubgoalAndWait(dg);

		// Update beliefs.
		getLogger().info("Dropping waste to wastebin!");
		wastebin.addWaste(waste);

		// Todo: Find out why atomic is needed.
		startAtomic();
		IBeliefSet wbs = getBeliefbase().getBeliefSet("wastebins");
		if(wbs.containsFact(wastebin))
			wbs.updateFact(wastebin);
		else
			wbs.addFact(wastebin);
		//getBeliefbase().getBeliefSet("wastebins").updateOrAddFact(wastebin);
		getBeliefbase().getBelief("carriedwaste").setFact(null);
		endAtomic();
	}
}
