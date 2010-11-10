package jadex.adapter.fipa;

/**
 *  The service description.
 */
public class ServiceDescription	extends ServiceDescriptionData implements Cloneable
{
	//	-------- attributes ----------
	
	protected Long serviceId;
	
	//-------- constructors --------

	/**
	 *  Create a new service description.
	 */
	public ServiceDescription()
	{
		this(null, null, null);
	}

	/**
	 *  Create a new service description.
	 *  @param name The name.
	 *  @param type The type expression.
	 *  @param ownership The ownership.
	 */
	public ServiceDescription(String name, String type, String ownership)
	{
		this.setName(name);
		this.setType(type);
		this.setOwnership(ownership);
	}
	
	/** 
	 * @param obj
	 * @return true if obj is an ServiceDescription and both are equal
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof ServiceDescription)) return false;
		ServiceDescription sd=(ServiceDescription)obj;
		
		return eq(sd.name, name) 
		       && eq(sd.ownership, ownership)
		       && eq(sd.type, type)
			   && eq(sd.languages, languages) 
			   && eq(sd.ontologies, ontologies)
			   && eq(sd.protocols, protocols)
			   && eq(sd.properties, properties);
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
			return (ServiceDescription)super.clone();
		}
		catch(CloneNotSupportedException e)
		{
			throw new RuntimeException("Cannot clone: "+this);
		}
	}

	/**
	 * @return the serviceId
	 */
	protected Long getServiceId()
	{
		return serviceId;
	}

	/**
	 * @param serviceId the serviceId to set
	 */
	protected void setServiceId(Long serviceId)
	{
		this.serviceId = serviceId;
	}
}
