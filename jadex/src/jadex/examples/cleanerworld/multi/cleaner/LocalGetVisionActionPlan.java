package jadex.examples.cleanerworld.multi.cleaner;

import jadex.examples.cleanerworld.multi.*;
import jadex.runtime.Plan;

/**
 *  Pick up a piece of waste in the environment.
 */
public class LocalGetVisionActionPlan extends	Plan
{
	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		IEnvironment	environment	= (IEnvironment)getBeliefbase().getBelief("environment").getFact();
		Cleaner cl = new Cleaner((Location)getBeliefbase().getBelief("my_location").getFact(),
			getAgentName(),
			(Waste)getBeliefbase().getBelief("carriedwaste").getFact(),
			((Number)getBeliefbase().getBelief("my_vision").getFact()).doubleValue(),
			((Number)getBeliefbase().getBelief("my_chargestate").getFact()).doubleValue());

		Vision	vision	= environment.getVision(cl);

		getParameter("vision").setValue(vision);
	}
}
