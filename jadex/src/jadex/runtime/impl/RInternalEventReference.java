package jadex.runtime.impl;

import jadex.model.*;

/**
 *  The reference to an event.
 */
public class RInternalEventReference extends REventReference implements IRInternalEvent
{
	//-------- constructor --------

	/**
	 *  Create a new event.
	 *  @param name The name.
	 *  @param event The event model element.
	 *  @param owner The owner.
	 */
	protected RInternalEventReference(String name, IMEventReference event,
			IMConfigInternalEvent state, RElement owner, RReferenceableElement creator)
	{
		super(name, event, state, owner, creator);
	}

	//-------- methods --------

	// This is a complete HACK!!!
	/** The saved condition. */
	protected IRCondition cond;

	/**
	 *  HACK! Save condition or condition reference.
	 *  Cannot be done with parameter, because these are forwarded
	 *  to the original event :-(
	 */
	public void setCondition(IRCondition triggeredcond)
	{
		this.cond = triggeredcond;
	}

	/**
	 *  HACK! Save condition or condition reference.
	 *  Cannot be done with parameter, because these are forwarded
	 *  to the original event :-(
	 */
	public IRCondition getCondition()
	{
		return this.cond;
	}

	/**
	 *  HACK! Save condition or condition reference.
	 *  Cannot be done with parameter, because these are forwarded
	 *  to the original event :-(
	 * /
	public String getConditionName()
	{
		return this.cond.getName();
	}*/

	

}
