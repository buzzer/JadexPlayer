package jadex.adapter.standalone;

import jadex.adapter.IAgentAdapter;
import jadex.adapter.fipa.AMSAgentDescription;
import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.fipa.SearchConstraints;

import java.util.Map;

/**
 *  Interface for the agent management system (AMS). It provides basic platform 
 *  services for managing agent creation, deletion and search.
 *  // todo: can't this interface be reduced somehow?
 */
public interface IAMS
{
	/**
	 *  Create a new agent on the platform.
	 *  The agent will not run before the {@link startAgent(AgentIdentifier)}
	 *  method is called.
	 *  Ensures (in non error case) that the aid of
	 *  the new agent is added to the AMS when call returns.
	 *  @param name The agent name (null for auto creation)
	 *  @param model The model name.
	 *  @param confi The configuration.
	 *  @param args The arguments map (name->value).
	 */
	public AgentIdentifier	createAgent(String name, String model, String config, Map args);
	
	/**
	 *  Start a previously created agent on the platform.
	 *  @param agent	The id of the previously created agent.
	 */
	public void	startAgent(AgentIdentifier agent);
	
	/**
	 *  Destroy (forcefully terminate) an agent on the platform.
	 *  @param aid	The agent to destroy.
	 */
	public void destroyAgent(AgentIdentifier aid);

	/**
	 *  Suspend the execution of an agent.
	 *  @param aid The agent identifier.
	 */
	public void suspendAgent(AgentIdentifier aid);
	
	/**
	 *  Resume the execution of an agent.
	 *  @param aid The agent identifier.
	 */
	public void resumeAgent(AgentIdentifier aid);
	
	/**
	 *  Remove an agent identifier from the list of agents.
	 *  @param aid	The agent identifier to remove.
	 *  // todo: remove this method somehow
	 */
	public void removeAgent(AgentIdentifier aid);
	
	/**
	 *  Search for agents matching the given description.
	 *  @param adesc The agent description to search (null for any agent).
	 *  @param con The search constraints restricting search and/or result size.
	 *  @return An array of matching agent descriptions.
	 */
	public AMSAgentDescription[] searchAgents(AMSAgentDescription adesc, SearchConstraints con);
	
	/**
	 *  Test if an agent is currently living on the platform.
	 *  @param aid The agent identifier.
	 *  @return True, if agent is hosted on platform.
	 */
	public boolean containsAgent(AgentIdentifier aid);
	
	/**
	 *  Get the agent adapters.
	 *  @return The agent adapters.
	 */
	public AgentIdentifier[] getAgentIdentifiers();
	
	/**
	 *  Get the agent adapters.
	 *  @return The agent adapters.
	 */
	public IAgentAdapter[] getAgentAdapters();
	
	/**
	 *  Get the agent adapter for an agent identifier.
	 *  @param aid The agent identifier.
	 *  @return The agent adapter.
	 */
	public IAgentAdapter getAgentAdapter(AgentIdentifier aid);
	
	/**
	 *  Get the agent description of a single agent.
	 *  @param aid The agent identifier.
	 *  @return The agent description of this agent.
	 */
	public AMSAgentDescription getAgentDescription(AgentIdentifier aid);
	
	/**
	 *  Get the number of active agents.
	 *  @return The number of active agents.
	 */
	public int getAgentCount();
	
	/**
	 *  Copy and refresh local agent identifier.
	 *  @param aid The agent identifier.
	 *  @return The refreshed copy of the aid.
	 */
	public AgentIdentifier refreshAgentIdentifier(AgentIdentifier aid);
	
	/**
	 *  Called when the platform shuts down.
	 *  At this time all agents already had time to kill themselves
	 *  (as specified in the platform shutdown time).
	 *  Remaining agents should be discarded.
	 */
	public void	shutdown();

	//-------- property methods --------

	// todo: introduce ams listeners?!
	
	/**
     *  Add a PropertyChangeListener to the listener list.
     *  The listener is registered for all properties.
     *  @param listener  The PropertyChangeListener to be added.
     * /
    public void addPropertyChangeListener(PropertyChangeListener listener);*/

    /**
     *  Remove a PropertyChangeListener from the listener list.
     *  This removes a PropertyChangeListener that was registered
     *  for all properties.
     *  @param listener  The PropertyChangeListener to be removed.
     * /
    public void removePropertyChangeListener(PropertyChangeListener listener);*/
}
