package jadex.runtime.planwrapper;

import jadex.runtime.*;
import jadex.runtime.impl.*;


/**
 *  The user level view on an event.
 *  Methods can only be called from plans,
 *  otherwise exceptions are thrown.
 */
public class EventWrapper extends ParameterElementWrapper implements IEvent
{
	//-------- attributes --------

	/** The original event. */
	protected IREvent event;

	//-------- constructors --------

	/**
	 *  Create a new goalbase wrapper.
	 */
	protected EventWrapper(IREvent event)
	{
		super(event);
		this.event = event;
	}

	//-------- BDI event properties --------

	/**
	 *  Is it a post-to-all event.
	 *  @return True, if post-to-all is set.
	 */
	public boolean isPostToAll()
	{
		checkThreadAccess();
		return event.isPostToAll();
	}

	/**
	 *  Get the random selection flag.
	 *  @return True, when applicable
	 *  selection is random style.
	 */
	public boolean	isRandomSelection()
	{
		checkThreadAccess();
		return event.isRandomSelection();
	}

	//-------- methods ---------

	/**
	 *  Get the event type.
	 *  @return The event type.
	 */
	public String	getType()
	{
		checkThreadAccess();
		return event.getType();
	}
}
