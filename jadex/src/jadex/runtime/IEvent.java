package jadex.runtime;

/**
 *  The interface for all events (concrete and referenced).
 */
public interface IEvent extends IParameterElement
{
	//-------- BDI event properties --------

	/**
	 *  Is it a post-to-all event.
	 *  @return True, if post-to-all is set.
	 */
	public boolean isPostToAll();

	/**
	 *  Get the random selection flag.
	 *  @return True, when applicable
	 *  selection is random style.
	 */
	public boolean	isRandomSelection();

	//-------- methods ---------

	/**
	 *  Get the event type.
	 *  @return The event type.
	 */
	public String	getType();
}
