package jadex.runtime.externalaccesswrapper;

import jadex.model.IMEvent;
import jadex.model.IMEventReference;
import jadex.model.ISystemEventTypes;
import jadex.runtime.*;
import jadex.runtime.impl.*;
import jadex.util.Tuple;


/**
 *  The user level view on an eventbase.
 *  Methods can only be called from plans,
 *  otherwise exceptions are thrown.
 */
public class EventbaseWrapper	extends ElementWrapper	implements IEventbase
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
		super(eventbase.getScope().getAgent(), eventbase);
		this.eventbase	= eventbase;
	}

	//-------- eventbase methods --------

	/**
	 *  Send a message.
	 *  @param me	The message event.
	 *  @return The filter to wait for an answer.
	 */
	// Hack!!! GUI cannot use filter???
	public IFilter sendMessage(final IMessageEvent me)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object = eventbase.sendMessage((IRMessageEvent)((EventWrapper)me).unwrap());
			}
		};
		return (IFilter)exe.object;
	}

	/**
	 *  Dispatch an event.
	 *  @param event The event.
	 */
	public void dispatchInternalEvent(final IInternalEvent event)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				eventbase.dispatchInternalEvent((IRInternalEvent)((InternalEventWrapper)event).unwrap());
			}
		};
	}

	/**
	 *  Create a new message event.
	 *  @return The new message event.
	 */
	public IMessageEvent createMessageEvent(final String type)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object = eventbase.createMessageEvent(type);
			}
		};
		return new MessageEventWrapper((IRMessageEvent)exe.object);
	}


	/**
	 *  Create a new message event.
	 *  @return The new message event.
	 * /
	public IMessageEvent createMessageEventFromNative(final Object message, final Object content)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object = eventbase.createMessageEventFromNative(message, content);
			}
		};
		return new MessageEventWrapper((IRMessageEvent)exe.object);
	}*/

	/**
	 *  Create a new intenal event.
	 *  @return The new intenal event.
	 */
	public IInternalEvent createInternalEvent(final String type)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object =  eventbase.createInternalEvent(type);
			}
		};
		return new InternalEventWrapper((IRInternalEvent)exe.object);
	}

	/**
	 *  Create a new intenal event.
	 *  @return The new intenal event.
	 */
	public IInternalEvent createInternalEvent(final String type, final Object content)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object =  eventbase.createInternalEvent(type, content);
			}
		};
		return new InternalEventWrapper((IRInternalEvent)exe.object);
	}

	/**
	 *  Register a new event model.
	 *  @param mevent The event model.
	 */
	public void registerEvent(final IMEvent mevent)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				eventbase.registerEvent(mevent);
			}
		};
	}

	/**
	 *  Register a new event reference model.
	 *  @param mevent The event reference model.
	 */
	public void registerEventReference(final IMEventReference mevent)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				eventbase.registerEventReference(mevent);
			}
		};
	}

	/**
	 *  Deregister an event model.
	 *  @param mevent The event model.
	 */
	public void deregisterEvent(final IMEvent mevent)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				eventbase.deregisterEvent(mevent);
			}
		};
	}


	/**
	 *  Deregister an event reference model.
	 *  @param mevent The event reference model.
	 */
	public void deregisterEventReference(final IMEventReference mevent)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				eventbase.deregisterEventReference(mevent);
			}
		};
	}

	//-------- listeners --------

	/**
	 *  Add a internal event listener.
	 *  @param listener The internal event listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addInternalEventListener(final String type, final IInternalEventListener userlistener, final boolean async)
	{
		new AgentInvocation()
		{
			public void run()
			{
				IFilter filter = new SystemEventFilter(new String[]{ISystemEventTypes.INTERNAL_EVENT_OCCURRED}, type);
				AsynchronousSystemEventListener listener = new AsynchronousSystemEventListener(userlistener, new Tuple(new Object[]{userlistener, eventbase, type, IInternalEventListener.class}));
				getCapability().addSystemEventListener(listener, filter, true, async);
			}
		};
	}
	
	/**
	 *  Remove a internal event listener.
	 *  @param listener The internal event listener.
	 */
	public void removeInternalEventListener(final String type, final IInternalEventListener userlistener)
	{
		new AgentInvocation()
		{
			public void run()
			{
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
		};
	}
	
	/**
	 *  Add a message event listener.
	 *  @param listener The message event listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addMessageEventListener(final String type, final IMessageEventListener userlistener, final boolean async)
	{
		new AgentInvocation()
		{
			public void run()
			{
				IFilter filter = new SystemEventFilter(new String[]{ISystemEventTypes.MESSAGE_RECEIVED,
					ISystemEventTypes.MESSAGE_SENT}, type);
				AsynchronousSystemEventListener listener = new AsynchronousSystemEventListener(userlistener, new Tuple(new Object[]{userlistener, eventbase, type, IMessageEventListener.class}));
				getCapability().addSystemEventListener(listener, filter, true, async);
			}
		};
	}
	
	/**
	 *  Remove a message event listener.
	 *  @param listener The message event listener.
	 */
	public void removeMessageEventListener(final String type, final IMessageEventListener userlistener)
	{
		new AgentInvocation()
		{
			public void run()
			{
				Object	identifier	= new Tuple(new Object[]{userlistener, eventbase, type, IMessageEventListener.class});
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
		};
	}
	
	//-------- internal methods --------

	/**
	 *  Wrap an event to the corresponding wrapper element.
	 */
	public static IEvent wrap(IREvent event)
	{
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
