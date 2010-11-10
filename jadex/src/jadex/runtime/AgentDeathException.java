package jadex.runtime;

import jadex.runtime.impl.RBDIAgent;


/**
 *  This exception indicates that an operation could not be performed
 *  because the agent has died.
 */
public class AgentDeathException extends BDIFailureException
{
	//-------- attributes --------

	/** The died agent. */
	protected RBDIAgent	agent;

	//-------- constructors --------

	/**
	 *  Create a new goal failure exception.
	 *  @param agent	The died agent.
	 */
	public AgentDeathException(RBDIAgent agent)
	{
		super(null, null);
		this.agent	= agent;
	}

	//-------- methods --------

	/**
	 *  Create a string representation of the exception.
	 */
	public String	toString()
	{
		String s = getClass().getName();
		String message = getLocalizedMessage();
		return (message != null) ? (s + "("+agent+"): " + message) : s + "("+agent+")";
	}
}
