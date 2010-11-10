package jadex.adapter.standalone.transport.niotcpmtp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.standalone.IMessageEnvelope;
import jadex.adapter.standalone.IMessageService;
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
public class NIOTCPTransport implements ITransport
{
	//-------- constants --------
	
	/** The schema name. */
	public static final String SCHEMA = "nio-mtp://";
	
	/** How long to keep connections alive (5 min). */
	protected static final int	MAX_KEEPALIVE	= 300000;

	/** The prolog size. */
	protected static final int PROLOG_SIZE = 5;

	/** 2MB as message buffer */
	protected static final int BUFFER_SIZE	= 1024 * 1024 * 2;
	
	/** Maximum number of outgoing connections */
	protected static final int MAX_CONNECTIONS	= 10;
	
	/** Default port. */
	protected static final int DEFAULT_PORT	= 8765;
	
	//-------- attributes --------
	
	/** The platform. */
	protected IPlatform platform;
	
	/** The addresses. */
	protected String[] addresses;
	
	/** The port. */
	protected int port;
	
	/** The server socket for receiving messages. */
	protected ServerSocketChannel ssc;
	
	/** The selector for fetching new incoming requests. */
	protected Selector selector;
	
	/** The opened connections for addresses. (aid address -> connection). */
	protected Map connections;
	
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
	public void init(final IPlatform platform, Map settings)
	{
		try
		{
			this.logger = Logger.getLogger("NIOTCPTransport" + this);
			this.codecfac = new CodecFactory();
			
			this.platform = platform;
			if(settings!=null && settings.containsKey(PORT))
				this.port = ((Integer)settings.get(PORT)).intValue();
			
			// Set up sending side.
			this.connections = SCollection.createLRU(MAX_CONNECTIONS);
			((LRU)this.connections).setCleaner(new ILRUEntryCleaner()
			{
				public void cleanupEldestEntry(Entry eldest)
				{
					NIOTCPOutputConnection con = (NIOTCPOutputConnection)eldest.getValue();
					con.close();
				}
			});
			
			// Set up receiver side.
			// If port==0 -> any free port
			this.ssc = ServerSocketChannel.open();
			this.ssc.configureBlocking(false);
			ServerSocket serversocket = ssc.socket();
			serversocket.bind(new InetSocketAddress(port));
			this.port = serversocket.getLocalPort();
			this.selector = Selector.open();
			ssc.register(selector, SelectionKey.OP_ACCEPT);
			
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
			
			// Start receiver thread.
			ThreadPoolFactory.getThreadPool().execute(new Runnable()
			{
				public void run()
				{
					while(ssc.isOpen())
					{
						// This is a blocking call that only returns when traffic occurs.
						Iterator it = null;
						try
						{
							selector.select();
							it = selector.selectedKeys().iterator();
						}
						catch(IOException e)
						{
							logger.warning("NIOTCP selector error.");
							//e.printStackTrace();
						}
						
						while(it!=null && it.hasNext())
						{
				            // Get the selection key
				            SelectionKey key = (SelectionKey)it.next();
				    
				            // Remove it from the list to indicate that it is being processed
				            it.remove();
				            
							if(key.isValid() && key.isAcceptable())
							{
								try
								{
									// Returns only null if no connection request is available.
									SocketChannel sc = ssc.accept();
									if(sc!=null) 
									{
										sc.configureBlocking(false);
										sc.register(selector, SelectionKey.OP_READ, new NIOTCPInputConnection(sc, codecfac));
									}
								}
								catch(IOException e)
								{
									logger.warning("NIOTCP connection error on receiver side.");
									//e.printStackTrace();
									key.cancel();
								}
							}
							else if(key.isValid() && key.isReadable())
							{
								NIOTCPInputConnection con = (NIOTCPInputConnection)key.attachment();
								try
								{
									for(IMessageEnvelope msg=con.read(); msg!=null; msg=con.read())
										platform.getMessageService().deliverMessage(msg);
								}
								catch(IOException e)
								{ 
									logger.warning("NIOTCP receiving error while reading data.");
									//e.printStackTrace();
									con.close();
									key.cancel();
								}
							}
							else
							{
								key.cancel();
							}
						}
					}
					logger.info("TCPNIO receiver closed.");
				}
			});
			//platform.getLogger().info("Local address: "+getServiceSchema()+lhostname+":"+listen_port);
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
		try{this.ssc.close();}catch(Exception e){}
		connections = null; // Help gc
	}
	
	//-------- methods --------
	
	/**
	 *  Send a message.
	 *  @param message The message to send.
	 */
	public AgentIdentifier[] sendMessage(IMessageEnvelope message)
	{
		// Fetch all receivers 
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
			try
			{
				boolean fresh = false;
				// Is the cached connection is dead the call will
				// cause a IOException been thrown
				NIOTCPOutputConnection con = getConnection(addrs[i]);
				if(con==null)
				{
					fresh = true;
					con = createConnection(addrs[i]);
				}
						
				if(con!=null)
				{
					Set aidset = (Set)adrsets.get(addrs[i]);
					aidset.retainAll(undelivered);
					AgentIdentifier[] aids = (AgentIdentifier[])aidset.toArray(new AgentIdentifier[aidset.size()]);
					message.setReceivers(aids);
					
					// The send process must be performed once or twice
					// as there is no possibility to check if the cached connection
					// is still connected to the other end. This can only be
					// checked by the write operation.
					while(true)
					{
						try
						{
							con.send(message);
							undelivered.removeAll(aidset);
							break;
						}
						catch(IOException e)
						{
							removeConnection(addrs[i]);
							if(!fresh)
							{
								fresh = true;
								con = createConnection(addrs[i]);
								if(con==null)
									break;
							}
							else
							{
								logger.warning("Send connection closed: "+addrs[i]);
								break;
							}
						}
					}
				}
			}
			catch(IOException e)
			{
				logger.warning("Address unreachable: "+addrs[i]);
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
	 *  Get the cached connection.
	 *  @param address The address.
	 *  @return The cached connection.
	 */
	protected NIOTCPOutputConnection getConnection(String address) throws IOException
	{
		address = address.toLowerCase();
		
		Object ret = connections.get(address);
		if(ret instanceof NIOTCPDeadConnection)
		{
			NIOTCPDeadConnection dead = (NIOTCPDeadConnection)ret;
			// Reset connection if connection should be retried.
			if(dead.shouldRetry())
			{
				connections.remove(address);
				ret = null; 
			}
			else
			{
				throw new IOException("Dead connection.");
			}
		}
		return (NIOTCPOutputConnection)ret;
	}
	
	/**
	 *  Remove a cached connection.
	 *  @param address The address.
	 */
	protected void removeConnection(String address)
	{
		address = address.toLowerCase();
		
		NIOTCPOutputConnection con = (NIOTCPOutputConnection)connections.remove(address);
		if(con!=null)
			con.close();
	}
	
	/**
	 *  Create a outgoing connection.
	 *  @param address The connection address.
	 *  @return the connection to this address
	 */
	protected NIOTCPOutputConnection createConnection(String address)
	{
		address = address.toLowerCase();
		
		NIOTCPOutputConnection ret = null;
		
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
				ret = new NIOTCPOutputConnection(InetAddress.getByName(hostname), iport, codecfac, new Cleaner(address));
				connections.put(address, ret);
			}
			catch(Exception e)
			{ 
				connections.put(address, new NIOTCPDeadConnection());
				logger.warning("Could not establish connection to: "+hostname+":"+iport);
				//e.printStackTrace();
			}
		}
		
		return ret;
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