package jadex.model.jibximpl;

import jadex.model.IMElementReference;
import jadex.model.IMPerformGoalReference;

/**
 *  The reference for a perform goal.
 */
public class MPerformGoalReference extends MGoalReference implements IMPerformGoalReference
{
	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMPerformGoalReference;
	}
}
