package jadex.examples.cleanerworld.multi.cleanermobile;

import jadex.examples.cleanerworld.multi.*;
import jadex.runtime.*;

/**
 *  Pick up a piece of waste in the environment.
 */
public class LocalDropWasteActionPlan extends	MobilePlan
{
	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void action(IEvent event)
	{
		IEnvironment	environment	= (IEnvironment)getBeliefbase().getBelief("environment").getFact();
		Waste waste = (Waste)getParameter("waste").getValue();
		Wastebin wastebin = (Wastebin)getParameter("wastebin").getValue();

		boolean	success	= environment.dropWasteInWastebin(waste, wastebin);

		if(!success)
			throw new PlanFailureException();
	}
}
