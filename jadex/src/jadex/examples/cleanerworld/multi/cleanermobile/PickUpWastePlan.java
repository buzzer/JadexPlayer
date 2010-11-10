package jadex.examples.cleanerworld.multi.cleanermobile;

import jadex.examples.cleanerworld.multi.Waste;
import jadex.runtime.*;

/**
 *  Clean-up some waste.
 */
public class PickUpWastePlan extends MobilePlan
{

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public PickUpWastePlan()
	{
		getLogger().info("Created: "+this);
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void action(IEvent event)
	{
		Waste waste = (Waste)getParameter("waste").getValue();

		// Move to the waste position when necessary
		if(event instanceof IGoalEvent && !((IGoalEvent)event).isInfo())
		{
			getLogger().info("Moving to waste!");
			IGoal moveto = createGoal("achievemoveto");
			moveto.getParameter("location").setValue(waste.getLocation());
			dispatchSubgoalAndWait(moveto);
		}
		
		// Pickup the waste.
		else if(event instanceof IGoalEvent && ((IGoalEvent)event).getGoal().getType().equals("achievemoveto"))
		{
			IGoal dg = createGoal("pickup_waste_action");
			dg.getParameter("waste").setValue(waste);
			dispatchSubgoalAndWait(dg);
		}
		
		// Update the beliefs.
		else if(event instanceof IGoalEvent && ((IGoalEvent)event).getGoal().getType().equals("pickup_waste_action"))
		{
			getBeliefbase().getBelief("carriedwaste").setFact(waste);
			getBeliefbase().getBeliefSet("wastes").removeFact(waste);
			getLogger().info("Picked up-waste!");
		}
	}
}
