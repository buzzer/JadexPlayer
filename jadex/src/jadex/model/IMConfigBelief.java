package jadex.model;

/**
 *  Configuration settings for a belief.
 */
public interface IMConfigBelief extends IMConfigReferenceableElement
{
	//-------- fact --------

	/**
	 *  Get the initial fact of the belief.
	 *  @return The initial fact expression (if any).
	 */
	public IMExpression	getInitialFact();

	/**
	 *  Create the initial fact for the belief.
	 *  @param expression	The expression string.
	 *  @param mode	The evaluation mode.
	 *  @return The new fact expression.
	 */
	public IMExpression	createInitialFact(String expression, String mode);

	/**
	 *  Delete the initial fact of the belief.
	 */
	public void	deleteInitialFact();
}
