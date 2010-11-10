package jadex.testcases.misc;

import jadex.adapter.fipa.AgentDescription;
import jadex.adapter.fipa.SFipa;
import jadex.planlib.TestReport;
import jadex.runtime.*;
import jadex.util.SUtil;

import java.util.Date;

/**
 *  Test the df plans.
 */
public class DFTestPlan extends Plan
{
	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		int num = 1;
		num	= performInitialTests(num);
		num = performTests(num, null); // test locally
		performTests(num, SFipa.DF); // test remotely
	}
	
	/**
	 *  Test initial keep registered goal.
	 */
	public int	performInitialTests(int num)
	{
		AgentDescription desc = (AgentDescription)getScope().getPropertybase().getProperty("fipa.agentdescription.dftest");

		// Try to search at the df.
		TestReport	tr = new TestReport("#"+num++, "Try to search for initial registration.");
		getLogger().info("\nTrying to search...");
		IGoal search = createGoal("df_search");
		search.getParameter("description").setValue(desc);
		try
		{
			dispatchSubgoalAndWait(search);
			getLogger().info(" search ok: "+ SUtil.arrayToString(search.getParameterSet("result").getValues()));
			tr.setSucceeded(true);
		}
		catch(GoalFailureException gfe)
		{
			getLogger().info(" search failed. "+search.getParameterSet("result").getValues());
			tr.setReason("Search failed. "+search.getParameterSet("result").getValues());
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);

		// Check if keep goal continues to modify.
		getLogger().info("Trying to keep registered...");
		tr = new TestReport("#"+num++, "Try to keep (modify) initial registration.");
		try
		{
			IGoal	keep	= waitForGoal("df_keep_registered", 6000);
			if(keep.isSucceeded())
				getLogger().info(" modify succeeded. ");
			tr.setSucceeded(true);
			keep.drop();
		}
		catch(TimeoutException e)
		{
			getLogger().info(" modify failed.");
			tr.setReason("Modify failed.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);
		
		// Deregister by the df to asure clean state before next tests.
		IGoal deregister = createGoal("df_deregister");
		deregister.getParameter("description").setValue(desc);
		try
		{
			dispatchSubgoalAndWait(deregister);
		}
		catch(GoalFailureException gfe)
		{
			// No prob, registration already removed.
		}
		
		return num;
	}

	/**
	 *  Perform the tests.
	 */
	public int performTests(int num, BasicAgentIdentifier df)
	{
		AgentDescription desc = (AgentDescription)getScope().getPropertybase().getProperty("fipa.agentdescription.dftest");
		AgentDescription desc_clone = (AgentDescription)desc.clone();
		long olt = System.currentTimeMillis()+2000;
		desc_clone.setLeaseTime(new Date(olt));

		// Try to register by the df for some lease time.
		TestReport tr = new TestReport("#"+num++, "Test of lease time.");
		getLogger().info("Testing lease time...");
		IGoal register = createGoal("df_register");
		register.getParameter("description").setValue(desc_clone);
		register.getParameter("df").setValue(df);
		//register.getParameter("leasetime").setValue(new Long(2000));

		try
		{
			dispatchSubgoalAndWait(register);
			getLogger().info(" register ok.");
			waitFor(2200);
			//desc_clone.setLeaseTime(null);
			//desc_clone.setName(null);
			IGoal search = createGoal("df_search");
			search.getParameter("description").setValue(desc_clone);
			search.getParameter("df").setValue(df);
			dispatchSubgoalAndWait(search);
			if(search.getParameterSet("result").getValues().length>0)
			{
				//System.out.println(((AgentDescription)search.getParameterSet("result").getValues()[0]).getLeaseTime().getTime());
				getLogger().info(" lease time test failed. "+search.getParameterSet("result").getValues());
				tr.setReason("Lease time test failed. "+search.getParameterSet("result").getValues());
			}
			else
			{
				tr.setSucceeded(true);
			}
		}
		catch(GoalFailureException gfe)
		{
			//getLogger().info(" register failed. "+register.getResult());
			getLogger().info(" register failed. "+register.getParameter("result").getValue());
			tr.setReason("Register failed. "+register.getParameter("result").getValue());
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);
		//waitFor(2000);

		// Try to register by the df.
		tr = new TestReport("#"+num++, "Try to register.");
		getLogger().info("Trying to register...");
		register = createGoal("df_register");
		register.getParameter("description").setValue(desc);
		register.getParameter("df").setValue(df);
		try
		{
			dispatchSubgoalAndWait(register);
			getLogger().info(" register ok.");
			tr.setSucceeded(true);
		}
		catch(GoalFailureException gfe)
		{
			//getLogger().info(" register failed. "+register.getResult());
			getLogger().info(" register failed. "+register.getParameter("result").getValue());
			tr.setReason("Register failed. "+register.getParameter("result").getValue());
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);
		//waitFor(2000);

		// Try to search at the df.
		tr = new TestReport("#"+num++, "Try to search.");
		getLogger().info("\nTrying to search...");
		IGoal search = createGoal("df_search");
		search.getParameter("description").setValue(desc);
		search.getParameter("df").setValue(df);
		try
		{
			dispatchSubgoalAndWait(search);
			//getLogger().info(" search ok: "+SUtil.arrayToString(search.getResult()));
			getLogger().info(" search ok: "+ SUtil.arrayToString(search.getParameterSet("result").getValues()));
			tr.setSucceeded(true);
		}
		catch(GoalFailureException gfe)
		{
			//getLogger().info(" search failed. "+search.getResult());
			getLogger().info(" search failed. "+search.getParameterSet("result").getValues());
			tr.setReason("Search failed. "+search.getParameterSet("result").getValues());
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);
		//waitFor(2000);

		// Try to deregister by the df.
		tr = new TestReport("#"+num++, "Try to deregister.");
		getLogger().info("\nTrying to deregister...");
		IGoal deregister = createGoal("df_deregister");
		deregister.getParameter("description").setValue(desc);
		deregister.getParameter("df").setValue(df);
		try
		{
			dispatchSubgoalAndWait(deregister);
			getLogger().info(" deregister ok.");
			tr.setSucceeded(true);
		}
		catch(GoalFailureException gfe)
		{
			getLogger().info(" deregister failed. "+deregister);
			tr.setReason("Deregister failed: "+deregister);
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);

		// Try to keep registered by the df.
		tr = new TestReport("#"+num++, "Try to keep registered (initial register).");
		getLogger().info("Trying to keep registered (should modify each 5 sec)...");
		IGoal keep = createGoal("df_keep_registered");
		keep.getParameter("description").setValue(desc);
		keep.getParameter("leasetime").setValue(new Integer(5000));
		keep.getParameter("df").setValue(df);
		IEvent event = null;
		try
		{
			dispatchSubgoalAndWait(keep);
		}
		catch(GoalFailureException e)
		{
//			e.printStackTrace();
			getLogger().warning("Exception: "+e);
		}
		if(keep.isSucceeded())
		{
			getLogger().info(" initial register ok.");
			tr.setSucceeded(true);
		}
		else
		{
			getLogger().info(" initial register failed: "+keep.getLifecycleState()+", "+event);
			tr.setReason("Initial register failed: "+keep.getLifecycleState()+", "+event);
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);

		//waitFor(2000);
		getLogger().info("Trying to keep registered...");
		tr = new TestReport("#"+num++, "Try to keep registered (modify).");
		try
		{
			waitForSubgoal(keep, 6000);
			if(keep.isSucceeded())
				getLogger().info(" modify succeeded. ");
			tr.setSucceeded(true);
		}
		catch(TimeoutException e)
		{
			getLogger().info(" modify failed. "+keep);
			tr.setReason("Modify failed. "+keep);
		}
		keep.drop();
		getBeliefbase().getBeliefSet("reports").addFact(tr);

		//getLogger().info("\nAll tests finished");

		// Deregister and delete agent.
		try
		{
			IGoal	deregister2 = createGoal("df_deregister");
			deregister2.getParameter("description").setValue(desc);
			deregister2.getParameter("df").setValue(df);
			dispatchSubgoalAndWait(deregister2);
		}
		catch(GoalFailureException e)
		{
		}
		return num;
	}
}
