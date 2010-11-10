package jadex.adapter.standalone.transport.tcpmtp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.standalone.IMessageEnvelope;
import jadex.adapter.standalone.IPlatform;
import jadex.adapter.standalone.ITransport;
import jadex.adapter.standalone.transport.codecs.CodecFactory;
import jadex.util.SUtil;
import jadex.util.collection.ILRUEntryCleaner;
import jadex.util.collection.LRU;
import jadex.util.collection.MultiCollection;
import jadex.util.collection.SCollection;
import jadex.util.concurrent.ITimedObject;
import jadex.util.concurrent.ThreadPoolFactory;

/**
 *  The tcp transport for sending messages over
 *  tcp/ip connections. Initiates one receiving
 *  tcp/ip port under the specified settings and
 *  opens outgoing connections for all remote 
 *  platforms on demand.
 *  
 *  For the receiving side a separate listener
 *  thread is necessary as it must be continuously
 *  listened for incoming transmission requests.
 */
public class TCPTransport implements ITransport
{
	//-------- constants --------
	
	/** The schema name. */
	public final static String SCHEMA = "tcp-mtp://";
	
	/** Constant for asynchronous setting. */
	public final static String ASYNCHRONOUS = "asynchronous";
	
	/** The receiving port. */
	public final static String PORT = "port";
	
	/** How long to keep output connections alive (5 min). */
	protected final static int	MAX_KEEPALIVE	= 300000;

	/** The prolog size. */
	protected final static int PROLOG_SIZE = 5;
	
	/** 2MB as message buffer */
	protected final static int BUFFER_SIZE	= 1024 * 1024 * 2;
	
	/** Maximum number of outgoing connections */
	protected final static int MAX_CONNECTIONS	= 10;
	
	/** Default port. */
	protected final static int DEFAULT_PORT	= 9876;
	
	//-------- attributes --------
	
	/** The platform. */
	protected IPlatform platform;
	
	/** The addresses. */
	protected String[] addresses;
	
	/** The port. */
	protected int port;
	
	/** The server socket for receiving messages. */
	protected ServerSocket serversocket;
	
	/** The opened connections for addresses. (aid address -> connection). */
	protected Map connections;
	
	/** Should be received asynchronously? One thread for receiving is
		unavoidable. Async defines if the receival should be done on a
		new thread always or on the one receiver thread. */
	protected boolean async;
	
	/** The codec factory. */
	protected CodecFactory codecfac;
	
	/** The logger. */
	protected Logger logger;
	
	//-------- constructors --------
	
	/**
	 *  Init the transport.
	 *  @param platform The platform.
	 *  @param settings The settings.
	 */
	public void init(IPlatform platform, Map settings)
	{
		try
		{
			this.logger = Logger.getLogger("TCPTransport" + this);
			this.codecfac = new CodecFactory();
			
			this.platform = platform;
			if(settings!=null && settings.containsKey(PORT))
				this.port = ((Integer)settings.get(PORT)).intValue();
			
			this.async = true;
			if(settings!=null && settings.containsKey(ASYNCHRONOUS))
				this.async = ((Boolean)settings.get(ASYNCHRONOUS)).booleanValue();
			
			// Set up sending side.
			this.connections = SCollection.createLRU(MAX_CONNECTIONS);
			((LRU)this.connections).setCleaner(new ILRUEntryCleaner()
			{
				public void cleanupEldestEntry(Entry eldest)
				{
					TCPOutputConnection con = (TCPOutputConnection)eldest.getValue();
					con.close();
				}
			});
			this.connections = Collections.synchronizedMap(this.connections);
			
			// Set up receiver side.
			// If port==0 -> any free port
			this.serversocket = new ServerSocket(port);
			this.port = serversocket.getLocalPort();
	
			// Determine all transport addresses.
			InetAddress iaddr = InetAddress.getLocalHost();
			String lhostname = iaddr.getHostName().toLowerCase();
			InetAddress[] laddrs = InetAddress.getAllByName(lhostname);
	
			Set addrs = new HashSet();
			addrs.add(getAddress(iaddr.getHostAddress(), port));
			
			// Get the ip addresses
			for(int i=0; i<laddrs.length; i++)
			{
				String hostname = laddrs[i].getHostName().toLowerCase();
				String ip_addr = laddrs[i].getHostAddress();
				addrs.add(getAddress(ip_addr, port));
				if(!ip_addr.equals(hostname))
				{
					// We have a fully qualified domain name.
					addrs.add(getAddress(hostname, port));
				}
			}
			addresses = (String[])addrs.toArray(new String[addrs.size()]);
			
			// Start the receiver thread.
			ThreadPoolFactory.getThreadPool().execute(new Runnable()
			{
				public void run()
				{
					//try{serversocket.setSoTimeout(10000);} catch(SocketException e) {}
					while(!serversocket.isClosed())
					{
						try
						{
							final TCPInputConnection con = new TCPInputConnection(serversocket.accept(), codecfac);
							if(!async)
							{
								TCPTransport.this.deliverMessages(con);
							}
							else
							{
								// Each accepted incoming connection request is handled
								// in a separate thread in async mode.
								ThreadPoolFactory.getThreadPool().execute(new Runnable()
								{
									public void run()
									{
										TCPTransport.this.deliverMessages(con);
									}
								});
							}
						}
						catch(IOException e)
						{
							//logger.warning("TCPTransport receiver connect error: "+e);
							//e.printStackTrace();
						}
					}
					logger.warning("TCPTransport serversocket closed.");
				}
			});
		}
		catch(Exception e)
		{
			//e.printStackTrace();
			throw new RuntimeException("Transport initialization error: "+e.getMessage());
		}
	}

	/**
	 *  Perform cleanup operations (if any).
	 */
	public void shutdown()
	{
		try{this.serversocket.close();}catch(Exception e){}
		connections = null; // Help gc
	}
	
	//-------- methods --------
	
	/**
	 *  Send a message.
	 *  @param message The message to send.
	 *  (todo: On which thread this should be done?)
	 */
	public AgentIdentifier[] sendMessage(IMessageEnvelope message)
	{
		// Fetch all receivers 
		//AgentIdentifier[] recstodel = message.getReceiversToDeliver();
		//AgentIdentifier[] recstodel = message.getReceiversToDeliver();
		AgentIdentifier[] recstodel = message.getReceivers();
		List undelivered = SUtil.arrayToList(recstodel);
		
		// Find receivers with same address and send only once for 
		// them as message is delivered to all
		// address -> (aid1, aid2, ...)
		MultiCollection adrsets = new MultiCollection(SCollection.createHashMap(), HashSet.class);
		for(int i=0; i<recstodel.length; i++)
		{
			String[] addrs = recstodel[i].getAddresses();
			for(int j=0; j<addrs.length; j++)
			{
				adrsets.put(addrs[i], recstodel[i]);
			}
		}

		// Iterate over all different addresses and try to send
		// to missing and appropriate receivers
		String[] addrs = (String[])adrsets.getKeys(String.class);
		for(int i=0; i<addrs.length; i++)
		{
			TCPOutputConnection con = getConnection(addrs[i]);
			if(con!=null)
			{
				Set aidset = (Set)adrsets.get(addrs[i]);
				aidset.retainAll(undelivered);
				AgentIdentifier[] aids = (AgentIdentifier[])aidset.toArray(new AgentIdentifier[aidset.size()]);
				message.setReceivers(aids);
				if(con.send(message));
					undelivered.removeAll(aidset);
			}
		}
		
		return (AgentIdentifier[])undelivered.toArray(new AgentIdentifier[undelivered.size()]);
	}
	
	/**
	 *  Returns the prefix of this transport
	 *  @return Transport prefix.
	 */
	public String getServiceSchema()
	{
		return SCHEMA;
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
	
	//-------- helper methods --------
	
	/**
	 *  Get the address of this transport.
	 *  @param hostname The hostname.
	 *  @param port The port.
	 *  @return <scheme>:<hostname>:<port>
	 */
	protected String getAddress(String hostname, int port)
	{
		return getServiceSchema()+hostname+":"+port;
	}
	
	/**
	 *  Get the connection.
	 *  @param address
	 *  @return a connection of this type
	 */
	protected TCPOutputConnection getConnection(String address)
	{
		address = address.toLowerCase();
		
		Object ret = connections.get(address);
		if(ret instanceof TCPOutputConnection && ((TCPOutputConnection)ret).isClosed())
		{
			removeConnection(address);
			ret = null;
		}
		
		if(ret instanceof TCPDeadConnection)
		{
			TCPDeadConnection dead = (TCPDeadConnection)ret;
			// Reset connection if connection should be retried.
			if(dead.shouldRetry())
			{
				connections.remove(address);
				ret = null; 
			}
		}
		
		if(ret==null)
			ret = createConnection(address);
		if(ret instanceof TCPDeadConnection)
			ret = null;
		
		return (TCPOutputConnection)ret;
	}
	
	/**
	 *  Create a outgoing connection.
	 *  @param address The connection address.
	 *  @return the connection to this address
	 */
	protected TCPOutputConnection createConnection(String address)
	{
		TCPOutputConnection ret = null;
		
		address = address.toLowerCase();
		if(address.startsWith(getServiceSchema()))
		{
			// Parse the address
			int schemalen = getServiceSchema().length();
			int div = address.indexOf(':', schemalen);
			String hostname;
			int iport;
			if(div>0)
			{
				hostname = address.substring(schemalen, div);
				iport = Integer.parseInt(address.substring(div+1));
			}
			else
			{
				hostname = address.substring(schemalen);
				iport = DEFAULT_PORT;
			}

			try
			{
				ret = new TCPOutputConnection(InetAddress.getByName(hostname), iport, codecfac, new Cleaner(address));
				connections.put(address, ret);
			}
			catch(Exception e)
			{ 
				connections.put(address, new TCPDeadConnection());
				
				logger.warning("Could not create connection: "+e.getMessage());
				//e.printStackTrace();
			}
		}
		
		return ret;
	}
	
	/**
	 *  Remove a cached connection.
	 *  @param address The address.
	 */
	protected void removeConnection(String address)
	{
		address = address.toLowerCase();
		
		Object con = connections.remove(address);
		if(con instanceof TCPOutputConnection)
			((TCPOutputConnection)con).close();
	}
	
	/**
	 *  Deliver messages to local message service
	 *  for disptaching to the agents.
	 *  @param con The connection.
	 */
	protected void deliverMessages(TCPInputConnection con)
	{
		try
		{
			for(IMessageEnvelope msg=con.read(); msg!=null; msg=con.read())
				platform.getMessageService().deliverMessage(msg);
		}
		catch(Exception e)
		{
			logger.warning("TCPTransport receiving error: "+e);
			//e.printStackTrace();
			con.close();
		}
	}
	
	/**
	 *  Class for cleaning output connections after 
	 *  max keep alive time has been reached.
	 */
	protected class Cleaner implements ITimedObject
	{
		//-------- attributes --------
		
		/** The address of the connection. */
		protected String address;
		
		//-------- constructors --------
		
		/**
		 *  Cleaner for a specified output connection.
		 *  @param address The address.
		 */
		public Cleaner(String address)
		{
			this.address = address;
		}
		
		//-------- methods --------
		
		/**
		 *  Called when timepoint was reached.
		 */
		public void timeEventOccurred()
		{
			//System.out.println("Timeout reached for: "+address);
			removeConnection(address);
		}
		
		/**
		 *  Refresh the timeout.
		 */
		public void refresh()
		{
			platform.getTimerService().addEntry(this, System.currentTimeMillis()+MAX_KEEPALIVE);
		}
		
		/**
		 *  Remove this cleaner.
		 */
		public void remove()
		{
			platform.getTimerService().removeEntry(this);
		}
	}
}
