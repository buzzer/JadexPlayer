package jadex.model;

/**
 *  Model element for a belief.
 */
public interface IMBelief extends IMTypedElement
{
	//-------- fact --------

	/**
	 *  Get the default fact of the belief.
	 *  @return The default fact expression (if any).
	 */
	public IMExpression	getDefaultFact();

	/**
	 *  Create the default fact for the belief.
	 *  @param expression	The expression string.
	 *  @param mode	The evaluation mode.
	 *  @return The new fact expression.
	 */
	public IMExpression	createDefaultFact(String expression, String mode);

	/**
	 *  Delete the default fact of the belief.
	 */
	public void	deleteDefaultFact();
}
