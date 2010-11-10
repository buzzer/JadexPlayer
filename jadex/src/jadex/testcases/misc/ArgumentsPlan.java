package jadex.testcases.misc;

import java.util.Map;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.model.IMBelief;
import jadex.model.IMElement;
import jadex.model.IMReferenceableElement;
import jadex.model.IMTypedElement;
import jadex.planlib.TestReport;
import jadex.runtime.*;
import jadex.util.collection.SCollection;

/**
 *  Test if arguments can be accessed.
 */
public class ArgumentsPlan extends Plan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
		TestReport tr = new TestReport("#1", "Test if a worker agent can be started and supplied with arguments.");
		try
		{
			IGoal ca = createGoal("ams_create_agent");
			ca.getParameter("type").setValue("jadex.testcases.misc.ArgumentsWorker");
			Map args = SCollection.createHashMap();
			args.put("creator", getAgentIdentifier());
			ca.getParameter("arguments").setValue(args);
			dispatchSubgoalAndWait(ca);
			AgentIdentifier worker = (AgentIdentifier)ca.getParameter("agentidentifier").getValue();
			waitForMessageEvent("inform_created", 1000);
			tr.setSucceeded(true);
		}
		catch(GoalFailureException e)
		{
			tr.setReason("Could not create worker agent.");
			getLogger().severe("Exception while creating the worker agent: "+ e);
		}
		catch(TimeoutException e)
		{
			tr.setReason("Worker did not send message.");
			getLogger().severe("Timeout while waiting for message receival: "+ e);
			
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);
		
		/*String[] belnames = getBeliefbase().getBeliefNames();
		for(int i=0; i<belnames.length; i++)
		{
			IBelief bel = getBeliefbase().getBelief(belnames[i]);
			IMElement mbel = bel.getModelElement();
			if(mbel instanceof IMBelief && ((IMBelief)mbel).getExported().equals(IMTypedElement.EXPORTED_TRUE))
				System.out.println(bel.getName()+": "+bel.getFact());
		}*/
		//Map args = getScope().getArguments();
		//System.out.println("Arguments: "+args);
	}
}
