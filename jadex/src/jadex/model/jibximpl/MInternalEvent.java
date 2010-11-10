package jadex.model.jibximpl;

import jadex.model.IMElementReference;
import jadex.model.IMInternalEvent;
import jadex.model.IMInternalEventReference;

/**
 *  The internal event type.
 */
public class MInternalEvent extends MEvent implements IMInternalEvent
{
	//-------- constructors --------

	/**
	 *  Called when the element is setup.
	 */
	protected void init()
	{
		super.init();

		if(posttoall==null)
			setPostToAll(true);
	}

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
