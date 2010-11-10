package jadex.benchmarks;

import javax.swing.JFrame;
import javax.swing.JLabel;

import jadex.adapter.fipa.*;
import jadex.runtime.*;

/**
 *  Send a specified amount of messages.
 */
public class MessageSenderPlan	extends Plan
{
	//-------- methods --------

	/**
	 * The body of the plan.
	 */
	public void body()
	{
		int msgcnt = ((Integer)getBeliefbase().getBelief("msg_cnt").getFact()).intValue();
		AgentIdentifier receiver = (AgentIdentifier)getBeliefbase().getBelief("receiver").getFact();
		
		getLogger().info("Now sending " + msgcnt + " messages to " + receiver);
		
		getBeliefbase().getBelief("starttime").setFact(new Long(System.currentTimeMillis()));		
		
		// Send messages.
		for(int i=1; i<=msgcnt; i++)
		{
			IMessageEvent request = createMessageEvent("inform");
			request.getParameterSet(SFipa.RECEIVERS).addValue(receiver);
			request.getParameter(SFipa.REPLY_WITH).setValue("some reply id");
			sendMessage(request);
			
			getBeliefbase().getBelief("sent").setFact(new Integer(i));
			
			if(i % 10 == 0)
			{
				//System.out.print('.');
				waitFor(0);
			}
		}
	}
}

