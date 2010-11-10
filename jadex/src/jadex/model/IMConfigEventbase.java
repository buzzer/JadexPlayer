package jadex.model;

/**
 *  Configuration settings for the eventbase.
 */
public interface IMConfigEventbase extends IMConfigBase
{
	//-------- initial internal events --------

	/**
	 *  Get all known initial internal events.
	 *  @return The initial internal events.
	 */
	public IMConfigInternalEvent[] getInitialInternalEvents();

	/**
	 *  Create an initial internal event.
	 *  @param ref	The name of the referenced event.
	 *  @return The initial internal event.
	 */
	public IMConfigInternalEvent	createInitialInternalEvent(String ref);

	/**
	 *  Delete an initial internal event.
	 *  @param event	The initial internal event.
	 */
	public void	deleteInitialInternalEvent(IMConfigInternalEvent event);

	
	//-------- initial message events --------
	
	/**
	 *  Get all known initial message event.
	 *  @return The initial message events.
	 */
	public IMConfigMessageEvent[] getInitialMessageEvents();

	/**
	 *  Create an initial message event.
	 *  @param ref	The name of the referenced message event.
	 *  @return The initial message event.
	 */
	public IMConfigMessageEvent	createInitialMessageEvent(String ref);

	/**
	 *  Delete an initial message event.
	 *  @param event	The initial message event.
	 */
	public void	deleteInitialMessageEvent(IMConfigMessageEvent event);

	//-------- end internal events --------

	/**
	 *  Get all known end internal events.
	 *  @return The end internal events.
	 */
	public IMConfigInternalEvent[] getEndInternalEvents();

	/**
	 *  Create an end internal event.
	 *  @param ref	The name of the referenced event.
	 *  @return The end internal event.
	 */
	public IMConfigInternalEvent	createEndInternalEvent(String ref);

	/**
	 *  Delete an end internal event.
	 *  @param event	The end internal event.
	 */
	public void	deleteEndInternalEvent(IMConfigInternalEvent event);

	
	//-------- end message events --------
	
	/**
	 *  Get all known end message event.
	 *  @return The end message events.
	 */
	public IMConfigMessageEvent[] getEndMessageEvents();

	/**
	 *  Create an end message event.
	 *  @param ref	The name of the referenced message event.
	 *  @return The end message event.
	 */
	public IMConfigMessageEvent	createEndMessageEvent(String ref);

	/**
	 *  Delete an end message event.
	 *  @param event	The end message event.
	 */
	public void	deleteEndMessageEvent(IMConfigMessageEvent event);
}
