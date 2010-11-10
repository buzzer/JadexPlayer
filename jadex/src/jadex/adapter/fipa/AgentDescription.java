package jadex.adapter.fipa;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;


/**
 *  An agent description.
 */
public class AgentDescription	extends AgentDescriptionData implements Cloneable
{
	//	-------- attributes ----------
	
	protected Long descrId;
	
	//-------- constructor --------

	/**
	 *  Create a new agent description.
	 */
	public AgentDescription()
	{
		this(null);
	}

	/**
	 *  Create a new agent description.
	 *  @param name The name.
	 */
	public AgentDescription(AgentIdentifier name)
	{
		this(name, null, null, null, null, null);
	}

	/**
	 *  Create a new agent description.
	 *  @param name The name.
	 *  @param services The services.
	 *  @param protocols The protocols.
	 *  @param ontologies The ontologies.
	 *  @param languages The languages.
	 */
	public AgentDescription(AgentIdentifier name, ServiceDescription[] services,
		String[] protocols, String[] ontologies, String[] languages, Date leasetime)
	{
		this.setName(name);
		this.setLeaseTime(leasetime);
		if(services!=null)
			for(int i=0; i<services.length; i++)
				this.addService(services[i]);
		if(protocols!=null)
			for(int i=0; i<protocols.length; i++)
				this.addProtocol(protocols[i]);
		if(languages!=null)
			for(int i=0; i<languages.length; i++)
				this.addLanguage(languages[i]);
		if(ontologies!=null)
			for(int i=0; i<ontologies.length; i++)
				this.addOntology(ontologies[i]);
	}
	
	/** 
	 * @param obj
	 * @return true if obj is an AgentDescription and both are equal
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof AgentDescription)) return false;
		AgentDescription ad=(AgentDescription)obj;
		
		return eq(ad.name, name) 
		       && eq(ad.leasetime, leasetime)
			   && eq(ad.languages, languages) 
			   && eq(ad.ontologies, ontologies)
			   && eq(ad.protocols, protocols)
			   && eq(ad.services, services);
	}

	/** 
	 * @param leasetime
	 * @param leasetime2
	 * @return true if both ar null or both are equal
	 */
	private static final boolean eq(Object a, Object b)
	{
		return (a==b) || (a!=null && b!=null && a.equals(b));
	}

	/**
	 *  Clone an agent description.
	 */
	public Object clone()
	{
		try
		{
			AgentDescription ret = (AgentDescription)super.clone();
			ret.services = new ArrayList();
			ret.protocols = (List)((ArrayList)protocols).clone();
			ret.languages = (List)((ArrayList)languages).clone();
			ret.ontologies = (List)((ArrayList)ontologies).clone();
			for(int i=0; i<services.size(); i++)
			{
				ret.services.add(((ServiceDescription)services.get(i)).clone());
			}
			return ret;
		}
		catch(CloneNotSupportedException e)
		{
			throw new RuntimeException("Cannot clone: "+this);
		}
	}


	/**
	 *  Get a string representation of this AgentDescription.
	 *  @return The string representation.
	 */
	public String toString()
	{
		return "AgentDescription("+getName()+")";
	}

	/**
	 * @return the descrId
	 */
	protected Long getDescrId()
	{
		return descrId;
	}

	/**
	 * @param descrId the descrId to set
	 */
	protected void setDescrId(Long descrId)
	{
		this.descrId = descrId;
	}
}
