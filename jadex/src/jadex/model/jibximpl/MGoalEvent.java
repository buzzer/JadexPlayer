package jadex.model.jibximpl;

import jadex.model.IMElementReference;
import jadex.model.IMGoalEvent;
import jadex.model.IMGoalEventReference;

/**
 *  the goal event type.
 */
public class MGoalEvent extends MEvent implements IMGoalEvent
{
	//-------- constructors --------

	/**
	 *  Called when the element is setup.
	 */
	protected void init()
	{
		super.init();

		if(posttoall==null)
			setPostToAll(false);
	}

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
