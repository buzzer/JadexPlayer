package jadex.testcases.misc;

import jadex.runtime.IEvent;
import jadex.runtime.MobilePlan;

/**
 *  Check correct operation of end states.
 */
public class EndStateInitiatorPlan extends MobilePlan
{
	/**
	 *  Plan body.
	 *  Implemented as mobile plan to avoid microplansteps
	 *  between goal triggering and killing of agent.
	 */
	public void action(IEvent event)
	{
		// Set belief to true to trigger creation of goal.
		// Will be checked in worker plan.
		getBeliefbase().getBelief("trigger").setFact(Boolean.TRUE);

		// Kill agent to start end state.
		killAgent();
	}
}
