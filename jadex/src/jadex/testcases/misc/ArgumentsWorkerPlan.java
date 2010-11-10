package jadex.testcases.misc;

import jadex.runtime.IMessageEvent;
import jadex.runtime.Plan;

/**
 *  Plan that sends back a message to the creator.
 */
public class ArgumentsWorkerPlan extends Plan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
		IMessageEvent me = createMessageEvent("inform_created");
		sendMessage(me);
		killAgent();
	}
}
