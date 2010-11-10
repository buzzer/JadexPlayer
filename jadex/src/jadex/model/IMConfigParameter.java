package jadex.model;

/**
 *  An initial parameter value. 
 */
public interface IMConfigParameter extends IMConfigReferenceableElement
{
	//-------- value --------

	/**
	 *  Get the initial value of the parameter.
	 *  @return The initial value expression (if any).
	 */
	public IMExpression	getInitialValue();

	/**
	 *  Create the initial value for the parameter.
	 *  @param expression	The expression string.
	 *  @param mode	The evaluation mode.
	 *  @return The new value expression.
	 */
	public IMExpression	createInitialValue(String expression, String mode);

	/**
	 *  Delete the initial value of the parameter.
	 */
	public void	deleteInitialValue();
}
