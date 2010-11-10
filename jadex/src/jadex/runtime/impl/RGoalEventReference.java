package jadex.runtime.impl;

import jadex.model.*;

/**
 *  The reference to an event.
 */
public class RGoalEventReference extends REventReference implements IRGoalEvent
{
	//-------- attributes --------

	/** The goal reference. */
	protected RGoalReference goalref;

	//-------- constructor --------

	/**
	 *  Create a new event.
	 *  @param name The name.
	 *  @param event The event model element.
	 *  @param owner The owner.
	 */
	protected RGoalEventReference(String name, IMEventReference event,
			RElement owner, RReferenceableElement creator, RGoalReference goalref)
	{
		super(name, event, null, owner, creator);
		this.goalref = goalref;
	}

	//-------- methods --------

	/**
	 *  Is the event a info (result) event.
	 *  @return True, if it is an info event.
	 */
	public boolean isInfo()
	{
		return ((IRGoalEvent)getReferencedElement()).isInfo();
	}

	/**
	 *  Get the goal.
	 *  @return The goal.
	 */
	public IRGoal getGoal()
	{
		return goalref;
	}

}

