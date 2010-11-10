package jadex.testcases.semiautomatic;

import jadex.runtime.Plan;

/**
 *  Test cmd line args.
 */
public class ArgumentsPlan extends Plan
{
	final Object[] args;

	/**
	 * Constructor for ArgumentsPlan.
	 * @param args
	 */
	public ArgumentsPlan(Object args[])
	{
		this.args = args;
	}

	/**
	 * @see jadex.runtime.Plan#body()
	 */
	public void body()
	{
		for(int i=0; args!=null && i<args.length; i++)
			getLogger().info("# args: " + i + args[i]);
		if(args==null)
			getLogger().info("args are null");
	}
}