package jadex.testcases;

import jadex.runtime.Plan;

/**
 *  Print results to some output
 */
public class PrintResultPlan extends Plan
{
	//-------- attributes --------
	
	/** The result. */
	protected Object result;
	
	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public PrintResultPlan(Object result)
	{
		this.result = result;
	}
	
	
	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		getLogger().info(""+result);
	}
}
