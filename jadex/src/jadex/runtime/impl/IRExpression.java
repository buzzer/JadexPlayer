package jadex.runtime.impl;

/**
 *  The common interface for expressions.
 */
public interface IRExpression	extends IRReferenceableElement
{
	//-------- methods --------

	/**
	 *  Evaluate the expression.
	 *  @return	The value of the expression.
	 */
	public Object getValue();

	/**
	 *  Refresh the cached expression value.
	 */
	public void refresh();

	//-------- expression parameters --------

	/**
	 *  Set an expression parameter.
	 *  @param name The parameter name.
	 *  @param value The parameter value.
	 */
	public void setParameter(String name, Object value);

	/**
	 *  Get an expression parameter.
	 *  @param name The parameter name.
	 *  @return The parameter value.
	 */
	public Object getParameter(String name);

	//-------- IQuery methods --------

	/**
	 *  Execute the get.
	 *  @return the result value of the get.
	 */
	public Object	execute();

	/**
	 *  Execute the get using a local parameter.
	 *  @param name The name of the local parameter.
	 *  @param value The value of the local parameter.
	 *  @return the result value of the get.
	 */
	public Object	execute(String name, Object value);

	/**
	 *  Execute the query using local parameters.
	 *  @param names The names of parameters.
	 *  @param values The parameter values.
	 *  @return The return value.
	 */
	public Object	execute(String[] names, Object[] values);

	/**
	 *  Save if temporary. Model deletion on cleanup.
	 *  @param temporary The temporary state.
	 */
	public void setTemporary(boolean temporary);

	/**
	 *  Test if temporary. Model deletion on cleanup.
	 *  @return True, if temporary.
	 */
	public boolean isTemporary();
}