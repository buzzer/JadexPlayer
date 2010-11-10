package jadex.examples.booktrading.common;

import jadex.adapter.fipa.AgentDescription;
import jadex.adapter.fipa.SearchConstraints;
import jadex.runtime.IGoal;
import jadex.runtime.Plan;

/**
 */
public class FindServiceProvidersPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		AgentDescription dfadesc = (AgentDescription)getParameter("description").getValue();
		SearchConstraints constraints = new SearchConstraints();
		constraints.setMaxResults(-1);

		// Use a subgoal to search at the df.
		IGoal ft = createGoal("df_search");
		ft.getParameter("description").setValue(dfadesc);
		ft.getParameter("constraints").setValue(constraints);
		dispatchSubgoalAndWait(ft);

		AgentDescription[]	result = (AgentDescription[])ft.getParameterSet("result").getValues();
		if(result.length > 0)
		{
			for(int i = 0; i < result.length; i++)
			{
				getParameterSet("result").addValue(result[i].getName());
			}
		}
		else
		{
			fail();
		}
	}
}
