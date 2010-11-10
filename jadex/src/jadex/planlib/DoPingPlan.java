package jadex.planlib;

import jadex.runtime.*;
import jadex.adapter.fipa.SFipa;

/**
 *  Send a ping and wait for the reply.
 */
public class DoPingPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		// Send ping and wait for answer.
		IMessageEvent mevent = createMessageEvent("custom_query_ping");
		mevent.getParameter(SFipa.CONTENT).setValue(getParameter("content").getValue());
		mevent.getParameterSet(SFipa.RECEIVERS).addValue(getParameter("receiver").getValue());
		long timeout;
		if(getParameter("timeout").getValue()!=null)
			timeout	= ((Long)getParameter("timeout").getValue()).longValue();
		else
			timeout	= -1;
		sendMessageAndWait(mevent, timeout);
	}
}
