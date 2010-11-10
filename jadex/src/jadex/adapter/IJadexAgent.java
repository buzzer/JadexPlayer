package jadex.adapter;

import jadex.model.IMCapability;
import jadex.runtime.impl.RCapability;

import java.util.logging.Logger;


/**
 *  Jadex agent interface to be used (invoked) by adapters.
 *  To create a jadex agent instance use the factory @link{JadexAgentFactory}. 
 */
public interface IJadexAgent
{
	//-------- methods to be called by adapter --------

	/**
	 *  Main method to perform agent execution.
	 *  Whenever this method is called, the agent performs
	 *  one of its scheduled actions.
	 *  The platform can provide different execution models for agents
	 *  (e.g. thread based, or synchroneous).
	 *  To avoid idle waiting, the return value can be checked. 
	 *  @return True, when there are more actions waiting to be executed. 
	 */
	public boolean executeAction();

	/**
	 *  Notify the agent after the notifyIn time has elapsed.
	 *  @see IAgentAdapter#notifyIn(long)
	 */
	public void notifyDue();

	/**
	 *  Inform the agent that a message has arrived.
	 *  @param message The message that arrived.
	 */
	public void messageArrived(IMessageAdapter message);

	/**
	 *  Request agent to kill itself.
	 */
	public void killAgent();

	//-------- optional accessor methods --------

	//todo: remove as much as possible from these methods
	
	/**
	 *  Get the logger for the agent.
	 *  @return The logger object.
	 */
	public Logger getLogger();

	/**
	 *  Get a property of the agent as defined in the ADF,
	 *  or any enclosed capabilities.
	 */
	public Object	getProperty(String name);

	/**
	 *  Get property names matching the specified start string.
	 *  @return Matching property names of the agent as defined in the ADF,
	 *  or any enclosed capabilities.
	 */
	public String[]	getPropertyNames(String name);

	/**
	 *  WARNING. Does only work when MCapabilities
	 *  are loaded as prototypes! (hack?)
	 *  Get the capability for the capability model.
	 *  @param mcap The model capability.
	 *  @return The corresponding runtime capability.
	 */
	public RCapability lookupCapability(IMCapability mcap);

}
