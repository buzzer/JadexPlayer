package jadex.util.concurrent;

/**
 *  Interface for the timer that manages future timepoints and has the
 *  task to notify objects at the given time entries.
 */
public interface ITimerService
{
	/**
	 *  Add a timing entry.
	 *  When an entry for the given object already exists,
	 *  the old entry will be discarded.
	 *  @param to The timed object to notify.
	 *  @param time	The absolute notification time.
	 */
	public void	addEntry(ITimedObject to, long time);
	
	/**
	 *  Remove a timing entry.
	 *  @param to The object to notify.
	 */
	public void	removeEntry(ITimedObject to);

	/**
	 *  Shutdown the timer service.
	 *  Drops outstanding notification requests.
	 */
	public void shutdown();
}
