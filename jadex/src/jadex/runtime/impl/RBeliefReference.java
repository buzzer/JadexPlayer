package jadex.runtime.impl;

import jadex.model.*;
import jadex.runtime.*;

/**
 *  A belief implemented as a reference to another belief.
 */
public class RBeliefReference extends RElementReference	implements IRBelief
{
	//-------- constructor --------

	/**
	 *  Create a new belief.
	 *  @param belief The belief model element.
	 *  @param config The configuration.
	 *  @param owner The owner.
	 */
	protected RBeliefReference(IMBeliefReference belief, IMConfigBelief config,
		RElement owner,  RReferenceableElement creator)
	{
		super(belief.getName(), belief, config, owner, creator);
	}

	/**
	 *  Initialize the element.
	 */
	protected void	init()
	{
		if(isInited()) return;
		super.init();
		throwSystemEvent(SystemEvent.BELIEF_ADDED);
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
//		if(base.containsBelief(getName()))
//			base.deleteBelief(getName());
	}*/

	//-------- methods --------

	/**
	 *  Get the initial value.
	 *  Called from original element.
	 */
	protected Object	getInitialFact()
	{
		assert getConfiguration()!=null : this;

		// Use value from configuration if specified.
		Object	value	= null;
		IMExpression	mvalue	= ((IMConfigBelief)getConfiguration()).getInitialFact();

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
	 *  Set a fact of a belief.
	 *  @param fact The new fact.
	 */
	public void setFact(Object fact)
	{
		((IRBelief)getReferencedElement()).setFact(fact);
	}

	/**
	 *  Get the fact of a belief.
	 *  @return The fact.
	 */
	public Object	getFact()
	{
		return ((IRBelief)getReferencedElement()).getFact();
	}

	/**
	 *  Refresh the value of the belief.
	 */
	public void	refresh()
	{
		((IRBelief)getReferencedElement()).refresh();
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
	 *  Was the typed element modified by setting a value.
	 *  @return True, if modified.
	 */
	public boolean isModified()
	{
		return ((IRBelief)getReferencedElement()).isModified();
	}

	/**
	 *  Get the value class.
	 *  Shortcut for getModelElement().getClazz().
	 *  @return The value class.
	 */
	public Class	getClazz()
	{
		return ((IRBelief)getReferencedElement()).getClazz();
	}
}

