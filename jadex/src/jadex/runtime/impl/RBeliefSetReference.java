package jadex.runtime.impl;

import jadex.model.*;
import jadex.runtime.*;

/**
 *  A beliefset implemented as reference to another beliefset.
 */
public class RBeliefSetReference extends RElementReference	implements IRBeliefSet
{
	//-------- constructor --------

	/**
	 *  Create a new belief.
	 *  @param beliefset The belief model element.
	 *  @param config The configuration.
	 *  @param owner The owner.
	 */
	protected RBeliefSetReference(IMBeliefSetReference beliefset, IMConfigBeliefSet config,
		RElement owner, RReferenceableElement creator)
	{
		super(beliefset.getName(), beliefset, config, owner, creator);
	}

	/**
	 *  Initialize the element.
	 */
	protected void	init()
	{
		if(isInited()) return;
		super.init();
		throwSystemEvent(SystemEvent.BELIEF_ADDED);

		// Hack!!! Throw value added events for values of original element.
		Object[]	facts	= getFacts();
		for(int i=0; i<facts.length; i++)
		{
			throwSystemEvent(new SystemEvent(SystemEvent.BSFACT_ADDED, this, facts[i], i));
		}
	}

	/**
	 *  Perform any necessary clean up, when this element is
	 *  no longer used.
	 *  Subclasses may override this method to do their
	 *  cleanup operations. They must call super.cleanup
	 *  to ensure that the cleanedup property is set.
	 * /
	public void	cleanup()
	{
		if(cleanedup)
			return;

		super.cleanup();

		// Hack!!! May be removed already, when not deleting on cascade.
//		RBeliefbase	base	= (RBeliefbase)getOwner();
//		if(base.containsBeliefSet(getName()))
//			base.deleteBeliefSet(getName());
	}*/

	//-------- methods --------

	/**
	 *  Add a fact to a belief.
	 *  @param fact The new fact.
	 */
	public void addFact(Object fact)
	{
		((IRBeliefSet)getReferencedElement()).addFact(fact);
	}

	/**
	 *  Remove a fact to a belief.
	 *  @param fact The new fact.
	 */
	public void removeFact(Object fact)
	{
		((IRBeliefSet)getReferencedElement()).removeFact(fact);
	}

	/**
	 *  Add facts to a parameter set.
	 */
	public void addFacts(Object[] values)
	{
		((IRBeliefSet)getReferencedElement()).addFacts(values);
	}

	/**
	 *  Remove all facts from a belief.
	 */
	public void removeFacts()
	{
		((IRBeliefSet)getReferencedElement()).removeFacts();
	}

	/**
	 *  Test if a fact is contained in a belief.
	 *  @param fact The fact to test.
	 *  @return True, if fact is contained.
	 */
	public boolean containsFact(Object fact)
	{
		return ((IRBeliefSet)getReferencedElement()).containsFact(fact);
	}

	/**
	 *  Get the facts of a beliefset.
	 *  @return The facts.
	 */
	public Object[]	getFacts()
	{
		return ((IRBeliefSet)getReferencedElement()).getFacts();
	}

	/**
	 *  Update a fact to a new fact. Searches the old
	 *  value with equals, removes it and stores the new fact.
	 *  @param newfact The new fact.
	 */
	public void updateFact(Object newfact)
	{
		((IRBeliefSet)getReferencedElement()).updateFact(newfact);
	}

	/**
	 *  Update or add a fact. When the fact is already
	 *  contained it will be updated to the new fact.
	 *  Otherwise the value will be added.
	 *  @param fact The new or changed fact.
	 * /
	public void updateOrAddFact(Object fact)
	{
		((IRBeliefSet)getReferencedElement()).updateOrAddFact(fact);
	}*/

	/**
	 *  Replace a fact with another one.
	 *  @param oldfact The old fact.
	 *  @param newfact The new fact.
	 * /
	public void replaceFact(Object oldfact, Object newfact)
	{
		((IRBeliefSet)getReferencedElement()).replaceFact(oldfact, newfact);
	}*/

	/**
	 *  Get a value equal to the given object.
	 *  @param oldval The old value.
	 */
	public Object	getFact(Object oldval)
	{
		return ((IRBeliefSet)getReferencedElement()).getFact(oldval);
	}

	/**
	 *  Get the number of values currently
	 *  contained in this set.
	 *  @return The values count.
	 */
	public int size()
	{
		return ((IRBeliefSet)getReferencedElement()).size();
	}

	/**
	 *  Is this belief accessible.
	 *  @return False, if the belief cannot be accessed.
	 */
	public boolean isAccessible()
	{
		return isBound();
	}

	/**
	 *  Get the initial values (if any).
	 *  Called from original element.
	 */
	protected Object[]	getInitialFacts()
	{
		assert getConfiguration()!=null : this;

		// Use value from configuration if specified.
		Object[]	values	= null;
		IMExpression[]	mvalues	= ((IMConfigBeliefSet)getConfiguration()).getInitialFacts();

		// Create initial values from multiple expressions.
		if(mvalues!=null)
		{
			values	= new Object[mvalues.length];
			for(int i=0; i<mvalues.length; i++)
			{
				values[i] = getScope().getExpressionbase().evaluateInternalExpression(mvalues[i], this);
			}
		}

		return values;
	}

	/**
	 *  Get the initial values expression (if any).
	 *  Called from original element.
	 */
	protected Object	getInitialFactsExpression()
	{
		assert getConfiguration()!=null : this;

		// Use value from configuration if specified.
		Object	value	= null;
		IMExpression	minivals	= ((IMConfigBeliefSet)getConfiguration()).getInitialFactsExpression();

		// Create initial values from <values> expression.
		if(minivals!=null)
		{
			if(minivals.getEvaluationMode().equals(IMExpression.MODE_STATIC))
			{
				value	= getScope().getExpressionbase().evaluateInternalExpression(minivals, this);
			}
			else
			{
				value	= getScope().getExpressionbase().createInternalExpression(minivals, this, new SystemEvent(SystemEvent.BSFACTS_CHANGED, this));
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
		return ((IRBeliefSet)getReferencedElement()).isModified();
	}

	/**
	 *  Get the value class.
	 *  Shortcut for getModelElement().getClazz().
	 *  @return The value class.
	 */
	public Class	getClazz()
	{
		return ((IRBeliefSet)getReferencedElement()).getClazz();
	}

	/**
	 *  Internal method to get the inivals expression.
	 */
	public IRExpression internalGetInivals()
	{
		return ((IRBeliefSet)getReferencedElement()).internalGetInivals();
	}
}

