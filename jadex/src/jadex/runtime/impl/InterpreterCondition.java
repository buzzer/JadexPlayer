package jadex.runtime.impl;

import java.io.Serializable;
import java.util.Iterator;
import jadex.runtime.impl.agenda.*;
import jadex.runtime.*;
import jadex.util.SReflect;

/**
 *  The base class for interpreter conditions.
 */
public abstract class InterpreterCondition implements IInterpreterCondition, Serializable
{
	//-------- attributes --------

	/** The trace mode. */
	protected String	trace;

	/** The scope. */
	protected RCapability scope;

	/** Flag to indicate that the conditions was disabled (set to NEVER) manually. */
	protected boolean	disabled;

	/** The action to execute when the condition is triggered. */
	protected IAgendaAction action;

	//-------- constructor -------

	/**
	 *  Create a condition.
	 *  @param action	The action to execute when the condition is triggered.
	 */
	protected InterpreterCondition(IAgendaAction action, RCapability scope)
	{
		this.scope = scope;
		this.action = action;

		// Todo: should always use default (eg ANDed with custom precondition)?
		//this.precondition	= action.getPrecondition()==null ? new DefaultPrecondition() : precondition;
		this.trace	= ICondition.TRACE_NEVER;
	}

	/**
	 *  Perform any necessary clean up, when this element is
	 *  no longer used.
	 *  Subclasses may override this method to do their
	 *  cleanup operations. They must call super.cleanup
	 *  to ensure that the cleanedup property is set.
	 */
	public void	cleanup()
	{
		//(cleanedup) // todo
		//	return;

		setTraceMode(ICondition.TRACE_NEVER);

		// done automatically when tracemode is set to never.
		//getScope().getExpressionbase().removeCondition(this);
	}

	//-------- attribute accessors --------

	/**
	 *  Get the trace mode.
	 *  @return The trace mode.
	 */
	public String	getTraceMode()
	{
		return trace;
	}

	/**
	 *  Set the trace mode without generating events immediately.
	 *  @param trace	The new trace mode.
	 */
	public void	setTraceMode(String trace)
	{
		//if(getName().indexOf("keep")!=-1)
		//	System.out.println("+++++ : "+getName()+" "+trace);
		//System.out.println("Trace mode: "+trace+" "+this.getName());

		// Trace mode changed and valid?
		if(!trace.equals(this.trace))
		{
			if(trace.equals(ICondition.TRACE_NEVER))
			{
				scope.getExpressionbase().removeCondition(this);
				// Indicate that the conditions was disabled manually.
				disabled = true;
			}
			else if(trace.equals(ICondition.TRACE_ONCE) || trace.equals(ICondition.TRACE_ALWAYS))
			{
				scope.getExpressionbase().addCondition(this);
			}
			else
			{
				throw new RuntimeException("Unknown trace mode: "+trace);
			}
			this.trace	= trace;
		}
	}

	/**
	 *  Trace this condition once, until it is triggerd.
	 *  If the condition is now true, an event is immediately generated.
	 */
	public void	traceOnce()
	{
		this.setTraceMode(ICondition.TRACE_ONCE);
		doTrace(null);
	}

	/**
	 *  Trace this condition always, generating an event whenever
	 *  it evaluates to true. If the condition is now true, an event
	 *  is immediately generated.
	 */
	public void	traceAlways()
	{
		this.setTraceMode(ICondition.TRACE_ALWAYS);
		doTrace(null);
	}

	//-------- methods --------

	/**
	 *  Get the filter to wait for the condition.
	 *  @return The filter.
	 * /
	public IFilter	getFilter()
	{
		InternalEventFilter confi	= new InternalEventFilter(IMEventbase.TYPE_CONDITION_TRIGGERED);
		confi.addValue(IMEventbase.CONDITION, this);
		return confi;
	}*/

	/**
	 *  Execute the expression's action
	 *  when it is affected.
	 */
	public void	systemEventsOccurred(Iterator events)
	{
		// Is this ok?
		//assert !trace.equals(ICondition.TRACE_NEVER): this;

		// Condition is only affected when traced.
		if(trace.equals(ICondition.TRACE_NEVER))
			return;

		boolean	affected	= false;
		for(int i=0; !affected && events.hasNext(); i++)
		{
			SystemEvent syse = (SystemEvent)events.next();
			if(isAffected(syse))
			{
				//if(getName().indexOf("nothing_to_do")!=-1)// && getName().indexOf("achievecleanup")!=-1)
				//	System.out.println("affected: "+events[i].getType());
				//System.out.println("expression is affected: "+getName()+" :"+event);

				affected	= true;
				doTrace(syse);
			}
		}
	}

	/**
	 *  Trace the condition and execute the action, when it is triggered.
	 */
	protected void doTrace(SystemEvent cause)
	{
		if(isTriggered())
		{
			//if(getName().indexOf("walk_around_context")!=-1)
			//	System.out.println("triggered: ");

			if(action==null)
			{
				throw new RuntimeException("Error, condition has no action: "+this);
			}
			scope.getAgent().getInterpreter().addAgendaEntry(action, cause);
		}
	}

	/**
	 *  Set the action.
	 */
	public void setAction(IAgendaAction action)
	{
		this.action = action;
	}

	/**
	 *  Get the action.
	 */
	public IAgendaAction getAction()
	{
		return action;
	}

	/**
	 *  Test if disabled.
	 *  @return True, if disabled.
	 */
	public boolean isDisabled()
	{
		return disabled;
	}
	
	/**
	 *  Get the scope.
	 *  @return The scope.
	 */
	public RCapability getScope()
	{
		return scope;
	}
	
	/**
	 *  Generate a change event for this element.
	 *  @param event The event type.
	 *  (copied from RReferenceableElement)
	 */
	protected void throwSystemEvent(String event)
	{
		throwSystemEvent(new SystemEvent(event, this));
	}
	
	/**
	 *  Generate a change event for this element.
	 *  @param event The event.
	 *  (nearly copied from RReferenceableElement)
	 */
	public void throwSystemEvent(SystemEvent event)
	{
		if(getScope().getAgent().isTransactionStarted() && !event.isChangeRelevant())
		{
			// When info event occurs inside transaction just add it.
			getScope().addInfoEvent(event);
		}
		else
		{
			getScope().getAgent().startSystemEventTransaction();
			getScope().collectSystemEvents(event); // changed this as no references can occur
			getScope().getAgent().commitSystemEventTransaction();
		}
	}
	
	/**
	 *  Create a string representation of this element.
	 *  @return	This element represented as string.
	 */
	public String	toString()
	{
		StringBuffer	sb	= new StringBuffer();
		sb.append(SReflect.getInnerClassName(this.getClass()));
		sb.append(", trace=");
		sb.append(getTraceMode());
		sb.append(")");
		return sb.toString();
	}

	/**
	 *  Test if the condition is affected from an event.
	 *  @return True, if affected.
	 */
	public abstract boolean isAffected(SystemEvent event);

	/**
	 *  Evaluates the condition to a value that is triggered.
	 *  Note that this method modifies the variable lastvalue.
	 */
	public abstract boolean isTriggered();
}

