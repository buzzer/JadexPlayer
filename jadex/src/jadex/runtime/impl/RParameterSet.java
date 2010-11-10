package jadex.runtime.impl;

import java.util.Iterator;

import jadex.model.*;
import jadex.runtime.*;
import jadex.util.SReflect;

/**
 *  A parameter set instance stores runtime
 *  information about a parameter set.
 */
public class RParameterSet extends RTypedElementSet	implements IRParameterSet
{
	//-------- attributes --------

	/** The configuration. */
	protected IMConfigParameterSet	config;

	//-------- constructor --------

	/**
	 *  Create a new parameter set.
	 *  To evaluate the initial values, init() has to be called.
	 *  @param param The parameter.
	 *  @param owner The owner.
	 */
	protected RParameterSet(IMParameterSet param, IMConfigParameterSet config, RElement owner)
	{
		// parameter set instances have the same name as their model
		// elements, because there is only one instance per goal.
		super(param.getName(), param, config, owner, null);
		this.config	= config;
	}

	//-------- initialization methods --------

	/**
	 *  Set the initial values.
	 *  May be default values (from model),
	 *  or initiual values provided on creation.
	 */
	public void	setInitialValues()
	{
		boolean	found	= false;

		// Use values from creator of element (if any).
		RReferenceableElement	root	= ((RParameterElement)getOwner()).getCreator();
		// Hack??? May cross mor than one capability border!
		while(root!=null && root.getCreator()!=null)
			root	= root.getCreator();
		if(root!=null && root.getConfiguration()!=null)	// Hack???
		{
			RParameterSetReference	ref	= ((RParameterElementReference)root).getParameterSetReference(this);
			if(ref.getConfiguration()!=null)	// Hack???
			{
				found	= true;
				// Check for separate initial values. 
				Object[]	values	= ref.getInitialValues();
				for(int i=0; values!=null && i<values.length; i++)
				{
					addValue(values[i]);
				}
				// Check for initial values expression.
				Object	inivals	= ref.getInitialValuesExpression();
				if(inivals instanceof IRExpression)
				{
					// Dynamic expression -> create expression reference;
					IMExpressionReference	mref	= ((IMCapability)getScope().getModelElement())
						.getExpressionbase().getExpressionReference(IMExpressionbase.STANDARD_EXPRESSION_REFERENCE);
					inivals	= getScope().getExpressionbase().createExpression(mref, (RReferenceableElement)inivals);
					setInitialValuesExpression((IRExpression)inivals);
				}
				else if(inivals!=null)
				{
					// Value of static expression -> create iterator to extract single values. 
					Iterator	ivalues	= SReflect.getIterator(inivals);
					while(ivalues.hasNext())
					{
						addValue(ivalues.next());
					}
				}
			}
		}

		if(!found)
		{
			// Use values from initial state if specified.
			IMExpression[]	mvalues	= null;
			IMExpression	minivals	= null;
			if(getConfiguration()!=null)
			{
				mvalues	= ((IMConfigParameterSet)getConfiguration()).getInitialValues();
				minivals	= ((IMConfigParameterSet)getConfiguration()).getInitialValuesExpression();
			}
			// Otherwise use default values from model.
			else
			{
				mvalues	= ((IMParameterSet)getModelElement()).getDefaultValues();
				minivals	= ((IMParameterSet)getModelElement()).getDefaultValuesExpression();
			}

			// Create initial values from multiple expressions.
			for(int i=0; mvalues!=null && i<mvalues.length; i++)
			{
				addValue(getScope().getExpressionbase().evaluateInternalExpression(mvalues[i], this));
			}

			// Create initial values from <values> expression.
			if(minivals!=null)
			{
				if(minivals.getEvaluationMode().equals(IMExpression.MODE_STATIC))
				{
					Iterator it = SReflect.getIterator(getScope().getExpressionbase()
						.evaluateInternalExpression(minivals, this));
					while(it.hasNext())
					{
						addValue(it.next());
					}
				}
				else
				{
					setInitialValuesExpression(getScope().getExpressionbase()
						.createInternalExpression(minivals, this, createSystemEvent(SystemEvent.ESVALUES_CHANGED, null, -1)));
				}
			}
		}
	}

	/**
	 *  Init this belief set.
	 */
	protected void init()
	{
		super.init();

		// Reset state attribute to allow garbage collection.
		config	= null;
	}

	//-------- methods --------

	/**
	 *  Add a value to the typed element.
	 *  @param value The new value.
	 */
	public void addValue(Object value)
	{
		if(!initing && ((IMParameterSet)getModelElement()).getDirection().equals(IMParameterSet.DIRECTION_FIXED))
			throw new RuntimeException("Fixed elements cannot be altered: "+this);
		//checkWriteAccess();
		super.addValue(value);
	}

	/**
	 *  Remove a value from a typed element
	 *  @param value The new value.
	 */
	public void removeValue(Object value)
	{
		if(!initing && ((IMParameterSet)getModelElement()).getDirection().equals(IMParameterSet.DIRECTION_FIXED))
			throw new RuntimeException("Fixed elements cannot be altered: "+this);
		//checkWriteAccess();
		super.removeValue(value);
	}

	/**
	 *  Remove all values from a typed element.
	 */
	public void removeValues()
	{
		if(!initing && ((IMParameterSet)getModelElement()).getDirection().equals(IMParameterSet.DIRECTION_FIXED))
			throw new RuntimeException("Fixed elements cannot be altered: "+this);
		//checkWriteAccess();
		super.removeValues();
	}

	/**
	 *  Update or add a value. When the value is already
	 *  contained it will be updated to the new value.
	 *  Otherwise the value will be added.
	 *  @param value The new or changed value
	 * /
	public void updateOrAddValue(Object value)
	{
		//checkWriteAccess();
		super.updateOrAddValue(value);
	}*/

	/**
	 *  Update a value to a new value. Searches the old
	 *  value with equals, removes it and stores the new fact.
	 *  @param newvalue The newvalue
	 */
	public void updateValue(Object newvalue)
	{
		if(!initing && ((IMParameterSet)getModelElement()).getDirection().equals(IMParameterSet.DIRECTION_FIXED))
			throw new RuntimeException("Fixed elements cannot be altered: "+this);

		//checkWriteAccess();
		super.updateValue(newvalue);
	}

	/**
	 *  Replace a value with another one.
	 *  @param oldval The old value.
	 *  @param newval The new value.
	 * /
	public void replaceValue(Object oldval, Object newval)
	{
		//checkWriteAccess();
		super.replaceValue(oldval, newval);
	}*/

	/**
	 *  Get a value equal to the given object.
	 *  @param oldval The old value.
	 */
	public Object	getValue(Object oldval)
	{
		//checkReadAccess();
		return super.getValue(oldval);
	}

	/**
	 *  Test if a value is contained in a typed element.
	 *  @param value The value to test.
	 *  @return True, if value is contained.
	 */
	public boolean containsValue(Object value)
	{
		//checkReadAccess();
		return super.containsValue(value);
	}

	/**
	 *  Get the values of a typed element.
	 *  @return The values.
	 */
	public Object[]	getValues()
	{
		//checkReadAccess();
		return super.getValues();
	}

	//-------- protection mode --------

	/**
	 *  Get the protection mode of this parameter.
	 *  @return The protection mode.
	 */
	public String getProtectionMode()
	{
		//assert getOwner() instanceof RParameterElement;

		String ret = null;
		if(getOwner() instanceof RParameterElement)
		{
			RParameterElement owner = (RParameterElement)getOwner();
			ret = owner.getParameterProtectionMode();
		}
		return ret;
	}

	/**
	 *  Check if this paramter can be accessed for read access.
	 */
	public void checkReadAccess()
	{
		/*Object dir = ((IMParameterSet)getModelElement()).getDirection();

		if(RParameterElement.ACCESS_PROTECTION_INIT.equals(getProtectionMode())
			&& dir.equals(IMParameterSet.DIRECTION_IN))
		{
			throw new RuntimeException("Read access not allowed to parameter set: "
				+((IMParameterSet)getModelElement()).getDirection()+" "+getName());
			//System.out.println(getName()+" - "+getProtectionMode()+" in");
		}*/
//		else if(getProtectionMode().equals(RParameterElement.ACCESS_PROTECTION_PROCESSING)
//			&& dir.equals(IMParameterSet.IDirectionData.OUT))
//		{
//			throw new RuntimeException("Read access not allowed to parameter set: "
//				+((IMParameterSet)getModelElement()).getDirection()+" "+getName());
//			//System.out.println(getName()+" - "+getProtectionMode()+" in");
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
		boolean ret = true;
		Object dir = ((IMParameterSet)getModelElement()).getDirection();

		if(RParameterElement.ACCESS_PROTECTION_INIT.equals(getProtectionMode())
			&& dir.equals(IMParameterSet.DIRECTION_OUT))
		{
			throw new RuntimeException("Read access not allowed to parameter set: "
				+((IMParameterSet)getModelElement()).getDirection()+" "+getName());
			//System.out.println(getName()+" - "+getProtectionMode()+" out");
		}
		else if(RParameterElement.ACCESS_PROTECTION_PROCESSING.equals(getProtectionMode())
			&& dir.equals(IMParameterSet.DIRECTION_IN))
		{
			throw new RuntimeException("Read access not allowed to parameter set: "
				+((IMParameterSet)getModelElement()).getDirection()+" "+getName());
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

