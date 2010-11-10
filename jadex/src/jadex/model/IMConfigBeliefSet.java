package jadex.model;

/**
 *  Configuration for a belief set.
 */
public interface IMConfigBeliefSet extends IMConfigReferenceableElement
{
	//-------- initial facts --------

	/**
	 *  Get the initial facts.
	 *  @return	The initial facts.
	 */
	public IMExpression[]	getInitialFacts();

	/**
	 *  Create a initial fact.
	 *  @param expression	The fact expression string.
	 *  @return the newly created fact expression.
	 */
	public IMExpression	createInitialFact(String expression);

	/**
	 *  Delete a initial fact.
	 *  @param fact	The fact expression.
	 */
	public void	deleteInitialFact(IMExpression fact);


	//-------- initial facts expression --------

	/**
	 *  Get the initial facts, when represented as a single expression (returning a collection).
	 *  @return	The initial facts.
	 */
	public IMExpression	getInitialFactsExpression();

	/**
	 *  Create the initial facts expression.
	 *  @param expression	The facts expression string.
	 *  @param mode	The evaluation mode.
	 *  @return the newly created facts expression.
	 */
	public IMExpression	createInitialFactsExpression(String expression, String mode);

	/**
	 *  Delete the initial facts expression.
	 */
	public void	deleteInitialFactsExpression();
}
