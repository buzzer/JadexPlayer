package jadex.runtime.externalaccesswrapper;

import jadex.model.*;
import jadex.runtime.*;
import jadex.runtime.impl.*;
import jadex.util.Tuple;

/**
 *  The expression base wrapper accessible from external threads (e.g. gui).
 */
// Todo: remember created expressions/conditions for cleanup!!!
public class ExpressionbaseWrapper	extends ElementWrapper	implements IExpressionbase
{
	//-------- attributes --------

	/** The original expressionbase. */
	protected RExpressionbase expressionbase;

	//-------- constructors --------

	/**
	 *  Create a new expressionbase wrapper.
	 */
	protected ExpressionbaseWrapper(RExpressionbase expressionbase)
	{
		super(expressionbase.getScope().getAgent(), expressionbase);
		this.expressionbase = expressionbase;
	}

	//-------- methods --------

	/**
	 * Get a get created from a predefined expression.
	 * @param name The name of an expression defined in the ADF.
	 * @return The get object.
	 */
	public IExpression getExpression(final String name)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object = expressionbase.getExpression(name);
			}
		};
		return new ExpressionWrapper((IRExpression)exe.object);
	}

	/**
	 * Create a precompiled get.
	 * @param query The get expression.
	 * @return The precompiled get.
	 */
	public IExpression createExpression(final String query)
	{
		return createExpression(query, null, null);
	}

	/**
	 * Create a precompiled get.
	 * @param query The get expression.
	 * @return The precompiled get.
	 */
	public IExpression createExpression(final String query, final String[] paramnames, final Class[] paramtypes)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object = expressionbase.createExpression(query, paramnames, paramtypes);
			}
		};
		return new ExpressionWrapper((IRExpression)exe.object);
	}

	/**
	 *  Get a condition, that is triggered whenever the expression
	 *  value changes to true.
	 *  @param name	The condition name.
	 *  @return The condition.
	 */
	public ICondition	getCondition(final String name)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object = expressionbase.getCondition(name);
			}
		};
		return new ConditionWrapper((IRCondition)exe.object);
	}

	/**
	 * Create a condition, that is triggered whenever the expression value changes to true.
	 * @param expression The condition expression.
	 * @return The condition.
	 */
	public ICondition createCondition(final String expression)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object = expressionbase.createCondition(expression);
			}
		};
		return new ConditionWrapper((IRCondition)exe.object);
	}

	/**
	 *  Create a condition.
	 *  @param expression	The condition expression.
	 *  @param trigger	The condition trigger.
	 *  @return The condition.
	 */
	public ICondition	createCondition(final String expression, final String trigger, final String[] paramnames, final Class[] paramtypes)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object = expressionbase.createCondition(expression, trigger, paramnames, paramtypes);
			}
		};
		return new ConditionWrapper((IRCondition)exe.object);
	}

	/**
	 *  Register a new expression model.
	 *  @param mexpression The expression model.
	 */
	public void registerExpression(final IMExpression mexpression)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				expressionbase.registerExpression(mexpression);
			}
		};
	}

	/**
	 *  Register a new expression reference model.
	 *  @param mexpressionref The expression reference model.
	 */
	public void registerExpressionReference(final IMExpressionReference mexpressionref)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				expressionbase.registerExpressionReference(mexpressionref);
			}
		};
	}

	/**
	 *  Register a new condition model.
	 *  @param mcondition The condition model.
	 */
	public void registerCondition(final IMCondition mcondition)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				expressionbase.registerCondition(mcondition);
			}
		};
	}

	/**
	 *  Register a new condition reference model.
	 *  @param mconditionref The condition reference model.
	 */
	public void registerConditionReference(final IMConditionReference mconditionref)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				expressionbase.registerConditionReference(mconditionref);
			}
		};
	}

	/**
	 *  Deregister an expression model.
	 *  @param mexpression The expression model.
	 */
	public void deregisterExpression(final IMExpression mexpression)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				expressionbase.deregisterExpression(mexpression);
			}
		};
	}

	/**
	 *  Deregister an expression reference model.
	 *  @param mexpressionref The expression reference model.
	 */
	public void deregisterExpressionReference(final IMExpressionReference mexpressionref)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				expressionbase.deregisterExpressionReference(mexpressionref);
			}
		};
	}

	/**
	 *  Deregister an condition model.
	 *  @param mcondition The condition model.
	 */
	public void deregisterCondition(final IMCondition mcondition)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				expressionbase.deregisterCondition(mcondition);
			}
		};
	}

	/**
	 *  Deregister an condition reference model.
	 *  @param mconditionref The condition reference model.
	 */
	public void deregisterConditionReference(final IMConditionReference mconditionref)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				expressionbase.deregisterConditionReference(mconditionref);
			}
		};
	}
	
	//-------- listeners --------
	
	/**
	 *  Add a condition listener.
	 *  @param listener The condition listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addConditionListener(final String type, final IConditionListener userlistener, final boolean async)
	{
		new AgentInvocation()
		{
			public void run()
			{
				IFilter filter = new SystemEventFilter(new String[]{ISystemEventTypes.CONDITION_TRIGGERED}, type);
				AsynchronousSystemEventListener listener 
					= new AsynchronousSystemEventListener(userlistener, new Tuple(new Object[]{userlistener, expressionbase, type}));
				getCapability().addSystemEventListener(listener, filter, true, async);
			}
		};
	}
	
	/**
	 *  Remove a condition listener.
	 *  @param type The condition type.
	 *  @param listener The condition listener.
	 */
	public void removeConditionListener(final String type, final IConditionListener userlistener)
	{
		new AgentInvocation()
		{
			public void run()
			{
				Object	identifier	= new Tuple(new Object[]{userlistener, expressionbase, type});
				ISystemEventListener[] listeners = getAgent().getSystemEventListeners();
				for(int i=0; i<listeners.length; i++)
				{
					if((listeners[i] instanceof AsynchronousSystemEventListener) 
						&& ((AsynchronousSystemEventListener)listeners[i]).getIdentifier().equals(identifier))
					{
						getCapability().removeSystemEventListener(listeners[i]);
						break;
					}
				}
			}
		};
	}
}
