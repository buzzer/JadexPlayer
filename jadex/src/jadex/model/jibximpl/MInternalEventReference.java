package jadex.model.jibximpl;

import jadex.model.IMElementReference;
import jadex.model.IMInternalEventReference;

/**
 *  The reference for an internal event.
 */
public class MInternalEventReference extends MEventReference implements IMInternalEventReference
{
	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMInternalEventReference;
	}
}