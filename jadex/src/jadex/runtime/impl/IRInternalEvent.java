package jadex.runtime.impl;

/**
 *  The interface for all internal events (concrete and referenced).
 */
public interface IRInternalEvent extends IREvent
{
	//-------- parameter handling --------

	/**
	 *  Get a parameter.
	 *  @param name The name.
	 *  @return The parameter.
	 */
	public IRParameter getParameter(String name);

	/**
	 *  Get a parameter.
	 *  @param name The name.
	 *  @return The parameter set.
	 */
	public IRParameterSet getParameterSet(String name);

	/**
	 *  Set a parameter.
	 *  @param name The parameter name.
	 *  @param value The parameter value.
	 * /
	public void getParameter(String name, Object value);

	/**
	 *  Set a parameter.
	 *  @param name The parameter name.
	 *  @param value The parameter value.
	 * /
	public void addParameterSetValue(String name, Object value);

	/**
	 *  Get a parameter value.
	 *  @param name The name.
	 *  @return The param value.
	 * /
	public Object getParameterValue(String name);

	/**
	 *  Get a parameter set values.
	 *  @param name The name.
	 *  @return The param value.
	 * /
	public Object[] getParameterSetValues(String name);*/
}
