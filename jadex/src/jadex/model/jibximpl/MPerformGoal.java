package jadex.model.jibximpl;

import jadex.model.IMElementReference;
import jadex.model.IMPerformGoal;
import jadex.model.IMPerformGoalReference;

/**
 *  The perform goal type.
 */
public class MPerformGoal extends MGoal implements IMPerformGoal
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
