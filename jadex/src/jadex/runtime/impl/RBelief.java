package jadex.runtime.impl;

import jadex.model.*;
import jadex.util.*;
import jadex.util.collection.SCollection;
import jadex.runtime.*;
import java.util.*;


/**
 *  A belief instance stores runtime
 *  information about a belief.
 */
public class RBelief extends RTypedElement	implements IRBelief
{
	//-------- constructor --------

	/**
	 *  Create a new belief.
	 *  To evaluate the initial facts, init() has to be called.
	 *  @param belief The belief.
	 *  @param owner The owner.
	 */
	protected RBelief(IMBelief belief, IMConfigBelief state, RElement owner, RReferenceableElement creator)
	{
		// Belief instances have the same name as their model
		// elements, because there is only one instance per model element.
		super(belief.getName(), belief, state, owner, creator);
	}

	/**
	 *  Initialize the initial values.
	 */
	protected void	init()
	{
		// Hack!!! May have been called already from another element's init.
		if(!isInited())
		{
			throwSystemEvent(SystemEvent.BELIEF_ADDED);	// Hack!!! has to be done before init() throws fact_chamged events.
			super.init();
		}
	}

	//-------- initialization methods --------

	/**
	 *  Set the initial value.
	 *  May be a default value (from model),
	 *  or an initiual value provided on creation.
	 */
	public void	setInitialValue()
	{
		boolean found = false;

		// Use argument value (if any) for direct belief.
		String argname = null;
		IMBelief mbel = (IMBelief)getModelElement();
		//if(mbel.getName().indexOf("arg")!=-1)
		//	System.out.println("hip");
		if(getScope()==getScope().getAgent() 
			&& mbel.getExported().equals(IMTypedElement.EXPORTED_TRUE))
		{
			argname = mbel.getName();
		}
		List occs = SCollection.createArrayList();
		occs.addAll(getAllOccurrences());
		occs.remove(this);
		if(argname==null)
		{
			// Use argument value (if any) if exists exported agent belief reference
			for(int i=0; i<occs.size(); i++)
			{
				RBeliefReference tmp = (RBeliefReference)occs.get(i);
				if(tmp.getScope().equals(getScope().getAgent())
					&& ((IMBeliefReference)tmp.getModelElement()).getExported().equals(IMTypedElement.EXPORTED_TRUE))
				{
					argname = tmp.getName();
				}
			}
		}
		if(argname!=null)
		{
			Map args = getScope().getAgent().getArguments();
			if(args!=null && args.containsKey(argname))
			{
				setInitialValue(args.get(argname));
				found = true;
			}
		}
		
		// Use value from outer most element (if any).
		if(!found)
		{
			Collections.sort(occs, new Comparator()
			{
				public int compare(Object o, Object o1)
				{
					return ((RReferenceableElement)o1).getScope().getNestingLevel() -
					((RReferenceableElement)o).getScope().getNestingLevel();
				}
			});
	
			//System.out.println("Found: "+occs);
			for(int i=0; i<occs.size(); i++)
			{
				RBeliefReference ref = (RBeliefReference)occs.get(i);
	
				if(ref.getConfiguration()!=null)
				{
					found = true;
					Object	fact	= ref.getInitialFact();
					if(fact instanceof IRExpression)
					{
						IMExpressionReference mref = ((IMCapability)getScope().getModelElement())
							.getExpressionbase().getExpressionReference(IMExpressionbase.STANDARD_EXPRESSION_REFERENCE);
						fact = getScope().getExpressionbase().createExpression(mref, (RReferenceableElement)fact); // todo: use internalCreate?
					}
					setInitialValue(fact);
				}
			}
		}

		if(!found)
		{
			// Use value from configuration if specified.
			IMExpression	mvalue	= null;
			if(getConfiguration()!=null)
			{
				mvalue	= ((IMConfigBelief)getConfiguration()).getInitialFact();
			}
			// Otherwise use default value from model.
			else
			{
				mvalue	= ((IMBelief)getModelElement()).getDefaultFact();
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
						mvalue, this, new SystemEvent(SystemEvent.FACT_CHANGED, this)));
				}
			}
		}
	}
	
	//-------- methods --------

	/**
	 *  Set a fact of a belief.
	 *  @param fact The new fact.
	 */
	public void setFact(Object fact)
	{
		setValue(fact);
	}

	/**
	 *  Get the fact of a belief.
	 *  @return The fact.
	 */
	public Object	getFact()
	{
		Object fact = getValue();
		// info event
		throwSystemEvent(new SystemEvent(SystemEvent.FACT_READ, this, fact));
		return fact;
	}

	/**
	 *  Is this belief accessible.
	 *  @return False, if the belief cannot be accessed.
	 */
	public boolean isAccessible()
	{
		return true;
	}

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public String toString()
	{
		StringBuffer	sb	= new StringBuffer();
		sb.append(SReflect.getInnerClassName(this.getClass()));
		sb.append("(name=");
		sb.append(getName());
		//sb.append(", fact=");
		//sb.append(value);
		sb.append(")");
		return sb.toString();
	}

	/**
	 *  Generate a property representation for encoding this element
	 *  (eg to SL).
	 *  Method will be overridden by subclasses. When the method
	 *  is invoked it newly fetches several proporties.
	 *  @return A properties object representing this element.
	 */
	public Map getEncodableRepresentation()
	{
		Map rep = super.getEncodableRepresentation();
		// Set static representation values.
		// Use model name, as there is only one instance per mbelief.
		//representation.put("name", belief.getName());
		rep.put("exported", ((IMBelief)getModelElement()).getExported());
		return rep;
	}

	//-------- helper methods --------

	/**
	 *  Generate a change event for this element
	 *  using the current representation.
	 *  @param event	The event type.
	 */
	public SystemEvent 	createSystemEvent(String event)
	{
		// adapt event type
		if(SystemEvent.VALUE_CHANGED.equals(event))
			event = SystemEvent.FACT_CHANGED;

		return super.createSystemEvent(event);
	}
  
	/**
	 * @param event
	 * @see jadex.runtime.impl.RReferenceableElement#throwSystemEvent(jadex.runtime.SystemEvent)
	 */
	public void throwSystemEvent(SystemEvent event)
	{
		RPlan plan=getScope().getAgent().getCurrentPlan();
		if (plan!=null) event.setCause(plan.name);
		super.throwSystemEvent(event);
	}
}

