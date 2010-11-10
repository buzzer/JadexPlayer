package jadex.model.jibximpl;

import jadex.model.IMElementReference;
import jadex.model.IMGoalEventReference;

/**
 *  The reference for a goal event.
 */
public class MGoalEventReference extends MEventReference implements IMGoalEventReference
{
	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMGoalEventReference;
	}
}
