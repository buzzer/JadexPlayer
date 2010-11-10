package jadex.adapter.standalone.transport.localmtp;

import java.util.List;
import java.util.Map;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.standalone.IMessageEnvelope;
import jadex.adapter.standalone.IMessageService;
import jadex.adapter.standalone.IPlatform;
import jadex.adapter.standalone.ITransport;
import jadex.config.Configuration;
import jadex.util.collection.SCollection;

/**
 *  The local transport for sending messages on the
 *  same platform (just calls the local deliver method).
 */
public class LocalTransport implements ITransport
{
	//-------- constants --------
	
	/** The schema name. */
	public final static String SCHEMA = "local-mtp://";
	
	//-------- attributes --------
	
	/** The message service. */
	protected IMessageService msgservice;
	
	/** The addresses. */
	protected String[] addresses;
	
	//-------- constructors --------
	
	/**
	 *  Init the transport.
	 *  @param platform The platform.
	 *  @param settings The settings.
	 */
	public void init(IPlatform platform, Map settings)
	{
		this.msgservice = platform.getMessageService();
		this.addresses = new String[0];
	}

	/**
	 *  Perform cleanup operations (if any).
	 */
	public void shutdown()
	{
	}
	
	//-------- methods --------
	
	/**
	 *  Send a message.
	 *  @param message The message to send.
	 *  @return The agent identifiers of the agents 
	 *  the message could not be sent to.
	 */
	public AgentIdentifier[] sendMessage(IMessageEnvelope message)
	{
		List todeliver = SCollection.createArrayList();
		List undelivered = SCollection.createArrayList();
		
		AgentIdentifier[] recs = message.getReceivers();
		//AgentIdentifier[] recs = message.getReceiversToDeliver();
		String hap = Configuration.getConfiguration().getProperty(Configuration.PLATFORMNAME);
		
		for(int i=0; i<recs.length; i++)
		{
			if(recs[i]==null)
				System.out.println("here");
			if(recs[i].getPlatformName().equals(hap))
				todeliver.add(recs[i]);
			else
				undelivered.add(recs[i]);
		}
		if(todeliver.size()>0)
		{
			message.setReceivers((AgentIdentifier[])todeliver
				.toArray(new AgentIdentifier[todeliver.size()]));
			
			msgservice.deliverMessage(message);
		}
		
		return (AgentIdentifier[])undelivered.toArray(new AgentIdentifier[undelivered.size()]);
	}
	
	/**
	 *  Returns the prefix of this transport
	 *  @return Transport prefix.
	 */
	public String getServiceSchema()
	{
		return "local:";
	}
	
	/**
	 *  Get the adresses of this transport.
	 *  @return An array of strings representing the addresses 
	 *  of this message transport mechanism.
	 */
	public String[] getAddresses()
	{
		return addresses;
	}

}
