package jadex.runtime;

/**
 *  The info objects for plan candidates.
 */
public interface ICandidateInfo
{
	/**
	 *  Get the plan instance.
	 *  @return	The plan instance.
	 */
	public IPlan getPlan();

	/**
	 *  Get the associated event.
	 *  @return	The associated event.
	 */
	public IEvent	getEvent();

}
