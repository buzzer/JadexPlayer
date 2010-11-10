package jadex.runtime;

import jadex.model.*;

/**
 *  Common interface for all conditions
 */
public interface ICondition	extends IExpression
{
	//-------- constants --------

	/** Do not trace this condition (the default). */
	public static final String	TRACE_NEVER	= "never";
	
	/** Always trace this condition (generate event whenever satisfied). */
	public static final String	TRACE_ALWAYS	= "always";
	
	/** Trace this condition once (until satisfied). */
	public static final String	TRACE_ONCE	= "once";

	/** The trigger modes. */

	/** Trigger on every evaluation regardless of state. */
	public static final String	TRIGGER_ALWAYS = IMCondition.TRIGGER_ALWAYS;

	/** Trigger when the state changes. */
	public static final String	TRIGGER_CHANGES = IMCondition.TRIGGER_CHANGES;

	/** Trigger when the state changes to false. */
	public static final String	TRIGGER_CHANGES_TO_FALSE = IMCondition.TRIGGER_CHANGES_TO_FALSE;

	/** Trigger when the state changes to true. */
	public static final String	TRIGGER_CHANGES_TO_TRUE = IMCondition.TRIGGER_CHANGES_TO_TRUE;

	/** Trigger when the state is to false. */
	public static final String	TRIGGER_IS_FALSE = IMCondition.TRIGGER_IS_FALSE;

	/** Trigger when the state is to true. */
	public static final String	TRIGGER_IS_TRUE = IMCondition.TRIGGER_IS_TRUE;

	//-------- attribute accessors --------
	
	/**
	 *  Get the trace mode.
	 */
	public String getTraceMode();

	/**
	 *  Set the trace mode without generating events immediately.
	 *  @param trace	The new trace mode.
	 */
	public void setTraceMode(String trace);

	/**
	 *  Trace this condition once, until it is triggerd.
	 *  If the condition is now true, an event is immediately generated.
	 */
	public void traceOnce();

	/**
	 *  Trace this condition always, generating an event whenever
	 *  it evaluates to true. If the condition is now true, an event
	 *  is immediately generated.
	 */
	public void traceAlways();

	/**
	 *  Get the filter to wait for the condition.
	 *  @return The filter.
	 */
	public IFilter getFilter();
	
	//-------- listeners --------
	
	/**
	 *  Add a condition listener.
	 *  @param listener The condition listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addConditionListener(IConditionListener listener, boolean async);
	
	/**
	 *  Remove a condition listener.
	 *  @param listener The condition listener.
	 */
	public void removeConditionListener(IConditionListener listener);
}