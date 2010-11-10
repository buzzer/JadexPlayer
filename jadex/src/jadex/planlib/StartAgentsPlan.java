package jadex.planlib;

import jadex.runtime.IGoal;
import jadex.runtime.Plan;

/**
 *  Plan for starting some Jadex agents.
 */
public class StartAgentsPlan extends Plan
{
	/**
	 *  Execute a plan.
	 */
	public void body()
	{
		// todo: support starting on other platforms
		StartAgentInfo[] startinfos = (StartAgentInfo[])getParameterSet("agentinfos").getValues();
		for(int i=0; i<startinfos.length; i++)
		{
			try
			{
				IGoal	create	= createGoal("ams_create_agent");
				create.getParameter("type").setValue(startinfos[i].getType());
				create.getParameter("name").setValue(startinfos[i].getName());
				create.getParameter("configuration").setValue(startinfos[i].getConfiguration());
				if(startinfos[i].getArguments()!=null)
					create.getParameter("arguments").setValue(startinfos[i].getArguments());

				dispatchSubgoalAndWait(create);
				getParameterSet("agentidentifiers").addValue(
					create.getParameter("agentidentifier").getValue());

				waitFor(startinfos[i].getDelay());
			}
			catch(Exception e)
			{
				System.out.println("Problem occurred while trying to start agent: "
					+startinfos[i].getNamePrototype());
				e.printStackTrace();
			}
		}
	}
}
