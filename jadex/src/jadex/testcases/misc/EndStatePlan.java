package jadex.testcases.misc;

import java.util.List;
import java.util.Map;

import jadex.adapter.fipa.AMSAgentDescription;
import jadex.adapter.fipa.AgentDescription;
import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.fipa.ServiceDescription;
import jadex.planlib.TestReport;
import jadex.runtime.IGoal;
import jadex.runtime.IMessageEvent;
import jadex.runtime.Plan;
import jadex.util.collection.SCollection;

/**
 *  Check correct operation of end states.
 */
public class EndStatePlan extends Plan
{
	/**
	 *  Plan body.
	 */
	public void body()
	{
		// Store report message from worker agent.
		getWaitqueue().addMessageEvent("inform_reports");
		
		// Create worker agent.
		IGoal	create	= createGoal("ams_create_agent");
		create.getParameter("type").setValue("jadex.testcases.misc.EndStateWorker");
		Map args = SCollection.createHashMap();
		args.put("testagent", getAgentIdentifier());
		create.getParameter("arguments").setValue(args);
		dispatchSubgoalAndWait(create);
		AgentIdentifier	worker	= (AgentIdentifier)create.getParameter("agentidentifier").getValue();
		
		// Wait for reports from worker agent.
		IMessageEvent	msg	= waitForMessageEvent("inform_reports");
		getWaitqueue().removeMessageEvent("inform_reports");
		List	reports	= (List)msg.getContent();
		
		// Check if worker agent has been correctly removed.
		waitFor(100);	// Hack!!! how to ensure that agent has time to remove itself?
		IGoal	search	= createGoal("ams_search_agents");
		search.getParameter("description").setValue(new AMSAgentDescription(worker));
		dispatchSubgoalAndWait(search);
		TestReport	report	= new TestReport("termination", "Test if the worker agent has been terminated");
		if(search.getParameterSet("result").getValues().length==0)
		{
			report.setSucceeded(true);
		}
		else
		{
			report.setFailed("Worker agent still alive.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
		
		// Add test results from worker.
		for(int i=0; i<reports.size(); i++)
		{
			getBeliefbase().getBeliefSet("reports").addFact(reports.get(i));
		}

		// Create deregister agent.
		report	= new TestReport("deregister", "Test if an agent can deregister on termination.");
		create	= createGoal("ams_create_agent");
		create.getParameter("type").setValue("jadex.testcases.misc.EndStateDeregister");
		dispatchSubgoalAndWait(create);
		AgentIdentifier	deregister	= (AgentIdentifier)create.getParameter("agentidentifier").getValue();

		// Check if deregister agent is registered.
		waitFor(100);	// Hack!!! how to ensure that agent has time to register itself?
		AgentDescription	desc	= new AgentDescription();
		desc.addService(new ServiceDescription(null, "endstate_testservice", null));
		IGoal	dfsearch	= createGoal("df_search");
		dfsearch.getParameter("description").setValue(desc);
		dispatchSubgoalAndWait(dfsearch);
		if(dfsearch.getParameterSet("result").getValues().length==0)
		{
			report.setFailed("Agent is not registered at DF.");
		}
		else
		{
			// Kill deregister agent.
			IGoal	destroy	= createGoal("ams_destroy_agent");
			destroy.getParameter("agentidentifier").setValue(deregister);
			dispatchSubgoalAndWait(destroy);
			
			// Check if deregister agent is deregistered.
			waitFor(100);	// Hack!!! how to ensure that agent has time to deregister itself?
			dfsearch	= createGoal("df_search");
			dfsearch.getParameter("description").setValue(desc);
			dispatchSubgoalAndWait(dfsearch);
			if(dfsearch.getParameterSet("result").getValues().length!=0)
			{
				report.setFailed("Agent is still registered at DF.");
			}
			else
			{
				// Check if deregister agent has been correctly removed.
				search	= createGoal("ams_search_agents");
				search.getParameter("description").setValue(new AMSAgentDescription(deregister));
				dispatchSubgoalAndWait(search);
				if(search.getParameterSet("result").getValues().length!=0)
				{
					report.setFailed("Deregister agent still alive.");
				}
				else
				{
					report.setSucceeded(true);
				}
			}
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
	}
}
