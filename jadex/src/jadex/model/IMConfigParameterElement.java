package jadex.model;

/**
 *  Base type for parameter elements in a configuration.
 */
public interface IMConfigParameterElement extends IMConfigReferenceableElement
{
	//-------- parameters --------

	/**
	 *  Get all initial parameters.
	 *  @return All initial parameters.
	 */
	public IMConfigParameter[]	getParameters();

	/**
	 *  Create a new initial parameter.
	 *  @param ref	The name of the referenced parameter.
	 *  @param expression	The value expression.
	 *  @param mode	The evaluation mode.
	 *  @return	The newly created initial parameter.
	 */
	public IMConfigParameter	createInitialParameter(String ref, String expression, String mode);
	
	/**
	 *  Delete a parameter.
	 *  @param parameter	The parameter to delete.
	 */
	public void	deleteInitialParameter(IMConfigParameter parameter);


	//-------- parameter sets --------

	/**
	 *  Get all initial parameter sets.
	 *  @return All initial parameter sets.
	 */
	public IMConfigParameterSet[]	getParameterSets();

	/**
	 *  Create a new parameter set.
	 *  @param ref	The name of the referenced parameter.
	 *  @param expression	The initial values expression (if any).
	 *  @return	The newly created parameter set.
	 */
	public IMConfigParameterSet	createInitialParameterSet(String ref, String expression);
	
	/**
	 *  Delete a parameter set.
	 *  @param parameterset	The parameter set to delete.
	 */
	public void	deleteInitialParameterSet(IMConfigParameterSet parameterset);

	
	//-------- not xml related --------
	
	/**
	 *  Get a parameter for an original element.
	 *  @param elem The parameter..
	 *  @return The parameter expression.
	 */
	public IMConfigParameter	getParameter(IMReferenceableElement elem);

	/**
	 *  Get a parameter set for an original element.
	 *  @param elem The parameter.
	 *  @return The parameter expression.
	 */
	public IMConfigParameterSet	getParameterSet(IMReferenceableElement elem);
}
