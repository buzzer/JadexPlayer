package jadex.model.jibximpl;

import jadex.model.IMElementReference;
import jadex.model.IMMaintainGoalReference;

/**
 *  The reference for a maintain goal type.
 */
public class MMaintainGoalReference extends MGoalReference implements IMMaintainGoalReference
{
	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMMaintainGoalReference;
	}
}
