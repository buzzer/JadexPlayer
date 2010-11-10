package jadex.adapter.standalone;



/**
 *  The interface for the message service. It is responsible for
 *  managing the transports and sending/delivering messages.
 */
public interface IMessageService
{
	/**
	 *  Send a message.
	 *  @param message The native message.
	 */
	public void sendMessage(IMessageEnvelope message);
	
	/**
	 *  Deliver a message to an agent.
	 *  @param message The native message.
	 *  @return The agent identifiers of the undelivered messages.
	 */
	public void deliverMessage(IMessageEnvelope message);
	
	/**
	 *  Adds a transport for the message service.
	 *  @param transport The transport.
	 */
	public void addTransport(ITransport transport);
	
	/**
	 *  Remove a transport for the message service.
	 *  @param transport The transport.
	 */
	public void removeTransport(ITransport transport);
	
	/**
	 *  Change transport position.
	 *  @param up Move up?
	 *  @param transport The transport to move.
	 */
	public void changeTransportPosition(boolean up, ITransport transport);
	
	/**
	 *  Get the transports.
	 *  @return The transports.
	 */
	public ITransport[] getTransports();
	
	/**
	 *  Get addresses of all transports.
	 *  @return The addresses of all transports.
	 */
	public String[] getAddresses();

	/**
	 *  Called when the platform shuts down.
	 *  Do necessary cleanup here (if any).
	 */
	public void	shutdown();
}
