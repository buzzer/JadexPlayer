package jadex.runtime.impl;

import java.util.Map;
import jadex.util.*;

/**
 *  A planinstance info denotes a running plan that waits for an event.
 */
public class PlanInstanceInfo	extends CandidateInfo
{
	//-------- constructor -------

	/**
	 *  Create a plan instance info.
	 *  @param event	The event.
	 *  @param rplan	The plan element.
	 */
	public PlanInstanceInfo(IREvent event, RPlan rplan)
	{
		super(event, rplan);
	}

	//-------- attribute accessors --------

	/**
	 *  Get an encodable representation.
	 *  @return A map containing the relevant properties of the object.
	 */
	public Map	getEncodableRepresentation()
	{
		Map	representation	= super.getEncodableRepresentation();
		representation.put("name", "RPlanCandidate_"+rplan.getName());
		representation.put("rplan", ""+rplan);
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
		return (object instanceof PlanInstanceInfo)
			&& rplan.equals(((PlanInstanceInfo)object).getPlanInstance())
			&& revent.equals(((PlanInstanceInfo)object).getEventInstance());
	}

	/**
	 *  Calculate the hashcode.
	 */
	public int hashCode()
	{
		int code = 0;
		if(rplan!=null)
			code = code ^ rplan.hashCode(); // todo: rplans do not support explicit hashcode
		if(revent!=null)
			code = code ^ revent.hashCode(); // todo: events do not support explicit hashcode
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
		sb.append(")");
		return sb.toString();
	}
}

