package jadex.runtime.impl;

import jadex.runtime.IFilter;

/**
 *  The interface for all message events (concrete and referenced).
 */
public interface IRMessageEvent extends IREvent
{
	/**
	 *  Get the native (platform specific) message object.
	 */
	public Object getMessage();

	/**
	 *  Get the message direction.
	 *  @return True, if message is incoming.
	 */
	//public boolean isIncoming();

	/**
	 *  Get the content.
	 *  Allowed content objects depend on the platform.
	 *  @return The content.
	 */
	public Object getContent();

	/**
	 *  Set the content.
	 *  Allowed content objects depend on the platform.
	 *  @param content The content.
	 */
	public void setContent(Object content);

	/**
	 *  Create a reply to this message event.
	 *  @param msgeventtype	The message event type.
	 *  @return The reply event.
	 */
	public IRMessageEvent	createReply(String msgeventtype);

	/**
	 *  Create a reply to this message event.
	 *  @param msgeventtype	The message event type.
	 *  @param content	The message content.
	 *  @return The reply event.
	 */
	public IRMessageEvent	createReply(String msgeventtype, Object content);

	/**
	 *  Test, if this message is a reply to another message.
	 *  @return True, if it is a reply.
	 */
	public boolean isReply();

	/**
	 *  Get the original message (if this is a reply).
	 */
	public IRMessageEvent getInReplyMessageEvent();

	/**
	 *  Set the original message (if this is a reply).
	 */
	public void setInReplyMessageEvent(IRMessageEvent event);

	/**
	 *  Set the native (platform specific) message object.
	 *  @param message	The message object.
	 */
	public void setMessage(Object message);

	/**
	 *  Get a filter for matching the reply of a message.
	 *  @return The reply filter.
	 */
	public IFilter getFilter();
	
	/**
	 *  Get the unique message id (if any).
	 *  @return the ID of this message
	 */
	public String getId();
	
	/**
	 *  Set the unique message id.
	 *  @param id The message id.
	 */
	public void setId(String id);
}
