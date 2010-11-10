package jadex.examples.cleanerworld.multi.cleaner;

import jadex.examples.cleanerworld.multi.*;
import jadex.runtime.*;

/**
 *  Go to the charging station and load the battery.
 */
public class LoadBatteryPlan extends Plan
{
	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public LoadBatteryPlan()
	{
		getLogger().info("Created: "+this);
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		// Hack! Should be done with goal..
	    // todo: test if goal state (-> in_process) could be used 
		//getBeliefbase().getBelief("is_loading").setFact(new Boolean(true));

		// Move to station.
		IGoal findstation = createGoal("querychargingstation");
		dispatchSubgoalAndWait(findstation);
		//Chargingstation station = (Chargingstation)findstation.getResult();
		Chargingstation station = (Chargingstation)findstation.getParameter("result").getValue();

		if(station!=null)
		{
			IGoal moveto = createGoal("achievemoveto");
			moveto.getParameter("location").setValue(station.getLocation());
			dispatchSubgoalAndWait(moveto);

			Location	location = (Location)getBeliefbase().getBelief("my_location").getFact();
			double	charge	= ((Double)getBeliefbase().getBelief("my_chargestate").getFact()).doubleValue();

			while(location.getDistance(station.getLocation())<0.01 && charge<1.0)
			{
				waitFor(100);
				charge	= ((Double)getBeliefbase().getBelief("my_chargestate").getFact()).doubleValue();
				charge	= Math.min(charge + 0.01, 1.0);
				getBeliefbase().getBelief("my_chargestate").setFact(new Double(charge));
				location = (Location)getBeliefbase().getBelief("my_location").getFact();
				IGoal dg = createGoal("get_vision_action");
				dispatchSubgoalAndWait(dg);
			}
		}

		getLogger().info("Loading finished.");
		//getBeliefbase().getBelief("is_loading").setFact(new Boolean(false));
	}

}
