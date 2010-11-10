package jadex.runtime.impl;

import java.util.Iterator;
import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.agenda.conditions.ConditionDefaultAction;
import jadex.runtime.*;
import jadex.model.*;
import jadex.util.SReflect;

/**
 *  The condition element represents a condition that is true or false.
 *  A condition can execute actions, when it is traced.
 *  A condition is triggered (executes its action) when the trigger type
 *  matches the actual state change after an evaluation.
 *  Three states are possible: (t)rue, (f)alse and (u)nknown.
 *  The state is unknown initially and when it cannot be evaluated
 *  (this will cause a warning too).
 *  The following table illustrates the different triggering types and
 *  their effects:
 *
 *  is_true: t/f/u->t
 *  is_false: t/f/u->f
 *  changes_to_true: f/u->t
 *  changes_to_false: f/u->t
 *  always: t/f/u->t/f/u
 *  changes: x->!x (x means t/f/u)
 */
public class RCondition	extends RExpression	implements IRCondition, IInterpreterCondition
{
	//-------- attributes --------

	/** The last value. */
	protected Boolean lastvalue;

	/** The eventually overwritten trigger mode. */
	protected String trigger;

	/** The condition delegate. */
	protected InterpreterCondition condel;

	/** Flag to indicate that the condition ignores init events (when an element is created). */
	//protected boolean initignoring;

	//-------- constructor -------

	/**
	 *  Create a condition.
	 *  @param model	The model of this element.
	 *  @param owner	The owner of this element.
	 *  @param action	The action to execute when the condition is triggered.
	 */
	protected RCondition(final IMCondition model, RElement owner,
		RReferenceableElement creator, IAgendaAction action)
	{
		super(model, owner, creator, null);
		//System.out.println("RCondition created: "+getName()+" "+getScope().getName());
		//System.out.println("-+-+-+: "+model.getRelevantList());

		// Todo: should always use default (eg ANDed with custom precondition)?
		//this.precondition	= action.getPrecondition()==null ? new DefaultPrecondition() : precondition;
		//this.references = SCollection.createArrayList();

		this.condel = getDelegate(action);
	}

	/**
	 *  Get the condition delegate.
	 */
	protected InterpreterCondition getDelegate(IAgendaAction action)
	{
		return new InterpreterCondition(action==null? new ConditionDefaultAction(this): action, getScope())
		{
			public boolean isAffected(SystemEvent event)
			{
				return RCondition.this.isAffected(event);
			}

			public boolean isTriggered()
			{
				return RCondition.this.isTriggered();
			}
		};
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
		if(cleanedup)
			return;

		super.cleanup();

		setTraceMode(ICondition.TRACE_NEVER);

		if(isTemporary())
		{
			IMReferenceableElement elem = (IMReferenceableElement)getModelElement();
			((IMExpressionbase)getScope().getExpressionbase().getModelElement()).deleteReferenceableElement(elem);
		}
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
		return condel.getTraceMode();
	}

	/**
	 *  Set the trace mode without generating events immediately.
	 *  @param trace	The new trace mode.
	 */
	public void	setTraceMode(String trace)
	{
		condel.setTraceMode(trace);
	}

	/**
	 *  Trace this condition once, until it is triggerd.
	 *  If the condition is now true, an event is immediately generated.
	 */
	public void	traceOnce()
	{
		condel.traceOnce();
	}

	/**
	 *  Trace this condition always, generating an event whenever
	 *  it evaluates to true. If the condition is now true, an event
	 *  is immediately generated.
	 */
	public void	traceAlways()
	{
		condel.traceAlways();
	}

	/**
	 *  Evaluate the condition.
	 *  Updates the value and lastvalue attributes.
	 *  @return True, if the condition is satisfied, false otherwise
	 *  and null (unknown) when it could not be evaluated.
	 */
	public Boolean	evaluate()
	{
		Boolean value = null;

		try
		{
			value = (Boolean)getValue();
		}
		catch(Exception e)
		{
			//System.out.println("*ex* "+this.getName()+" "+e);
			e.printStackTrace();
			System.out.println("Expression could not be evaluated: "+this);
		}

		return value;
	}

	//-------- methods --------

	/**
	 *  Test if this condition ignores init events.
	 *  @return True, if the condition ignores init events.
	 * /
	public boolean isInitIgnoring()
	{
		return initignoring;
	}*/

	/**
	 *  Set if this condition ignores init events.
	 *  @param initignoring True, if this condition ignores init events.
	 * /
	public void setInitIgnoring(boolean initignoring)
	{
		this.initignoring = initignoring;
	}*/

	/**
	 *  Test, if an expression is affected by a bdi event.
	 *  True, if the expression is affected.
	 * /
	public boolean isAffected(SystemEvent event)
	{
		boolean ret = false;
		if(!isInitIgnoring() || !event.isInit())
			ret = super.isAffected(event);
		return ret;
	}*/

	/**
	 *  Evaluates the condition to a value that is triggered.
	 *  Note that this method modifies the variable lastvalue.
	 */
	public boolean isTriggered()
	{
		// Compute the actual value.
		Boolean value = evaluate();

		// Per default not triggered.
		boolean triggered = false;

		// Triggered when one of the following statements.
		Object triggertype = getTrigger();
		if(triggertype.equals(ICondition.TRIGGER_ALWAYS)
			|| (triggertype.equals(ICondition.TRIGGER_IS_TRUE) && value!=null && value.booleanValue())
			|| (triggertype.equals(ICondition.TRIGGER_IS_FALSE) && value!=null&& !value.booleanValue())
			|| (triggertype.equals(ICondition.TRIGGER_CHANGES_TO_TRUE) && value!=null && value.booleanValue()
				&& (lastvalue==null || !lastvalue.booleanValue()))
			|| (triggertype.equals(ICondition.TRIGGER_CHANGES_TO_FALSE) && value!=null && !value.booleanValue()
				&& (lastvalue==null || lastvalue.booleanValue()))
			|| (triggertype.equals(ICondition.TRIGGER_CHANGES) && ((value!=null && !value.equals(lastvalue))
				|| (lastvalue!=null && !lastvalue.equals(value)))))
		{
			triggered = true;
			// Stop tracing ONCE.
			if(ICondition.TRACE_ONCE.equals(condel.getTraceMode()))
			{
				// Hack!!! Do not use setTraceMode, to avoid action being removed from agenda.
				condel.trace = ICondition.TRACE_NEVER;
				getScope().getExpressionbase().removeCondition(condel);
			}
		}
		//if(triggered && getName().indexOf("achievecleanup")!=-1)
		//	System.out.println(":::isTriggered "+getName());

		// Save the last computed value.
		this.lastvalue = value;
		
		if(triggered)
			throwSystemEvent(ISystemEventTypes.CONDITION_TRIGGERED);

		return triggered;
	}

	/**
	 *  Get the last evaluated value
	 *  of the condition.
	 *  @return The last evaluated value.
	 */
	public Boolean getLastValue()
	{
		return this.lastvalue;
	}

	/**
	 *  Get the trigger mode.
	 */
	public Object getTrigger()
	{
		return trigger != null? trigger: ((IMCondition)getModelElement()).getTrigger();
	}

	/**
	 *  Set the trigger type.
	 *  @param trigger The trigger.
	 */
	public void setTrigger(String trigger)
	{
		this.trigger = trigger;
	}

	/**
	 *  Get the filter to wait for the condition.
	 *  @return The filter.
	 */
	public IFilter	getFilter()
	{
		InternalEventFilter confi	= new InternalEventFilter(IMEventbase.TYPE_CONDITION_TRIGGERED);
		confi.addValue(IMEventbase.CONDITION, this);
		return confi;
	}

	/**
	 *  Execute the expression's action
	 *  when it is affected.
	 */
	public void	systemEventsOccurred(Iterator events)
	{
		condel.systemEventsOccurred(events);
	}

	/**
	 *  Trace the condition and execute the action, when it is triggered.
	 */
	protected void doTrace(SystemEvent cause)
	{
		condel.doTrace(cause);
	}

	/**
	 *  Set the action.
	 */
	public void setAction(IAgendaAction action)
	{
		condel.setAction(action);
	}

	/**
	 *  Get the action.
	 */
	public IAgendaAction getAction()
	{
		return condel.getAction();
	}

	/**
	 *  Test if disabled.
	 *  @return True, if disabled.
	 */
	public boolean isDisabled()
	{
		return condel.isDisabled();
	}

	/**
	 *  Reset the condition.
	 *  Next time isTriggered is called, it will behave as
	 *  if it has just been created.
	 */
	public void	reset()
	{
		lastvalue	= null;
	}

	/**
	 *  Create a string representation of this element.
	 *  @return	This element represented as string.
	 */
	public String	toString()
	{
		StringBuffer	sb	= new StringBuffer();
		sb.append(SReflect.getInnerClassName(this.getClass()));
		sb.append("(name=");
		sb.append(name);
		//sb.append(", value=");
		//sb.append(evaluate());
		//sb.append(", expression=");
		//sb.append(getE);
		sb.append(", trace=");
		sb.append(getTraceMode());
		sb.append(")");
		return sb.toString();
	}
}

