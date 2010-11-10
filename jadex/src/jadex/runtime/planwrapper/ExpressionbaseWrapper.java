package jadex.runtime.planwrapper;

import jadex.model.*;
import jadex.runtime.*;
import jadex.runtime.impl.AsynchronousSystemEventListener;
import jadex.runtime.impl.RExpressionbase;
import jadex.runtime.impl.SystemEventFilter;
import jadex.util.Tuple;

/**
 *  The expression base wrapper accessible from within plans.
 */
// Todo: remember created expressions/conditions for cleanup!!!
public class ExpressionbaseWrapper extends ElementWrapper implements IExpressionbase
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
		super(expressionbase);
		this.expressionbase = expressionbase;
	}

	//-------- methods --------

	/**
	 * Get a get created from a predefined expression.
	 * @param name The name of an expression defined in the ADF.
	 * @return The get object.
	 */
	public IExpression getExpression(String name)
	{
		checkThreadAccess();
		return new ExpressionWrapper(expressionbase.getExpression(name));
	}

	/**
	 * Create a precompiled get.
	 * @param query The get expression.
	 * @return The precompiled get.
	 */
	public IExpression createExpression(String query)
	{
		return createExpression(query, null, null);
	}

	/**
	 *  Create a precompiled expression.
	 *  @param expression	The expression string.
	 *  @param paramnames The parameter names.
	 *  @param paramtypes The parameter types.
	 *  @return The precompiled expression.
	 */
	public IExpression	createExpression(String expression, String[] paramnames, Class[] paramtypes)
	{
		checkThreadAccess();
		return new ExpressionWrapper(expressionbase.createExpression(expression, paramnames, paramtypes));
	}

	/**
	 *  Get a condition, that is triggered whenever the expression
	 *  value changes to true.
	 *  @param name	The condition name.
	 *  @return The condition.
	 */
	public ICondition	getCondition(String name)
	{
		checkThreadAccess();
		return new ConditionWrapper(expressionbase.getCondition(name));
	}

	/**
	 * Create a condition, that is triggered whenever the expression value changes to true.
	 * @param expression The condition expression.
	 * @return The condition.
	 */
	public ICondition createCondition(String expression)
	{
		checkThreadAccess();
		return new ConditionWrapper(expressionbase.createCondition(expression));
	}

	/**
	 * Create a condition.
	 * @param expression The condition expression.
	 * @param trigger The condition trigger.
	 * @return The condition.
	 */
	public ICondition	createCondition(final String expression, final String trigger, final String[] paramnames, final Class[] paramtypes)
	{
		checkThreadAccess();
		return new ConditionWrapper(expressionbase.createCondition(expression, trigger, paramnames, paramtypes));
	}

	/**
	 *  Register a new expression model.
	 *  @param mexpression The expression model.
	 */
	public void registerExpression(IMExpression mexpression)
	{
		checkThreadAccess();
		expressionbase.registerExpression(mexpression);
	}

	/**
	 *  Register a new expression reference model.
	 *  @param mexpressionref The expression reference model.
	 */
	public void registerExpressionReference(IMExpressionReference mexpressionref)
	{
		checkThreadAccess();
		expressionbase.registerExpressionReference(mexpressionref);
	}

	/**
	 *  Register a new condition model.
	 *  @param mcondition The condition model.
	 */
	public void registerCondition(IMCondition mcondition)
	{
		checkThreadAccess();
		expressionbase.registerCondition(mcondition);
	}

	/**
	 *  Register a new condition reference model.
	 *  @param mconditionref The condition reference model.
	 */
	public void registerConditionReference(IMConditionReference mconditionref)
	{
		checkThreadAccess();
		expressionbase.registerConditionReference(mconditionref);
	}

	/**
	 *  Deregister an expression model.
	 *  @param mexpression The expression model.
	 */
	public void deregisterExpression(IMExpression mexpression)
	{
		checkThreadAccess();
		expressionbase.deregisterExpression(mexpression);
	}

	/**
	 *  Deregister an expression reference model.
	 *  @param mexpressionref The expression reference model.
	 */
	public void deregisterExpressionReference(IMExpressionReference mexpressionref)
	{
		checkThreadAccess();
		expressionbase.deregisterExpressionReference(mexpressionref);
	}

	/**
	 *  Deregister an condition model.
	 *  @param mcondition The condition model.
	 */
	public void deregisterCondition(IMCondition mcondition)
	{
		checkThreadAccess();
		expressionbase.deregisterExpression(mcondition);
	}

	/**
	 *  Deregister an condition reference model.
	 *  @param mconditionref The condition reference model.
	 */
	public void deregisterConditionReference(IMConditionReference mconditionref)
	{
		checkThreadAccess();
		expressionbase.deregisterConditionReference(mconditionref);
	}
	
	//-------- listeners --------
	
	/**
	 *  Add a condition listener.
	 *  @param listener The condition listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addConditionListener(String type, IConditionListener userlistener, boolean async)
	{
		checkThreadAccess();
		IFilter filter = new SystemEventFilter(new String[]{ISystemEventTypes.CONDITION_TRIGGERED}, type);
		AsynchronousSystemEventListener listener 
			= new AsynchronousSystemEventListener(userlistener, new Tuple(new Object[]{userlistener, expressionbase, type}));
		getCapability().addSystemEventListener(listener, filter, true, async);
	}
	
	/**
	 *  Remove a condition listener.
	 *  @param listener The condition listener.
	 */
	public void removeConditionListener(String type, IConditionListener userlistener)
	{
		checkThreadAccess();
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
}
