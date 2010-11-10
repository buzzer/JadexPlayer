package jadex.examples.cleanerworld.single;

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
		getBeliefbase().getBelief("is_loading").setFact(new Boolean(true));

		// Move to station.
		Location station = (Location)getBeliefbase().getBelief("chargingstation_location").getFact();
		IGoal moveto = createGoal("achievemoveto");
		moveto.getParameter("location").setValue(station);
		dispatchSubgoalAndWait(moveto);

		Location	location = (Location)getBeliefbase().getBelief("my_location").getFact();
		double	charge	= ((Double)getBeliefbase().getBelief("chargestate").getFact()).doubleValue();

		while(location.getDistance(station)<0.01 && charge<1.0)
		{
			waitFor(100);
			startAtomic();
			charge	= ((Double)getBeliefbase().getBelief("chargestate").getFact()).doubleValue();
			charge	= Math.min(charge + 0.01, 1.0);
			getBeliefbase().getBelief("chargestate").setFact(new Double(charge));
			if(charge>=1)
			{
				getLogger().info("Loading finished.");
				getBeliefbase().getBelief("is_loading").setFact(new Boolean(false));
			}
			endAtomic();
			location = (Location)getBeliefbase().getBelief("my_location").getFact();
		}


	}

}
