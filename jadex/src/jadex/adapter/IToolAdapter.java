package jadex.adapter;

import jadex.adapter.fipa.AgentAction;
import jadex.adapter.fipa.AgentIdentifier;

/**
 *  This interface can be used to provide tool-specific
 *  platform-independent functionality to application agents.
 */
public interface IToolAdapter
{
	/**
	 *  Handle a request from a tool agent.
	 *  @param tool	The tool agent that issued the request.
	 *  @param request	The the request or query.
	 *  @param reply	A callback interface to the platform allowing the
	 *    tool adapter to send reply messages to the tool agents.
	 */
	public void	handleToolRequest(AgentIdentifier tool, AgentAction request, IToolReply reply);

	/**
	 *  The tool type supported by this adapter (e.g. "tracer").
	 *  @return the class of messages this tool will handle
	 */
	public Class	getMessageClass();

	//-------- helper interface --------
	
	/**
	 *  This interface allows e.g. tool adapters to send native
	 *  messages to tool agents (without requiring ADF-defined message events).
	 */
	public interface IToolReply
	{
		/**
		 *  Send an inform message.
		 *  @param content The message content.
		 *  @param sync If true, wait for a reception aknowledgement of the recipient.
		 */
		public void	sendInform(Object content, boolean sync);
	
		/**
		 *  Send a failure message.
		 *  @param content The message content.
		 *  @param sync If true, wait for a reception aknowledgement of the recipient.
		 */
		public void	sendFailure(Object content, boolean sync);
		
		/**
		 *  Cleanup outstanding reply waits.
		 */
		public void	cleanup();
	}
}
