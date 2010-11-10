package jadex.model;

/**
 *  Initial values of a parameter set of an element in a configuration.
 */
public interface IMConfigParameterSet extends IMConfigReferenceableElement
{
	//-------- initial values --------

	/**
	 *  Get the initial values.
	 *  @return	The initial values.
	 */
	public IMExpression[]	getInitialValues();

	/**
	 *  Create a initial value.
	 *  @param expression	The value expression string.
	 *  @return the newly created value expression.
	 */
	public IMExpression	createInitialValue(String expression);

	/**
	 *  Delete a initial value.
	 *  @param value	The value expression.
	 */
	public void	deleteInitialValue(IMExpression value);


	//-------- initial values expression --------

	/**
	 *  Get the initial values, when represented as a single expression (returning a collection).
	 *  @return	The initial values.
	 */
	public IMExpression	getInitialValuesExpression();

	/**
	 *  Create the initial values expression.
	 *  @param expression	The values expression string.
	 *  @param mode	The evaluation mode.
	 *  @return the newly created values expression.
	 */
	public IMExpression	createInitialValuesExpression(String expression, String mode);

	/**
	 *  Delete the initial values expression.
	 */
	public void	deleteInitialValuesExpression();
}
