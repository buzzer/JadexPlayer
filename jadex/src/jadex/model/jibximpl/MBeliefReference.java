package jadex.model.jibximpl;

import jadex.model.IMBeliefReference;
import jadex.model.IMElementReference;

/**
 *  A reference to a belief element.
 */
public class MBeliefReference extends MTypedElementReference implements IMBeliefReference
{
	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMBeliefReference;
	}
}
