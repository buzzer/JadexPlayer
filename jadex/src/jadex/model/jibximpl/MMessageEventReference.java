package jadex.model.jibximpl;

import jadex.model.IMElementReference;
import jadex.model.IMMessageEventReference;

/**
 *  The reference for a message event.
 */
public class MMessageEventReference extends MEventReference implements IMMessageEventReference
{
	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMMessageEventReference;
	}
}
