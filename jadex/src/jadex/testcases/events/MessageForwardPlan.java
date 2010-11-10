package jadex.testcases.events;

import java.util.List;
import java.util.Map;

import jadex.planlib.TestReport;
import jadex.runtime.*;
import jadex.testcases.AbstractMultipleAgentsPlan;
import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.fipa.SFipa;

/**
 *  Receive a message sent to itself and forward it to another agent
 *  by exchanging the receiver in the message.
 */
public class MessageForwardPlan extends AbstractMultipleAgentsPlan
{

	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		if(((Integer)getBeliefbase().getBelief("testcase_cnt").getFact()).intValue()==0)
		{
//			System.out.println("Received forwarded message succesfully!");
			return;
		}

		List agents = createAgents("jadex.testcases.events.MessageForward", "receiver", new Map[1]);
		
		TestReport tr = new TestReport("forward_message", "Forward a received message.");
		if(assureTest(tr))
		{		
			try
			{
				IMessageEvent me = (IMessageEvent)getInitialEvent();
				me.getParameterSet(SFipa.RECEIVERS).removeValues();
				me.getParameterSet(SFipa.RECEIVERS).addValue(agents.get(0));
				sendMessage(me);
				tr.setSucceeded(true);
			}
			catch(Exception e)
			{
				//e.printStackTrace();
				tr.setReason(e.getMessage());
			}
		}

		getBeliefbase().getBeliefSet("reports").addFact(tr);
	}
}
