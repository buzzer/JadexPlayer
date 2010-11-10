package jadex.planlib;

import jadex.runtime.*;
import jadex.adapter.fipa.SFipa;

/**
 *  The pinging plan continously sends ping messages
 *  to another agent on the same platform.
 */
public class PingingPlan	extends Plan
{
	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		while(true)
		{
			// Send ping and wait for answer.
			IMessageEvent mevent = createMessageEvent("custom_query_ping");
			mevent.getParameter(SFipa.CONTENT).setValue(getBeliefbase().getBelief("ping_content").getFact());
			mevent.getParameterSet(SFipa.RECEIVERS).addValue(getBeliefbase().getBelief("ping_receiver").getFact());
			//mevent.setContent(getBeliefbase().getBelief("ping_content").getFact());
			long timeout;
			if(hasParameter("timeout"))
				timeout	= ((Long)getParameter("timeout").getValue()).longValue();
			else
				timeout	= -1;
			try
			{
				sendMessageAndWait(mevent, timeout);
			}
			catch(TimeoutException e)
			{
				// todo: implement some metrics when to give up...
			}
			// When agent answered, wait before sending next ping.
			long sleep = ((Long)getBeliefbase().getBelief("ping_delay").getFact()).longValue();
			waitFor(sleep);
		}
	}
}
