package jadex.adapter;

import jadex.adapter.fipa.AgentIdentifier;

/**
 *  The adapter for a specific platform agent (e.g. a Jade agent).
 *  These are the methods a Jadex agents needs to call on its host agent.
 */
public interface IAgentAdapter
{
	/**
	 *  Called by the agent when it probably awoke from an idle state.
	 *  The platform has to make sure that the agent will be executed
	 *  again from now on.
	 *  Note, this method can be called also from external threads
	 *  (e.g. property changes). Therefore, on the calling thread
	 *  no agent related actions must be executed (use some kind
	 *  of wake-up mechanism).
	 *  Also proper synchronization has to be made sure, as this method
	 *  can be called concurrently from different threads.
	 */
	public void	wakeup();
	
	/**
	 *  Notify the agent in a time interval.
	 *  This method is called by the jadex agent to realize internal timing
	 *  issues. The platform only has to consider the last call
	 *  of this method, and may forget about earlier notification requests.
	 *  The method should return immediately, after saving the notification
	 *  request. When the notification is due, the platform has to call the
	 *  {@link IJadexAgent#notifyDue()} method.
	 *  @param millis The relative time in millis (-1 if the agent no longer whishes to be notified)..
	 */
	public void notifyIn(long millis);

	/**
	 *  This is called, after the agent has decided to kill itself.
	 *  All platform-specific resources/entries regarding the agent
	 *  should now be removed.
	 */
	public void cleanupAgent();

	/**
	 *  Used to determine the platform type.
	 *  Each adapter implementation should provide the value of a constant
	 *  defined somewhere.
	 *  Using the platform type, e.g. plan context conditions can check
	 *  if plans are applicable in current context. 
	 */
	public String getPlatformType();

	/**
	 *  Return the native agent-identifier that allows to send
	 *  messages to this agent.
	 */
	public AgentIdentifier getAgentIdentifier();

	/**
	 *  Get all avaiable transports available on the native platform.
	 *  @return All transports.
	 *  // todo: remove
	 */
	public IMessageEventTransport[] getMessageEventTransports();
}
