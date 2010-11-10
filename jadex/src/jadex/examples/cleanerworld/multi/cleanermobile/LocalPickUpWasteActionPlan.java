package jadex.examples.cleanerworld.multi.cleanermobile;

import jadex.examples.cleanerworld.multi.*;
import jadex.runtime.*;

/**
 *  Pick up a piece of waste in the environment.
 */
public class LocalPickUpWasteActionPlan extends	MobilePlan
{
	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void action(IEvent event)
	{
		IEnvironment	environment	= (IEnvironment)getBeliefbase().getBelief("environment").getFact();
		Waste waste = (Waste)getParameter("waste").getValue();

		boolean	success	= environment.pickUpWaste(waste);

		if(!success)
			throw new PlanFailureException();
	}
}
