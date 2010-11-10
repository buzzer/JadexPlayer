package jadex.runtime.impl;

import jadex.runtime.IFilter;

/**
 *  Common interface for all conditions
 */
public interface IRCondition	extends IRExpression
{
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

	/**
	 *  Get the scope.
	 */
	public RCapability getScope();

	/**
	 *  Evaluate the condition.
	 *  Updates the value and lastvalue attributes.
	 *  @return True, if the condition is satisfied, false otherwise
	 *  and null (unknown) when it could not be evaluated.
	 */
	public Boolean	evaluate();
}