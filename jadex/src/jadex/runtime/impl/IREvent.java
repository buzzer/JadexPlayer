package jadex.runtime.impl;

/**
 *  Internal interface for unified access to REvent and REventReference.
 */
public interface IREvent	extends IRParameterElement
{
	//-------- BDI event properties --------

	/**
	 *  Is it a post-to-all event.
	 *  @return True, if post-to-all is set.
	 */
	public abstract boolean isPostToAll();

	/**
	 *  Get the random selection flag.
	 *  @return True, when applicable
	 *  selection is random style.
	 */
	public abstract boolean	isRandomSelection();

	/**
	 *  Get the recalculation flag.
	 *  @return True, if the applicable candidates
	 *  list should be recalculated each time the
	 *  event is handled.
	 * /
	public abstract boolean	isRecalculating();*/ //todo


	//-------- event methods ---------

	/**
	 *  Get the event type.
	 *  @return The event type.
	 */
	public String	getType();

	/**
	 *  Called when the event is dispatched.
	 */
	public void	dispatched();

	/**
	 *  Get (or create) the apl for the event.
	 *  @return The apl.
	 */
	public ApplicableCandidateList getApplicableCandidatesList();
}
