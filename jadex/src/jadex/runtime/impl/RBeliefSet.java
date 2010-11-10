package jadex.runtime.impl;

import jadex.model.*;
import jadex.util.*;
import jadex.util.collection.SCollection;
import jadex.runtime.*;
import java.util.*;


/**
 *  A belief set instance stores runtime
 *  information about a belief.
 */
public class RBeliefSet extends RTypedElementSet	implements IRBeliefSet
{
	//-------- constructors --------

	/**
	 *  Create a new beliefset.
	 *  To evaluate the initial facts, init() has to be called.
	 *  @param belief The belief.
	 *  @param owner The owner.
	 */
	protected RBeliefSet(IMBeliefSet belief, IMConfigBeliefSet state, RElement owner, 
		RReferenceableElement creator)
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
		if(isInited()) return;
		// Hack!!! init() will throw fact_added events, so throw belief_added first.
		throwSystemEvent(SystemEvent.BELIEF_ADDED);
		super.init();
	}

	//-------- initialization methods --------

	/**
	 *  Set the initial values.
	 *  May be default values (from model),
	 *  or initiual values provided on creation.
	 */
	protected void	setInitialValues()
	{
		boolean found = false;

		// Use value from outer most element (if any).
		List occs = SCollection.createArrayList();
		occs.addAll(getAllOccurrences());
		occs.remove(this);
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
			RBeliefSetReference ref = (RBeliefSetReference)occs.get(i);

			if(ref.getConfiguration()!=null)
			{
				found	= true;
				// Check for separate initial values.
				Object[]	values	= ref.getInitialFacts();
				for(int j=0; values!=null && j<values.length; j++)
				{
					addValue(values[j]);
				}
				// Check for initial values expression.
				Object	inivals	= ref.getInitialFactsExpression();
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
			// Use values from configuration if specified.
			IMExpression[]	mvalues	= null;
			IMExpression	minivals	= null;
			if(getConfiguration()!=null)
			{
				mvalues	= ((IMConfigBeliefSet)getConfiguration()).getInitialFacts();
				minivals	= ((IMConfigBeliefSet)getConfiguration()).getInitialFactsExpression();
			}
			// Otherwise use default values from model.
			else
			{
				mvalues	= ((IMBeliefSet)getModelElement()).getDefaultFacts();
				minivals	= ((IMBeliefSet)getModelElement()).getDefaultFactsExpression();
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
						.createInternalExpression(minivals, this, createSystemEvent(SystemEvent.BSFACTS_CHANGED, null, -1)));
				}
			}
		}
	}

	//-------- methods --------

	/**
	 *  Add a fact to a belief.
	 *  @param fact The new fact.
	 */
	public void addFact(Object fact)
	{      
		addValue(fact);
	}

	/**
	 *  Remove a fact to a belief.
	 *  @param fact The new fact.
	 */
	public void removeFact(Object fact)
	{
		removeValue(fact);
	}

	/**
	 *  Add facts to a parameter set.
	 */
	public void addFacts(Object[] values)
	{
		addValues(values);
	}

	/**
	 *  Remove all facts from a belief.
	 */
	public void removeFacts()
	{
		removeValues();
	}

	/**
	 *  Get a value equal to the given object.
	 *  @param oldval The old value.
	 */
	public Object	getFact(Object oldval)
	{   
		Object value=getValue(oldval);
		// info event
		throwSystemEvent(new SystemEvent(SystemEvent.BSFACT_READ, this, value, indexOf(oldval)));
		return value;
	}

	/**
	 *  Test if a fact is contained in a belief.
	 *  @param fact The fact to test.
	 *  @return True, if fact is contained.
	 */
	public boolean containsFact(Object fact)
	{
		// info event
		throwSystemEvent(new SystemEvent(SystemEvent.BSFACT_READ, this, fact, indexOf(fact)));
		return containsValue(fact);
	}

	/**
	 *  Get the facts of a beliefset.
	 *  @return The facts.
	 */
	public Object[]	getFacts()
	{
		Object[] facts=getValues();
		// info event
		throwSystemEvent(new SystemEvent(SystemEvent.BSFACT_READ, this, facts));
		return facts;
	}

	/**
	 *  Update a fact to a new fact. Searches the old
	 *  value with equals, removes it and stores the new fact.
	 *  @param newfact The new fact.
	 */
	public void updateFact(Object newfact)
	{  
		updateValue(newfact);
	}

	/**
	 *  Update or add a fact. When the fact is already
	 *  contained it will be updated to the new fact.
	 *  Otherwise the value will be added.
	 *  @param fact The new or changed fact.
	 * /
	public void updateOrAddFact(Object fact)
	{
		updateOrAddValue(fact);
	}*/

	/**
	 *  Replace a fact with another one.
	 *  @param oldfact The old fact.
	 *  @param newfact The new fact.
	 * /
	public void replaceFact(Object oldfact, Object newfact)
	{
		replaceValue(oldfact, newfact);
	}*/

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
//		sb.append(", facts=");
//		sb.append(SUtil.arrayToString(getValues()));
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
		rep.put("exported", ((IMBeliefSet)getModelElement()).getExported());
		return rep;
	}

	//-------- helper methods --------

	/**
	 *  Generate a change event for this element
	 *  using the current representation.
	 *  @param event	The event.
	 */
	public SystemEvent 	createSystemEvent(String event, Object value, int index)
	{
		// adapt event type
		if(event.equals(SystemEvent.ESVALUE_ADDED))
			event=SystemEvent.BSFACT_ADDED;
		else if(event.equals(SystemEvent.ESVALUE_REMOVED))
			event=SystemEvent.BSFACT_REMOVED;
		else if(event.equals(SystemEvent.ESVALUE_CHANGED))
			event=SystemEvent.BSFACT_CHANGED;
		else if(event.equals(SystemEvent.ESVALUES_CHANGED))
			event=SystemEvent.BSFACTS_CHANGED;

		return super.createSystemEvent(event, value, index);
	}
  
	/**
	 *  @param event
	 *  @see jadex.runtime.impl.RReferenceableElement#throwSystemEvent(jadex.runtime.SystemEvent)
	 */
	public void throwSystemEvent(SystemEvent event)
	{
		RPlan plan=getScope().getAgent().getCurrentPlan();
		if (plan!=null) event.setCause(plan.name);
		super.throwSystemEvent(event);
	}
}

