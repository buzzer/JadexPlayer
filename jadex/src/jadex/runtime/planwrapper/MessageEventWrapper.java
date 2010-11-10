package jadex.runtime.planwrapper;

import jadex.runtime.IFilter;
import jadex.runtime.IMessageEvent;
import jadex.runtime.IMessageEventListener;
import jadex.runtime.ISystemEventListener;
import jadex.runtime.impl.AsynchronousSystemEventListener;
import jadex.runtime.impl.IRMessageEvent;
import jadex.runtime.impl.SystemEventMessageFilter;
import jadex.util.Tuple;


/**
 *  The user level view on a message event.
 *  Methods can only be called from plans,
 *  otherwise exceptions are thrown.
 */
public class MessageEventWrapper	extends EventWrapper	implements IMessageEvent
{
	//--------attributes --------

	/** The original event. */
	protected IRMessageEvent	event;

	//-------- constructors --------

	/**
	 *  Create a new MessageEventWrapper.
	 *  @param event	The original event.
	 */
	public MessageEventWrapper(IRMessageEvent event)
	{
		super(event);
		this.event	= event;
	}

	//-------- message event methods --------

	/**
	 *  Get the (platform specific) message object.
	 */
	public Object getMessage()
	{
		checkThreadAccess();
		return event.getMessage();
	}

	/**
	 *  Get the message direction.
	 *  @return True, if message is incoming.
	 * /
	public boolean isIncoming()
	{
		checkThreadAccess();
		return event.isIncoming();
	}*/

	/**
	 *  Get the content.
	 *  Allowed content objects depend on the platform.
	 *  @return The content.
	 */
	public Object getContent()
	{
		checkThreadAccess();
		return event.getContent();
	}

	/**
	 *  Set the content.
	 *  Allowed content objects depend on the platform.
	 *  @param content The content.
	 */
	public void setContent(Object content)
	{
		checkThreadAccess();
		event.setContent(content);
	}

	/**
	 *  Create a reply to this message event.
	 *  @param type	The reply message event type (defined in the ADF).
	 *  @return The reply event.
	 */
	public IMessageEvent	createReply(String type)
	{
		checkThreadAccess();
		return new MessageEventWrapper(event.createReply(type));
	}

	/**
	 *  Create a reply to this message event.
	 *  @param type	The reply message event type (defined in the ADF).
	 *  @param content	The message content (optional).
	 *  @return The reply event.
	 */
	public IMessageEvent	createReply(String type, Object content)
	{
		checkThreadAccess();
		return new MessageEventWrapper(event.createReply(type, content));
	}

	/**
	 *  Get the filter to wait for a reply.
	 *  @return The filter.
	 */
	public IFilter getFilter()
	{
		checkThreadAccess();
		return event.getFilter();		
	}
	
	//-------- listeners --------

	/**
	 *  Add a message event listener.
	 *  @param listener The message event listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addMessageEventListener(IMessageEventListener userlistener, boolean async)
	{
		checkThreadAccess();
		//IFilter filter = new SystemEventFilter(
		//	new String[]{ISystemEventTypes.MESSAGE_RECEIVED, ISystemEventTypes.MESSAGE_SENT}, unwrap());
		IFilter filter = new SystemEventMessageFilter((IRMessageEvent)unwrap());
		AsynchronousSystemEventListener listener 
			= new AsynchronousSystemEventListener(userlistener, new Tuple(userlistener, event));
		getCapability().addSystemEventListener(listener, filter, true, async);
	}
	
	/**
	 *  Remove a message event listener.
	 *  @param listener The message event listener.
	 */
	public void removeMessageEventListener(IMessageEventListener userlistener)
	{
		checkThreadAccess();
		Object	identifier	= new Tuple(userlistener, event);
		ISystemEventListener[] listeners = getAgent().getSystemEventListeners();
		for(int i=0; i<listeners.length; i++)
		{
			if((listeners[i] instanceof AsynchronousSystemEventListener) 
				&& ((AsynchronousSystemEventListener)listeners[i]).getIdentifier().equals(identifier))
			{
				getCapability().removeSystemEventListener(listeners[i]);
				break;
			}
		}
	}
}
