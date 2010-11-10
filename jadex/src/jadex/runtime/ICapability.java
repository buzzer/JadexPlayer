package jadex.runtime;

import jadex.model.IMCapabilityReference;

import java.util.logging.Logger;


/**
 *  A capability is a self-contained agent module
 *  as specified in  an agent definition file (ADF).
 */
public interface ICapability	extends IElement
{
	/**
	 *  Get the scope.
	 *  @return The scope.
	 */
	public IExternalAccess getExternalAccess();

	/**
	 *  Get the belief base.
	 *  @return The belief base.
	 */
	public IBeliefbase getBeliefbase();

	/**
	 *  Get the goal base.
	 *  @return The goal base.
	 */
	public IGoalbase getGoalbase();

	/**
	 *  Get the plan base.
	 *  @return The plan base.
	 */
	public IPlanbase getPlanbase();

	/**
	 *  Get the event base.
	 *  @return The event base.
	 */
	public IEventbase getEventbase();

	/**
	 * Get the expression base.
	 * @return The expression base.
	 */
	public IExpressionbase getExpressionbase();

	/**
	 * Get the property base.
	 * @return The property base.
	 */
	public IPropertybase getPropertybase();

	/**
	 *  Add a new subcapability.
	 *  @param name	The name to give to the new capability.
	 *  @param capafile The capability identifier or filename.
	 *  @deprecated
	 */
	public void	addSubcapability(String name, String capafile);
	
	/**
	 *  Remove a subcapability.
	 *  @param name The subcapability name.
	 *  @deprecated
	 */
	public void removeSubcapability(String name);

	/**
	 *  Register a subcapability.
	 *  @param subcap	The subcapability.
	 */
	public void	registerSubcapability(IMCapabilityReference subcap);

	/**
	 *  Deregister a subcapability.
	 *  @param subcap	The subcapability.
	 */
	public void	deregisterSubcapability(IMCapabilityReference subcap);

	/**
	 *  Get the logger.
	 *  @return The logger.
	 */
	public Logger getLogger();

	/**
	 * Get the agent name.
	 * @return The agent name.
	 */
	public String getAgentName();

	/**
	 * Get the configuration name.
	 * @return The configuration name.
	 */
	public String getConfigurationName();

	/**
	 * Get the agent identifier.
	 * @return The agent identifier.
	 */
	public BasicAgentIdentifier	getAgentIdentifier();

	/**
	 * Get the platform agent.
	 * @return The platform agent.
	 */
	public Object getPlatformAgent();

	/**
	 * Get the platform type.
	 * @return The platform type.
	 */
	public String getPlatformType();
	
	/**
	 *  Get the agent's (command-line) argument.
	 *  @param name The argument name.
	 *  @return  The agent's argument.
	 */
	//public Object getArgument(String name);
	
	/**
	 *  Get the command-line arguments.
	 *  @return The command-line arguments. 
	 */
	//public  Map getArguments();

	/**
	 *  Kill the agent.
	 */
	public void killAgent();

	/**
	 *  Add an agent listener
	 *  @param listener The listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addAgentListener(IAgentListener listener, boolean async);
	
	/**
	 *  Add an agent listener
	 *  @param listener The listener.
	 */
	public void removeAgentListener(IAgentListener listener);
}