package jadex.model;

/**
 *  The event container.
 */
public interface IMEventbase extends IMBase
{
	//-------- constants --------

	/** The constant for defining the standard goal event. */
	public static final String STANDARD_GOAL_EVENT = "standard_goal_event";
	/** The constant for defining the standard goal event. */
	public static final String STANDARD_GOAL_EVENT_REFERENCE = "standard_goal_event_reference";

	/** The constant for defining the legacy internal event. todo: remove */
	public static final String LEGACY_INTERNAL_EVENT = "legacy_internal_event";
	/** The custom event type parameter for legacy internal events.
	 *  @deprecated Use explicitly defined custom types in ADF. */
	public static final String LEGACY_TYPE	= "legacy_type";
	/** The custom event content parameter for legacy internal events.
	 *  @deprecated Use explicitly defined custom types in ADF. */
	public static final String LEGACY_CONTENT	= "legacy_content";

	/** The timeout internal event type. */
	public static final String TYPE_TIMEOUT	= "InternalEvent_timeout";

	/** The condition satisfied internal event type. */
	public static final String TYPE_CONDITION_TRIGGERED	= "InternalEvent_condition_triggered";
	/** The condition satisfied internal event type. */
	public static final String TYPE_CONDITION_TRIGGERED_REFERENCE	= "InternalEvent_condition_triggered_reference";
	/** The condition parameter of condition triggered events. */
	public static final String CONDITION = "condition";
	/** The cause parameter of condition triggered events. */
	public static final String CAUSE = "cause";

	/** The execute plan event type. */
	// Only a marker for initial plan step, not used for internal reasoning
	public static final String TYPE_EXECUTEPLAN	= "InternalEvent_execute_plan";


	//-------- internal events --------

	/**
	 *  Get all known internal events.
	 *  @return The internal events.
	 */
	public IMInternalEvent[] getInternalEvents();

	/**
	 *  Get an internal event by name.
	 *  @param name The internal event name.
	 *  @return The internal event with that name (if any).
	 */
	public IMInternalEvent getInternalEvent(String name);

	/**
	 *  Create an internal event.
	 *  @param name	The name of the event.
	 *  @param exported	Flag indicating if this event may be referenced from outside capabilities.
	 *  @return The internal event.
	 */
	public IMInternalEvent	createInternalEvent(String name, String exported);

	/**
	 *  Delete an internal event.
	 *  @param event	The internal event.
	 */
	public void	deleteInternalEvent(IMInternalEvent event);


	//-------- internal event references --------

	/**
	 *  Get all known internal event references.
	 *  @return The internal event references.
	 */
	public IMInternalEventReference[] getInternalEventReferences();

	/**
	 *  Get an event by name.
	 *  @param name The internal event reference name.
	 *  @return The internal event reference with that name (if any).
	 */
	public IMInternalEventReference getInternalEventReference(String name);

	/**
	 *  Create an internal event reference.
	 *  @param name	The name of the event reference.
	 *  @param exported	Flag indicating if this event reference may be referenced from outside capabilities.
	 *  @param ref	The referenced event (or null for abstract).
	 *  @param req Is a reference required (only for abstract).
	 *  @return The internal event reference.
	 */
	public IMInternalEventReference	createInternalEventReference(String name, String exported, String ref, boolean req);

	/**
	 *  Delete an internal event reference.
	 *  @param event	The internal event reference.
	 */
	public void	deleteInternalEventReference(IMInternalEventReference event);

	
	//-------- message events --------
	
	/**
	 *  Get all known message event.
	 *  @return The message events.
	 */
	public IMMessageEvent[] getMessageEvents();

	/**
	 *  Get a message event by name.
	 *  @param name The message event name.
	 *  @return The message event with that name (if any).
	 */
	public IMMessageEvent getMessageEvent(String name);

	/**
	 *  Create an message event.
	 *  @param name	The name of the message event.
	 *  @param type	The type of the message event (e.g. "fipa").
	 *  @param direction	The direction of the message event (send/receive).
	 *  @param exported	Flag indicating if this event may be referenced from outside capabilities.
	 *  @return The message event.
	 */
	public IMMessageEvent	createMessageEvent(String name, String type, String direction, String exported);

	/**
	 *  Delete an message event.
	 *  @param event	The message event.
	 */
	public void	deleteMessageEvent(IMMessageEvent event);

	
	//-------- message event references --------
	
	/**
	 *  Get all known message event references.
	 *  @return The message event references.
	 */
	public IMMessageEventReference[] getMessageEventReferences();

	/**
	 *  Get a message event reference by name.
	 *  @param name The message event reference name.
	 *  @return The message event reference with that name (if any).
	 */
	public IMMessageEventReference getMessageEventReference(String name);

	/**
	 *  Create an message event reference.
	 *  @param name	The name of the event reference.
	 *  @param exported	Flag indicating if this event reference may be referenced from outside capabilities.
	 *  @param ref	The referenced event (or null for abstract).
	 *  @param req Is a reference required (only for abstract).
	 *  @return The message event reference.
	 */
	public IMMessageEventReference	createMessageEventReference(String name, String exported, String ref, boolean req);

	/**
	 *  Delete an message event reference.
	 *  @param event	The message event reference.
	 */
	public void	deleteMessageEventReference(IMMessageEventReference event);

	
	//-------- goal events --------

	/**
	 *  Get all known goal events.
	 *  @return The goal events.
	 */
	public IMGoalEvent[] getGoalEvents();

	/**
	 *  Get an event by name.
	 *  @param name The goal event name.
	 *  @return The goal event with that name (if any).
	 */
	public IMGoalEvent getGoalEvent(String name);


	//-------- goal event references --------

	/**
	 *  Get all known goal event references.
	 *  @return The goal event references.
	 */
	public IMGoalEventReference[] getGoalEventReferences();

	/**
	 *  Get a goal event reference by name.
	 *  @param name The goal event reference name.
	 *  @return The goal event reference with that name (if any).
	 */
	public IMGoalEventReference getGoalEventReference(String name);
}
