package jadex.runtime.impl;

import java.util.Set;

import jadex.model.ISystemEventTypes;
import jadex.runtime.IFilter;
import jadex.runtime.SystemEvent;
import jadex.util.collection.SCollection;

/**
 *  A filter for system events.
 */
public class SystemEventFilter implements IFilter
{
	//-------- attributes --------
	
	/** The relevant event types (if any). */
	protected Set eventtypes;
	
	/** The relevant element (if any). */
	protected IRElement element;
	
	/** The relevant type (if any). */
	protected String type;
	
	//-------- constructors --------
	
	/**
	 *  Create a new system event filter.
	 *  @param eventtypes The event types.
	 *  @param element The element.
	 */
	public SystemEventFilter(String[] eventtypes, IRElement element)
	{
		this(eventtypes);
		this.element = element;
	}
	
	/**
	 *  Create a new system event filter.
	 *  @param eventtypes The event types.
	 *  @param type The element model name.
	 */
	public SystemEventFilter(String[] eventtypes, String type)
	{
		this(eventtypes);
		this.type = type;
	}
	
	/**
	 *  Create a new system event filter.
	 *  @param eventtypes The event types.
	 */
	public SystemEventFilter(String[] eventtypes)
	{
		setEventTypes(eventtypes);
	}

	//-------- methods --------
	
	/**
	 *  Match an object against the filter.
	 *  Exceptions are interpreted as non-match.
	 *  @param object The object.
	 *  @return True, if the filter matches.
	 */
	public boolean filter(Object object)	throws Exception
	{
		if(!(object instanceof SystemEvent))
			return false;
		
		boolean ret = true;
		SystemEvent se = (SystemEvent)object;
		
		// Check if eventtype is relevant.
		if(eventtypes.size()>0 && !eventtypes.contains(se.getType()))
			ret = false;
		
		// Check if type is ok.
		if(ret && type!=null)
		{
			Object src = se.getSource();
			if(!(src instanceof IRElement) || !((IRElement)src).getType().equals(type))
				ret = false;
		}
		
		// Check if element is ok.
		if(ret && element!=null)
		{
			if(!element.equals(se.getSource()))
				ret = false;
		}
		
		return ret;
	}

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public String toString()
	{
		return "SystemEventFilter(eventtypes="+eventtypes+", type="+type+", element"+element+")";
	}

	/**
	 *  Set the event types relevant for this filter.
	 *  Automatically includes all subtypes of the given events.
	 */
	public void setEventTypes(String[] eventtypes)
	{
		this.eventtypes = SCollection.createHashSet();
		for(int i=0; i<eventtypes.length; i++)
			this.eventtypes.addAll(ISystemEventTypes.Subtypes.getSubtypes(eventtypes[i]));
	}
}