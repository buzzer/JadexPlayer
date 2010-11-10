package jadex.adapter.fipa;

import jadex.config.Configuration;

/**
 * An agent identifier (AID), see FIPASC00023.
 */
public class AgentIdentifier extends AgentIdentifierData
{
	//-------- constructors --------

	/**
	 *  Create a new agent identifier.
	 *  Bean constructor
	 */
	public AgentIdentifier()
	{
		this(null, false, null, null);
	}

	/**
	 *  Create a new agent identifier with a given global
	 *  name.
	 *  @param name A global name (e.g. "ams@lars").
	 */
	public AgentIdentifier(String name)
	{
		this(name, false, null, null);
	}

	/**
	 *  Create a new agent identifier from a local or global name.
  	 *  @param name A local or global name.
  	 *  @param local True, if the name should be interpreted as local name
  	 *    (i.e. "@platform" will be appended).
	 */
	public AgentIdentifier(String name, boolean local)
	{
		this(name, local, null, null);
	}

	/**
	 *  Create a new agent identifier with a global name and given addresses.
	 *  @param name A global name (e.g. "ams@lars").
	 *  @param addresses A list of transport addresses.
	 */
	public AgentIdentifier(String name, String[] addresses)
	{
		this(name, false, addresses, null);
	}

	/**
	 *  Create a new agent identifier.
  	 *  @param name A local or global name.
  	 *  @param local True, if the name should be interpreted as local name
  	 *    (i.e. "@platform" will be appended).
	 *  @param addresses A list of transport addresses.
	 *  @param resolvers A list of resolvers, which may provide additional transport adresses.
	 */
	public AgentIdentifier(String name, boolean local, String[] addresses, AgentIdentifier[] resolvers)
	{
		super();
		setName(local? name+"@"+Configuration.getConfiguration().getProperty(Configuration.PLATFORMNAME): name);
		for(int i = 0; addresses!=null && i<addresses.length; i++)
			addAddress(addresses[i]);
		for(int i = 0; resolvers!=null && i<resolvers.length; i++)
			addResolver(resolvers[i]);
	}

	//--------- methods --------
	
	/**
	 * Clone this agent identifier.
	 * Does a deep copy.
	 */
	public Object clone()
	{
		AgentIdentifier clone = new AgentIdentifier(super.getName(), false, getAddresses(), null);

		// Deep copy of resolvers.
		AgentIdentifier[] res = getResolvers();
		for(int i = 0; i<res.length; i++)
			clone.addResolver((AgentIdentifier)res[i].clone());

		return clone;
	}

	/**
	 * Checks if this adress equals one or more addresses in the identifier
	 * @param address
	 * @return true
	 */
	public boolean hasAddress(String address)
	{
		boolean ret = false;
		for(int i=0; !ret && i<addresses.size(); i++)
			ret = address.equals(addresses.get(i));
		
		return ret;
	}
	
	/**
	 * @return the local name of an agent
	 */
	public String getLocalName()
	{
		String	ret	= super.getName();
		int	idx;
		if((idx=ret.indexOf('@'))!=-1)
			ret	= ret.substring(0, idx);
		return ret;
	}

	/**
	 *  Get the platform name.
	 *  @return The platform name.
	 */
	public String getPlatformName()
	{
		String	ret	= super.getName();
		int	idx;
		if((idx=ret.indexOf('@'))!=-1)
			ret	= ret.substring(idx+1);
		return ret;
	}

}
