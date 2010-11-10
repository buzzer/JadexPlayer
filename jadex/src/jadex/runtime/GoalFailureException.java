package jadex.runtime;

/**
 *  An exception automatically thrown, when a subgoal of a plan fails.
 *  Note: This exception will not be logged by the logger
 *  as it occurs in plans (normal plain failure).
 */
public class GoalFailureException	extends BDIFailureException
{
	//-------- attributes --------

	/** The failed goal. */
	protected IGoal	goal;

	//-------- constructors --------

	/**
	 *  Create a new goal failure exception.
	 *  @param goal	The failed goal.
	 */
	public GoalFailureException(IGoal goal, Throwable cause)
	{
		super(null, cause);
		this.goal	= goal;
	}

	/**
	 *  Create a new goal failure exception.
	 *  @param goal	The failed goal.
	 *  @param message The message.
	 */
	public GoalFailureException(IGoal goal, String message)
	{
		super(message, null);
		this.goal	= goal;
	}

	//-------- methods --------

	/**
	 *  Get the goal that failed.
	 */
	public IGoal	getGoal()
	{
		return goal;
	}
	
	/**
	 *  Create a string representation of the exception.
	 */
	public String	toString()
	{
		String s = getClass().getName();
		String message = getLocalizedMessage();
		return (message != null) ? (s + "("+goal+"): " + message) : s + "("+goal+")";
	}

	/**
	 *  Set the goal of the exception.
	 */
	// Todo: remove.
	protected void setGoal(IGoal goal)
	{
		this.goal	= goal;
	}
}
