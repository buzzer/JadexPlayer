package jadex.model;

/**
 *  The interface for all elements with parameters.
 */
public interface IMParameterElement extends IMReferenceableElement
{

	//-------- parameters --------

	/**
	 *  Get all parameters.
	 *  @return All parameters.
	 */
	public IMParameter[]	getParameters();

	/**
	 *  Get a parameter by name.
	 *  @param name The parameter name.
	 *  @return The parameter.
	 */
	public IMParameter	getParameter(String name);

	/**
	 *  Create a new parameter.
	 *  @param name	The name of the parameter.
	 *  @param clazz	The class for values.
	 *  @param direction	The direction (in/out).
	 *  @param updaterate	The updaterate (or -1 for none).
	 *  @param expression	The default value expression (if any).
	 *  @param mode	The evaluation mode.
	 *  @return	The newly created parameter.
	 */
	public IMParameter	createParameter(String name, Class clazz, String direction, long updaterate, String expression, String mode);
	
	/**
	 *  Delete a parameter.
	 *  @param parameter	The parameter to delete.
	 */
	public void	deleteParameter(IMParameter parameter);


	//-------- parameter sets --------

	/**
	 *  Get all parameter sets.
	 *  @return All parameter sets.
	 */
	public IMParameterSet[]	getParameterSets();

	/**
	 *  Get a parameter by name.
	 *  @param name The parameter name.
	 *  @return The parameter expression.
	 */
	public IMParameterSet	getParameterSet(String name);

	/**
	 *  Create a new parameter set.
	 *  @param name	The name of the  parameter set.
	 *  @param clazz	The class for values.
	 *  @param direction	The direction (in/out).
	 *  @param updaterate	The updaterate (or -1 for none).
	 *  @param expression	The default values expression (if any).
	 *  @param mode	The evaluation mode.
	 *  @return	The newly created parameter set.
	 */
	public IMParameterSet	createParameterSet(String name, Class clazz, String direction, long updaterate, String expression, String mode);
	
	/**
	 *  Delete a parameter set.
	 *  @param parameterset	The parameter set to delete.
	 */
	public void	deleteParameterSet(IMParameterSet parameterset);

	
	//-------- not xml related --------
	
	/**
	 *  Get the bindings.
	 *  @return The binding parameters.
	 */
	public IMParameter[] getBindingParameters();
}
