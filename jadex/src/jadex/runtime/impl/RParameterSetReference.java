package jadex.runtime.impl;

import jadex.model.*;
import jadex.runtime.*;

/**
 *  A parameter set implemented as a reference to another parameter.
 */
public class RParameterSetReference extends RElementReference	implements IRParameterSet
{
	//-------- constructor --------

	/**
	 *  Create a new parameter set reference.
	 *  @param parameterset The parameter set model element.
	 *  @param owner The owner.
	 */
	protected RParameterSetReference(IMParameterSetReference parameterset,
		IMConfigParameterSet state, RElement owner)
	{
		// parameterset references have the same name as their model
		// elements, because there is only one instance per goal reference.
		super(parameterset.getName(), parameterset, state, owner, null);
	}

	/**
	 *  Initialize the element.
	 */
	protected void	init()
	{
		if(isInited()) return;
		super.init();
		//throwSystemEvent(SystemEvent.PARAMETER_ADDED);
	}

	/**
	 *  Initialize the element.
	 *  Overwrites resolve from RElementReference.
	 */
	protected void	resolveReference(RReferenceableElement creator)
	{
		assert !isInited();

		// Initialize reference.
		IRParameterElement	orig = (IRParameterElement)((RParameterElementReference)getOwner())
			.getReferencedElement();
		if(orig==null)
			throw new RuntimeException("Could not resolve reference of: "+getName());
		IMReferenceableElement	mref	= ((IMParameterSetReference)getModelElement()).getReferencedElement();
		if(mref==null)
			throw new RuntimeException("No referenced element for: "+getName());
		RReferenceableElement elem = (RReferenceableElement)orig.getParameterSet(mref.getName());
		if(elem==null)
			throw new RuntimeException("Could not resolve reference of: "+getName());
		setReferencedElement(elem);
		elem.addReference(this);
		setResolved(true);
	}

	//-------- methods --------

	/**
	 *  Add a value to a parameter.
	 *  @param value The new value.
	 */
	public void addValue(Object value)
	{
		((IRParameterSet)getReferencedElement()).addValue(value);
	}

	/**
	 *  Remove a value to a parameter.
	 *  @param value The new value.
	 */
	public void removeValue(Object value)
	{
		((IRParameterSet)getReferencedElement()).removeValue(value);
	}

	/**
	 *  Add values to a parameter.
	 *  @param values The new values.
	 */
	public void addValues(Object[] values)
	{
		((IRParameterSet)getReferencedElement()).addValues(values);
	}

	/**
	 *  Remove all values from a parameter.
	 */
	public void removeValues()
	{
		((IRParameterSet)getReferencedElement()).removeValues();
	}

	/**
	 *  Get a value equal to the given object.
	 *  @param oldval The old value.
	 */
	public Object	getValue(Object oldval)
	{
		return ((IRParameterSet)getReferencedElement()).getValue(oldval);
	}

	/**
	 *  Test if a value is contained in a parameter.
	 *  @param value The value to test.
	 *  @return True, if value is contained.
	 */
	public boolean containsValue(Object value)
	{
		return ((IRParameterSet)getReferencedElement()).containsValue(value);
	}

	/**
	 *  Get the values of a parameterset.
	 *  @return The values.
	 */
	public Object[]	getValues()
	{
		return ((IRParameterSet)getReferencedElement()).getValues();
	}

	/**
	 *  Update a value to a new value. Searches the old
	 *  value with equals, removes it and stores the new value.
	 *  @param newvalue The new value.
	 */
	public void updateValue(Object newvalue)
	{
		((IRParameterSet)getReferencedElement()).updateValue(newvalue);
	}

	/**
	 *  Get the number of values currently
	 *  contained in this set.
	 *  @return The values count.
	 */
	public int size()
	{
		return ((IRParameterSet)getReferencedElement()).size();
	}

	/**
	 *  Update or add a value. When the value is already
	 *  contained it will be updated to the new value.
	 *  Otherwise the value will be added.
	 *  @param value The new or changed value.
	 * /
	public void updateOrAddValue(Object value)
	{
		((IRParameterSet)getReferencedElement()).updateOrAddValue(value);
	}*/

	/**
	 *  Replace a value with another one.
	 *  @param oldvalue The old value.
	 *  @param newvalue The new value.
	 * /
	public void replaceValue(Object oldvalue, Object newvalue)
	{
		((IRParameterSet)getReferencedElement()).replaceValue(oldvalue, newvalue);
	}*/

	/**
	 *  Get the initial values (if any).
	 *  Called from original element.
	 */
	protected Object[]	getInitialValues()
	{
		assert getConfiguration()!=null : this;

		// Todo: check for creator ??? (currently done by original)

		// Use value from configuration if specified.
		Object[]	values	= null;
		IMExpression[]	mvalues	= ((IMConfigParameterSet)getConfiguration()).getInitialValues();

		// Create initial values from multiple expressions.
		if(mvalues!=null)
		{
			values	= new Object[mvalues.length];
			for(int i=0; i<mvalues.length; i++)
			{
				values[i]	= getScope().getExpressionbase().evaluateInternalExpression(mvalues[i], this);
			}
		}

		return values;
	}

	/**
	 *  Get the initial values expression (if any).
	 *  Called from original element.
	 */
	protected Object	getInitialValuesExpression()
	{
		assert getConfiguration()!=null : this;

		// Todo: check for creator ??? (currently done by original)

		// Use value from configuration if specified.
		Object	value	= null;
		IMExpression	minivals	= ((IMConfigParameterSet)getConfiguration()).getInitialValuesExpression();

		// Create initial values from <values> expression.
		if(minivals!=null)
		{
			if(minivals.getEvaluationMode().equals(IMExpression.MODE_STATIC))
			{
				value	= getScope().getExpressionbase().evaluateInternalExpression(minivals, this);
			}
			else
			{
				value	= getScope().getExpressionbase().createInternalExpression(minivals, this, new SystemEvent(SystemEvent.ESVALUES_CHANGED, this));
			}
		}

		return value;
	}

	/**
	 *  Was the typed element modified by setting a value.
	 *  @return True, if modified.
	 */
	public boolean isModified()
	{
		return ((IRParameterSet)getReferencedElement()).isModified();
	}

	/**
	 *  Get the value class.
	 *  Shortcut for getModelElement().getClazz().
	 *  @return The value class.
	 */
	public Class	getClazz()
	{
		return ((IRParameterSet)getReferencedElement()).getClazz();
	}

	//-------- parameter protection methods --------
	
	/**
	 *  Check if this paramter can be accessed for read access.
	 */
	public void checkReadAccess()
	{
		((IRParameterSet)getReferencedElement()).checkReadAccess();
	}

	/**
	 *  Check if this paramter can be accessed for write access.
	 */
	public void checkWriteAccess()
	{
		((IRParameterSet)getReferencedElement()).checkWriteAccess();
	}

	/**
	 *  Set the initial values.
	 *  May be default values (from model),
	 *  or initiual values provided on creation.
	 */
	public void	setInitialValues()
	{
		((IRParameterSet)getReferencedElement()).setInitialValues();
	}
}

