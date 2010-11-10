package jadex.tutorial;

import jadex.runtime.Plan;

/**
 *  The thank you plan congratulates a user for using the translation
 *  service every 10th translation.
 */
public class ThankYouPlanC3 extends Plan
{
	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public ThankYouPlanC3()
	{
		getLogger().info("Created:"+this);
	}

	//-------- methods --------

	/**
	 *  Execute the plan.
	 */
	public void body()
	{
		int cnt = ((Integer)getBeliefbase().getBelief("transcnt").getFact()).intValue();
		getLogger().info("Congratulations! You have translated the  "+cnt
			+" word today!");
	}
}