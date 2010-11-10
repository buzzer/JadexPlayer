package jadex.testcases.semiautomatic;

import jadex.runtime.Plan;

/**
 *  The plan throws internal events.
 */
public class EventThrowerPlan extends Plan
{
	//-------- constructors --------

	/**
	 *  Create a new countdown plan.
	 */
	public EventThrowerPlan()
	{
		getLogger().info("Created: "+this);
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		int i=0;
		while(true)
		{
			i++;
			createInternalEvent("user_defined", "hallo: "+i);
			getLogger().info("Created internal event: "+i);
			waitFor(2000);
		}
	}
}
