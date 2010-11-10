package jadex.examples.hunterprey.environment;

import jadex.examples.hunterprey.*;
import jadex.runtime.*;
import jadex.adapter.fipa.Done;

/**
 *  Handle move requests by the environment.
 */
public class  MovePlan extends Plan
{
	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public  MovePlan()
	{
		getLogger().info("Created: "+this);
	}

	//------ methods -------

	/**
	 *  The plan body.
	 */
	public void body()
	{
//		System.out.println("a) move: "+getName());
		RequestMove rm = (RequestMove)getParameter("action").getValue();

		Environment env = (Environment)getBeliefbase().getBelief("environment").getFact();
		TaskInfo ti = env.addMoveTask(rm.getCreature(), rm.getDirection());

		// Wait until all tasks are processed by the environment.
		waitForCondition("$beliefbase.environment.getTaskSize()==0");

//		System.out.println("b) move: "+getName());

		// Result is null, when creature died and action was not executed.
		if(ti.getResult()!=null && ((Boolean)ti.getResult()).booleanValue())
		{
			Done done = new Done();
			done.setAction(rm);
			getParameter("result").setValue(done);
		}
		else
		{
			fail();
		}
	}
}
