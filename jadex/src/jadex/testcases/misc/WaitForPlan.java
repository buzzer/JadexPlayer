package jadex.testcases.misc;

import jadex.runtime.IGoal;
import jadex.runtime.IMessageEvent;
import jadex.runtime.Plan;
import jadex.runtime.TimeoutException;
import jadex.adapter.fipa.SFipa;
import jadex.planlib.TestReport;

/**
 *  Test various waitFor methods first from plan, then from external access.
 */
public class WaitForPlan extends Plan	implements Runnable
{
	/** Boolean that indicates if the thread is finished. */
	boolean thread_finished;

	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		TestReport	report	= new TestReport("time", "Waiting for 100 ms.");
		waitFor(100);
		report.setSucceeded(true);
		getBeliefbase().getBeliefSet("reports").addFact(report);

		report	= new TestReport("beliefchange", "Waiting for belief 'time' to change.");
		long oldt = ((Long)getBeliefbase().getBelief("time").getFact()).longValue();
		try
		{
			waitForBeliefChange("time", 2000);
			long newt = ((Long)getBeliefbase().getBelief("time").getFact()).longValue();
			if(newt!=oldt)
				report.setSucceeded(true);
			else
				report.setReason("No change in belief detected.");
		}
		catch(TimeoutException e)
		{
			report.setReason("Timeout occurred.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);

		report	= new TestReport("condition", "Waiting for a condition to become true.");
		oldt = ((Long)getBeliefbase().getBelief("time").getFact()).longValue();
		try
		{
			waitForCondition("$beliefbase.time>"+(oldt+100)+"L", 3000);
			long newt = ((Long)getBeliefbase().getBelief("time").getFact()).longValue();
			if(newt>oldt+100)
				report.setSucceeded(true);
			else
				report.setReason("Condition does not hold.");
		}
		catch(TimeoutException e)
		{
			report.setReason("Timeout occurred.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
		
		report	= new TestReport("truecondition", "Waiting for a condition that is initially true.");
		try
		{
			waitForCondition("true", 1000);
			report.setSucceeded(true);
		}
		catch(TimeoutException e)
		{
			report.setReason("Timeout occurred.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);

		report	= new TestReport("goal", "Waiting for a goal to complete.");
		IGoal goal = getGoalbase().createGoal("test");
		try
		{
			dispatchSubgoalAndWait(goal, 1000);
			report.setSucceeded(true);
		}
		catch(TimeoutException e)
		{
			report.setReason("Timeout occurred.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
		
		report	= new TestReport("message", "Waiting for a message reply.");
		IMessageEvent me = createMessageEvent("default_query_ping");
		me.getParameterSet(SFipa.RECEIVERS).addValue(getScope().getAgentIdentifier());
		try
		{
			sendMessageAndWait(me, 2000);
			report.setSucceeded(true);
		}
		catch(TimeoutException e)
		{
			report.setReason("Timeout occurred.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
		
		
		report	= new TestReport("timeout", "Waiting for a timeout.");
		try
		{
			IMessageEvent rep = waitForMessageEvent("query_ping", 1000);
			report.setReason("Received message: "+rep);
		}
		catch(TimeoutException e)
		{
			report.setSucceeded(true);
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
		
		// Test external access.
		new Thread(this).start();

		while(!thread_finished)
			waitFor(100);
	}

	/**
	 * The run method is called from the external thread.
	 */
	public void run()
	{
		TestReport	report	= new TestReport("x-time", "Waiting for 100 ms.");
		getExternalAccess().waitFor(100);
		report.setSucceeded(true);
		getExternalAccess().getBeliefbase().getBeliefSet("reports").addFact(report);

		report	= new TestReport("x-beliefchange", "Waiting for belief 'time' to change.");
		try
		{
			// It can happen that we are just before the belief change scheduled.
			// Therefore we have to ensure that we are in a fresh period.
			getExternalAccess().waitForBeliefChange("time", 5000);

			long oldt = ((Long)getExternalAccess().getBeliefbase().getBelief("time").getFact()).longValue();
			getExternalAccess().waitForBeliefChange("time", 5000);
			long newt = ((Long)getExternalAccess().getBeliefbase().getBelief("time").getFact()).longValue();
			if(newt!=oldt)
				report.setSucceeded(true);
			else
				report.setReason("No change in belief detected: "+oldt+" "+newt);
		}
		catch(TimeoutException e)
		{
			report.setReason("Timeout occurred.");
		}
		getExternalAccess().getBeliefbase().getBeliefSet("reports").addFact(report);

		report	= new TestReport("x-condition", "Waiting for a condition to become true.");
		long oldt = ((Long)getExternalAccess().getBeliefbase().getBelief("time").getFact()).longValue();
		try
		{
			getExternalAccess().waitForCondition("$beliefbase.time>"+(oldt+100)+"L", 3000);
			long newt = ((Long)getExternalAccess().getBeliefbase().getBelief("time").getFact()).longValue();
			if(newt>oldt+100)
				report.setSucceeded(true);
			else
				report.setReason("Condition does not hold.");
		}
		catch(TimeoutException e)
		{
			report.setReason("Timeout occurred.");
		}
		getExternalAccess().getBeliefbase().getBeliefSet("reports").addFact(report);

		report	= new TestReport("x-truecondition", "Waiting for a condition that is initially true.");
		try
		{
			getExternalAccess().waitForCondition("true", 1000);
			report.setSucceeded(true);
		}
		catch(TimeoutException e)
		{
			report.setReason("Timeout occurred.");
		}
		getExternalAccess().getBeliefbase().getBeliefSet("reports").addFact(report);

		report	= new TestReport("x-goal", "Waiting for a goal to complete.");
		IGoal goal = getExternalAccess().getGoalbase().createGoal("test");
		try
		{
			getExternalAccess().dispatchTopLevelGoalAndWait(goal, 1000);
			report.setSucceeded(true);
		}
		catch(TimeoutException e)
		{
			report.setReason("Timeout occurred.");
		}
		getExternalAccess().getBeliefbase().getBeliefSet("reports").addFact(report);

		report	= new TestReport("x-message", "Waiting for a message reply.");
		IMessageEvent me = getExternalAccess().createMessageEvent("default_query_ping");
		me.getParameterSet(SFipa.RECEIVERS).addValue(getExternalAccess().getAgentIdentifier());
		try
		{
			getExternalAccess().sendMessageAndWait(me, 1000);
			report.setSucceeded(true);
		}
		catch(TimeoutException e)
		{
			report.setReason("Timeout occurred.");
		}
		getExternalAccess().getBeliefbase().getBeliefSet("reports").addFact(report);
		
		report	= new TestReport("x-timeout", "Waiting for a timeout.");
		try
		{
			IMessageEvent rep = getExternalAccess().waitForMessageEvent("query_ping", 1000);
			report.setReason("Received message: "+rep);
		}
		catch(TimeoutException e)
		{
			report.setSucceeded(true);
		}
		getExternalAccess().getBeliefbase().getBeliefSet("reports").addFact(report);

		thread_finished = true;
	}
}
