package jadex.runtime;

import jadex.runtime.impl.IRMessageEvent;

/**
 *  A message filter checks if an object matches
 *  a message event.
 */
public class MessageEventFilter	extends ParameterElementFilter
{
	//-------- attributes --------

	/** The event type to match. */
	protected String	type;

	/** The event name to match (optional).
	 	It is matched against the in-reply message name of a message. */
	protected String	replyname;
	
	//-------- constructors --------

	/**
	 *  Create an event filter to match against message events.
	 *  @param type	The event type.
	 */
	public MessageEventFilter(String type)
	{
		this(type, null);
	}

	/**
	 *  Create an event filter to match against message events.
	 *  @param type	The event type.
	 *  @param replyname The event (instance) name.
	 */
	public MessageEventFilter(String type, String replyname)
	{
		this.type	= type;
		this.replyname	= replyname;
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
		if(object instanceof IRMessageEvent)
		{
			IRMessageEvent event	= (IRMessageEvent)object;
			IRMessageEvent inreply	= event.getInReplyMessageEvent();
			ret	= (type==null || event.getType().equals(type))
				&& (replyname==null || replyname.equals(inreply!=null? inreply.getName(): null))
				&& super.filter(event);
		}
		return ret;
	}
}
