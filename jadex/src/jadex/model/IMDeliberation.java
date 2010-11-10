package jadex.model;

/**
 *  Goal deliberation options.
 */
public interface IMDeliberation	extends IMElement
{
	//-------- inhibits --------

	/**
	 *  Get all inhibition links.
	 *  @return The inhibition links originating from the enclosing goal.
	 */
	public IMInhibits[]	getInhibits();

	/**
	 *  Create a new inhibition link.
	 *  @param ref	The goal type to be inhibited.
	 *  @param expression	An optional boolean expression specifying the context in which the inhibition link is active.
	 *  @param inhibit	An optional identifier for the goal's lifecycle state in which the inhibition link is active.
	 */
	public IMInhibits	createInhibits(String ref, String expression, String inhibit);

	/**
	 *  Delete an inhibition link.
	 *  @param inhibits The inhibition link.
	 */
	public void	deleteInhibits(IMInhibits inhibits);

	
	//-------- cardinality --------
	
	/**
	 *  Get the cardinality (i.e. max. number of active goals)
	 *  or -1 if not set.
	 */
	public int getCardinality();

	/**
	 *  Set the cardinality (i.e. max. number of active goals)
	 *  or -1 if unlimited.
	 */
	public void setCardinality(int cardinality);

	
	//-------- not xml related --------
	
	/**
	 *  Get the inhibited goals.
	 * /
	public IMGoal[]	getInhibitedGoals();*/
}
