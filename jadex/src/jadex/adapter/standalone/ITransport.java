package jadex.adapter.standalone;

import java.util.Map;

import jadex.adapter.fipa.AgentIdentifier;

/**
 *  Interface for Jadex Standalone transports.
 */
public interface ITransport
{
	//-------- constants --------
		
	/** The receiving port (if any). */
	public final static String PORT = "port";
	
	//-------- methods --------
	
	/**
	 *  Init the transport.
	 *  @param platform The platform.
	 *  @param settings The settings.
	 */
	public void init(IPlatform platform, Map settings);

	/**
	 *  Perform cleanup operations (if any).
	 */
	public void shutdown();
	
	/**
	 *  Send a message.
	 *  @param message The message to send.
	 *  @return The agent identifiers to which this 
	 *  message could not be delivered.
	 */
	public AgentIdentifier[] sendMessage(IMessageEnvelope message);
	
	/**
	 *  Returns the prefix of this transport
	 *  @return Transport prefix.
	 */
	public String getServiceSchema();
	
	/**
	 *  Get the adresses of this transport.
	 *  @return An array of strings representing the addresses 
	 *  of this message transport mechanism.
	 */
	public String[] getAddresses();

	/**
	 *  Requests a connection from the transport
	 *  @param address the url of other platform
	 *  @return a conncetion to the specified address
	 * /
	public IConnection createConnection(String address);*/

	/**
	 *  Informs the transport about agents on this platform
	 *  @param agent
	 * /
	public void add(StandaloneAgentAdapter agent);*/

	/**
	 *  @param agent
	 * /
	public void remove(StandaloneAgentAdapter agent);*/
}
