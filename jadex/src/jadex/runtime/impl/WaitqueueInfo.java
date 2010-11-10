package jadex.runtime.impl;

import java.util.Map;
import jadex.util.*;
import jadex.runtime.*;

/**
 *  A waitqueue info denotes a plan instance candidate that
 *  will later be scheduled for execution, because it
 *  had registered a permanent filter for an event.
 */
public class WaitqueueInfo	extends CandidateInfo
{
	//-------- attributes --------

	/** The matching permanent filter. */
	protected IFilter	filter;

	//-------- constructor -------

	/**
	 *  Create a waitqueue info object.
	 *  @param event	The event.
	 *  @param rplan	The plan element.
	 *  @param filter	The matching permanent filter.
	 */
	public WaitqueueInfo(IREvent event, RPlan rplan, IFilter filter)
	{
		super(event, rplan);
		this.rplan	= rplan;
		this.filter	= filter;
	}

	//-------- attribute accessors --------

	/**
	 *  Get the matching permanent filter.
	 *  @return	The matching permanent filter.
	 */
	public IFilter	getFilter()
	{
		return this.filter;
	}

	/**
	 *  Get an encodable representation.
	 *  @return A map containing the relevant properties of the object.
	 */
	public Map	getEncodableRepresentation()
	{
		Map	representation	= super.getEncodableRepresentation();
		representation.put("name", "Waitqueue_"+rplan.getName());
		representation.put("rplan", ""+rplan);
		representation.put("filter", filter instanceof IEncodable?
			(Object)((IEncodable)filter).getEncodableRepresentation(): ""+filter);
		return representation;
	}

	//-------- overridings --------

	/**
	 *  Test if this waitqueue info is equal to another object.
	 *  Two waitqueue infos are equal when they have the same
	 *  plan instance info and filter.
	 *  @param object	The object against which to test equality.
	 *  @return	True, when the object is equal to this waitqueue info.
	 */
	public boolean	equals(Object object)
	{
		return (object instanceof WaitqueueInfo)
			&& rplan.equals(((WaitqueueInfo)object).getPlanInstance())
			&& filter.equals(((WaitqueueInfo)object).getFilter());
	}

	/**
	 *  Calculate the hashcode.
	 */
	public int hashCode()
	{
		int code = 0;
		if(rplan!=null) // todo: rplans do not support explicit hashcode
			code = code ^ rplan.hashCode();
		if(filter!=null) // todo: filter do not support explicit hashcode 
			code = code ^ filter.hashCode();
		return code;
	}

	/**
	 *  Create a string representation of this plan instance info.
	 *  @return	This plan instance info represented as string.
	 */
	public String	toString()
	{
		StringBuffer	sb	= new StringBuffer();
		sb.append(SReflect.getInnerClassName(this.getClass()));
		sb.append("(plan=");
		sb.append(rplan.getName());
		sb.append(", filter=");
		sb.append(filter);
		sb.append(")");
		return sb.toString();
	}
}

