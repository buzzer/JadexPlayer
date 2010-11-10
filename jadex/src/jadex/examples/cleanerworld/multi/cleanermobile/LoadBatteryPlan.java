package jadex.examples.cleanerworld.multi.cleanermobile;

import jadex.examples.cleanerworld.multi.*;
import jadex.runtime.*;

/**
 *  Go to the charging station and load the battery.
 */
public class LoadBatteryPlan extends MobilePlan
{
	//-------- attributes --------
	
	/** The charging station. */
	protected Chargingstation station;
	
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
	public void action(IEvent event)
	{
		// Find nearest station.
		if(station==null && event instanceof IGoalEvent && !((IGoalEvent)event).isInfo())
		{
			IGoal findstation = createGoal("querychargingstation");
			dispatchSubgoalAndWait(findstation);
		}
		// Move to station.
		else if(station==null && event instanceof IGoalEvent && ((IGoalEvent)event).isInfo())
		{
			station = (Chargingstation)((IGoalEvent)event).getGoal().getParameter("result").getValue();
			IGoal moveto = createGoal("achievemoveto");
			moveto.getParameter("location").setValue(station.getLocation());
			dispatchSubgoalAndWait(moveto);
		}

		// Stay until recharged.
		else if(station!=null)
		{
			Location	location = (Location)getBeliefbase().getBelief("my_location").getFact();
			double	charge	= ((Double)getBeliefbase().getBelief("my_chargestate").getFact()).doubleValue();

			if(location.getDistance(station.getLocation())<0.01 && charge<1.0)
			{
				charge	= ((Double)getBeliefbase().getBelief("my_chargestate").getFact()).doubleValue();
				charge	= Math.min(charge + 0.01, 1.0);
				getBeliefbase().getBelief("my_chargestate").setFact(new Double(charge));
				waitFor(100);
			}
		}
	}
}
