package jadex.runtime.planwrapper;

import jadex.model.ISystemEventTypes;
import jadex.runtime.*;
import jadex.runtime.impl.AsynchronousSystemEventListener;
import jadex.runtime.impl.RPlan;
import jadex.runtime.impl.SystemEventFilter;
import jadex.util.Tuple;

/**
 *  The user level view on a plan.
 *  Methods can only be called from plans,
 *  otherwise exceptions are thrown.
 */
public class PlanWrapper extends ParameterElementWrapper implements IPlan
{
	//-------- attributes --------

	/** The waitqueue wrapper. */
	protected WaitqueueWrapper waitqueue;

	/** The plan. */
	protected RPlan original;

	//-------- constructors --------

	/**
	 *  Create a new goalbase wrapper.
	 */
	public PlanWrapper(RPlan original)
	{
		super(original);
		this.original = original;
	}

	/**
	 *  Get the waitqueue.
	 *  @return The waitqueue.
	 */
	public IWaitqueue getWaitqueue()
	{
		if(waitqueue==null)
			waitqueue = new WaitqueueWrapper(original.getWaitqueue());
		return waitqueue;
	}

	/**
	 *  Get the body.
	 *  @return The body.
	 */
	public Object getBody()
	{
		return original.getBody();
	}

	/**
	 *  Create the body.
	 */
	public Object createBody() throws Exception
	{
		return original.createBody();
	}
	
	//-------- listeners --------
	
	/**
	 *  Add a plan listener.
	 *  @param listener The plan listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addPlanListener(IPlanListener userlistener, boolean async)
	{
		IFilter filter = new SystemEventFilter(
			new String[]{ISystemEventTypes.PLAN_ADDED, ISystemEventTypes.PLAN_REMOVED}, unwrap());
		final AsynchronousSystemEventListener listener 
			= new AsynchronousSystemEventListener(userlistener, new Tuple(userlistener, original));
		getCapability().addSystemEventListener(listener, filter, true, async);
	}
	
	/**
	 *  Remove a plan listener.
	 *  @param listener The plan listener.
	 */
	public void removePlanListener(IPlanListener userlistener)
	{
		checkThreadAccess();
		Object	identifier	= new Tuple(userlistener, original);
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
