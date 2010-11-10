package jadex.model;

/**
 *  The parameter element reference.
 */
public interface IMParameterElementReference extends IMElementReference
{
	//-------- parameter references --------

	/**
	 *  Get all parameter references.
	 *  @return All parameter references.
	 */
	public IMParameterReference[]	getParameterReferences();

	/**
	 *  Get a parameter reference by name.
	 *  @param name The parameter reference name.
	 *  @return The parameter reference.
	 */
	// Todo: replace by getReference(original)
	public IMParameterReference	getParameterReference(String name);

	/**
	 *  Create a new parameter reference.
	 *  @param ref	The name of the referenced parameter.
	 *  @param clazz	The class for values.
	 *  @return	The newly created parameter.
	 */
	public IMParameterReference	createParameterReference(String ref, Class clazz);
	
	/**
	 *  Delete a parameter reference.
	 *  @param parameter	The parameter reference to delete.
	 */
	public void	deleteParameterReference(IMParameterReference parameter);


	//-------- parameter set references --------

	/**
	 *  Get all parameter set references.
	 *  @return All parameter set references.
	 */
	public IMParameterSetReference[]	getParameterSetReferences();

	/**
	 *  Get a parameter by name.
	 *  @param name The parameter name.
	 *  @return The parameter expression.
	 */
	// Todo: replace by getReference(original)
	public IMParameterSetReference	getParameterSetReference(String name);

	/**
	 *  Create a new parameter set reference.
	 *  @param ref	The name of the referenced parameter set.
	 *  @param clazz	The class for values.
	 *  @return	The newly created parameter set reference.
	 */
	public IMParameterSetReference	createParameterSetReference(String ref, Class clazz);
	
	/**
	 *  Delete a parameter set reference.
	 *  @param ref	The parameter set reference to delete.
	 */
	public void	deleteParameterSetReference(IMParameterSetReference ref);

}
