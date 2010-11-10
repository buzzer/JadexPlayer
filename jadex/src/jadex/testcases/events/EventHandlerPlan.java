package jadex.testcases.events;

import jadex.planlib.TestReport;
import jadex.runtime.IInternalEvent;
import jadex.runtime.Plan;
import jadex.runtime.TimeoutException;
import jadex.util.SUtil;

/**
 *  Handle internal events sent by initial event test.
 */
public class EventHandlerPlan extends Plan
{
	public void body()
	{
		TestReport tr = new TestReport("handle_event", "Handle initial internal event");
		try
		{
			for(int i=1; i<=2; i++)
			{
				IInternalEvent	ie	= waitForInternalEvent("ievent", 3000);
				if(!(""+i).equals(ie.getParameter("param2").getValue()))
				{
					tr.setReason("Wrong param2 content: "+ie.getParameter("param2").getValue());
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
