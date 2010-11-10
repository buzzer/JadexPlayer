package jadex.examples.garbagecollector;

import jadex.runtime.Plan;

/**
 *  Create pieces of garbage in the environment.
 */
public class CreatePlan extends Plan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
		Environment env = (Environment)getBeliefbase().getBelief("env").getFact();

		int garb_cnt = 0;
		while(true)
		{
			// Add a piece of waste randomly.
			waitFor(1000);
			if(env.getWorldObjects().length<(env.getGridSize()*env.getGridSize())/2)
			{
				env.addWorldObject(Environment.GARBAGE, "garbage#"+garb_cnt++, null);
			}
		}
	}
}
