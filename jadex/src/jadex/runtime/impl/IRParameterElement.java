package jadex.runtime.impl;

/**
 *  The internal interface for all elements with parameters.
 */
public interface IRParameterElement extends IRReferenceableElement
{
	/**
	 *  Get the parameter protection mode.
	 *  @return The parameter protection mode.
	 */
	public String getParameterProtectionMode();

	/* *
	 *  Set the parameter protection mode.
	 *  @param protectionmode The protection mode.
	 * /
	public void setParameterProtectionMode(String protectionmode);*/

	/**
	 *  Get all parameters.
	 *  @return All parameters.
	 */
	public IRParameter[]	getParameters();

	/**
	 *  Get all parameter sets.
	 *  @return All parameter sets.
	 */
	public IRParameterSet[]	getParameterSets();

	/**
	 *  Get the parameter element.
	 *  @param name The name.
	 *  @return The param.
	 */
	public IRParameter getParameter(String name);

	/**
	 *  Get the parameter set element.
 	 *  @param name The name.
	 *  @return The param set.
	 */
	public IRParameterSet getParameterSet(String name);

	/**
	 *  Has the element a parameter element.
	 *  @param name The name.
	 *  @return True, if it has the parameter.
	 */
	public boolean hasParameter(String name);

	/**
	 *  Has the element a parameter set element.
	 *  @param name The name.
	 *  @return True, if it has the parameter set.
	 */
	public boolean hasParameterSet(String name);

	//-------- Hack!!! needed because of bean access --------

	/**
	 *  Get a value corresponding to a belief.
	 *  @param name The name identifiying the belief.
	 *  @return The value.
	 */
	public Object getParameterValue(String name);
	 /**
	 *  Get all values corresponding to one beliefset.
	 *  @param name The name identifiying the beliefset.
	 *  @return The values.
	 */
	public Object[] getParameterSetValues(String name);
}