package jadex.model.jibximpl;

import jadex.model.IMBeliefSetReference;
import jadex.model.IMElementReference;

/**
 *  The reference for a belief set.
 */
public class MBeliefSetReference extends MTypedElementSetReference implements IMBeliefSetReference
{
	//-------- constructors --------
	
	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMBeliefSetReference;
	}
}
