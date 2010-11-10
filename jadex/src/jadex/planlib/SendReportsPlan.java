package jadex.planlib;

import jadex.adapter.fipa.*;
import jadex.runtime.*;

/**
 *  Send the reports to a test service.
 */
public class SendReportsPlan extends Plan
{
	/**
	 *  The body method is called on the
	 *  instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		// Compose results in test case.
		TestReport[]	reports	= (TestReport[])getBeliefbase().getBeliefSet("reports").getFacts();
		int cnt = ((Integer)getBeliefbase().getBelief("testcase_cnt").getFact()).intValue();
		AgentIdentifier	testcenter	= (AgentIdentifier)getBeliefbase().getBelief("testcenter").getFact();
		Testcase	testcase	= new Testcase(cnt, reports);
		
		// Send reports to the test service.
		IMessageEvent me = createMessageEvent("inform_reports");
		me.setContent(testcase);
		me.getParameterSet(SFipa.RECEIVERS).addValue(testcenter);
		sendMessage(me);

		if(!((Boolean)getBeliefbase().getBelief("keepalive").getFact()).booleanValue())
			killAgent();
	}
}
