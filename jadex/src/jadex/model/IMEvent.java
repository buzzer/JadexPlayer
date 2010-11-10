package jadex.model;

/**
 *	Model element for an event.
 */
public interface IMEvent extends IMParameterElement
{
	//-------- event flags --------

	/**
	 *  Get the random selection flag.
	 *  @return The flag indicating if plans should be selected at random or by prominence.
	 */
	public boolean	isRandomSelection();

	/**
	 *  Set the random selection flag.
	 *  @param randomselection	The flag indicating if plans should be selected at random or by prominence.
	 */
	public void	setRandomSelection(boolean randomselection);


	/**
	 *  Get the post-to-all flag.
	 *  @return The flag indicating if all applicable plans should be executed at once.
	 */
	public boolean	isPostToAll();

	/**
	 *  Set the post-to-all flag.
	 *  @param posttoall	The flag indicating if all applicable plans should be executed at once.
	 */
	public void	setPostToAll(boolean posttoall);
}
