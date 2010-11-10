package jadex.runtime.impl;

import jadex.model.*;

/**
 *  A reference to a perform goal (reference).
 */
public class RPerformGoalReference extends RGoalReference
{
	//-------- constructor --------

	/**
	 *  Create a new goal.
	 *  @param name The name.
	 *  @param goal The goal model element.
	 *  @param owner The owner.
	 */
	protected RPerformGoalReference(String name, IMGoalReference goal,
			IMConfigGoal state, RElement owner, RReferenceableElement creator)
	{
		super(name, goal, state, owner, creator);
	}
}
