package jadex.runtime;

import jadex.adapter.fipa.AgentIdentifier;


/**
 *  Interface that can be used to create, destroy and access agents
 *  from external Java code.
 *  <p><i>This interface must not be used inside agent implementations,
 *  conceptually, because it violates the autonomy principle
 *  and technically, because it easily leads to deadlocks!</i></p>
 */
public interface IPlatform
{
	/**
	 *  Create a new agent.
	 *  @param name	The agent's name.
	 *  @param model	The agent model filename.
	 *  @param config	The configuration (uses default if null).
	 *  @param args Arguments for the agent (if any).
	 *  @return	The id of the created agent.
	 */
	public AgentIdentifier	createAgent(String name, String model, String config, Object[] args);

	/**
	 *  Destroy an agent.
	 *  @param id	The id of the agent to destroy.
	 */
	public void	destroyAgent(AgentIdentifier id);

	/**
	 *  Get the agents on the platform.
	 *  @return	The ids of the agents.
	 */
	public AgentIdentifier[]	getAgents();

	/**
	 *  Get an accessor object for an agent
	 *  represented by a given agent id.
	 *  The accessor object allows to manipulate an agent
	 *  in an object-oriented way.
	 *  @param id	The id of the agent to destroy.
	 *  @return The accessor object.
	 */
	public IExternalAccess	getAgentInterface(AgentIdentifier id);	
}
