package jadex.runtime.externalaccesswrapper;

import jadex.runtime.IExpression;
import jadex.runtime.impl.IRExpression;
import jadex.util.Tuple;


/**
 *  The user level view on a condition.
 *  Methods can only be called from external thread,
 *  otherwise exceptions are thrown.
 */
public class ExpressionWrapper	extends ElementWrapper	implements IExpression
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
		super(expression.getScope().getAgent(), expression);
		this.expression = expression;
	}

	//-------- expression methods --------

	/**
	 *  Evaluate the expression.
	 *  @return	The value of the expression.
	 */
	public Object getValue()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				object	= expression.getValue();
			}
		};
		return exe.object;
	}

	/**
	 *  Refresh the cached expression value.
	 */
	public void refresh()
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				expression.refresh();
			}
		};
	}

	//-------- expression parameters --------

	/**
	 *  Set an expression parameter.
	 *  @param name The parameter name.
	 *  @param value The parameter value.
	 */
	public void setParameter(final String name, final Object value)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				expression.setParameter(name, value);
			}
		};
	}

	/**
	 *  Get an expression parameter.
	 *  @param name The parameter name.
	 *  @return The parameter value.
	 */
	public Object getParameter(final String name)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				object	= expression.getParameter(name);
			}
		};
		return exe.object;
	}

	//-------- IQuery methods --------

	/**
	 *  Execute the query.
	 *  @return the result value of the query.
	 */
	public Object	execute()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				object	= expression.execute();
			}
		};
		return exe.object;
	}

	/**
	 *  Execute the query using a local parameter.
	 *  @param name The name of the local parameter.
	 *  @param value The value of the local parameter.
	 *  @return the result value of the query.
	 */
	public Object	execute(final String name, final Object value)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				object	= expression.execute(name, value);
			}
		};
		return exe.object;
	}

	/**
	 *  Execute the query using local parameters.
	 *  @param names The names of parameters.
	 *  @param values The parameter values.
	 *  @return The return value.
	 */
	public Object	execute(final String[] names, final Object[] values)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				object	= expression.execute(names, values);
			}
		};
		return exe.object;
	}
}
