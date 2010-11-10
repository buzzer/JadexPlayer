package jadex.runtime.impl;

import jadex.model.*;

/**
 *  A reference to a get goal (reference).
 */
public class RMetaGoalReference extends RQueryGoalReference
{
	//-------- constructor --------

	/**
	 *  Create a new goal.
	 *  @param name The name.
	 *  @param goal The goal model element.
	 *  @param owner The owner.
	 */
	protected RMetaGoalReference(String name, IMGoalReference goal,
			IMConfigGoal state, RElement owner, RReferenceableElement creator)
	{
		super(name, goal, state, owner, creator);
	}
}
