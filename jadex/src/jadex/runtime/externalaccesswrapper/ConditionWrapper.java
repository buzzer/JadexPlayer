package jadex.runtime.externalaccesswrapper;

import jadex.model.ISystemEventTypes;
import jadex.runtime.ICondition;
import jadex.runtime.IConditionListener;
import jadex.runtime.IFilter;
import jadex.runtime.ISystemEventListener;
import jadex.runtime.impl.AsynchronousSystemEventListener;
import jadex.runtime.impl.IRCondition;
import jadex.runtime.impl.SystemEventFilter;
import jadex.util.Tuple;


/**
 *  The user level view on a condition.
 *  Methods can only be called from plans,
 *  otherwise exceptions are thrown.
 */
public class ConditionWrapper	extends ExpressionWrapper	implements ICondition
{
	//-------- attributes --------

	/** The original plan. */
	protected IRCondition condition;

	//-------- constructors --------

	/**
	 *  Create a new goalbase wrapper.
	 */
	public ConditionWrapper(IRCondition condition) // make protected
	{
		super(condition);
		this.condition = condition;
	}

	//-------- methods --------

	/**
	 *  Get the trace mode.
	 */
	public String getTraceMode()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				string	= condition.getTraceMode();
			}
		};
		return exe.string;
	}

	/**
	 *  Set the trace mode without generating events immediately.
	 *  @param trace	The new trace mode.
	 */
	public void setTraceMode(final String trace)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				condition.setTraceMode(trace);
			}
		};
	}

	/**
	 *  Trace this condition once, until it is triggerd.
	 *  If the condition is now true, an event is immediately generated.
	 */
	public void traceOnce()
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				condition.traceOnce();
			}
		};
	}

	/**
	 *  Trace this condition always, generating an event whenever
	 *  it evaluates to true. If the condition is now true, an event
	 *  is immediately generated.
	 */
	public void traceAlways()
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				condition.traceAlways();
			}
		};
	}

	/**
	 *  Get the filter to wait for the condition.
	 *  @return The filter.
	 */
	public IFilter getFilter()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				object	= condition.getFilter();
			}
		};
		return (IFilter)exe.object;
	}

	//-------- helper methods --------

	/**
	 *  Get the original condition.
	 */
	public IRCondition	getOriginalCondition()
	{
		return condition;
	}
	
	//-------- listeners --------
	
	/**
	 *  Add a condition listener.
	 *  @param listener The condition listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addConditionListener(final IConditionListener userlistener, final boolean async)
	{
		new AgentInvocation()
		{
			public void run()
			{
				IFilter filter = new SystemEventFilter(new String[]{ISystemEventTypes.CONDITION_TRIGGERED}, unwrap());
				AsynchronousSystemEventListener listener = new AsynchronousSystemEventListener(userlistener, new Tuple(userlistener, condition));
				getCapability().addSystemEventListener(listener, filter, true, async);
			}
		};
	}
	
	/**
	 *  Remove a condition listener.
	 *  @param listener The condition listener.
	 */
	public void removeConditionListener(final IConditionListener userlistener)
	{
		new AgentInvocation()
		{
			public void run()
			{
				Object	identifier	= new Tuple(userlistener, condition);
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
