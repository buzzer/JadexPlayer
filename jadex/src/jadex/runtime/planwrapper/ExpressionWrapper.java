package jadex.runtime.planwrapper;

import jadex.runtime.*;
import jadex.runtime.impl.*;


/**
 *  The user level view on a condition.
 *  Methods can only be called from plans,
 *  otherwise exceptions are thrown.
 */
public class ExpressionWrapper extends ElementWrapper implements IExpression
{
	//-------- attributes --------

	/** The original expression. */
	protected IRExpression expression;

	//-------- constructors --------

	/**
	 *  Create a new goalbase wrapper.
	 */
	protected ExpressionWrapper(IRExpression expression)
	{
		super(expression);
		this.expression = expression;
	}

	//-------- expression methods --------

	/**
	 *  Evaluate the expression.
	 *  @return	The value of the expression.
	 */
	public Object getValue()
	{
		checkThreadAccess();
		return expression.getValue();
	}

	/**
	 *  Refresh the cached expression value.
	 */
	public void refresh()
	{
		checkThreadAccess();
		expression.refresh();
	}

	//-------- expression parameters --------

	/**
	 *  Set an expression parameter.
	 *  @param name The parameter name.
	 *  @param value The parameter value.
	 */
	public void setParameter(String name, Object value)
	{
		checkThreadAccess();
		expression.setParameter(name, value);
	}

	/**
	 *  Get an expression parameter.
	 *  @param name The parameter name.
	 *  @return The parameter value.
	 */
	public Object getParameter(String name)
	{
		checkThreadAccess();
		return expression.getParameter(name);
	}

	//-------- IQuery methods --------

	/**
	 *  Execute the query.
	 *  @return the result value of the query.
	 */
	public Object	execute()
	{
		checkThreadAccess();
		return expression.execute();
	}

	/**
	 *  Execute the query using a local parameter.
	 *  @param name The name of the local parameter.
	 *  @param value The value of the local parameter.
	 *  @return the result value of the query.
	 */
	public Object	execute(String name, Object value)
	{
		checkThreadAccess();
		return expression.execute(name, value);
	}

	/**
	 *  Execute the query using local parameters.
	 *  @param names The names of parameters.
	 *  @param values The parameter values.
	 *  @return The return value.
	 */
	public Object	execute(String[] names, Object[] values)
	{
		checkThreadAccess();
		return expression.execute(names, values);
	}
}
