package jadex.adapter.standalone.ams;

import jadex.adapter.IAgentAdapter;
import jadex.adapter.fipa.*;
import jadex.adapter.standalone.IAMS;
import jadex.adapter.standalone.IPlatform;
import jadex.adapter.standalone.StandaloneAgentAdapter;
import jadex.util.SimplePropertyChangeSupport;
import jadex.util.collection.SCollection;
import jadex.model.SXML;

import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.logging.Logger;

/**
 *  Built-in standalone agent platform, with only basic features.
 */
public class AMS implements IAMS
{
	//-------- constants --------

	/** The agent counter. */
	public static int agentcnt = 0;

	//-------- attributes --------

	/** The agent platform. */
	protected IPlatform platform;

	/** The agents (aid->adapter agent). */
	protected Map adapters;
	
	/** The ams agent descriptions. */
	protected Map agentdescs;
	
	/** The property change support. */
	protected SimplePropertyChangeSupport pcs;
	
	/** The logger. */
	protected Logger logger;

    //-------- constructors --------

    /**
     *  Create a new AMS.
     */
    public AMS(IPlatform platform)
    {
		this.platform = platform;
		this.adapters = Collections.synchronizedMap(SCollection.createHashMap());
		this.agentdescs = Collections.synchronizedMap(SCollection.createHashMap());
		this.pcs = new SimplePropertyChangeSupport(this);
		this.logger = Logger.getLogger(platform.getName()+".ams");
    }

    //-------- interface methods --------
    
	/**
	 *  Create a new agent on the platform.
	 *  Ensures (in non error case) that the aid of
	 *  the new agent is added to the AMS when call returns.
	 *  @param name The agent name (null for auto creation)
	 *  @param model The model name.
	 *  @param confi The configuration.
	 *  @param args The arguments map (name->value).
	 */
	public AgentIdentifier	createAgent(String name, String model, String config, Map args)
	{
		if(name!=null && name.indexOf('@')!=-1)
			throw new RuntimeException("No '@' allowed in agent name.");

		if(platform.isShuttingDown())
			throw new RuntimeException("No new agents may be created when platform is shutting down.");

		AgentIdentifier aid;
		StandaloneAgentAdapter agent;
		synchronized(adapters)
		{
			synchronized(agentdescs)
			{
				if(name==null)
				{
					aid = generateAgentIdentifier(SXML.getShortName(model));
				}
				else
				{
					aid = new AgentIdentifier(name+"@"+platform.getName()); // Hack?!
					aid.setAddresses(platform.getMessageService().getAddresses());
					if(adapters.containsKey(aid))
						throw new RuntimeException("Agent name already exists on agent platform.");
				}
		
				agent = new StandaloneAgentAdapter(platform, aid, model, config, args);
				adapters.put(aid, agent);
				
				AMSAgentDescription	ad	= new AMSAgentDescription(aid);
				ad.setState(AMSAgentDescription.STATE_INITIATED);
				agent.setState(AMSAgentDescription.STATE_INITIATED);
				agentdescs.put(aid, ad);
			}
		}
//		System.out.println("added: "+agents.size()+", "+aid);

		pcs.firePropertyChange("agents", null, adapters);
		return (AgentIdentifier)aid.clone();
	}

	/**
	 *  Start a previously created agent on the platform.
	 *  @param agent	The id of the previously created agent.
	 */
	public void	startAgent(AgentIdentifier agent)
	{
		synchronized(adapters)
		{
			synchronized(agentdescs)
			{
				AMSAgentDescription	desc	= (AMSAgentDescription)agentdescs.get(agent);
				if(desc!=null && AMSAgentDescription.STATE_INITIATED.equals(desc.getState()))
				{
					StandaloneAgentAdapter	adapter	= (StandaloneAgentAdapter)adapters.get(agent);
					if(adapter!=null)
					{
						desc.setState(AMSAgentDescription.STATE_ACTIVE);
						adapter.setState(AMSAgentDescription.STATE_ACTIVE);
						adapter.startAgent();
					}
					else
					{
						// Shouldn't happen?
						throw new RuntimeException("Cannot start unknown agent: "+agent);
					}
				}
				else if(desc!=null)
				{
					throw new RuntimeException("Cannot start agent "+agent+" in state: "+desc.getState());
				}
				else
				{
					throw new RuntimeException("Cannot start unknown agent: "+agent);
				}
			}
		}
	}
	
	/**
	 *  Destroy (forcefully terminate) an agent on the platform.
	 *  @param aid	The agent to destroy.
	 */
	public void destroyAgent(AgentIdentifier aid)
	{
		synchronized(adapters)
		{
			synchronized(agentdescs)
			{
				//System.out.println("killing: "+aid);
				
				StandaloneAgentAdapter	agent	= (StandaloneAgentAdapter)adapters.get(aid);
				if(agent==null)
				{
					//System.out.println(agentdescs);
					throw new RuntimeException("Agent "+aid+" does not exist.");
				}
				
				// todo: does not work always!!! A search could be issued before agents had enough time to kill itself!
				// todo: killAgent should only be called once for each agent?
				AMSAgentDescription	desc	= (AMSAgentDescription)agentdescs.get(aid);
				if(desc!=null)
				{
					// Resume a suspended agent before killing it.
					if(AMSAgentDescription.STATE_SUSPENDED.equals(desc.getState()))
						resumeAgent(aid);
					
					if(AMSAgentDescription.STATE_ACTIVE.equals(desc.getState()))
					//if(!AMSAgentDescription.STATE_TERMINATING.equals(desc.getState()))
					{
						agent.setState(AMSAgentDescription.STATE_TERMINATING);
						desc.setState(AMSAgentDescription.STATE_TERMINATING);
						agent.killAgent();
					}
					//removeAgent(aid);	// Done after killAgent is complete.
					else if(AMSAgentDescription.STATE_INITIATED.equals(desc.getState()))
					{
						// Do not kill agents, which have never been started.
						agent.cleanupAgent();
					}
					else
					{
						throw new RuntimeException("Cannot kill "+aid+" agent: "+desc.getState());
					}
				}
			}
		}
	}
	
	/**
	 *  Remove an agent identifier from the list of agents.
	 *  @param aid	The agent identifier to remove.
	 */
	public void removeAgent(AgentIdentifier aid)
	{
		synchronized(adapters)
		{
			synchronized(agentdescs)
			{
//				System.err.println("remove called for: "+aid);
				StandaloneAgentAdapter	adapter	= (StandaloneAgentAdapter)adapters.remove(aid);
				if(adapter==null)
					throw new RuntimeException("Agent Identifier not registered in AMS: "+aid);
				adapter.setState(AMSAgentDescription.STATE_TERMINATED);
				agentdescs.remove(aid);
				pcs.firePropertyChange("agents", null, adapters);
			}
		}
	}
	
	/**
	 *  Suspend the execution of an agent.
	 *  @param aid The agent identifier.
	 *  // todo: make sure that agent is really suspended an does not execute
	 *  an action currently.
	 */
	public void suspendAgent(AgentIdentifier aid)
	{
		StandaloneAgentAdapter adapter = (StandaloneAgentAdapter)adapters.get(aid);
		AMSAgentDescription ad = (AMSAgentDescription)agentdescs.get(aid);
		if(adapter==null || ad==null)
			throw new RuntimeException("Agent Identifier not registered in AMS: "+aid);
		if(!AMSAgentDescription.STATE_ACTIVE.equals(ad.getState()))
			throw new RuntimeException("Only active agents can be suspended: "+aid+" "+ad.getState());
		ad.setState(AMSAgentDescription.STATE_SUSPENDED);
		adapter.setState(AMSAgentDescription.STATE_SUSPENDED);
		platform.getExecutorService().suspend(adapter);
		pcs.firePropertyChange("agents", null, adapters);
	}
	
	/**
	 *  Resume the execution of an agent.
	 *  @param aid The agent identifier.
	 */
	public void resumeAgent(AgentIdentifier aid)
	{
		StandaloneAgentAdapter adapter = (StandaloneAgentAdapter)adapters.get(aid);
		AMSAgentDescription ad = (AMSAgentDescription)agentdescs.get(aid);
		if(adapter==null || ad==null)
			throw new RuntimeException("Agent Identifier not registered in AMS: "+aid);
		if(!AMSAgentDescription.STATE_SUSPENDED.equals(ad.getState()))
			throw new RuntimeException("Only suspended agents can be resumed: "+aid+" "+ad.getState());
		ad.setState(AMSAgentDescription.STATE_ACTIVE);
		adapter.setState(AMSAgentDescription.STATE_ACTIVE);
		platform.getExecutorService().resume(adapter);
		pcs.firePropertyChange("agents", null, adapters);
	}

	/**
	 *  Search for agents matching the given description.
	 *  @return An array of matching agent descriptions.
	 */
	public AMSAgentDescription[]	searchAgents(AMSAgentDescription adesc, SearchConstraints con)
	{
//		System.out.println("search: "+agents);
		AMSAgentDescription[] ret;

		// If name is supplied, just lookup description.
		if(adesc!=null && adesc.getName()!=null)
		{
			AMSAgentDescription ad = (AMSAgentDescription)agentdescs.get(adesc.getName());
			if(ad!=null && ad.getName().equals(adesc.getName()))
			{
				ad.setName(refreshAgentIdentifier(ad.getName()));
				AMSAgentDescription	desc	= (AMSAgentDescription)ad.clone();
				ret = new AMSAgentDescription[]{desc};
			}
			else
			{
				ret	= new AMSAgentDescription[0];
			}
		}

		// Otherwise search for matching descriptions.
		else
		{
			List	tmp	= new ArrayList();
			synchronized(agentdescs)
			{
				for(Iterator it=agentdescs.values().iterator(); it.hasNext(); )
				{
					AMSAgentDescription	test	= (AMSAgentDescription)it.next();
					if(adesc==null ||
						(adesc.getOwnership()==null || adesc.getOwnership().equals(test.getOwnership()))
						&& (adesc.getState()==null || adesc.getState().equals(test.getState())))
					{
						tmp.add(test.clone());
					}
				}
			}
			ret	= (AMSAgentDescription[])tmp.toArray(new AMSAgentDescription[tmp.size()]);
		}

		//System.out.println("searched: "+ret);
		return ret;
	}
	
	/**
	 *  Test if an agent is currently living on the platform.
	 *  @param aid The agent identifier.
	 *  @return True, if agent is hosted on platform.
	 */
	public boolean containsAgent(AgentIdentifier aid)
	{
		return adapters.containsKey(aid);
	}
	
	/**
	 *  Get the agent adapters.
	 *  @return The agent adapters.
	 */
	public IAgentAdapter[] getAgentAdapters()
	{
		synchronized(adapters)
		{
			return (IAgentAdapter[])adapters.values().toArray(new IAgentAdapter[adapters.size()]);
		}
	}
	
	/**
	 *  Get the agent adapter for an agent identifier.
	 *  @param aid The agent identifier.
	 *  @return The agent adapter.
	 */
	public IAgentAdapter getAgentAdapter(AgentIdentifier aid)
	{
		return (IAgentAdapter)adapters.get(aid);
	}
	
	/**
	 *  Get the agent description of a single agent.
	 *  @param aid The agent identifier.
	 *  @return The agent description of this agent.
	 */
	public AMSAgentDescription getAgentDescription(AgentIdentifier aid)
	{
		AMSAgentDescription ret = (AMSAgentDescription)agentdescs.get(aid);
		if(ret!=null)
		{
			ret.setName(refreshAgentIdentifier(aid));
			ret	= (AMSAgentDescription)ret.clone();
		}
		return ret;
	}
	
	/**
	 *  Get the agent adapters.
	 *  @return The agent adapters.
	 */
	public AgentIdentifier[] getAgentIdentifiers()
	{
		synchronized(adapters)
		{
			AgentIdentifier[] ret = (AgentIdentifier[])adapters.keySet().toArray(new AgentIdentifier[adapters.size()]);
			for(int i=0; i<ret.length; i++)
				ret[i] = refreshAgentIdentifier(ret[i]);
			return ret;
		}
	}
	
	/**
	 *  Get the number of active agents.
	 *  @return The number of active agents.
	 */
	public int getAgentCount()
	{
		return adapters.size();
	}
	
	/**
	 *  Copy and refresh local agent identifier.
	 *  @param aid The agent identifier.
	 *  @return The refreshed copy of the aid.
	 */
	public AgentIdentifier refreshAgentIdentifier(AgentIdentifier aid)
	{
		AgentIdentifier	ret	= (AgentIdentifier)aid.clone();
		if(adapters.containsKey(aid))
			ret.setAddresses(platform.getMessageService().getAddresses());
		return ret;
	}

	//-------- Helper methods --------
		
	/**
	 *  Create an agent identifier that is allowed on the platform.
	 *  @param typename The type name.
	 *  @return The agent identifier.
	 */
	protected AgentIdentifier generateAgentIdentifier(String typename)
	{
		AgentIdentifier ret = null;

		do
		{
			ret = new AgentIdentifier(typename+(agentcnt++)+"@"+platform.getName()); // Hack?!
		}
		while(adapters.containsKey(ret));
		
		ret.setAddresses(platform.getMessageService().getAddresses());

		return ret;
	}

	/**
	 *  Called when the platform shuts down.
	 *  At this time all agents already had time to kill themselves
	 *  (as specified in the platform shutdown time).
	 *  Remaining agents should be discarded.
	 */
	public void shutdown()
	{
		synchronized(adapters)
		{
			AgentIdentifier[]	agents	= (AgentIdentifier[])adapters.keySet()
				.toArray(new AgentIdentifier[adapters.keySet().size()]);
			for(int i=0; i<agents.length; i++)
			{
				logger.warning("Agent did not terminate: "+agents[i]);
				try
				{
					removeAgent(agents[i]);
				}
				catch(RuntimeException e)
				{
				}
			}
		}
	}
	
	//-------- property methods --------
	
	/**
     *  Add a PropertyChangeListener to the listener list.
     *  The listener is registered for all properties.
     *  @param listener  The PropertyChangeListener to be added.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.addPropertyChangeListener(listener);
    }

    /**
     *  Remove a PropertyChangeListener from the listener list.
     *  This removes a PropertyChangeListener that was registered
     *  for all properties.
     *  @param listener  The PropertyChangeListener to be removed.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.removePropertyChangeListener(listener);
    }
}
