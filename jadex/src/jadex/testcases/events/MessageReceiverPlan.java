package jadex.testcases.events;

import jadex.planlib.TestReport;
import jadex.runtime.IMessageEvent;
import jadex.runtime.Plan;
import jadex.runtime.TimeoutException;
import jadex.util.SUtil;

/**
 *  Receives messages sent by initial event test.
 */
public class MessageReceiverPlan extends Plan
{
	public void body()
	{
		TestReport tr = new TestReport("receive_message", "Receive initial message event");
		try
		{
			for(int i=1; i<=3; i++)
			{
				IMessageEvent	me	= waitForMessageEvent("just_born_receive", 3000);
				if(!("hello_"+i).equals(me.getContent()))
				{
					tr.setReason("Wrong content: "+me.getContent());
					getBeliefbase().getBeliefSet("reports").addFact(tr);
					return;
				}
			}

			if(getWaitqueue().isEmpty())
			{
				tr.setSucceeded(true);
				getBeliefbase().getBeliefSet("reports").addFact(tr);
			}
			else
			{
				tr.setReason("Received too much events: "+SUtil.arrayToString(getWaitqueue().getEvents()));
				getBeliefbase().getBeliefSet("reports").addFact(tr);
			}
		}
		catch(TimeoutException e)
		{
			tr.setReason("Timeout");
			getBeliefbase().getBeliefSet("reports").addFact(tr);
		}
	}
}
