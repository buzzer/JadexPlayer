package jadex.examples.cleanerworld.multi.cleanermobile;

import jadex.examples.cleanerworld.multi.*;
import jadex.runtime.*;

/**
 *  Clean-up some waste.
 */
public class DropWastePlan extends MobilePlan
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
	public void action(IEvent event)
	{
		Waste waste = (Waste)getBeliefbase().getBelief("carriedwaste").getFact();
		Wastebin wastebin = (Wastebin)getParameter("wastebin").getValue();

		// Move to wastebin.
		if(event instanceof IGoalEvent && !((IGoalEvent)event).isInfo())
		{
			// Move to a not full waste-bin
			if(wastebin==null)
				throw new PlanFailureException();
	
			Location location = wastebin.getLocation();
			IGoal moveto = createGoal("achievemoveto");
			moveto.getParameter("location").setValue(location);
			dispatchSubgoalAndWait(moveto);
		}

		// Drop waste to waste-bin.
		else if(event instanceof IGoalEvent && ((IGoalEvent)event).getGoal().getType().equals("achievemoveto"))
		{
			//IEnvironment env = (IEnvironment)getBeliefbase().getBelief("environment").getFact();
			//boolean success = env.dropWasteInWastebin(waste, wastebin);
			IGoal dg = createGoal("drop_waste_action");
			dg.getParameter("waste").setValue(waste);
			dg.getParameter("wastebin").setValue(wastebin);
			dispatchSubgoalAndWait(dg);
		}

		// Update beliefs.
		else if(event instanceof IGoalEvent && ((IGoalEvent)event).getGoal().getType().equals("drop_waste_action"))
		{
			getLogger().info("Dropping waste to wastebin!");
			wastebin.addWaste(waste);
	
			IBeliefSet wbs = getBeliefbase().getBeliefSet("wastebins");
			if(wbs.containsFact(wastebin))
				wbs.updateFact(wastebin);
			else
				wbs.addFact(wastebin);
			//getBeliefbase().getBeliefSet("wastebins").updateOrAddFact(wastebin);
			getBeliefbase().getBelief("carriedwaste").setFact(null);
		}
	}
}
