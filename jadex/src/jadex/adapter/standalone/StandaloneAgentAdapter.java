package jadex.adapter.standalone;

import jadex.adapter.*;
import jadex.adapter.IToolAdapter.IToolReply;
import jadex.adapter.fipa.*;
import jadex.runtime.NuggetsXMLContentCodec;
import jadex.util.collection.SCollection;
import jadex.util.concurrent.IExecutable;
import jadex.util.concurrent.ITimedObject;
import nuggets.Nuggets;

import java.io.Serializable;
import java.util.*;

/**
 *  Agent adapter for built-in standalone platform. 
 *  This platform is built for simplicity and for being
 *  able to execute Jadex agents without any 3rd party
 *  agent platform.
 */
public class StandaloneAgentAdapter implements IAgentAdapter, IExecutable, ITimedObject, Serializable
{
	
	/** The message preprocessors property identifier. */
	public static final String PROPERTY_TOOL_ADAPTERS = "tooladapter";

	/** The name of the adapter properties. */
	public static final String ADAPTER_PROPERTIES = "jadex.adapter.standalone.standalone_adapter";

	//-------- attributes --------

	/** The platform. */
	protected transient IPlatform	platform;

	/** The agent identifier. */
	protected AgentIdentifier	aid;

	/** The jadex agent. */
	protected IJadexAgent	agent;

	/** The tool adapters for managing communication with tool agents (tooltype -> adapter). */
	protected List	tooladapters;

	/** The tool message queue. */
	protected final LinkedList	toolmsgqueue;
	
	/** The reply identifiers of outstanding messages. */
	// Hack???
	protected Set replywiths;

	/** The none-existing external transports of the platform. */
	protected transient IMessageEventTransport[] transports;
	
	/** The state of the agent (according to FIPA, managed by AMS). */
	protected String	state; 
	
	//-------- constructors --------

	/**
	 *  Create a new StandaloneAgentAdapter.
	 *  Uses the thread pool for executing the jadex agent.
	 */
	public StandaloneAgentAdapter(IPlatform platform, AgentIdentifier aid, String model, String state, Map args)
	{
		this.platform	= platform;
		this.aid	= aid;
		this.toolmsgqueue	= SCollection.createLinkedList();
		this.replywiths = SCollection.createHashSet();

		this.transports = new IMessageEventTransport[1];
		transports[0] = new StandaloneMessageEventTransport(this);

		// Use Jadex factory to create BDI engine instance for this agent.
		this.agent	= JadexAgentFactory.createJadexAgent(this, model, state, args);
		
		this.tooladapters = SCollection.createArrayList();
		String[] keys = agent.getPropertyNames(PROPERTY_TOOL_ADAPTERS);
		for(int i=0; i<keys.length; i++)
		{
			IToolAdapter	adapter	= (IToolAdapter)agent.getProperty(keys[i]);
			tooladapters.add(adapter);
		}
	}

	/**
	 *  Start the agent execution.
	 */
	public void startAgent()
	{
		wakeup();
	}

	//-------- IAgentAdapter methods --------
	
	/**
	 *  Called by the agent when it probably awoke from an idle state.
	 *  The platform has to make sure that the agent will be executed
	 *  again from now on.
	 *  Note, this method can be called also from external threads
	 *  (e.g. property changes). Therefore, on the calling thread
	 *  no agent related actions must be executed (use some kind
	 *  of wake-up mechanism).
	 *  Also proper synchronization has to be made sure, as this method
	 *  can be called called concurrently from different threads.
	 */
	public void wakeup()
	{
		// Verify that the  agent is running.
		assert !AMSAgentDescription.STATE_INITIATED.equals(state) : this;
		
		// Resume execution of the agent (when idle).
		platform.getExecutorService().execute(this);
	}

	/**
	 *  Notify the agent in a time interval.
	 *  This method is called by the jadex agent to realize internal timing
	 *  issues. The platform only has to consider the last call
	 *  of this method, and may forget about earlier notification requests.
	 *  The method should return immediately, after saving the notification
	 *  request. When the notification is due, the platform has to call the
	 *  @link{IJadexAgent#notifyDue()} method.
	 *  @param millis The relative time in millis.
	 */
	public void notifyIn(long millis)
	{
		// Verify that the  agent is running.
		assert !AMSAgentDescription.STATE_INITIATED.equals(state) : this;

		// Timing issues (of all standalone agents) are handled by the platform timer.
		// Note that the platform timer uses absolute timepoints
		// while the Jadex agent request relative durations!
		if(millis>-1)
		{
			platform.getTimerService().addEntry(this, System.currentTimeMillis() + millis);
		}
		else
		{
			platform.getTimerService().removeEntry(this);
		}
	}
	
	/**
	 *  This is called, after the agent has decided to kill itself.
	 *  All platform-specific resources/entries regarding the agent
	 *  should now be removed.
	 */
	public void cleanupAgent()
	{
		// Remove agent from platform
		platform.getAMS().removeAgent(aid);
		
		// Stop timer for agent.
		platform.getTimerService().removeEntry(this);
		
		// Stop execution of agent.
		platform.getExecutorService().cancel(this);
	}

	/**
	 *  Used to determine the platform type.
	 *  Each adapter implementation should provide the value of a constant
	 *  defined somewhere.
	 *  Using the platform type, e.g. plan context conditions can check
	 *  if plans are applicable in current context. 
	 */
	public String getPlatformType()
	{
		return SStandalone.PLATFORM_TYPE;
	}

	/**
	 *  Get the adapter properties name for a special platform.
	 *  @return The name of the adapter properties.
	 */
	public String getPlatformPropertiesName()
	{
		return ADAPTER_PROPERTIES;
	}

	/**
	 *  Get all avaiable transports available on the native platform.
	 *  @return All transports.
	 */
	public IMessageEventTransport[] getMessageEventTransports()
	{
		return transports;
	}

	/**
	 *  Return an agent-identifier that allows to send
	 *  messages to this agent.
	 *  Return a copy of the original.
	 */
	public AgentIdentifier getAgentIdentifier()
	{
		return platform.getAMS().refreshAgentIdentifier(aid);
		//return (AgentIdentifier)aid.clone();
	}
	
	/**
	 *  String represnetation of the agent.
	 */
	public String	toString()
	{
		return "StandaloneAgentAdapter("+aid.getName()+")";
	}

	//-------- methods called by the standalone platform --------

	/**
	 *  Gracefully terminate the agent.
	 *  This call is delegated to the reasoning engine,
	 *  which might perform arbitrary cleanup actions, goals, etc.
	 */
	public void killAgent()
	{
		agent.killAgent();
	}

	/**
	 *  Called when a message was sent to the agent.
	 */
	public void	receiveMessage(final IMessageEnvelope msgenv)
	{
		if(!msgenv.getTypeName().equals(SFipa.MESSAGE_TYPE_NAME_FIPA))
			throw new RuntimeException("Only type fipa currently supported: "+msgenv);

		final Map message = msgenv.getMessage();

		String onto=(String)message.get(SFipa.ONTOLOGY);
		
//		System.out.println(getAgentIdentifier()+" msg: "+message);

		// Handle outstanding tool replies.
		if(replywiths.contains(message.get(SFipa.IN_REPLY_TO)))
		{
//			System.err.println("Tool reply received: "+message.get(SFipa.IN_REPLY_TO));

			// Removing last replywith will let agent to continue normal execution.
			replywiths.remove(message.get(SFipa.IN_REPLY_TO));
			if(replywiths.isEmpty())
				wakeup();
		}

		// Check for tool message.
		// Some tool messages are also handled with normal message arrived!
		else if(onto!=null && onto.startsWith("jadex.tools")
			&& (SFipa.REQUEST.equals(message.get(SFipa.PERFORMATIVE))))
		{
//			System.out.println(getAgentIdentifier()+" msg: "+message);

			// Handle tool messages in executor (on agent thread).
			synchronized(toolmsgqueue)
			{
				toolmsgqueue.addLast(message);
			}
			wakeup();
		}
		// Handle normal messages.
		else
		{
			agent.messageArrived(new FipaMessageAdapter(agent, message)
			{
				public Object getRawValue(String name)
				{
					return message.get(name);
				}

				public String getId()
				{
					return (String)message.get(SFipa.X_MESSAGE_ID);
				}
			});
		}
	}
	
	/**
	 *  Set the state of the agent.
	 */
	public void	setState(String state)
	{
		this.state	= state;
	}
	
	/**
	 *  Get the state of the agent.
	 */
	public String	getState()
	{
		return  state;
	}
	
	
	//-------- ITimedObject interface --------
	
	/**
	 *  Called when the submitted timepoint was reached.
	 */
	public void timeEventOccurred()
	{
		if(state!=null && !state.equals(AMSAgentDescription.STATE_INITIATED)
			 && !state.equals(AMSAgentDescription.STATE_TERMINATED))
		{
			agent.notifyDue();
		}
	}

	//-------- methods for plans --------

	/**
	 *  Get the platform.
	 *  @return the platform of this agent
	 */
	public IPlatform	getPlatform()
	{
		return platform;
	}

	//-------- IExecutable interface --------
	
	/**
	 *  Exucutable code for running the agent
	 *  in the platforms executor service.
	 */
	public boolean	execute()
	{
		// Hack!!! Shouldn't be called during jadex agent creation.
		if(agent==null)
			return false;
		
		// Handle tool requests (if any).
		Map	toolmsg	= null;
		synchronized(toolmsgqueue)
		{
			if(!toolmsgqueue.isEmpty())
				toolmsg	= (Map)toolmsgqueue.removeFirst();
		}

		boolean	executed	= false;
		
		if(toolmsg!=null)
		{
			handleToolMessage(toolmsg);
			executed	= true;
		}

		// Execute while actions are available.
		else
		{
			// When waiting for tool reply, do not execute any actions.
			if(!replywiths.isEmpty())
				return false;
		
			try
			{
				//System.out.println("Executing: "+agent);
				executed	= agent.executeAction();
			}
			catch(Throwable e)
			{
				// Fatal error!
				e.printStackTrace();
				agent.getLogger().severe("Fatal error, agent '"+aid+"' will be removed.");
				
				// Remove agent from platform.
				cleanupAgent();				
			}
		}
		
		return executed;
	}
	
	//-------- tool message handling --------

	/**
	 *  Handle a tool message.
	 *  // todo: messages are replied native, is that right?
	 *  // todo: what do we do with standalone tools? what if some Jadex transport was used?
	 */
	public void	handleToolMessage(final Map msg)
	{
		final AgentIdentifier sender = (AgentIdentifier)msg.get(SFipa.SENDER);
		AgentAction request = (AgentAction)Nuggets.objectFromXML((String)msg.get(SFipa.CONTENT));
		boolean processed = false;
		for(int i=0; i<tooladapters.size(); i++)
		{
			IToolAdapter adapter = (IToolAdapter)tooladapters.get(i);
			if(adapter.getMessageClass().isInstance(request))
			{
				try
				{
					adapter.handleToolRequest(sender, request, new StandaloneToolReply(msg, sender));
					processed = true;
				}
				catch(RuntimeException e)
				{
					agent.getLogger().severe("Tool adapter "+adapter+"threw exception "+e);
					e.printStackTrace();
				}
			}
		}
		if(!processed)
		{
			agent.getLogger().warning("No tool adapter to handle: "+request);
		}
	}

	/**
	 *  A tool reply for natively sending messages.
	 */
	private final class StandaloneToolReply implements IToolReply
	{
		private Map				msg;
		private AgentIdentifier	sender;

		/**
		 *
		 * @param msg
		 * @param sender
		 */
		private StandaloneToolReply(Map msg, AgentIdentifier sender)
		{
			this.msg = msg;
			this.sender = sender;
		}

		/**
		 *
		 * @param content
		 * @param sync
		 */
		public void sendInform(Object content, boolean sync)
		{
			sendNative(SFipa.INFORM, content, sync);
		}

		/**
		 *
		 * @param content
		 * @param sync
		 */
		public void sendFailure(Object content, boolean sync)
		{
			sendNative(SFipa.FAILURE, content, sync);
		}
		
		/**
		 *  Cleanup
		 */
		public void	cleanup()
		{
			// Hack!!! Should only clear replywiths of one tool.
			replywiths.clear();
		}
		
		/**
		 *
		 * @param performative
		 * @param content
		 * @param sync
		 */
		protected void sendNative(String performative, Object content, boolean sync)
		{
			Map msg = SCollection.createHashMap();
			
			msg.put(SFipa.SENDER, aid);
			msg.put(SFipa.RECEIVERS, new AgentIdentifier[]{this.sender});
			msg.put(SFipa.PERFORMATIVE, performative);
			msg.put(SFipa.CONTENT, Nuggets.objectToXML(content));
			msg.put(SFipa.IN_REPLY_TO, this.msg.get(SFipa.REPLY_WITH));
			msg.put(SFipa.CONVERSATION_ID, this.msg.get(SFipa.CONVERSATION_ID));
			msg.put(SFipa.LANGUAGE, NuggetsXMLContentCodec.NUGGETS_XML);
			msg.put(SFipa.ONTOLOGY, this.msg.get(SFipa.ONTOLOGY));
			msg.put(SFipa.PROTOCOL, this.msg.get(SFipa.PROTOCOL));
			MessageEnvelope reply	= new MessageEnvelope(msg, new AgentIdentifier[]{this.sender}, SFipa.MESSAGE_TYPE_NAME_FIPA);
			
			if(sync) 
			{
				// Wait for reply is done by setting replywith to non-null.
				final String	replyid	= SFipa.createUniqueId(aid.getName());
				replywiths.add(replyid);
				msg.put(SFipa.REPLY_WITH, replyid);
				platform.getMessageService().sendMessage(reply);
				
//				System.err.println("Agent waiting for tool reply: "+replyid);
				
				// Add timeout listener (todo: customize timeout?).
				platform.getTimerService().addEntry(new ITimedObject()
				{
					public void timeEventOccurred()
					{
						// When agent is still waiting, reset and restart.
						if(replywiths.remove(replyid))
						{
//							System.err.println("Tool reply timeout: "+replyid);
							StandaloneAgentAdapter.this.wakeup();
						}
					}
				}, System.currentTimeMillis() + 10000);
			}
			else
			{
				platform.getMessageService().sendMessage(reply);
			}
		}
	}
}
