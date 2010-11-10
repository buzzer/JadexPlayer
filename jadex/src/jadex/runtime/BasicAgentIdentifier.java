package jadex.runtime;

import java.io.Serializable;


/**
 *  Basic agent identifier to address agents by name only
 */
public class BasicAgentIdentifier	implements Serializable
{
	//-------- attributes --------

	/** The agent name. */
	protected String	name;

	//-------- constructors --------
	
	/**
	 *  Create a new BasicAgentIdentifier.
	 */
	// Bean constructor, do not remove...
	public BasicAgentIdentifier()
	{
	}

	/**
	 *  Create a new BasicAgentIdentifier.
	 *  @param name	The agent name.
	 */
	public BasicAgentIdentifier(String name)
	{
		this.name	= name;
	}

	//-------- methods --------

	/**
	 *  Get the agent name.
	 */
	public String	getName()
	{
		return  this.name;
	}

	/**
	 *  Set the agent name.
	 *  @param name	The agent name.
	 */
	public void	setName(String name)
	{
		this.name	= name;
	}

	/**
	 *
	 */
	public boolean equals(Object o)
	{
		// todo: what about locally defined names?
		return o instanceof BasicAgentIdentifier && ((BasicAgentIdentifier)o).getName().equals(getName());
	}

	/**
	 *  The hashcode of the AID.
	 */
	public int hashCode()
	{
		return getName().hashCode();
	}

	/**
	 *  Get a string representation.
	 */
	public String	toString()
	{
		// todo: what about locally defined names?
		return getName();
	}
}
