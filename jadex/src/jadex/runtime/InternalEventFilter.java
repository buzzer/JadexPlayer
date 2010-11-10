package jadex.runtime;

import jadex.runtime.impl.IRInternalEvent;

/**
 *  An internal event filter to match against internal events.
 */
public class InternalEventFilter	extends ParameterElementFilter
{
	//-------- attributes --------

	/** The event type to match. */
	protected String	type;

	/** The event name to match (optional). */
	protected String	name;
	
	//-------- constructors --------

	/**
	 *  Create an event filter to match against process goal events.
	 *  @param type	The event type.
	 */
	public InternalEventFilter(String type)
	{
		this(type, null);
	}

	/**
	 *  Create an event filter to match against goal events.
	 *  @param type	The event type.
	 *  @param name	The event (instance) name.
	 */
	public InternalEventFilter(String type, String name)
	{
		this.type	= type;
		this.name	= name;
	}

	//-------- IFilter methods --------

	/**
	 *  Match an object against the filter.
	 *  @param object The object.
	 *  @return True, if the filter matches.
	 * @throws Exception
	 */
	public boolean filter(Object object) throws Exception
	{
		if(object instanceof jadex.runtime.planwrapper.ElementWrapper)
		{
			object	= ((jadex.runtime.planwrapper.ElementWrapper)object).unwrap();
		}
		else if(object instanceof jadex.runtime.externalaccesswrapper.ElementWrapper)
		{
			object	= ((jadex.runtime.externalaccesswrapper.ElementWrapper)object).unwrap();
		}

		boolean	ret	= false;
		if(object instanceof IRInternalEvent)
		{
			IRInternalEvent	event	= (IRInternalEvent)object;
			boolean	typecheck	= type==null || event.getType().equals(type);
			boolean namecheck	= name==null || event.getName().equals(name);
			boolean supercheck	= super.filter(event);
			ret	= typecheck && namecheck && supercheck;
		}
		return ret;
	}
}
