package jadex.model.jibximpl;

import jadex.model.IMElementReference;
import jadex.model.IMMetaGoalReference;

/**
 *  The reference for a meta goal.
 */
public class MMetaGoalReference extends MGoalReference implements IMMetaGoalReference
{
	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMMetaGoalReference;
	}
}
