package jadex.planlib;

import jadex.runtime.IEvent;
import jadex.runtime.MobilePlan;


/**
 *  Wait until tests are finished and then post the tests_finished goal.
 */
// Implemented as mobile plan, because of problems with microplansteps. 
public class TestsFinishedPlan extends MobilePlan
{
	/** The current step of the plan. */
	protected int step	= 0;
	
	/**
	 *  The plan body, called for each step.
	 */
	public void action(IEvent event)
	{
		switch(step++)
		{
			case 0:
				waitForCondition(getCondition("tests_finished"));
				break;
			case 1:
				dispatchSubgoalAndWait(createGoal("tests_finished"));
				break;
			case 2:
				// Last step, ignore.
		}
	}

	/**
	 *  Called when the plan is aborted
	 */
	public void aborted(IEvent event)
	{
//		System.err.println("Plan aborted in step: "+step+", "+this);
	}
}
