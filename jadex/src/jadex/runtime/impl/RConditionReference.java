package jadex.runtime.impl;

import jadex.model.*;
import jadex.runtime.*;

/**
 *  Reference for a condition.
 */
public class RConditionReference	extends RExpressionReference	implements IRCondition
{
	//-------- constructor -------

	/**
	 *  Create a condition.
	 *  @param modelelement	The model of this element.
	 *  @param owner	The owner of this element.
	 */
	protected RConditionReference(IMConditionReference modelelement,
			RElement owner, RReferenceableElement creator)
	{
 		super(modelelement, owner, creator);
	}

	//-------- attribute accessors --------

	/**
	 *  Get the trace mode.
	 *  @return The trace mode.
	 */
	public String	getTraceMode()
	{
		return ((IRCondition)getReferencedElement()).getTraceMode();
	}

	/**
	 *  Set the trace mode without generating events immediately.
	 *  @param trace	The new trace mode.
	 */
	public void	setTraceMode(String trace)
	{
		((IRCondition)getReferencedElement()).setTraceMode(trace);
	}

	/**
	 *  Trace this condition once, until it is triggerd.
	 *  If the condition is now true, an event is immediately generated.
	 */
	public void	traceOnce()
	{
		((IRCondition)getReferencedElement()).traceOnce();
	}

	/**
	 *  Trace this condition always, generating an event whenever
	 *  it evaluates to true. If the condition is now true, an event
	 *  is immediately generated.
	 */
	public void	traceAlways()
	{
		((IRCondition)getReferencedElement()).traceAlways();
	}

	/**
	 *  Evaluate the condition.
	 *  Updates the value and lastvalue attributes.
	 *  @return True, if the condition is satisfied, false otherwise
	 *  and null (unknown) when it could not be evaluated.
	 */
	public Boolean	evaluate()
	{
		return ((IRCondition)getReferencedElement()).evaluate();
	}

	//-------- methods --------


	/**
	 *  Get the filter to wait for the condition.
	 *  @return The filter.
	 */
	// Hack!!! Should call original?
	public IFilter	getFilter()
	{
		// How to wait for condition from other scope???
		InternalEventFilter confi	= new InternalEventFilter(IMEventbase.TYPE_CONDITION_TRIGGERED_REFERENCE);
		confi.addValue(IMEventbase.CONDITION, this);
		return confi;
	}
}
