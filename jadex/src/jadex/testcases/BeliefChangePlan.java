package jadex.testcases;

import jadex.runtime.Plan;

/**
 *  Change a belief to a value.
 */
public class BeliefChangePlan extends Plan
{
	//-------- attributes --------

	/** The belief name. */
	protected String belname;

	/** The new value. */
	protected Object value;

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public BeliefChangePlan(String belname, Object value)
	{
		this.belname = belname;
		this.value = value;
	}

	//-------- methods --------

	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		getLogger().info("Setting belief: "+belname+" to :"+value);
		getBeliefbase().getBelief(belname).setFact(value);
	}
}
