package jadex.model.jibximpl;

import jadex.model.IMElementReference;
import jadex.model.IMQueryGoalReference;

/**
 *  The reference for query goals.
 */
public class MQueryGoalReference extends MGoalReference implements IMQueryGoalReference
{
	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMQueryGoalReference;
	}
}
