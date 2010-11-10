package jadex.runtime.impl;

import jadex.model.IMExpression;
import jadex.model.IMConfigParameter;
import jadex.model.IMParameterReference;
import jadex.runtime.SystemEvent;

/**
 *  A parameter implemented as a reference to another parameter.
 */
public class RParameterReference extends RElementReference	implements IRParameter
{
	//-------- constructor --------

	/**
	 *  Create a new parameter.
	 *  @param parameter The parameter model element.
	 *  @param owner The owner.
	 */
	protected RParameterReference(IMParameterReference parameter, IMConfigParameter state, RElement owner)
	{
		// parameter references have the same name as their model
		// elements, because there is only one instance per goal reference.
		super(parameter.getName(), parameter, state, owner, null);
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
		String name;

		/*if(((IMElementReference)getOwner().getModelElement()).isAbstract())
		{
			// todo: Hack!!! Assume identical names.
			name	= getModelElement().getName();

		}
		else
		{*/
			name	= ((IMParameterReference)getModelElement()).getReferencedElement().getName();
		//}

		RReferenceableElement elem = (RReferenceableElement)orig.getParameter(name);
		if(elem==null)
			throw new RuntimeException("Could not resolve reference of: "+getName());
		setReferencedElement(elem);
		elem.addReference(this);
		setResolved(true);
	}

	//-------- methods --------

	/**
	 *  Set a value of a parameter.
	 *  @param value The new value.
	 */
	public void setValue(Object value)
	{
		((IRParameter)getReferencedElement()).setValue(value);
	}

	/**
	 *  Get the value of a parameter.
	 *  @return The value.
	 */
	public Object	getValue()
	{
		return ((IRParameter)getReferencedElement()).getValue();
	}

	/**
	 *  Refresh the value of the parameter.
	 * /
	public void	refresh()
	{
		((IRParameter)getReferencedElement()).refresh();
	}*/

	/**
	 *  Get the initial value.
	 *  Called from original element.
	 */
	protected Object	getInitialValue()
	{
		assert getConfiguration()!=null : this;

		// Todo: check for creator ??? (currently done by original)

		// Use value from configuration if specified.
		Object	value	= null;
		IMExpression	mvalue	= ((IMConfigParameter)getConfiguration()).getInitialValue();

		// Create initial value for a single valued element.
		if(mvalue!=null)
		{
			if(mvalue.getEvaluationMode().equals(IMExpression.MODE_STATIC))
			{
				// Static value.
				value	= getScope().getExpressionbase().evaluateInternalExpression(mvalue, this);
			}
			else
			{
				// Dynamic value (expression).
				value	= getScope().getExpressionbase().createInternalExpression(
					mvalue, this, new SystemEvent(SystemEvent.VALUE_CHANGED, this));
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
		return ((IRParameter)getReferencedElement()).isModified();
	}

	/**
	 *  Get the value class.
	 *  Shortcut for getModelElement().getClazz().
	 *  @return The value class.
	 */
	public Class	getClazz()
	{
		return ((IRParameter)getReferencedElement()).getClazz();
	}

	//-------- parameter protection methods --------
	
	/**
	 *  Check if this paramter can be accessed for read access.
	 */
	public void checkReadAccess()
	{
		((IRParameter)getReferencedElement()).checkReadAccess();
	}

	/**
	 *  Check if this paramter can be accessed for write access.
	 */
	public void checkWriteAccess()
	{
		((IRParameter)getReferencedElement()).checkWriteAccess();
	}

	/**
	 *  Get the protection mode of this parameter.
	 *  @return The protection mode.
	 * /
	public String getProtectionMode()
	{
		return ((IRParameter)getReferencedElement()).getProtectionMode();
	}*/

	/**
	 *  Set the initial value.
	 *  May be a default value (from model),
	 *  or an initiual value provided on creation.
	 */
	public void	setInitialValue()
	{
		((IRParameter)getReferencedElement()).setInitialValue();
	}
}

