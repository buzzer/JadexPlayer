package jadex.testcases;

import jadex.runtime.*;

/**
 *  This plan waits for a specified time,
 *  sets the assigned result as goal state and finishes.
 */
public class ResultPlan extends Plan
{
	//-------- attributes --------

	/** The waiting time. */
	protected long	wait;

	/** The result state. */
	protected boolean	success;

	/** The result value (if any). */
	protected Object	result;

	/** The belief name for storing the result. */
	protected String belief;
	
	//-------- constructors --------

	/**
	 *  Create a new result plan.
	 *  @param result The result value.
	 */
	public ResultPlan(Object result)
	{
		this(-1, true, result);
	}
	
	/**
	 *  Create a new result plan.
	 *  @param success	The goal state after execution.
	 */
	public ResultPlan(boolean success)
	{
		this(-1, success, null);
	}

	/**
	 *  Create a new result plan.
	 *  @param wait	The waiting time.
	 *  @param success	The goal state after execution.
	 */
	public ResultPlan(long wait, boolean success)
	{
		this(wait, success, null);
	}

	/**
	 *  Create a new result plan.
	 *  @param success	The goal state after execution.
	 *  @param result The result.
	 */
	public ResultPlan(boolean success, Object result)
	{
		this(-1, success, result);
	}

	/**
	 *  Create a new result plan.
	 *  @param wait	The waiting time.
	 *  @param success	The goal state after execution.
	 *  @param result The result.
	 */
	public ResultPlan(long wait, boolean success, Object result)
	{
		this(wait, success, result, null);
	}

	/**
	 *  Create a new result plan.
	 *  @param result The result.
	 *  @param belief	The belief to set to the result.
	 */
	public ResultPlan(Object result, String belief)
	{
		this(-1, true, result, belief);
	}
	
	/**
	 *  Create a new result plan.
	 *  @param success	The goal state after execution.
	 *  @param result The result.
	 *  @param belief	The belief to set to the result.
	 */
	public ResultPlan(boolean success, Object result, String belief)
	{
		this(-1, success, result, belief);
	}

	/**
	 *  Create a new result plan.
	 *  @param wait	The waiting time.
	 *  @param success	The goal state after execution.
	 *  @param result The result.
	 *  @param belief	The belief to set to the result.
	 */
	public ResultPlan(long wait, boolean success, Object result, String belief)
	{
		this.wait	= wait;
		this.success = success;
		this.result = result;
		this.belief = belief;
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		//System.out.println("executing: "+wait+" "+success+" "+result+" "+belief);
		if(result!=null && hasParameter("result"))
			getParameter("result").setValue(result);
		if(wait>-1)
			waitFor(wait);
		if(belief!=null)
		{
			if(getBeliefbase().containsBelief(belief))
			{
				getBeliefbase().getBelief(belief).setFact(result);
			}
			else if(getBeliefbase().containsBeliefSet(belief))
			{
				getBeliefbase().getBeliefSet(belief).addFact(result);
			}
			else
			{
				getLogger().info("Could not find belief(set): "+belief);
				fail();
			}
		}
		getLogger().info("Plan: "+this.getName()+" finished with state "+success
			+ (result==null?"":", result is "+result));
		if(!success)
			fail();
	}
}
