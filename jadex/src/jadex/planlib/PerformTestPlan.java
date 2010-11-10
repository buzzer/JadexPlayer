package jadex.planlib;

import java.util.HashMap;
import java.util.Map;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.fipa.SFipa;
import jadex.runtime.*;

/**
 *  Perform one testcase.
 */
public class PerformTestPlan extends Plan
{
	//-------- attributes --------
	
	/** The created test agent. */
	protected AgentIdentifier	testagent;
	
	//-------- methods --------
	
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		Testcase testcase = (Testcase)getParameter("testcase").getValue();
		Long timeout = (Long)getBeliefbase().getBelief("timeout").getFact();

		getLogger().info("Performing testcase: "+testcase.getType());

		try
		{
			// Create test agent without starting.
			IGoal create = createGoal("ams_create_agent");
			create.getParameter("type").setValue(testcase.getType());
			Map	args	= new HashMap();
			args.put("timeout", timeout);
			args.put("testcenter", this.getAgentIdentifier());
			create.getParameter("arguments").setValue(args);
			create.getParameter("start").setValue(Boolean.FALSE);
			dispatchSubgoalAndWait(create);
			this.testagent	= (AgentIdentifier)create.getParameter("agentidentifier").getValue();

			// Add filter to waitqueue and start test agent.
			MessageEventFilter fil = new MessageEventFilter("inform_reports");
			fil.addValue(SFipa.SENDER, testagent);
			getWaitqueue().addFilter(fil);
			IGoal start = createGoal("ams_start_agent");
			start.getParameter("agentidentifier").setValue(testagent);
			dispatchSubgoalAndWait(start);
			
			try
			{
				IMessageEvent	ans	= (IMessageEvent)waitFor(fil, timeout.longValue());
				Testcase	result	= (Testcase)ans.getContent();
				//getLogger().info("Test results are: "+result);
				testcase.setTestCount(result.getTestCount());
				testcase.setReports(result.getReports());
			}
			catch(TimeoutException te)
			{
				IGoal destroy = createGoal("ams_destroy_agent");
				destroy.getParameter("agentidentifier").setValue(testagent);
				try
				{
					dispatchSubgoalAndWait(destroy);
				}
				catch(GoalFailureException ge)
				{
					getLogger().info("Test agent could not be deleted.");
				}
				testagent	= null;
				//getLogger().info("Test agent failed. No answer received.");
				testcase.setReports(new TestReport[]{new TestReport("answer", "Test center report", false, "Test agent did not answer.")});
			}
		}
		catch(GoalFailureException ge)
		{
			//getLogger().info("Test agent could not be created.");
			Throwable	cause	= ge;
			while(cause.getCause()!=null && cause.getCause()!=cause)
				cause	= cause.getCause();
			testcase.setReports(new TestReport[]{new TestReport("creation", "Test center report", false, "Test agent could not be created: "+cause)});
		}
	}
	
	/**
	 *  When plan is aborted, kill created agent.
	 */
	public void aborted()
	{
		if(testagent!=null)
		{
			IGoal destroy = createGoal("ams_destroy_agent");
			destroy.getParameter("agentidentifier").setValue(testagent);
			try
			{
				dispatchSubgoalAndWait(destroy);
			}
			catch(GoalFailureException ge)
			{
				getLogger().info("Test agent could not be deleted.");
			}
		}
	}
}
