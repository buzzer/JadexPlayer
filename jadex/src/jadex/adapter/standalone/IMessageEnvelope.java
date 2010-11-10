package jadex.adapter.standalone;

import java.util.Map;

import jadex.adapter.fipa.AgentIdentifier;

/**
 *  The mesage envelope that holds the native message,
 *  the receivers (to deliver) and the message type (e.g. fipa).
 */
public interface IMessageEnvelope
{
	/**
	 *  Get native message.
	 *  @return The native message.
	 */
	public Map getMessage();
	
	/**
	 * Get the receivers.
	 */
	public AgentIdentifier[] getReceivers();
	
	/**
	 * Get the receivers.
	 */
	public void setReceivers(AgentIdentifier[] receivers);
	
	/**
	 *  Set the type (e.g. "fipa").
	 * @param messagetypename 
	 */
	public void setTypeName(String messagetypename);

	/**
	 *  Get the type (e.g. "fipa").
	 */
	public String getTypeName();
}
