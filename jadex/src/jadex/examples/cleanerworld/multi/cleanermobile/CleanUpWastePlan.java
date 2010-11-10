package jadex.examples.cleanerworld.multi.cleanermobile;

import jadex.examples.cleanerworld.multi.*;
import jadex.runtime.*;
import jadex.runtime.impl.*;
import jadex.runtime.planwrapper.ElementWrapper;

/**
 *  Clean-up some waste.
 */
public class CleanUpWastePlan extends MobilePlan
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
	public void action(IEvent event)
	{
		//System.out.println("Clean-up waste plan started.");

		if(getBeliefbase().getBelief("carriedwaste").getFact()==null
			&& !(event instanceof IGoalEvent && ((IGoalEvent)event).getGoal().getType().equals("achievedropwaste")))
		{
			Waste waste = (Waste)getParameter("waste").getValue();
			// the following is only for debugging
			if(waste==null)
			{
				RProcessGoal	proc	= (RProcessGoal)((ElementWrapper)getRootGoal()).unwrap();
				IRGoal	orig	= proc.getProprietaryGoal();
				throw new RuntimeException("Waste is null: " + proc.getParameter("waste") + ", "+orig.getParameter("waste"));
			}
			//System.out.println("Pickup goal created.");
			IGoal pickup = createGoal("achievepickupwaste");
			pickup.getParameter("waste").setValue(waste);
			dispatchSubgoalAndWait(pickup);
		}

		// Find a not full waste-bin
		else if(!(event instanceof IGoalEvent && ((IGoalEvent)event).getGoal().getType().equals("querywastebin"))
			&& !(event instanceof IGoalEvent && ((IGoalEvent)event).getGoal().getType().equals("achievedropwaste")))
		{
			IGoal findbin = createGoal("querywastebin");
			dispatchSubgoalAndWait(findbin);
		}

		// Drop waste to wastebin.
		else if(event instanceof IGoalEvent && ((IGoalEvent)event).getGoal().getType().equals("querywastebin"))
		{
			Wastebin wastebin = (Wastebin)((IGoalEvent)event).getGoal().getParameter("result").getValue();
			IGoal	drop = createGoal("achievedropwaste");
			drop.getParameter("wastebin").setValue(wastebin);
			dispatchSubgoalAndWait(drop);
		}
	}
}
