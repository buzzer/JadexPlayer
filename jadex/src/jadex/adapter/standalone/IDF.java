package jadex.adapter.standalone;

import jadex.adapter.fipa.AgentDescription;
import jadex.adapter.fipa.SearchConstraints;

/**
 *  Interface for the directory facilitator (DF). Provides services for registering,
 *  modifying, deregistering and searching of agent resp. service descriptions.
 */
public interface IDF
{
	/**
	 *  Register an agent description.
	 *  @throws RuntimeException when the agent is already registered.
	 */
	public void	register(AgentDescription adesc);
	
	/**
	 *  Deregister an agent description.
	 *  @throws RuntimeException when the agent is not registered.
	 */
	public void	deregister(AgentDescription adesc);
	
	/**
	 *  Modify an agent description.
	 *  @throws RuntimeException when the agent is not registered.
	 */
	public void	modify(AgentDescription adesc);
	
	/**
	 *  Search for agents matching the given description.
	 *  @return An array of matching agent descriptions. 
	 */
	public AgentDescription[] search(AgentDescription adesc, SearchConstraints con);

	/**
	 *  Called when the platform shuts down.
	 *  Do necessary cleanup here (if any).
	 */
	public void	shutdown();
}
