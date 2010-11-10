package jadex.runtime.impl;

import jadex.model.*;
import jadex.util.*;
import jadex.util.collection.*;
import jadex.runtime.*;
import java.util.*;

/**
 *  An expression is an evaluable element.
 */
public class RExpression extends RReferenceableElement implements IRExpression
{
	//-------- attributes --------

	/** The value. */
	protected Object value;

	/** The system event which is thrown when
	 this element is affected from another element. */
	protected SystemEvent systemevent;

	/** The relevant events (cached because isAffected is bottleneck). */
	protected MultiCollection	relevant;

	/** The relevant events for any element (cached because isAffected is bottleneck). */
	protected Collection	relevant_any;

	/** The temporary state. If true, model will be deleted on cleanup. */
	protected boolean temporary;

	//--------- constructors --------

	/**
	 *  Create a new expression.
	 *  @param modelelement	The model of this element.
	 *  @param owner	The owner.
	 */
	protected RExpression(IMExpression modelelement, RElement owner, RReferenceableElement creator,
		SystemEvent systemevent)
	{
		// todo: support configurations for expressions.
		super(null, modelelement, null, owner, creator, null);

		if(modelelement.getEvaluationMode()==null)
			throw new RuntimeException("Evaluation mode nulls: "+modelelement.getName()+" "+modelelement.getExpressionText());

		if(!modelelement.getEvaluationMode().equals(IMExpression.MODE_DYNAMIC))
		{
			this.value = evaluateTerm(null);
			//System.out.println(getName()+" evaluated: "+value);
		}

		this.systemevent = systemevent;
		if(systemevent!=null)
		{
			systemevent.setDerived(true);
			getScope().getExpressionbase().addExpression(this);
		}

		this.relevant = ((IMExpression)getModelElement()).getRelevantList();
		this.relevant_any	= relevant.getCollection(IMExpression.ANY_ELEMENT);
	}

	/**
	 *  Perform any necessary clean up, when this element is
	 *  no longer used.
	 *  Subclasses may override this method to do their
	 *  cleanup operations. They must call super.cleanup
	 *  to ensure that the cleanedup property is set.
	 */
	public void	cleanup()
	{
		if(cleanedup)
			return;
		
		super.cleanup();

		// Cleanup is optional as it could be a condition.
		if(this.getClass().equals(RExpression.class))
		{
			// Remove expression from model.
			if(isTemporary())
			{
				IMReferenceableElement elem = (IMReferenceableElement)getModelElement();
				((IMExpressionbase)getScope().getExpressionbase().getModelElement()).deleteReferenceableElement(elem);
			}
			// Remove expression from runtime.
			// Only traced expressions are stored.
			if(systemevent!=null)
				getScope().getExpressionbase().removeExpression(this);
		}
	}

	//-------- methods --------

	/**
	 *  Evaluate the expression.
	 *  @return	The value of the expression.
	 */
	public Object	getValue()
	{
		return getValue(null);
	}

	/**
	 *  Evaluate the expression.
	 *  @return	The value of the expression.
	 * /
	// Todo: remove???
	public boolean getBooleanValue()
	{
		return ((Boolean)getValue()).booleanValue();
	}*/

	/**
	 *  Evaluate the expression.
	 *  @return	The value of the expression.
	 */
	protected Object	getValue(Map params)
	{
		Object ret = value;
		
		//System.out.println("gvc: "+getName()+" --- "
		//	+((IMExpression)getModelElement()).getEvaluationMode()+" "+getScope().getName());

		if(((IMExpression)getModelElement()).getEvaluationMode()
			.equals(IMExpression.MODE_DYNAMIC))
		{
			ret = evaluateTerm(params);
		}
		//System.out.println("GetValueCalled: "+getName()+" --- "+ret+" "
		//	+((IMExpression)getModelElement()).getEvaluationMode()+" "+getScope().getName());
		return ret;
	}

	/**
	 *  Refresh the cached expression value.
	 */
	public void	refresh()
	{
		// If dynamic we don't need to refresh as the expression is evaluated on any access.
		if(!IMExpression.MODE_DYNAMIC.equals(((IMExpression)getModelElement()).getEvaluationMode()))
		{
			this.value = evaluateTerm(null);
		}
	}
	
	/**
	 *  Test, if an expression is affected by a bdi event.
	 *  True, if the expression is affected.
	 */
	public boolean isAffected(SystemEvent event)
	{
		boolean	affected = false;

//		if(!(this instanceof RCondition) && getScope().getAgent().getName().indexOf("Trigger")!=-1 && event.getType().equals(SystemEvent.BINDING_EVENT))
//			System.out.println("debug64");
		
		// The event type must be relevant for the types specified
		// for any element or the source element (in the relevant coll).
		affected	= relevant_any.contains(event.getType());

		if(!affected && event.getSource() instanceof RElement)
		{
			affected	= relevant.getCollection(((RElement)event.getSource())
				.getModelElement()).contains(event.getType());
		}
		// Hack!!! Needed because of binding parameters (see BindingHelper)
		else if(!affected && event.getSource() instanceof IMElement)
		{
			affected	= relevant.getCollection(event.getSource()).contains(event.getType());
		}

		//if(((IMExpression)getModelElement()).getExpressionText().indexOf("$goal.checkResults()")!=-1)
		//	System.out.println("++++"+getName()+" "+event);

//		if(affected && getScope().getAgent().getName().indexOf("Trigger")!=-1)
//			System.out.println(this+" affected by "+event);

		return affected;
	}

	/**
	 *  Get the system event caused by the given system event.
	 *  Used to propagate changes of affected expressions.
	 *  @return The system event.
	 */
	public SystemEvent getSystemEvent(SystemEvent event)
	{
		SystemEvent	ret	= null;

//		if(systemevent.getType().equals(SystemEvent.BINDING_EVENT))
//			System.out.println("debug75");

		if(systemevent!=null && getOwner()!=event.getSource() && isAffected(event))
		{
			// Do not create new event (hack? for speed)
			ret	= systemevent;

			// Hack!!! For speed only read value when tools are registered.
			if(!getScope().listenerinfos.isEmpty())
			{
				try
				{
					systemevent.setValue(getValue());
				}
				catch(Exception e)
				{
					systemevent.setValue("n/a");
				}
			}
		}

		return ret;
	}

	/**
	 *  Create a string representation of this get.
	 *  @return a string representation of this get.
	 */
	public String	toString()
	{
		return getName()+" "+((IMExpression)getModelElement()).getExpressionText();
	}

	//-------- helper methods --------

    /**
     *  Evaluate the expression term.
     *  @param parameters The local parameters.
     *  @return The evaluated object.
     */
    protected Object evaluateTerm(Map parameters)
    {
    	IMExpression	mex	= (IMExpression)getModelElement();

		// Add local parameters (if any).
		if(parameters!=null)
		{
			parameters	= SCollection.createNestedMap(new Map[]{parameters, getExpressionParameters()});
		}
		else
		{
			parameters	= getExpressionParameters();
		}
		

		// Evaluate term.
		//Object	value	= mex.getTerm().getValue(getScope(), parameters);
		Object	value	= evaluateExpression(mex, parameters);

		//if(getModelElement().getName().indexOf("get")!=-1)
		//	System.out.println("Evaluating: "+getName()+" "+value);

		if(mex.getClazz()!=null)
		{
			// Perform maybe necessary conversions (e.g. between Long, Integer).
			value	= SReflect.convertWrappedValue(value, mex.getClazz());
		}
		return value;
    }

	//-------- expression parameters --------

	/**
	 *  Set an expression parameter.
	 *  @param name The parameter name.
	 *  @param value The parameter value.
	 */
	public void setParameter(String name, Object value)
	{
		setExpressionParameter(name, value);
	}

	/**
	 *  Get an expression parameter.
	 *  @param name The parameter name.
	 *  @return The parameter value.
	 */
	public Object	getParameter(String name)
	{
		return getExpressionParameter(name);
	}

	//-------- IQuery methods --------

	/**
	 *  Execute the get.
	 *  @return the result value of the get.
	 */
	public Object	execute()
	{
		return getValue();
	}

	/**
	 *  Execute the get using a local parameter.
	 *  @param name The name of the local parameter.
	 *  @param value The value of the local parameter.
	 *  @return the result value of the get.
	 */
	public Object	execute(String name, Object value)
	{
		Map	params	= SCollection.createHashMap();
		params.put(name, value);
		return getValue(params);
	}

	/**
	 *  Execute the query using local parameters.
	 *  @param names The names of parameters.
	 *  @param values The parameter values.
	 *  @return The return value.
	 */
	public Object	execute(String[] names, Object[] values)
	{
		Map	params	= SCollection.createHashMap();
		for(int i=0; i<names.length; i++)
			params.put(names[i], values[i]);
		return getValue(params);
	}

	/**
	 *  Save temporary state. Model deletion on cleanup.
	 *  @param temporary The temporary state.
	 */
	public void setTemporary(boolean temporary)
	{
		this.temporary = temporary;
	}

	/**
	 *  Test if temporary. Model deletion on cleanup.
	 *  @return True, if temporary.
	 */
	public boolean isTemporary()
	{
		return this.temporary;
	}
	
	//-------- static part --------
	
	/**
	 *  Evaluate an expression model using the given parameters.
	 */
	public static Object	evaluateExpression(IMExpression mex, Map params)
	{
		try
		{
			return mex.getTerm().getValue(params);
		}
		catch(Exception e)
		{
			//System.err.println("Error evaluating expression: "+mex.getExpressionText());
			//e.printStackTrace();
			throw new ExpressionEvaluationException("Error evaluating expression '"+mex.getExpressionText()+"' of element "+mex.getOwner()+".", e);
		}
	}
}

