package jadex.runtime.externalaccesswrapper;

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

	/** The original plan. */
	protected RPlan plan;

	/** The waitqueue wrapper. */
	protected WaitqueueWrapper waitqueue;

	//-------- constructors --------

	/**
	 *  Create a new goalbase wrapper.
	 */
	public PlanWrapper(RPlan plan)
	{
		super(plan);
		this.plan = plan;
	}

	/**
	 *  Get the waitqueue.
	 *  @return The waitqueue.
	 */
	public IWaitqueue getWaitqueue()
	{
		if(waitqueue==null)
			waitqueue = new WaitqueueWrapper(getAgent(), plan.getWaitqueue());
		return waitqueue;
	}

	/**
	 *  Get the body.
	 *  @return The body.
	 */
	public Object getBody()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object =  plan.getBody();
			}
		};
		return exe.object;
	}

	/**
	 *  Create the body.
	 */
	public Object createBody() throws Exception
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				try
				{
					object =  plan.createBody();
				}
				catch(Exception e)
				{
					exception = e;
				}
			}
		};
		if(exe.exception!=null)
			throw exe.exception;
		
		return exe.object;
	}
	
	//-------- listeners --------
	
	/**
	 *  Add a plan listener.
	 *  @param listener The plan listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addPlanListener(final IPlanListener userlistener, final boolean async)
	{
		new AgentInvocation()
		{
			public void run()
			{
				IFilter filter = new SystemEventFilter(
					new String[]{ISystemEventTypes.PLAN_ADDED, ISystemEventTypes.PLAN_REMOVED}, unwrap());
				final AsynchronousSystemEventListener listener 
					= new AsynchronousSystemEventListener(userlistener, new Tuple(userlistener, plan));
				getCapability().addSystemEventListener(listener, filter, true, async);
			}
		};
	}
	
	/**
	 *  Remove a plan listener.
	 *  @param listener The plan listener.
	 */
	public void removePlanListener(final IPlanListener userlistener)
	{
		new AgentInvocation()
		{
			public void run()
			{
				Object	identifier	= new Tuple(userlistener, plan);
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
}
