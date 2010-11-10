package jadex.runtime;

/**
 *  A listener interested in system events.
 */
public interface ISystemEventListener
{
	/**
	 *  Notify that system events occured.
	 *  @see SystemEvent
	 */
	public void systemEventsOccurred(SystemEvent[] events);
}
