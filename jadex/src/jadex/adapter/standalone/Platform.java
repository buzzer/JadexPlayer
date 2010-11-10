package jadex.adapter.standalone;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.standalone.ams.AMS;
import jadex.adapter.standalone.df.DF;
import jadex.adapter.standalone.transport.localmtp.LocalTransport;
import jadex.adapter.standalone.transport.niotcpmtp.NIOTCPTransport;
import jadex.adapter.standalone.transport.tcpmtp.TCPTransport;
import jadex.config.Configuration;
import jadex.parser.SParser;
import jadex.util.SReflect;
import jadex.util.collection.SCollection;
import jadex.util.concurrent.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 *  Built-in standalone agent platform, with onyl basic features.
 */
public class Platform implements IPlatform
{
	//-------- constants --------

	/** The maximum shutdown time. */
	public static final long MAX_SHUTDOWM_TIME = 3000;

	/** The allowed command-line options. */
	public static final Set COMMAND_LINE_OPTIONS;

	/** The configuration file. */
	public static final String CONFIGURATION = "conf";

	/** The configuration file. */
	public static final String TRANSPORT = "transport";

	/** Starting with antother df class. */
	public static final String DF = "df";
	
	/** The allowed command-line flags. */
	public static final Set COMMAND_LINE_FLAGS;
	
	/** Starting without gui. */
	public static final String NOGUI = "nogui";

	/** Flag for creating no ams agent. */
	public static final String NOAMSAGENT = "noamsagent";

	/** Flag for creating no df agent. */
	public static final String NODFAGENT = "nodfagent";

	/** Start the platform without transport mechanism. */
	public static final String NOTRANSPORT = "notransport";

	/** Shut down the platform, when the last agent is killed. */
	public static final String AUTOSHUTDOWN = "autoshutdown";

	/** The platform name. */
	public static final String PLATFORMNAME = Configuration.PLATFORMNAME;
	
	/** The fallback configuration. */
	public static final String FALLBACK_CONFIGURATION = "jadex/adapter/standalone/standalone_conf.properties";

	/** Configuration entry for platform shutdown delay (time for agents to terminate gracefully). */
	public static String PLATFORM_SHUTDOWN_TIME_CONFIG = "platform_shutdown_time";

	/** Absolute start time (for testing and benchmarking). */
	public static final long starttime	= System.currentTimeMillis();
	
	/** The time neededfor platform startup (for testing and benchmarking). */
	public static long startup;
	

	static
	{
		COMMAND_LINE_OPTIONS = SCollection.createHashSet();
		COMMAND_LINE_OPTIONS.add("-"+Configuration.PLATFORMNAME);
		COMMAND_LINE_OPTIONS.add("-"+CONFIGURATION);
		COMMAND_LINE_OPTIONS.add("-"+TRANSPORT);
		COMMAND_LINE_OPTIONS.add("-"+DF);

		COMMAND_LINE_FLAGS = SCollection.createHashSet();
		COMMAND_LINE_FLAGS.add("-"+NOGUI);
		COMMAND_LINE_FLAGS.add("-"+NOAMSAGENT);
		COMMAND_LINE_FLAGS.add("-"+NODFAGENT);
		COMMAND_LINE_FLAGS.add("-"+NOTRANSPORT);
		COMMAND_LINE_FLAGS.add("-"+AUTOSHUTDOWN);
	}

	//-------- attributes --------

	/** The platform timer (shared by all agents). */
	protected ITimerService timer;
	
	/** The agent executor service (shared by all agents). */
	protected IExecutorService executor;

	/** The ams of the platform. */
	protected IAMS ams;
	
	/** The optional system agents (ams, df). */
	protected Set systemagents;
	
	/** The df of the platform. */
	protected IDF df;
	
	/** the message service of this platform */
	protected IMessageService msgservice;

	/** The logger. */
	protected Logger logger;
	
	/** The shutdown flag. */
	protected boolean	shuttingdown;

    //-------- constructors --------

    /**
     *  Create a new Platform.
     */
    public Platform()
    {
//    	long freeStartupMemory = Runtime.getRuntime().freeMemory();
//    	long startupTime = System.currentTimeMillis();
//    	try
//    	{
//	    	FileOutputStream fos = new FileOutputStream("debug.txt", false);
//	    	PrintStream ps = new PrintStream(fos);
//	    	 System.setErr(ps);
//	    	 System.setOut(ps);
//    	}
//    	catch(Exception e)
//    	{
//    		e.printStackTrace();
//    	}
//    	Logger.info("Free Memory: " + freeStartupMemory + " bytes ("+Runtime.getRuntime().totalMemory()+" bytes)\n");
    	
		// Save the platform name in the configuration for static access.
    	if(Configuration.getConfiguration().getProperty(Configuration.PLATFORMNAME)==null)
    		Configuration.getConfiguration().setProperty(Configuration.PLATFORMNAME, getName());

    	// Save start time.
    	Configuration.getConfiguration().setProperty(Configuration.STARTTIME, ""+starttime);

    	this.timer = createTimer();
        this.executor = createExecutorService();
        this.ams = createAMS();
		this.df = createDF();
        this.msgservice = createMessageService();
   		
        this.logger = Logger.getLogger("Platform" + this);
        
        initializeTransports();
        
        try
		{
			// Do not use Class.getMethod (slow).
			Method	meth	= SReflect.getMethod(ams.getClass(),
				"addPropertyChangeListener", new Class[]{PropertyChangeListener.class});
			if(meth!=null)
			{
				meth.invoke(ams, new Object[]{new PropertyChangeListener()
		        {
					public void propertyChange(PropertyChangeEvent evt)
					{
						// Hack!!! need a way to identify daemon agents. 
						if(ams.getAgentCount()<=systemagents.size() && Configuration.getConfiguration().getProperty(AUTOSHUTDOWN)!=null)
						{
							//System.out.println("shutdown");
							shutdown();
						}
					}
		        }});
			}
		}
		//		catch(NoSuchMethodException e){}
		catch(IllegalAccessException e){}
		catch(InvocationTargetException e){}

		this.systemagents = SCollection.createHashSet();
		if(Configuration.getConfiguration().getProperty(NOAMSAGENT)==null)
		{
			AgentIdentifier amsagent = ams.createAgent("ams", "jadex.adapter.standalone.ams.AMS", null, null);
			systemagents.add(amsagent);
			ams.startAgent(amsagent);
		}
		if(Configuration.getConfiguration().getProperty(NODFAGENT)==null)
		{
			AgentIdentifier dfagent = ams.createAgent("df", "jadex.adapter.standalone.df.DF", null, null);
			systemagents.add(dfagent);
			ams.startAgent(dfagent);
		}
		
		// ams.createAgent("rf", "jadex.awareness.rf.RF", null, null);
		
		// Logger.info("Free Memory: " + Runtime.getRuntime().freeMemory() + " bytes");
		// Runtime.getRuntime().gc();
		// Logger.info("Free Memory: " + Runtime.getRuntime().freeMemory() + " bytes (after GC)");
		// Logger.info("Jadex-footprint:"+(freeStartupMemory-Runtime.getRuntime().freeMemory())+" bytes\n");
		
		// Logger.info("Startup took " + (System.currentTimeMillis() - startupTime)+ " ms\n");
    }
    
	//-------- methods --------

   	/**
	 *  Get the timer service.
	 *  @return The timer service.
	 */
	public ITimerService getTimerService()
	{
		return timer;
	}
	
	/**
	 *  Set the timer service.
	 *  @param timer The timer service.
	 *  // todo: support clean setting of a new timer service
	 *  // must maintain entries and shutdown? old
	 * /
	public void setTimerService(ITimerService timer)
	{
		this.timer = timer;
	}*/
	
	/**
	 *  Get the executor service.
	 *  @return The executor service. 
	 */
	public IExecutorService getExecutorService()
	{
		return executor;
	}
	
	/**
	 *  Set the executor service.
	 *  @param executor The executor service.
	 *  // todo: support clean setting of a new executor service
	 *  // must suspend old one
	 * /
	public void setExecutorService(IExecutorService executor)
	{
		this.executor = executor;
	}*/

	/**
	 *  Get the AMS of the platform.
	 *  @return The AMS.
	 */
	public IAMS	getAMS()
	{
		return ams;
	}
	
	/**
	 *  Set the AMS of the platform.
	 *  @return The AMS.
	 *  // todo: support clean setting of a new ams
	 *  // must maintain entries etc.
	 * /
	public void	setAMS(IAMS ams)
	{
		this.ams = ams;
	}*/

	/**
	 *  Get the DF of the platform.
	 *  @return The DF.
	 */
	public IDF	getDF()
	{
		return df;
	}
	
	/**
	 *  Set the AMS of the platform.
	 *  @param df The df.
	 *  // todo: support clean setting of a new ams
	 *  // must maintain entries etc.
	 * /
	public void	setDF(IDF df)
	{
		this.df = df;
	}*/
	
	/**
	 *  Get the message service.
	 *  @return The message service.
	 */
	public IMessageService getMessageService()
	{
		return msgservice;
	}
	
	/**
	 *  Set the message service of the platform.
	 *  @param msgservice The message service.
	 *  // todo: support clean setting of a new message servcie
	 *  // must maintain entries etc.
	 * /
	public void	setMessageService(IMessageService msgservice)
	{
		this.msgservice = msgservice;
	}*/
	
	/**
     *  Check if the platform is currently shutting down.
     */
	public boolean	isShuttingDown()
	{
		return shuttingdown;
	}
	
	/**
	 *  Get the platform logger.
     *  @return The platform logger.
     * /
    public Logger getLogger()
    {
      return logger;
    }*/
    
    /**
	 *  Get the name of the platform
	 *  @return The name of this platform.
	 */
	public String getName()
	{
		String ret = Configuration.getConfiguration().getProperty(Configuration.PLATFORMNAME);
		if(ret==null)
		{
			try
			{
      			InetAddress iaddr = InetAddress.getLocalHost();
				//ret = iaddr.getCanonicalHostName().toLowerCase(); // works for 1.4 only.
				ret = iaddr.getHostName().toLowerCase(); // todo: can this cause problems due to name conflicts?
			}
			catch(UnknownHostException e)
			{
				ret = "localhost";
			}
		}
		return ret;
	}

    /**
     *  Shutdown the platform.
     */
	public void shutdown()
	{
		//System.out.println("Shutting down the platform: "+getName());
		// Hack !!! Should be sysnchronized with AMS.
		synchronized(this)
		{
			if(shuttingdown)
				return;
			
			this.shuttingdown	= true;
			
			AgentIdentifier[]	agents	= ams.getAgentIdentifiers();
			for(int i=0; i<agents.length; i++)
			{
				try
				{
					// Do not kill ams and df agents immediately.
					if(!systemagents.contains(agents[i]))
					{
						ams.destroyAgent(agents[i]);
						//System.out.println("Killing normal agent: "+agents[i]);
					}
				}
				catch(RuntimeException e)
				{
					// Due to race conditions, agent may have killed itself already.
				}
			}
		}
				
		new Thread(new Runnable()
		{
			public void run()
			{
				// Hack!!! Start cleaning up after 1 sec :-(
				// Problem is that the ams could be closed too early.
				// Need some way to determine if ams or df are still busy?
				try
				{
					Thread.sleep(1000);
				}
				catch(InterruptedException e)
				{
				}
				
				long shutdown	= System.currentTimeMillis() + MAX_SHUTDOWM_TIME;
				try
				{
					String	shutdownprop	= Configuration.getConfiguration().getProperty(PLATFORM_SHUTDOWN_TIME_CONFIG);
					if(shutdownprop!=null)
						shutdown	= System.currentTimeMillis() + Long.parseLong(shutdownprop);
				}
				catch(NumberFormatException e){}

				// Wait until agents have died.
				// Hack!!! Should not poll AMS?
				while(ams.getAgentCount()>systemagents.size() && System.currentTimeMillis()<shutdown)
				{
					try
					{
						Thread.sleep(100);
					}
					catch(InterruptedException e)
					{
					}
				}
				
				AgentIdentifier[] sagents = (AgentIdentifier[])systemagents.toArray(new AgentIdentifier[systemagents.size()]);
				for(int i=0; i<sagents.length; i++)
				{
					try
					{
						//System.out.println("Killing system agent: "+sagents[i]);
						ams.destroyAgent(sagents[i]);
					}
					catch(RuntimeException e)
					{
						e.printStackTrace();
						// Due to race conditions, agent may have killed itself already.
					}
				}
				
				// Hack!!! Should not poll AMS?
				long start = System.currentTimeMillis();
				while(ams.getAgentCount()>0 && start+1000>System.currentTimeMillis())
				{
					try
					{
						Thread.sleep(100);
					}
					catch(InterruptedException e)
					{
					}
				}
				
				ams.shutdown();
				df.shutdown();
				msgservice.shutdown();
				timer.shutdown();

				// Hack!!! Threadpool may be used outside platform?
				ThreadPoolFactory.getThreadPool().dispose();

				System.exit(0);
			}
		}).start();
	}
	
	//-------- Internal methods --------
	
	/**
	 *  Create the timer.
	 *  @return The timer.
	 */
	protected ITimerService createTimer()
	{
		return new TimerService();
	}
	
	/**
	 *  Create the executor service.
	 *  @return The executor service.
	 */
	protected IExecutorService createExecutorService()
	{
		//return new SyncExecutorService();
		return new AsyncExecutorService();
	}
	
	/**
	 *  Create the ams.
	 *  @return The ams.
	 */
	protected IAMS createAMS()
	{
		return new AMS(this);
	}
	
	/**
	 *  Create the df.
	 *  @return The df.
	 */
	protected IDF createDF()
	{
		String dfClass = Configuration.getConfiguration().getProperty(DF);
		if(dfClass == null)
			return new DF();
		try
		{
			return (IDF) jadex.util.SReflect.classForName(dfClass).newInstance();
		}
		catch(InstantiationException e)
		{
			System.out.println("Could not initialize df " + dfClass + ": " + e);
			System.out.println("Using jadex.adapter.standalone.df.DF now.");
			return new DF();
		}
		catch(IllegalAccessException e)
		{
			System.out.println("Could not initialize df " + dfClass + ": " + e);
			System.out.println("Using jadex.adapter.standalone.df.DF now.");
			return new DF();
		}
		catch(ClassNotFoundException e)
		{
			System.out.println("Could not initialize df " + dfClass + ": " + e);
			System.out.println("Using jadex.adapter.standalone.df.DF now.");
			return new DF();
		}
	}
	
	/**
	 *  Create the message service.
	 *  @return The message service.
	 */
	protected IMessageService createMessageService()
	{
		return new MessageService(this, true);
	}
	
	/**
	 *  Initialize the transports.
	 */
	protected void initializeTransports()
	{
		// todo: make logger level adjustable via settings?!
		
		// Add the local transport to the message service.
		ITransport local = new LocalTransport();
		local.init(this, null);
		msgservice.addTransport(local);
		
		if(Configuration.getConfiguration().getProperty(NOTRANSPORT)==null)
		{			
			// todo: refactor the way transports are initialized
			String trspname = Configuration.getConfiguration().getProperty(TRANSPORT);
			if(trspname!=null)
			{
				try
				{
					// todo: make more arguments for the transport 
					// specification possible
					String clsname = trspname;
					int port = -1;
					int sep = trspname.indexOf(":");
					if(sep!=-1)
					{
						port = Integer.parseInt(clsname.substring(sep+1));
						clsname = clsname.substring(0, sep);
					}
					ITransport trsp = (ITransport)SReflect.findClass(clsname, null).newInstance();
					Map settings = SCollection.createHashMap();
					if(port!=-1)
						settings.put(ITransport.PORT, new Integer(port));
					trsp.init(this, settings);
					msgservice.addTransport(trsp);
				}
				catch(Throwable e)
				{
					//e.printStackTrace();
					System.out.println("Could not initialize transport: "+e);
				}
			}
			else
			{
				// Add the standard tcp/ip transport to the message service.
				// -transport jadex.adapter.standalone.transport.niotcpmtp.NIOTCPTransport:7777
				/*ITransport tcpip = new TCPTransport();
				Map settings = SCollection.createHashMap();
				settings.put(TCPTransport.PORT, new Integer(4567));
				settings.put(TCPTransport.ASYNCHRONOUS, new Boolean(true));
				tcpip.init(this, settings);
				msgservice.addTransport(tcpip);*/
				
				try
				{
					// Add the nio tcp/ip transport to the message service.
					ITransport niotcp = new NIOTCPTransport();
					Map settings = SCollection.createHashMap();
					settings.put(TCPTransport.PORT, new Integer(5678));
					niotcp.init(this, settings);
					msgservice.addTransport(niotcp);
				}
				catch(Exception e)
				{
					System.out.println("Could not initialize transport: "+e);
				}
			}
		}
	}
	
	//-------- Static part --------

	/**
	 *  Start a platform with the agents specified
	 *  by the arguments in the form "name:model" or just "model".
	 */
	public static void main(String[] args)
	{		
		// Read in properties or use default options.
		// Merges command line options into the configuration
		// remaining args (e.g. agents to start) are returned
		Properties	props	= new Properties();
		args = parseOptions(args, props);

		// Initialize platform configuration from args.
		String conffile = props.getProperty(CONFIGURATION);
		if(conffile!=null)
		{
			System.setProperty(Configuration.JADEX_CONFIGURATION, conffile);
		}
		else
		{
			Configuration.setFallbackConfiguration(FALLBACK_CONFIGURATION);
		}

		String[] propkeys = (String[])props.keySet().toArray(new String[props.size()]);
		for(int i=0; i<propkeys.length; i++)
			Configuration.getConfiguration().setProperty(propkeys[i], props.getProperty(propkeys[i]));

		// Create an instance of the platform.
		final Platform	platform	= new Platform();

		// todo: remove
		/*JFrame f = new JFrame();
		JPanel p = new JPanel(new FlowLayout());
		JButton sus = new JButton("suspend");
		JButton res = new JButton("resume");
		JButton async = new JButton("async");
		JButton sync = new JButton("sync");
		p.add(sus);
		p.add(res);
		p.add(async);
		p.add(sync);
		f.add("Center", p);
		f.pack();
		f.setVisible(true);
		sus.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				platform.getExecutorService().suspend();
			}
		});
		res.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				platform.getExecutorService().resume();
			}
		});
		async.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				IExecutorService exe = platform.getExecutorService();
				exe.suspend();
				platform.setExecutorService(new AsyncExecutorService());
			}
		});
		sync.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				IExecutorService exe = platform.getExecutorService();
				exe.suspend();
				platform.setExecutorService(new SyncExecutorService());
			}
		});*/
		
		if(props.get(NOGUI)==null)
		{
			AgentIdentifier	jcc	= platform.getAMS().createAgent("jcc", "jadex.tools.jcc.JCC", null, null);
			platform.getAMS().startAgent(jcc);
		}

		// Create agents on the platform.
		// Syntax: <name>:<model>(<config>,<arg1name=arg1>,...,<argNname=argN>)
		// e.g. hello:jadex.examples.helloworld.HelloWorld(default,HEY!)
		// <name>			corresponds to the name the agent is given by the platform
		// <model>			fully-qualified name of the agent-model (as defined in the agent's ADF)
		// <config>			configuration of the agent. Configurations are defined in the agent's ADF
		// <argX>			(list of) argument(s) for the agent. These are accessible to the programmer
		// 					either within the ADF by "$argname" or within every plan by calling getArgument(argname)
		for(int i=0; i<args.length; i++)
		{
			try
			{
				//System.out.println("arg:"+i+"="+args[i]);
				int	index	= args[i].indexOf(":");
				if(index!=-1)
				{
					String	name	= args[i].substring(0, index);
					String	model	= args[i].substring(index+1, args[i].length());
					String	config	= null;
					String[] aargs = null;
					Map argsmap = null;
					index	= model.indexOf("(");
					if(index!=-1)
					{
						if (model.lastIndexOf(")") != model.length()-1)
							throw new RuntimeException("Syntax-Error. Missing ')' at the end of agent's (command-line) argument: " + model);
						String agentargs	= model.substring(index+1, model.length()-1);
						model	= model.substring(0, index);
						StringTokenizer stok = new StringTokenizer(agentargs, ",");
						if(stok.hasMoreTokens())
						{
							String tmp = stok.nextToken();
							if(!tmp.equals("null"))
								config = tmp;
						}
						
						// Parse the arguments.
						argsmap = SCollection.createHashMap();
						while(stok.hasMoreTokens())
						{
							String tmp = stok.nextToken();
							int idx = tmp.indexOf("=");
							if(idx!=-1)
							{
								String argname = tmp.substring(0, idx).trim();
								String argvalstr = tmp.substring(idx+1).trim();
								//System.out.println("Found arg: "+argname+" = "+argvalstr);
								// Evaluate the argument value.
								Object argval = null;
								try
								{
									argval = SParser.evaluateExpression(argvalstr, null, null);
								}
								catch(Exception e)
								{
									System.out.println("Cannot evaluate argument: "+argname+". Reason: "+e.getMessage());
									//e.printStackTrace();
								}
								argsmap.put(argname, argval);
							}
						}
					}
					AgentIdentifier	agent	= platform.getAMS().createAgent(name, model, config, argsmap);
					platform.getAMS().startAgent(agent);
				}
				else
				{
					System.out.println("Illegal agent specification: "+args[i]);
					System.out.println("Syntax: <name>:<model> ... e.g. hello:jadex.examples.helloworld.HelloWorld");
					System.out.println("Syntax: <name>:<model>(<initialstate>,<arg1name=arg1>,...,<argNname=argNvalue>) ... \ne.g. hello:jadex.examples.helloworld.HelloWorld(default,msg=\"HEY!\")");
				}
			}
			catch(RuntimeException e)
			{
				e.printStackTrace();
			}
		}

		startup	= System.currentTimeMillis() - starttime;
		platform.logger.info("Platform startup time: "+startup+" ms.");
	}

	/**
	 *  Parse options from command line arguments.
	 *  @param args	The arguments to scan.
	 *  @param props	The properties to be read in from options.
	 *  @return The remaining arguments, which are not options.
	 */
	protected static String[]	parseOptions(String[] args, Properties props)
	{
		int	i = 0;
		while(i<args.length && args[i].startsWith("-"))
		{
			if(COMMAND_LINE_OPTIONS.contains(args[i]))
			{
				props.setProperty(args[i++].substring(1), args[i++]);
			}
			else if(COMMAND_LINE_FLAGS.contains(args[i]))
			{
				props.setProperty(args[i++].substring(1), "true");
			}
			else
			{
				System.out.println("Argument could not be understood: "+args[i++]);
				//throw new IllegalArgumentException("Unknown argument: "+args[i]);
			}
		}

		// Return remaining arguments, i.e. agents to start.
		String[]	ret	= new String[args.length-i];
		if(ret.length>0)
			System.arraycopy(args, i, ret, 0, ret.length);
		return ret;
	}
}
