package jadex.adapter.standalone.df;

import jadex.adapter.fipa.AgentDescription;
import jadex.adapter.fipa.SearchConstraints;
import jadex.adapter.fipa.ServiceDescription;
import jadex.adapter.standalone.IDF;
import jadex.util.collection.IndexMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;


/**
 *  Directory facilitator implementation for standalone platform.
 */
public class DF implements IDF
{
	//-------- attributes --------

	/** The registered agents. */
	protected IndexMap	agents;
	
	/** The logger. */
	//protected Logger logger;
	
	//-------- constructors --------

	/**
	 *  Create a standalone df.
	 */
	public DF()
	{
		this.agents	= new IndexMap();
		//this.logger = Logger.getLogger("DF" + this);
	}
	
	//-------- methods --------

	/**
	 *  Register an agent description.
	 *  @throws RuntimeException when the agent is already registered.
	 */
	public void	register(AgentDescription adesc)
	{
		//System.out.println("Registered: "+adesc.getName()+" "+adesc.getLeaseTime());
		AgentDescription clone = (AgentDescription)adesc.clone();

		// Add description, when valid.
		if(clone.getLeaseTime()==null || clone.getLeaseTime().getTime()>System.currentTimeMillis())
		{
			// Automatically throws exception, when key exists.
			if(agents.containsKey(clone.getName()))
				throw new RuntimeException("Agent already registered: "+adesc.getName());
			agents.add(clone.getName(), clone);
//			System.out.println("registered: "+clone.getName());
		}
		else
		{
//			System.out.println("not registered: "+clone.getName());			
		}
	}

	/**
	 *  Deregister an agent description.
	 *  @throws RuntimeException when the agent is not registered.
	 */
	public void	deregister(AgentDescription adesc)
	{
		if(!agents.containsKey(adesc.getName()))
			throw new RuntimeException("Agent not registered: "+adesc.getName());
		agents.removeKey(adesc.getName());
		//System.out.println("deregistered: "+adesc.getName());
	}

	/**
	 *  Modify an agent description.
	 *  @throws RuntimeException when the agent is not registered.
	 */
	public void	modify(AgentDescription adesc)
	{
		// Use clone to avoid caller manipulating object after insertion.
		AgentDescription clone = (AgentDescription)adesc.clone();

		// Change description, when valid.
		if(clone.getLeaseTime()==null || clone.getLeaseTime().getTime()>System.currentTimeMillis())
		{
			// Automatically throws exception, when key does not exist.
			agents.replace(clone.getName(), clone);
			//System.out.println("modified: "+clone.getName());
		}
		else
		{
			throw new RuntimeException("Invalid lease time: "+clone.getLeaseTime());
		}
	}

	/**
	 *  Search for agents matching the given description.
	 *  @return An array of matching agent descriptions. 
	 */
	public AgentDescription[]	search(AgentDescription adesc, SearchConstraints con)
	{
		//System.out.println("Searching: "+adesc.getName());

		List	ret	= new ArrayList();

		// If name is supplied, just lookup description.
		if(adesc.getName()!=null)
		{
			if(agents.containsKey(adesc.getName()))
			{
				AgentDescription ad = (AgentDescription)agents.get(adesc.getName());
				// Remove description when invalid.
				if(ad.getLeaseTime()!=null && ad.getLeaseTime().getTime()<System.currentTimeMillis())
					agents.removeKey(ad.getName());
				else
					ret.add(ad);
			}
		}

		// Otherwise search for matching descriptions.
		else
		{
			AgentDescription[]	descs	= (AgentDescription[])agents.toArray(new AgentDescription[agents.size()]);
			for(int i=0; (con==null || con.getMaxResults()==-1 || ret.size()<con.getMaxResults()) && i<descs.length; i++)
			{
				// Remove description when invalid.
				if(descs[i].getLeaseTime()!=null && descs[i].getLeaseTime().getTime()<System.currentTimeMillis())
				{
					agents.removeKey(descs[i].getName());
				}
				// Otherwise match against template.
				else
				{
					if(match(descs[i] ,adesc))
					{
						ret.add(descs[i]);
					}
				}
			}
		}

		//System.out.println("Searched: "+ret);
		return (AgentDescription[])ret.toArray(new AgentDescription[ret.size()]);
	}

	/**
	 *  Called when the platform shuts down.
	 *  Do necessary cleanup here (if any).
	 */
	public void shutdown()
	{
	}

	//-------- helper methods --------

	/**
	 *  Test if an agent description matches a given template.
	 */
	protected boolean	match(AgentDescription desc, AgentDescription template)
	{
		boolean	ret	= true;

		// Match protocols, languages, and ontologies.
		ret	= includes(desc.getLanguages(), template.getLanguages());
		ret	= ret && includes(desc.getOntologies(), template.getOntologies());
		ret	= ret && includes(desc.getProtocols(), template.getProtocols());

		// Match service descriptions.
		if(ret)
		{
			ServiceDescription[]	tservices	= template.getServices();
			for(int t=0; ret && t<tservices.length; t++)
			{
				ret	= false;
				ServiceDescription[]	dservices	= desc.getServices();
				for(int d=0; !ret && d<dservices.length; d++)
				{
					ret	= match(dservices[d], tservices[t]);
				}
			}
		}

		return ret;
	}

	/**
	 *  Test if a service description matches a given template.
	 */
	protected boolean	match(ServiceDescription desc, ServiceDescription template)
	{
		// Match name, type, and ownership;
		boolean	ret	= template.getName()==null || template.getName().equals(desc.getName());
		ret	= ret && (template.getType()==null || template.getType().equals(desc.getType()));
		ret	= ret && (template.getOwnership()==null || template.getOwnership().equals(desc.getOwnership()));

		// Match protocols, languages, ontologies, and properties.
		ret	= ret && includes(desc.getLanguages(), template.getLanguages());
		ret	= ret && includes(desc.getOntologies(), template.getOntologies());
		ret	= ret && includes(desc.getProtocols(), template.getProtocols());
		ret	= ret && includes(desc.getProperties(), template.getProperties());

		return ret;
	}

	/**
	 *  Test if one array of objects is included in the other
	 *  (without considering the order).
	 *  Test is performed using equals().
	 */
	protected boolean	includes(Object[] a, Object[] b)
	{
		Set	entries	= new HashSet();
		for(int i=0; i<b.length; i++)
			entries.add(b[i]);
		for(int i=0; i<a.length; i++)
			entries.remove(a[i]);
		return entries.isEmpty();
	}
}
