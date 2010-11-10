package jadex.testcases.misc;

import jadex.runtime.IGoal;
import jadex.runtime.IMessageEvent;
import jadex.runtime.Plan;
import jadex.util.collection.SCollection;

import java.util.List;
import java.util.Map;

/**
 *  Check naming of initial and end elements using config element ref worker agent.
 */
public class ConfigElementRefPlan extends Plan
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
		create.getParameter("type").setValue("jadex.testcases.misc.ConfigElementRefWorker");
		Map args = SCollection.createHashMap();
		args.put("testagent", getAgentIdentifier());
		create.getParameter("arguments").setValue(args);
		dispatchSubgoalAndWait(create);
		
		// Wait for reports from worker agent.
		IMessageEvent	msg	= waitForMessageEvent("inform_reports");
		getWaitqueue().removeMessageEvent("inform_reports");
		List	reports	= (List)msg.getContent();
		for(int i=0; i<reports.size(); i++)
		{
			getBeliefbase().getBeliefSet("reports").addFact(reports.get(i));
		}
	}
}
