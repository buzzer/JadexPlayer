package jadex.testcases;

import jadex.runtime.Plan;

/**
 *  This plan increments a belief in time intervals.
 */
public class BeliefIncrementPlan extends Plan
{
	//-------- attributes --------

	/** The beliefname (type has to be number). */
	protected String beliefname;

	/** The values to add. */
	protected int[] values;

	/** The change rate. */
	protected long rate;

	//-------- constructors --------

	/**
	 *  Create a new plan instance.
	 *  @param beliefname The belief name.
	 */
	public BeliefIncrementPlan(String beliefname)
	{
		this(beliefname, new int[]{1}, 0);
	}

	/**
	 *  Create a new plan instance.
	 *  @param beliefname The belief name.
	 */
	public BeliefIncrementPlan(String beliefname, int value)
	{
		this(beliefname, new int[]{value}, 0);
	}

	/**
	 *  Create a new plan instance.
	 *  @param beliefname The belief name.
	 *  @param value The change value.
	 *  @param rate The change rate in millis.
	 */
	public BeliefIncrementPlan(String beliefname, int value, long rate)
	{
		this(beliefname, new int[]{value}, rate);
	}

	/**
	 *  Create a new plan instance.
	 *  @param beliefname The belief name.
	 *  @param values The change values.
	 *  @param rate The change rate in millis.
	 */
	public BeliefIncrementPlan(String beliefname, int[] values, long rate)
	{
		//System.out.println("Created: " + this);
		getLogger().info("Created: " + this);
		this.beliefname = beliefname;
		this.values = values;
		this.rate = rate;
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		int cnt = 0;
		do
		{
			Number stepcnt = (Number)getBeliefbase().getBelief(beliefname).getFact();
			stepcnt = new Integer(stepcnt.intValue()+values[cnt++%values.length]);

			// Do atomic, to avoid being terminated before latest value is printed.
			startAtomic();
			getBeliefbase().getBelief(beliefname).setFact(stepcnt);
			//System.out.println(this.getName()+": belief "+beliefname+" changed to: " + stepcnt.intValue());
			getLogger().info(this.getName()+": belief "+beliefname+" changed to: " + stepcnt.intValue());
			endAtomic();

			if(rate>0)
				waitFor(rate);
		}
		while(rate>0);
	}
}
