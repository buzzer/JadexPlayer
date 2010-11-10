package jadex.runtime.planwrapper;

import jadex.model.ISystemEventTypes;
import jadex.runtime.*;
import jadex.runtime.externalaccesswrapper.ElementWrapper.AgentInvocation;
import jadex.runtime.impl.*;
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
	protected ConditionWrapper(IRCondition condition)
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
		checkThreadAccess();
		return condition.getTraceMode();
	}

	/**
	 *  Set the trace mode without generating events immediately.
	 *  @param trace	The new trace mode.
	 */
	public void setTraceMode(String trace)
	{
		checkThreadAccess();
		condition.setTraceMode(trace);
	}

	/**
	 *  Trace this condition once, until it is triggerd.
	 *  If the condition is now true, an event is immediately generated.
	 */
	public void traceOnce()
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{condition.traceOnce();}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Trace this condition always, generating an event whenever
	 *  it evaluates to true. If the condition is now true, an event
	 *  is immediately generated.
	 */
	public void traceAlways()
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{condition.traceAlways();}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Get the filter to wait for the condition.
	 *  @return The filter.
	 */
	public IFilter getFilter()
	{
		checkThreadAccess();
		return condition.getFilter();
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
	public void addConditionListener(IConditionListener userlistener, boolean async)
	{
		checkThreadAccess();
		IFilter filter = new SystemEventFilter(new String[]{ISystemEventTypes.CONDITION_TRIGGERED}, unwrap());
		AsynchronousSystemEventListener listener 
			= new AsynchronousSystemEventListener(userlistener, new Tuple(userlistener, condition));
		getCapability().addSystemEventListener(listener, filter, true, async);
	}
	
	/**
	 *  Remove a condition listener.
	 *  @param listener The condition listener.
	 */
	public void removeConditionListener(IConditionListener userlistener)
	{
		checkThreadAccess();
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
}
