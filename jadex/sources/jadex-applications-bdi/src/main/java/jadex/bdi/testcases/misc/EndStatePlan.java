package jadex.bdi.testcases.misc;

import jadex.base.fipa.IDF;
import jadex.base.fipa.IDFComponentDescription;
import jadex.base.fipa.IDFServiceDescription;
import jadex.base.fipa.SFipa;
import jadex.base.test.TestReport;
import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.IMessageEvent;
import jadex.bdi.runtime.Plan;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentManagementService;
import jadex.commons.collection.SCollection;
import jadex.commons.service.SServiceProvider;

import java.util.List;
import java.util.Map;

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
		IGoal	create	= createGoal("cmscap.cms_create_component");
		create.getParameter("type").setValue("/jadex/bdi/testcases/misc/EndStateWorker.agent.xml");
		Map args = SCollection.createHashMap();
		args.put("testagent", getComponentIdentifier());
		create.getParameter("arguments").setValue(args);
		dispatchSubgoalAndWait(create);
		IComponentIdentifier	worker	= (IComponentIdentifier)create.getParameter("componentidentifier").getValue();
		
		// Wait for reports from worker agent.
		IMessageEvent	msg	= waitForMessageEvent("inform_reports");
		getWaitqueue().removeMessageEvent("inform_reports");
		List	reports	= (List)msg.getParameter(SFipa.CONTENT).getValue();
		
		// Check if worker agent has been correctly removed.
		waitFor(1000);	// Hack!!! how to ensure that agent has time to remove itself?
		IGoal	search	= createGoal("cmscap.cms_search_components");
		IComponentManagementService cms = (IComponentManagementService)SServiceProvider.getService(
			getScope().getServiceProvider(), IComponentManagementService.class).get(this);
		search.getParameter("description").setValue(cms.createComponentDescription(worker, null, null, null, null, null));
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
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);
		
		// Add test results from worker.
		for(int i=0; i<reports.size(); i++)
		{
			getBeliefbase().getBeliefSet("testcap.reports").addFact(reports.get(i));
		}

		// Create deregister agent.
		report	= new TestReport("deregister", "Test if an agent can deregister on termination.");
		create	= createGoal("cmscap.cms_create_component");
		create.getParameter("type").setValue("/jadex/bdi/testcases/misc/EndStateDeregister.agent.xml");
		dispatchSubgoalAndWait(create);
		IComponentIdentifier deregister	= (IComponentIdentifier)create.getParameter("componentidentifier").getValue();

		// Check if deregister agent is registered.
		waitFor(100);	// Hack!!! how to ensure that agent has time to register itself?
		IDF df = (IDF)SServiceProvider.getService(getScope().getServiceProvider(), IDF.class).get(this);
		IDFServiceDescription sd = df.createDFServiceDescription(null, "endstate_testservice", null);
		IDFComponentDescription ad = df.createDFComponentDescription(null, sd);
		
		IGoal	dfsearch	= createGoal("dfcap.df_search");
		dfsearch.getParameter("description").setValue(ad);
		dispatchSubgoalAndWait(dfsearch);
		if(dfsearch.getParameterSet("result").getValues().length==0)
		{
			report.setFailed("Agent is not registered at DF.");
		}
		else
		{
			// Kill deregister agent.
			IGoal	destroy	= createGoal("cmscap.cms_destroy_component");
			destroy.getParameter("componentidentifier").setValue(deregister);
			dispatchSubgoalAndWait(destroy);
			
			// Check if deregister agent is deregistered.
			waitFor(100);	// Hack!!! how to ensure that agent has time to deregister itself?
			dfsearch	= createGoal("dfcap.df_search");
			dfsearch.getParameter("description").setValue(ad);
			dispatchSubgoalAndWait(dfsearch);
			if(dfsearch.getParameterSet("result").getValues().length!=0)
			{
				report.setFailed("Agent is still registered at DF.");
			}
			else
			{
				// Check if deregister agent has been correctly removed.
				search = createGoal("cmscap.cms_search_components");
				search.getParameter("description").setValue(cms.createComponentDescription(deregister, null, null, null, null, null));
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
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);
	}
}
