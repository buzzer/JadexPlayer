package jadex.examples.ping;

import jadex.runtime.*;
import jadex.adapter.fipa.SFipa;


/**
 *  The echo plan reacts on every message
 *  with the same message.
 */
public class EchoPlan extends Plan
{
	//-------- methods --------

	/**
	 *  Handle the ping request.
	 */
	public void body()
	{
		// Get the initial event.
		IMessageEvent me = (IMessageEvent)getInitialEvent();

		// Create the reply.
		IMessageEvent re = me.createReply("any_message", me.getContent());
		re.getParameter(SFipa.PERFORMATIVE).setValue(me.getParameter(SFipa.PERFORMATIVE).getValue());

		// Send back the reply and terminate.
		sendMessage(re);
	}
}

