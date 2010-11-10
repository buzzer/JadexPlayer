package jadex.runtime.impl;

import jadex.runtime.*;
import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.agenda.conditions.ConditionDefaultAction2;
import jadex.model.*;
import jadex.util.SUtil;

/**
 *  A fast pure runtime condition for checking belief changes.
 */
public class BeliefChangeCondition extends InterpreterCondition
{
	//-------- attributes --------

	/** The observed belief. */
	protected IRBelief belief;

	/** The old value. */
	protected Object oldval;

	/** Flag to indicate that the condition ignores init events (when an element is created). */
	protected boolean initignoring;

	//-------- constructors --------

	/**
	 *  Create a new belief condition.
	 */
	public BeliefChangeCondition(IRBelief belief, Object oldval, IAgendaAction action)
	{
		super(null, belief.getScope());
		this.belief = belief;
		this.oldval = oldval;
		this.action = action==null? new ConditionDefaultAction2(this, belief.getScope()): action;
		this.initignoring = true;
	}

	//-------- methods --------

	/**
	 *  Test if this condition ignores init events.
	 *  @return True, if the condition ignores init events.
	 */
	public boolean isInitIgnoring()
	{
		return initignoring;
	}

	/**
	 *  Set if this condition ignores init events.
	 *  @param initignoring True, if this condition ignores init events.
	 */
	public void setInitIgnoring(boolean initignoring)
	{
		this.initignoring = initignoring;
	}

	/**
	 * Test if the condition is affected from an event.
	 * @return True, if affected.
	 */
	public boolean isAffected(SystemEvent event)
	{
		boolean ret = false;
		if(event.getSource().equals(belief) && (!isInitIgnoring() || !event.isInit()))
		{
			ret = event.getType().equals(ISystemEventTypes.FACT_CHANGED);
			if(ret && event.isDerived())
			{
				Object newval = belief.getFact();
				ret = !SUtil.equals(oldval, newval);
				if(ret)
					oldval = newval;
			}
		}
		return ret;
	}

	/**
	 * Evaluates the condition to a value that is triggered.
	 * Note that this method modifies the variable lastvalue.
	 */
	public boolean isTriggered()
	{
		throwSystemEvent(ISystemEventTypes.CONDITION_TRIGGERED);
		return true;
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

}
