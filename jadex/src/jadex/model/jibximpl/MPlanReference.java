package jadex.model.jibximpl;

import jadex.model.IMElementReference;
import jadex.model.IMPlanReference;

/**
 *  The plan reference.
 */
public class MPlanReference extends MElementReference implements IMPlanReference
{
	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMPlanReference;
	}
}
