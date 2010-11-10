package jadex.runtime.externalaccesswrapper;

import jadex.runtime.*;
import jadex.runtime.impl.*;


/**
 *  The user level view on an internal event.
 *  Methods can only be called from plans,
 *  otherwise exceptions are thrown.
 */
public class InternalEventWrapper	extends EventWrapper	implements IInternalEvent
{
	//-------- constructors --------

	/**
	 *  Create a new InternalEventWrapper.
	 */
	public InternalEventWrapper(IRInternalEvent event) // make protected
	{
		super(event);
	}
}
