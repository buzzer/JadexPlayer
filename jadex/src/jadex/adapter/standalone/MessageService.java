package jadex.adapter.standalone;

import jadex.adapter.IAgentAdapter;
import jadex.adapter.fipa.AgentIdentifier;
import jadex.runtime.MessageFailureException;
import jadex.util.SUtil;
import jadex.util.collection.SCollection;
import jadex.util.concurrent.AsyncManager;
import jadex.util.concurrent.ICommand;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 *  The Message service serves several message-oriented purposes: a) sending and
 *  delivering messages by using transports b) managemang of transports
 *  (add/remove)
 */
public class MessageService implements IMessageService
{
	//-------- attributes --------

	/** The ams. */
	protected IPlatform platform;

	/** The transports. */
	protected List transports;

	/** All addresses of this platform. */
	private String[] addresses;

	/** Should be sent asynchronously (on a decoupled thread). */
	protected boolean async;

	/** Decouples calls on demand. */
	protected AsyncManager aman;

	/** The command to execute possibly on a decoupled thread. */
	protected ICommand sendmsg;

	/** The logger. */
	protected Logger logger;

	//-------- constructors --------

	/**
	 *  Constructor for Outbox.
	 *  @param platform
	 */
	protected MessageService(IPlatform platform, boolean async)
	{
		this.platform = platform;
		this.transports = SCollection.createArrayList();
		this.aman = new AsyncManager(async);
		this.sendmsg = new ICommand()
		{
			public void execute(Object args)
			{
				internalSendMessage((IMessageEnvelope)args);
			}
		};
		this.logger = Logger.getLogger("MessageService" + this);
	}

	//-------- interface methods --------

	/**
	 *  Send a message.
	 *  @param message The native message.
	 */
	public void sendMessage(IMessageEnvelope message)
	{
		aman.execute(sendmsg, message);
	}

	/**
	 *  Deliver a message to the intended agents. Called from transports.
	 *  @param message The native message. 
	 *  (Synchronized because can be called from concurrently executing transports)
	 */
	public synchronized void deliverMessage(IMessageEnvelope message)
	{
		// AgentIdentifier[] receivers = message.getReceiversToDeliver();
		AgentIdentifier[] receivers = message.getReceivers();

		for(int i = 0; i < receivers.length; i++)
		{
			IAgentAdapter agent = platform.getAMS().getAgentAdapter(receivers[i]);
			if(agent != null)
			{
				((StandaloneAgentAdapter)agent).receiveMessage(message);
			}
			else
			{
				logger.warning("Message could not be delivered to receiver(s): " + message);

				// todo: notify sender that message could not be delivered!
				// Problem: there is no connection back to the sender, so that
				// the only chance is sending a separate failure message.
			}
		}

		// return (AgentIdentifier[])undelivered.toArray(new
		// AgentIdentifier[undelivered.size()]);
	}

	/**
	 *  Adds a transport for this outbox.
	 *  @param transport The transport.
	 */
	public void addTransport(ITransport transport)
	{
		transports.add(transport);
		addresses = null;
	}

	/**
	 *  Remove a transport for the outbox.
	 *  @param transport The transport.
	 */
	public void removeTransport(ITransport transport)
	{
		transports.remove(transport);
		transport.shutdown();
		addresses = null;
	}

	/**
	 *  Moves a transport up or down.
	 *  @param up Move up?
	 *  @param transport The transport to move.
	 */
	public synchronized void changeTransportPosition(boolean up, ITransport transport)
	{
		int index = transports.indexOf(transport);
		if(up && index>0)
		{
			ITransport temptrans = (ITransport)transports.get(index - 1);
			transports.set(index - 1, transport);
			transports.set(index, temptrans);
		}
		else if(index!=-1 && index<transports.size()-1)
		{
			ITransport temptrans = (ITransport)transports.get(index + 1);
			transports.set(index + 1, transport);
			transports.set(index, temptrans);
		}
		else
		{
			throw new RuntimeException("Cannot change transport position from "
				+index+(up? " up": " down"));
		}
	}

	/**
	 *  Get the adresses of an agent.
	 *  @return The addresses of this agent.
	 */
	public String[] getAddresses()
	{
		if(addresses == null)
		{
			ITransport[] trans = (ITransport[])transports.toArray(new ITransport[transports.size()]);
			ArrayList addrs = new ArrayList();
			for(int i = 0; i < trans.length; i++)
			{
				String[] traddrs = trans[i].getAddresses();
				for(int j = 0; j < traddrs.length; j++)
					addrs.add(traddrs[j]);
			}
			addresses = (String[])addrs.toArray(new String[addrs.size()]);
		}

		return addresses;
	}

	/**
	 *  Get the transports.
	 *  @return The transports.
	 */
	public ITransport[] getTransports()
	{
		ITransport[] transportsArray = new ITransport[transports.size()];
		return (ITransport[])transports.toArray(transportsArray);
	}

	/**
	 *  Called when the platform shuts down. Do necessary cleanup here (if any).
	 */
	public void shutdown()
	{
		for(int i = 0; i < transports.size(); i++)
		{
			((ITransport)transports.get(i)).shutdown();
		}
	}

	// -------- internal methods --------

	/**
	 *  Send a message.
	 *  @param message The native message.
	 */
	protected void internalSendMessage(IMessageEnvelope message)
	{
		AgentIdentifier[] receivers = message.getReceivers();
		if(receivers.length == 0)
			throw new MessageFailureException(message, "No receiver specified");
		for(int i=0; i<receivers.length; i++)
		{
			if(receivers[i]==null)
				throw new MessageFailureException(message, "A receiver nulls");
		}

		// List recs = SUtil.arrayToList(receivers);
		ITransport[] trans = (ITransport[])transports.toArray(new ITransport[transports.size()]);

		for(int i = 0; i < trans.length; i++)
		{
			try
			{
				// The receivers to which this message should be delivered
				// Extra setting these receivers avoids sending messages to
				// agents twice.
				// message.setReceiversToDeliver(receivers);
				message.setReceivers(receivers);

				// Method returns agent identifiers of undelivered agents
				receivers = trans[i].sendMessage(message);
			}
			catch(Exception e)
			{
				// todo:
				e.printStackTrace();
			}
		}

		if(receivers.length > 0)
			logger.warning("Message could not be delivered to (all) receivers: " + SUtil.arrayToString(receivers));
		// throw new MessageFailureException(message, "Message could not be
		// delivered to all receivers: "
		// +SUtil.arrayToString(receivers));
	}
}