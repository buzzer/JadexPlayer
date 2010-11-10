package jadex.testcases.semiautomatic;

import jadex.runtime.*;

/**
 *  Test if the agent can be properly deleted.
 */
public class DeletionMobilePlan extends MobilePlan
{
	//-------- constructors --------

	/**
	 *  Create a new countdown plan.
	 */
	public DeletionMobilePlan()
	{
		getLogger().info("Created: "+this);
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void action(IEvent e)
	{
		if(!e.getType().equals(IInternalEvent.TYPE_TIMEOUT))
		{
			getLogger().info("Oki executing body.");
			getLogger().info("Now trying to delete myself.");
			getLogger().info("When no more output test has failed.");

			killAgent();

			getLogger().info("Alive while plan is running (When no more outputs, test succeeded).");

			waitFor(10);
		}
		else
		{
			getLogger().info("Still alive (TEST FAILED).");
		}
	}
}

