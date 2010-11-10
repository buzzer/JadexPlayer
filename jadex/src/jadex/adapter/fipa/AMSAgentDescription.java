package jadex.adapter.fipa;

/**
 *  Java class for concept AMSAgentDescription
 *  of beanynizer_beans_fipa_new ontology.
 */
public class AMSAgentDescription	extends AMSAgentDescriptionData implements Cloneable
{
	//-------- constructors --------
	
	/**
	 *  Create a new AMSAgentDescription.
	 */
	public AMSAgentDescription()
	{
	}

	/**
	 *  Create a new AMSAgentDescription.
	 */
	public AMSAgentDescription(AgentIdentifier aid)
	{
		this();
		setName(aid);
		setState(AMSAgentDescription.STATE_ACTIVE);
	}

	//-------- methods --------

	/**
	 *  Test if this description equals another description.
	 */
	public boolean	equals(Object o)
	{
		return o==this || o instanceof AMSAgentDescription && getName()!=null
			&& getName().equals(((AMSAgentDescription)o).getName());
	}

	/**
	 *  Get the hash code of this description.
	 */
	public int	hashCode()
	{
		return getName()!=null ? getName().hashCode() : 0;
	}

	/**
	 *  Get a string representation of this description.
	 */
	public String	toString()
	{
		return "AMSAgentDescription(name="+getName()+", state="+getState()+", ownership="+getOwnership()+")";
	}
	
	/**
	 *  Clone an agent description.
	 */
	public Object clone()
	{
		try
		{
			AMSAgentDescription ret = (AMSAgentDescription)super.clone();
			ret.setName((AgentIdentifier)name.clone());
			return ret;
		}
		catch(CloneNotSupportedException e)
		{
			throw new RuntimeException("Cannot clone: "+this);
		}
	}
}
