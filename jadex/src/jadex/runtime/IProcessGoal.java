package jadex.runtime;

/**
 *  The supertype for all goals (concrete and referenced)
 *  and all goal types (perform, achieve, get, maintain).
 */
public interface IProcessGoal extends IParameterElement
{
	/**
	 *  Abort this goal.
	 *  Causes the corresponding plan to be terminated
	 *  and subgoals to be dropped.
	 */
	public void abort();

	/**
	 *  Check if the corresponding plan was aborted because the
	 *  proprietary goal succeeded during the plan was running.
	 *  @return True, if the goal was aborted on success of the proprietary goal.
	 */
	public boolean isAbortedOnSuccess();

	/**
	 *  Get the goal type.
	 *  @return The goal type.
	 */
	public String	getType();

	//-------- parameter handling --------

	/**
	 *  Set the result for the goal.
	 *  This is a convenience method, as the goal result
	 *  is stored as property.
	 *  @param result The result.
	 *  @deprecated
	 */
	public void	setResult(Object result);

	/**
	 *  Get the result parameter.
	 *  @return The result parameter.
	 *  @deprecated
	 */
	public IParameter	getResultParameter();

	/**
	 *  Get the result of the goal.
	 *  This is a convenience method, as the goal result
	 *  is stored as property.
	 *  @return The result value.
	 * /
	public Object	getResult();*/
}
