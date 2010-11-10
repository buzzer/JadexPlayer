package jadex.testcases.semiautomatic;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.runtime.GoalFailureException;
import jadex.runtime.IGoal;
import jadex.runtime.Plan;

/**
 *  Test the shutdown of a platform
 */
public class ShutdownTesterPlan extends Plan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
		AgentIdentifier ams = (AgentIdentifier)getBeliefbase().getBelief("ams").getFact();
		IGoal sd = createGoal("ams_shutdown_platform");
		sd.getParameter("ams").setValue(ams);
		try
		{
			dispatchSubgoalAndWait(sd);
			System.out.println("Remote platform successfully shutdowned.");
		}
		catch(GoalFailureException e)
		{
			e.printStackTrace();
		}
	}

}
