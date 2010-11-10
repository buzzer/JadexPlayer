package jadex.runtime.planwrapper;

import jadex.runtime.*;
import jadex.runtime.externalaccesswrapper.ElementWrapper.AgentInvocation;
import jadex.runtime.impl.*;
import jadex.util.Tuple;
import jadex.model.*;


/**
 *  The user level view on an eventbase.
 *  Methods can only be called from plans,
 *  otherwise exceptions are thrown.
 */
public class EventbaseWrapper extends ElementWrapper implements IEventbase
{
	//--------attributes --------

	/** The original eventbase. */
	protected REventbase	eventbase;

	//-------- constructors --------

	/**
	 *  Create a new EventbaseWrapper.
	 *  @param eventbase	The original eventbase.
	 */
	protected EventbaseWrapper(REventbase eventbase)
	{
		super(eventbase);
		this.eventbase	= eventbase;
	}

	//-------- eventbase methods --------

	/**
	 *  Send a message.
	 *  @param me	The message event.
	 *  @return The filter to wait for an answer.
	 */
	public IFilter sendMessage(IMessageEvent me)
	{
		checkThreadAccess();
		return eventbase.sendMessage((IRMessageEvent)((EventWrapper)me).unwrap());
	}

	/**
	 *  Dispatch an event.
	 *  @param event The event.
	 */
	public void dispatchInternalEvent(IInternalEvent event)
	{
		checkThreadAccess();

		// Allow interrupt to process event.
		getCapability().getAgent().startMonitorConsequences();
		try
		{
			eventbase.dispatchInternalEvent((IRInternalEvent)((ElementWrapper)event).unwrap());
		}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Create a new message event.
	 *  @return The new message event.
	 */
	public IMessageEvent createMessageEvent(String type)
	{
		checkThreadAccess();
		return new MessageEventWrapper(eventbase.createMessageEvent(type));
	}

	/**
	 *  Create a new message event.
	 *  @return The new message event.
	 * /
	public IMessageEvent createMessageEventFromNative(Object message, Object content)
	{
		checkThreadAccess();
		return new MessageEventWrapper(eventbase.createMessageEventFromNative(message, content), getPlan());
	}*/

	/**
	 *  Create a new intenal event.
	 *  @return The new intenal event.
	 */
	public IInternalEvent createInternalEvent(String type)
	{
		checkThreadAccess();
		return new InternalEventWrapper(eventbase.createInternalEvent(type));
	}

	/**
	 *  Create a new intenal event.
	 *  @return The new intenal event.
	 */
	public IInternalEvent createInternalEvent(String type, Object content)
	{
		checkThreadAccess();
		return new InternalEventWrapper(eventbase.createInternalEvent(type, content));
	}

	/**
	 *  Register a new event model.
	 *  @param mevent The event model.
	 */
	public void registerEvent(final IMEvent mevent)
	{
		checkThreadAccess();
		eventbase.registerEvent(mevent);
	}

	/**
	 *  Register a new event reference model.
	 *  @param meventref The event reference model.
	 */
	public void registerEventReference(IMEventReference meventref)
	{
		checkThreadAccess();
		eventbase.registerEventReference(meventref);
	}

	/**
	 *  Deregister an event model.
	 *  @param mevent The event model.
	 */
	public void deregisterEvent(final IMEvent mevent)
	{
		checkThreadAccess();
		eventbase.deregisterEvent(mevent);
	}

	/**
	 *  Deregister an event reference model.
	 *  @param meventref The event reference model.
	 */
	public void deregisterEventReference(IMEventReference meventref)
	{
		checkThreadAccess();
		eventbase.deregisterEventReference(meventref);
	}

	//-------- listeners --------

	/**
	 *  Add a internal event listener.
	 *  @param listener The internal event listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addInternalEventListener(String type, IInternalEventListener userlistener, boolean async)
	{
		checkThreadAccess();
		IFilter filter = new SystemEventFilter(new String[]{ISystemEventTypes.INTERNAL_EVENT_OCCURRED}, type);
		AsynchronousSystemEventListener listener = new AsynchronousSystemEventListener(userlistener, new Tuple(new Object[]{userlistener, eventbase, type, IInternalEventListener.class}));
		getCapability().addSystemEventListener(listener, filter, true, async);
	}
	
	/**
	 *  Remove a internal event listener.
	 *  @param listener The internal event listener.
	 */
	public void removeInternalEventListener(String type, IInternalEventListener userlistener)
	{
		checkThreadAccess();
		Object	identifier	= new Tuple(new Object[]{userlistener, eventbase, type, IInternalEventListener.class});
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
	
	/**
	 *  Add a message event listener.
	 *  @param listener The message event listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addMessageEventListener(String type, IMessageEventListener userlistener, boolean async)
	{
		checkThreadAccess();
		IFilter filter = new SystemEventFilter(new String[]{ISystemEventTypes.MESSAGE_RECEIVED,
			ISystemEventTypes.MESSAGE_SENT}, type);
		AsynchronousSystemEventListener listener 
			= new AsynchronousSystemEventListener(userlistener, new Tuple(new Object[]{userlistener, eventbase, type, IMessageEventListener.class}));
		getCapability().addSystemEventListener(listener, filter, true, async);
	}

	/**
	 *  Remove a message event listener.
	 *  @param listener The message event listener.
	 */
	public void removeMessageEventListener(String type, IMessageEventListener userlistener)
	{
		checkThreadAccess();
		Object identifier	= new Tuple(new Object[]{userlistener, eventbase, type, IInternalEventListener.class});
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
	
	//-------- internal methods --------

	/**
	 *  Wrap an event to the corresponding wrapper element.
	 */
	public static IEvent wrap(IREvent event)
	{
		assert event!=null;
		IEvent ret;
		if(event instanceof IRInternalEvent)
			ret = new InternalEventWrapper((IRInternalEvent)event);
		else if(event instanceof IRMessageEvent)
			ret = new MessageEventWrapper((IRMessageEvent)event);
		else
			ret = new GoalEventWrapper((IRGoalEvent)event);

		return ret;
	}
}
