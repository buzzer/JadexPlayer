package jadex.model;


/**
 *  A simple parameter reference to provide a value.
 */
public interface IMReferenceParameter extends IMElement
{

	//-------- reference --------

	/**
	 *  Get the reference.
	 *  @return	The reference.
	 */
	public String	getReference();
	
	/**
	 *  Set the reference.
	 *  @param ref	Set reference.
	 */
	public void	setReference(String ref);


	//-------- value --------

	/**
	 *  Get the value of the parameter.
	 *  @return The value.
	 */
	public IMExpression	getValue();

	/**
	 *  Create a value for the parameter.
	 *  @param expression	The expression string.
	 *  @return The new value.
	 */
	public IMExpression	createValue(String expression);

	/**
	 *  Delete the value of the parameter.
	 */
	public void	deleteValue();


	//-------- match expression --------

	/**
	 *  Get the matchexpression.
	 *  @return The match expression.
	 * /
	public IMExpression	getMatchExpression();

	/**
	 *  Create a value for the parameter.
	 *  @param expression	The expression string.
	 *  @return The new value.
	 * /
	public IMExpression	createMatchExpression(String expression);

	/**
	 *  Delete the match expression of the parameter.
	 * /
	public void	deleteMatchExpression();*/

}
