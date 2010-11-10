package jadex.model;

/**
 *  A belief set holds a collection of facts.
 */
public interface IMBeliefSet extends IMTypedElementSet
{

	//-------- default facts --------

	/**
	 *  Get the default facts.
	 *  @return	The default facts.
	 */
	public IMExpression[]	getDefaultFacts();

	/**
	 *  Create a default fact.
	 *  @param expression	The fact expression string.
	 *  @return the newly created fact expression.
	 */
	public IMExpression	createDefaultFact(String expression);

	/**
	 *  Delete a default fact.
	 *  @param fact	The fact expression.
	 */
	public void	deleteDefaultFact(IMExpression fact);


	//-------- default facts expression --------

	/**
	 *  Get the default facts, when represented as a single expression (returning a collection).
	 *  @return	The default facts.
	 */
	public IMExpression	getDefaultFactsExpression();

	/**
	 *  Create the default facts expression.
	 *  @param expression	The facts expression string.
	 *  @param mode	The evaluation mode.
	 *  @return the newly created facts expression.
	 */
	public IMExpression	createDefaultFactsExpression(String expression, String mode);

	/**
	 *  Delete the default facts expression.
	 */
	public void	deleteDefaultFactsExpression();
}
