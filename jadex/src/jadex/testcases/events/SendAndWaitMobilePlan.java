package jadex.testcases.events;

import jadex.planlib.TestReport;
import jadex.runtime.*;
import jadex.adapter.fipa.SFipa;


/**
 *  A plan that shows how to wait for an answer.
 */
public class SendAndWaitMobilePlan	extends MobilePlan
{
	/**
	 *  The body of the plan.
	 */
	public void	action(IEvent event)
	{
		if(!(event instanceof IMessageEvent))
		{
			getLogger().info("Sending request and waiting for answer.");
	
			// Create request (send to self for testing).
			IMessageEvent	request	= createMessageEvent("rp_initiate");
			request.getParameterSet(SFipa.RECEIVERS).addValue(getScope().getAgentIdentifier());
	
			// Send message and wait for answer. Note that the acl message
			// should have ReplyWith or ConversationId to catch any answer messages!
			sendMessageAndWait(request, 1000);
		}
		else
		{
			getLogger().info("Success: Answer has been received: "+event);
			TestReport tr = new TestReport("send_message.", "Send a message a wait for an answer.", true, null);
			getBeliefbase().getBeliefSet("reports").addFact(tr);
		}
	}
	
	/**
	 *  Called when exception (e.g. timeout) occured.
	 */
	public void exception(Exception exception) throws Exception
	{
		getLogger().info("Failed: Answer has not been received");
		TestReport tr = new TestReport("send_message.", "Send a message a wait for an answer.", false, exception.toString());
		getBeliefbase().getBeliefSet("reports").addFact(tr);
	}
}

