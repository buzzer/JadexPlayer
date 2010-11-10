package jadex.runtime.impl;

import jadex.model.*;
import jadex.runtime.*;

/**
 *  A parameter instance stores runtime
 *  information about the parameter values.
 */
public class RParameter extends RTypedElement implements IRParameter
{
	//-------- constructor --------

	/**
	 *  Create a new element.
	 *  To evaluate the initial values, init() has to be called.
	 *  @param parameter The model element.
	 *  @param owner The owner.
	 */
	protected RParameter(IMParameter parameter, IMConfigParameter config, RElement owner)
	{
		// parameter instances have the same name as their model
		// elements, because there is only one instance per goal.
		super(parameter.getName(), parameter, config, owner, null);
	}

	//-------- initialization methods --------

	/**
	 *  Set the initial value.
	 *  May be a default value (from model),
	 *  or an initiual value provided on creation.
	 */
	public void	setInitialValue()
	{
		boolean	found	= false;

		// Init binding parameters with selected binding value.
		if(((IMParameter)getModelElement()).getBindingOptions()!=null && getConfiguration()==null)
		{
			assert getExpressionParameter(getModelElement().getName())!=null
				: "Binding value not found: "+getModelElement().getName()+" "+getExpressionParameters();
			setInitialValue(getExpressionParameters().get(getModelElement().getName()));
			found = true;
		}

		if(!found)
		{
			// Use value from creator of element (if any).
			RReferenceableElement	root	= ((RParameterElement)getOwner()).getCreator();
			// Hack??? May cross more than one capability border!
			while(root!=null && root.getCreator()!=null)
				root	= root.getCreator();
			if(root!=null)
			{
				RParameterReference	ref	= ((RParameterElementReference)root).getParameterReference(this);
				if(ref.getConfiguration()!=null)	// Hack???
				{
					Object	value	= ref.getInitialValue();
					if(value instanceof IRExpression)
					{
						IMExpressionReference	mref	= ((IMCapability)getScope().getModelElement())
							.getExpressionbase().getExpressionReference(IMExpressionbase.STANDARD_EXPRESSION_REFERENCE);
						value	= getScope().getExpressionbase().createExpression(mref, (RReferenceableElement)value); // todo: use internalCreate?
					}
					setInitialValue(value);
					found	= true;
				}
			}
		}

		if(!found)
		{
			// Use value from configuration if specified.
			IMExpression	mvalue	= null;
			if(getConfiguration()!=null)
			{
				mvalue	= ((IMConfigParameter)getConfiguration()).getInitialValue();
			}
			// Otherwise use default value from model.
			else
			{
				mvalue	= ((IMParameter)getModelElement()).getDefaultValue();
			}

			// Create initial value for a single valued element.
			if(mvalue!=null)
			{
				if(mvalue.getEvaluationMode().equals(IMExpression.MODE_STATIC))
				{
					// Set static value.
					setInitialValue(getScope().getExpressionbase().evaluateInternalExpression(mvalue, this));
				}
				else
				{
					// Set dynamic value (expression).
					setInitialValue(getScope().getExpressionbase().createInternalExpression(
						mvalue, this, new SystemEvent(SystemEvent.VALUE_CHANGED, this)));
				}
			}
		}
	}

	//-------- methods --------

	/**
	 *  Get the value of a typed element.
	 *  @return The value.
	 */
	public Object	getValue()
	{
		//checkReadAccess();
		return super.getValue();
	}

	/**
	 *  Set a value of a typed element.
	 *  @param value The new value.
	 */
	public void setValue(Object value)
	{
		//checkWriteAccess();
//		if(!initing && ((IMParameter)getModelElement()).getDirection().equals(IMParameter.DIRECTION_FIXED))
		if(((IMParameter)getModelElement()).getDirection().equals(IMParameter.DIRECTION_FIXED))
			throw new RuntimeException("Fixed elements cannot be altered: "+this);
		super.setValue(value);
	}

	//-------- protection mode --------

	/**
	 *  Get the protection mode of this parameter.
	 *  @return The protection mode.
	 */
	private String getProtectionMode()
	{
		//assert getOwner() instanceof RParameterElement;
		String ret = null;
		RParameterElement owner = (RParameterElement)getOwner();
		if(owner!=null)
			ret = owner.getParameterProtectionMode();
		return ret;
	}

	/**
	 *  Check if this paramter can be accessed for read access.
	 */
	public void checkReadAccess()
	{
		// No read access will be checked
		
		/*Object dir = ((IMParameter)getModelElement()).getDirection();

		if(RParameterElement.ACCESS_PROTECTION_INIT.equals(getProtectionMode())
			&& dir.equals(IMParameter.DIRECTION_IN))
		{
			//System.out.println(getName()+" - "+getProtectionMode()+" in");
			throw new RuntimeException("Read access not allowed to parameter: "
				+((IMParameter)getModelElement()).getDirection()+" "+getName()+" "+getOwner());
		}*/
//		else if(getProtectionMode().equals(RParameterElement.ACCESS_PROTECTION_PROCESSING)
//			&& dir.equals(IMParameter.IDirectionData.OUT))
//		{
//			//System.out.println(getName()+" - "+getProtectionMode()+" in");
//			throw new RuntimeException("Read access not allowed to parameter: "
//				+((IMParameter)getModelElement()).getDirection()+" "+getName());
//		}
//		else if(getProtectionMode().equals(RParameterElement.ACCESS_PROTECTION_NONE))
//		{
//			//System.out.println("unprotected access: "+getName());
//		}
	}

	/**
	 *  Check if this paramter can be accessed for write access.
	 */
	public void checkWriteAccess()
	{
		Object dir = ((IMParameter)getModelElement()).getDirection();

		//if(dir==null)
		//	throw new RuntimeException("Direction nulls: "+getName());

		if(RParameterElement.ACCESS_PROTECTION_INIT.equals(getProtectionMode())
			&& dir.equals(IMParameter.DIRECTION_OUT))
		{
			throw new RuntimeException("Write access not allowed to parameter: "
				+((IMParameter)getModelElement()).getDirection()+" "+getName());
			//System.out.println(getName()+" - "+getProtectionMode()+" out");
		}
		else if(RParameterElement.ACCESS_PROTECTION_PROCESSING.equals(getProtectionMode())
			&& dir.equals(IMParameter.DIRECTION_IN))
		{
			throw new RuntimeException("Write access not allowed to parameter: "
				+((IMParameter)getModelElement()).getDirection()+" "+getName());
			//System.out.println(getName()+" - "+getProtectionMode()+" in");
		}
//		else if(getProtectionMode().equals(RParameterElement.ACCESS_PROTECTION_NONE))
//		{
//			//System.out.println("unprotected access: "+getName());
//		}
	}

	/**
	 *  Generate a change event for this element.
	 *  @param event The event.
	 */
	public void throwSystemEvent(SystemEvent event)
	{
		// Only throw events, when element is active.
		if(RParameterElement.ACCESS_PROTECTION_PROCESSING.equals(getProtectionMode()))
			super.throwSystemEvent(event);
	}

}

