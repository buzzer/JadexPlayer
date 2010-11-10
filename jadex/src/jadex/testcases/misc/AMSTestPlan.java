package jadex.testcases.misc;

import jadex.adapter.fipa.*;
import jadex.runtime.*;
import jadex.planlib.TestReport;

/**
 *  Test the AMS plans.
 */
public class AMSTestPlan extends Plan
{
	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		int num=1;
		num = performTests(num, null); // test locally
		performTests(num, SFipa.AMS); // test remotely
	}

	/**
	 *  The failed method is called on plan failure/abort.
	 */
	public void	failed()
	{
		System.err.println("\nSome tests failed!");
	}

	/**
	 *  Perform the basic ams tests.
	 */
	public int performTests(int num, BasicAgentIdentifier ams)
	{
		// Try to search the AMS.
		TestReport tr = new TestReport("#"+num++, "Searching for all agents");
		getLogger().info("\nSearching for all agents.");
		AMSAgentDescription	desc	= new AMSAgentDescription();
		SearchConstraints	constraints	= new SearchConstraints();
		constraints.setMaxResults(-1);
		IGoal	search	= createGoal("ams_search_agents");
		search.getParameter("description").setValue(desc);
		search.getParameter("constraints").setValue(constraints);
		search.getParameter("ams").setValue(ams);
		try
		{
			dispatchSubgoalAndWait(search);
			AMSAgentDescription[]	result	= (AMSAgentDescription[])search.getParameterSet("result").getValues();
			getLogger().info("Success! Found agents: "+result.length);
			for(int i=0; i< result.length; i++)
				getLogger().info("Agent "+i+": "+result[i].getName());
			tr.setSucceeded(true);
		}
		catch(GoalFailureException e)
		{
			e.printStackTrace();
			tr.setReason("Search subgoal failed.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);

		// Try to create agent.
		tr = new TestReport("#"+num++, "Trying to create ping agent.");
		getLogger().info("\nTrying to create ping agent.");
		IGoal	create	= createGoal("ams_create_agent");
		create.getParameter("type").setValue("jadex.examples.ping.Ping");
		create.getParameter("ams").setValue(ams);
		try
		{
			dispatchSubgoalAndWait(create);
			//getLogger().info("Success: Created "+create.getResult());
			getLogger().info("Success: Created "+create.getParameter("agentidentifier").getValue());
			tr.setSucceeded(true);
		}
		catch(GoalFailureException e)
		{
			e.printStackTrace();
			tr.setReason("Create agent subgoal failed.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);

		// Try to find ping agent.
		tr = new TestReport("#"+num++, "Searching for ping agent.");
		if(create.isSucceeded())
		{
			getLogger().info("\nSearching for ping agent.");
			desc	= new AMSAgentDescription();
			//desc.setName((AgentIdentifier)create.getResult());
			desc.setName((AgentIdentifier)create.getParameter("agentidentifier").getValue());
			constraints	= new SearchConstraints();
			constraints.setMaxResults(-1);
			search	= createGoal("ams_search_agents");
			search.getParameter("description").setValue(desc);
			search.getParameter("constraints").setValue(constraints);
			search.getParameter("ams").setValue(ams);
			try
			{
				dispatchSubgoalAndWait(search);
				AMSAgentDescription[] result	= (AMSAgentDescription[])search.getParameterSet("result").getValues();
				if(result.length==1)
				{
					getLogger().info("Success! Found ping agent:"+result[0].getName());
					tr.setSucceeded(true);
				}
				else
				{
					//failure	= true;
					System.err.println("Failure! Found "+result.length+" agents.");
					for(int i=0; i< result.length; i++)
						System.err.println("Agent "+i+": "+result[i].getName());
					tr.setReason("Could not find ping agent.");
				}
			}
			catch(GoalFailureException e)
			{
				tr.setReason("Search subgoal failed.");
			}
		}
		else
		{
			tr.setReason("Cannot search because creation already failed.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);


		// Try to destroy created agent.
		tr = new TestReport("#"+num++, "Trying to destroy ping agent.");
		if(create.isSucceeded())
		{
			getLogger().info("\nTrying to destroy ping agent.");
			IGoal	destroy	= createGoal("ams_destroy_agent");
			//destroy.getParameter("agentidentifier").setValue(create.getResult());
			destroy.getParameter("agentidentifier").setValue(create.getParameter("agentidentifier").getValue());
			destroy.getParameter("ams").setValue(ams);
			try
			{
				dispatchSubgoalAndWait(destroy);
				getLogger().info("Success: Ping agent was destroyed.");
				tr.setSucceeded(true);
			}
			catch(GoalFailureException e)
			{
				tr.setReason("Destroy subgoal failed.");
			}
		}
		else
		{
			tr.setReason("Cannot destroy because creation already failed.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);


		//waitFor(200);
		tr = new TestReport("#"+num++, "Searching for ping agent again.");
		if(create.isSucceeded())
		{
			getLogger().info("\nSearching for ping agent again.");
			desc	= new AMSAgentDescription();
			//desc.setName((AgentIdentifier)create.getResult());
			desc.setName((AgentIdentifier)create.getParameter("agentidentifier").getValue());
			constraints	= new SearchConstraints();
			constraints.setMaxResults(-1);
			search	= createGoal("ams_search_agents");
			search.getParameter("description").setValue(desc);
			search.getParameter("constraints").setValue(constraints);
			search.getParameter("ams").setValue(ams);
			try
			{
				dispatchSubgoalAndWait(search);
				AMSAgentDescription[] result	= (AMSAgentDescription[])search.getParameterSet("result").getValues();
				if(result.length==0)
				{
					getLogger().info("Success! Found 0 agents.");
					tr.setSucceeded(true);
				}
				else
				{
					//failure	= true;
					System.err.println("Failure! Found "+result.length+" agents.");
					for(int i=0; i< result.length; i++)
						System.err.println("Agent "+i+": "+result[i].getName());
					tr.setReason("Found an agent that should not be there.");
				}
			}
			catch(GoalFailureException e)
			{
				tr.setReason("Search subgoal failed.");
			}
		}
		else
		{
			tr.setReason("Cannot search because creation already failed.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);
		return num;

		// Test summary.
		/*if(failure)
			System.err.println("\nSome tests failed!");
		else
			getLogger().info("\nAll tests succeded.");*/

		/*IGoal shutdown = createGoal("ams_shutdown_platform");
		shutdown.getParameter("ams").setValue(ams);
		dispatchSubgoalAndWait(shutdown);*/

		/*getLogger().info("Test 1: Creating an agent per message!");
		IGoal ca = createGoal("ams_create_agent");
		ca.getParameter("type").setValue("jadex.testcases.benchmarks.AgentCreation");
		ca.getParameter("name").setValue("Creator");
		ca.getParameterSet("arguments").addValue(new Integer(5));
		ca.getParameter("ams").setValue(SFipa.AMS);
		dispatchSubgoalAndWait(ca);
		CreateAgent ca = new CreateAgent();
		ca.setType("jadex.testcases.benchmarks.AgentCreation");
		ca.setName("Creator");
		ca.addArgument(new Integer(5));
		IMessageEvent rca = createMessageEvent("request_create_agent");
		rca.getParameterSet(SFipa.RECEIVERS).addValue(SFipa.AMS);
		rca.setContent(ca);
		IMessageEvent reply = sendMessageAndWait(rca, 10000);
		getLogger().info("Test 1 succeeded.");

		/*getLogger().info("Test 2: Destroying an agent per message!");
		DestroyAgent da = new DestroyAgent();
		da.setAgentIdentifier(((CreateAgent)((Done)reply.getContent()).getAction()).getAgentIdentifier());
		IMessageEvent rda = createMessageEvent("request_destroy_agent");
		rda.getParameterSet(SFipa.RECEIVERS).addValue(SFipa.AMS);
		rda.setContent(da);
		sendMessageAndWait(rda, 10000);
		getLogger().info("Test 2 succeeded.");

		getLogger().info("Test 3: Searching for agents per message!");
		SearchAgents sa = new SearchAgents();
		AMSAgentDescription	desc	= new AMSAgentDescription();
		SearchConstraints	constraints	= new SearchConstraints();
		constraints.setMaxResults(-1);
		sa.setAgentDescription(desc);
		sa.setSearchConstraints(constraints);
		IMessageEvent sda = createMessageEvent("request_search_agents");
		sda.getParameterSet(SFipa.RECEIVERS).addValue(SFipa.AMS);
		sda.setContent(sa);
		IMessageEvent rep = sendMessageAndWait(sda, 10000);
		getLogger().info("Test 3 succeeded: "+rep);

		getLogger().info("Test 4: Shutdown platform per message!");
		ShutdownPlatform sp = new ShutdownPlatform();
		IMessageEvent spa = createMessageEvent("request_shutdown_platform");
		spa.getParameterSet(SFipa.RECEIVERS).addValue(SFipa.AMS);
		spa.setContent(sp);
		IMessageEvent re = sendMessageAndWait(spa, 10000);
		getLogger().info("Test 4 succeeded: "+re);*/
	}
}
