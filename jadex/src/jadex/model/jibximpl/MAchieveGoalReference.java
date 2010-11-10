package jadex.model.jibximpl;

import jadex.model.IMAchieveGoalReference;
import jadex.model.IMElementReference;

/**
 *  The reference for an achieve goal.
 */
public class MAchieveGoalReference extends MGoalReference implements IMAchieveGoalReference
{
	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMAchieveGoalReference;
	}
}
