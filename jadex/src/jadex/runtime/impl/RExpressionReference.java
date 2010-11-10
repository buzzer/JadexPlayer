package jadex.runtime.impl;

import jadex.model.IMExpressionReference;

/**
 *  An expression is an evaluable element.
 */
public class RExpressionReference extends RElementReference	implements IRExpression
{
	//--------- constructors --------

	/**
	 *  Create a new expression.
	 *  @param modelelement	The model of this element.
	 *  @param owner	The owner.
	 */
	protected RExpressionReference(IMExpressionReference modelelement,
			 RElement owner, RReferenceableElement creator)
	{
		super(null, modelelement, null, owner, creator);
	}

	//-------- methods --------

	/**
	 *  Evaluate the expression.
	 *  @return	The value of the expression.
	 */
	public Object	getValue()
	{
		return ((IRExpression)getReferencedElement()).getValue();
	}

	/**
	 *  Refresh the cached expression value.
	 */
	public void	refresh()
	{
		((IRExpression)getReferencedElement()).refresh();
	}

	//-------- expression parameters --------

	/**
	 *  Set an expression parameter.
	 *  @param name The parameter name.
	 *  @param value The parameter value.
	 */
	public void setParameter(String name, Object value)
	{
		((IRExpression)getReferencedElement()).setParameter(name, value);
	}

	/**
	 *  Get an expression parameter.
	 *  @param name The parameter name.
	 *  @return The parameter value.
	 */
	public Object	getParameter(String name)
	{
		return ((IRExpression)getReferencedElement()).getParameter(name);
	}

	//-------- IExpression methods --------
	
	/**
	 *  Execute the get.
	 *  @return the result value of the get.
	 */
	public Object	execute()
	{
		return ((IRExpression)getReferencedElement()).execute();
	}

	/**
	 *  Execute the get using a local parameter.
	 *  @param name The name of the local parameter.
	 *  @param value The value of the local parameter.
	 *  @return the result value of the get.
	 */
	public Object	execute(String name, Object value)
	{
		return ((IRExpression)getReferencedElement()).execute(name, value);
	}

	/**
	 *  Execute the query using local parameters.
	 *  @param names The names of parameters.
	 *  @param values The parameter values.
	 *  @return The return value.
	 */
	public Object	execute(String[] names, Object[] values)
	{
		return ((IRExpression)getReferencedElement()).execute(names, values);
	}

	/**
	 *  Save temporary state. Model deletion on cleanup.
	 *  @param temporary The temporary state.
	 */
	public void setTemporary(boolean temporary)
	{
		((IRExpression)getReferencedElement()).setTemporary(temporary);
	}

	/**
	 *  Test if temporary. Model deletion on cleanup.
	 *  @return True, if temporary.
	 */
	public boolean isTemporary()
	{
		return ((IRExpression)getReferencedElement()).isTemporary();
	}
}

