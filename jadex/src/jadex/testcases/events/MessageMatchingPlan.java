package jadex.testcases.events;

import jadex.runtime.Plan;
import jadex.runtime.IMessageEvent;
import jadex.adapter.fipa.SFipa;
import jadex.planlib.TestReport;

/**
 *
 */
public class MessageMatchingPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		TestReport tr = new TestReport("#1", "Test if the message is correctly mapped.");
		IMessageEvent me = createMessageEvent("inform");
		me.setContent("two");
		me.getParameterSet("receivers").addValue(getScope().getAgentIdentifier());
		me.getParameter(SFipa.CONVERSATION_ID).setValue("conv-id");
		sendMessage(me);
		waitFor(300);

		if(getBeliefbase().getBeliefSet("results").containsFact("two"))
		{
			tr.setSucceeded(true);
		}
		else
		{
			tr.setReason("Message incorrectly mapped.");
			getLogger().info("Test #1 failed.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);


		tr = new TestReport("#2", "Test if the message is correctly not mapped.");
		me = createMessageEvent("inform");
		me.setContent("two");
		me.getParameterSet("receivers").addValue(getScope().getAgentIdentifier());
		sendMessage(me);
		waitFor(300);

		if(getBeliefbase().getBeliefSet("results").size()==1)
		{
			tr.setSucceeded(true);
		}
		else
		{
			tr.setReason("Message incorrectly mapped.");
			getLogger().info("Test #2 failed.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);


		tr = new TestReport("#3", "Test if the message is correctly mapped.");
		me = createMessageEvent("inform");
		me.setContent("one");
		me.getParameterSet("receivers").addValue(getScope().getAgentIdentifier());
		sendMessage(me);
		waitFor(300);

		if(getBeliefbase().getBeliefSet("results").containsFact("one"))
		{
			tr.setSucceeded(true);
		}
		else
		{
			tr.setReason("Message incorrectly mapped.");
			getLogger().info("Test #3 failed.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);


		tr = new TestReport("#4", "Test if the message is correctly not mapped.");
		me = createMessageEvent("inform");
		me.setContent("three");
		me.getParameterSet("receivers").addValue(getScope().getAgentIdentifier());
		sendMessage(me);
		waitFor(300);

		if(getBeliefbase().getBeliefSet("results").size()==2)
		{
			tr.setSucceeded(true);
		}
		else
		{
			tr.setReason("Message incorrectly mapped.");
			getLogger().info("Test #4 failed.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);
	}
}
