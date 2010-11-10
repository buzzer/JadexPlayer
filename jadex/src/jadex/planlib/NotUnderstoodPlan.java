package jadex.planlib;

import jadex.adapter.fipa.SFipa;
import jadex.runtime.*;

/**
 *  Send a not-understood message when
 *  no other plan is able to handle a message.
 */
public class NotUnderstoodPlan extends Plan
{
	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public NotUnderstoodPlan()
	{
		getLogger().info("Created: "+this);
	}

	//-------- methods --------

	/**
	 *  Execute the plan.
	 */
	public void body()
	{
		sendMessage(((IMessageEvent)getInitialEvent()).createReply(
			"not_understood", getInitialEvent().getParameter(SFipa.CONTENT).getValue()));
	}
}
