package jadex.runtime.impl;

import jadex.model.ISystemEventTypes;
import jadex.runtime.IFilter;
import jadex.runtime.SystemEvent;

/**
 *  A specifiy system event filter that takes into
 *  account that replies should be matched. 
 */
public class SystemEventMessageFilter extends SystemEventFilter
{
	//-------- attributes --------
	
	/** The filter. */
	protected IFilter filter;

	//-------- constructors --------

	/**
	 *  Create a new system event filter.
	 *  @param eventtypes The event types.
	 *  @param element The element.
	 */
	public SystemEventMessageFilter(IRMessageEvent mevent)
	{
		super(new String[]{ISystemEventTypes.MESSAGE_RECEIVED, ISystemEventTypes.MESSAGE_SENT}, mevent);
		WaitAbstraction wa = new WaitAbstraction(mevent.getScope());
		wa.addMessageEvent(mevent);
		this.filter = wa.getFilter();
	}
	
	//-------- methods --------
	
	/**
	 *  Match an object against the filter.
	 *  Exceptions are interpreted as non-match.
	 *  @param object The object.
	 *  @return True, if the filter matches.
	 */
	public boolean filter(Object object) throws Exception
	{
		if(!(object instanceof SystemEvent))
			return false;
		SystemEvent se = (SystemEvent)object;

		boolean ret;
		if(ISystemEventTypes.MESSAGE_RECEIVED.equals(se.getType()))
		{
			ret = filter.filter(se.getSource());
		}
		else
		{
			ret	= super.filter(object);
		}

		return ret;
	}
}
