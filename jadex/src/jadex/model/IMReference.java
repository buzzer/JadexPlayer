package jadex.model;

/**
 *  A simple reference for triggers.
 */
public interface IMReference	extends IMElement
{

	//-------- reference --------

	/**
	 *  Get the reference to the inhibited goal type.
	 *  @return	The inhibited goal type.
	 */
	public String	getReference();
	
	/**
	 *  Set the reference to the inhibited goal type.
	 *  @param ref	The inhibited goal type.
	 */
	public void	setReference(String ref);

	//-------- match expression --------

	/**
	 *  Get the parameters.
	 *  @return The parameters.
	 */
	public IMExpression	getMatchExpression();

	/**
	 *  Create a new parameter.
	 *  @param match	The match expression.
	 *  @return	The newly created match expression.
	 */
	public IMExpression	createMatchExpression(String match);

	/**
	 *  Delete a parameter.
	 */
	public void	deleteMatchExpression();

	
	//-------- parameters --------
	
	/**
	 *  Get the parameters.
	 *  @return The parameters.
	 * /
	public IMReferenceParameter[]	getParameters();*/

	/**
	 *  Get the parameter sets.
	 *  @return The parameter sets.
	 * /
	public IMReferenceParameterSet[]	getParameterSets();*/


	/**
	 *  Create a new parameter.
	 *  @param ref	The name of the referenced parameter.
	 *  @param expression	The value expression.
	 *  @return	The newly created parameter.
	 * /
	public IMReferenceParameter	createInitialParameter(String ref, String expression);*/
	
	/**
	 *  Delete a parameter.
	 *  @param parameter	The parameter to delete.
	 * /
	public void	deleteInitialParameter(IMReferenceParameter parameter);*/
}
