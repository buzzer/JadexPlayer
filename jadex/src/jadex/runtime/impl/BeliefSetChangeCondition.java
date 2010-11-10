package jadex.runtime.impl;

import jadex.runtime.impl.agenda.IAgendaAction;
import jadex.runtime.impl.agenda.conditions.ConditionDefaultAction2;
import jadex.runtime.*;
import jadex.model.*;
import jadex.util.SUtil;

/**
 *  A fast pure runtime condition for checking belief set changes.
 */
public class BeliefSetChangeCondition extends InterpreterCondition
{
	//-------- attributes --------

	/** The observed beliefset. */
	protected IRBeliefSet beliefset;

	/** The eventtypes. */
	protected String[] eventtypes;

	/** Flag to indicate that the condition ignores init events (when an element is created). */
	protected boolean initignoring;

	/** The old value. */
	protected Object oldvals;

	//-------- constructors --------

	/**
	 *  Create a new belief set condition.
	 */
	public BeliefSetChangeCondition(IRBeliefSet beliefset, Object[] oldvals, String[] eventtypes, IAgendaAction action)
	{
		super(null, beliefset.getScope());
		this.beliefset = beliefset;
		this.oldvals = oldvals;
		this.eventtypes = eventtypes;
		this.action = action==null? new ConditionDefaultAction2(this, beliefset.getScope()): action;
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
	 * /
	public boolean isAffected(SystemEvent event)
	{
		// todo: imrove by using isSubtype()
		boolean ret = false;
		if(event.getSource().equals(beliefset) && (!isInitIgnoring() || !event.isInit()))
		{
			for(int i=0; i<eventtypes.length && !ret; i++)
			{
				if(eventtypes[i].equals(event.getType()))
				{
					// For derived facts changed events check if value really changed.
					if(event.isDerived() && event.getType().equals(ISystemEventTypes.BSFACTS_CHANGED))
					{
						Object newvals = beliefset.getFacts();
						ret = !SUtil.arrayEquals(oldvals, newvals);
						if(ret)
							oldvals = newvals;
					}
					else
					{
						ret = true;
					}
				}
			}
		}
		return ret;
	}*/

	/**
	 * Test if the condition is affected from an event.
	 * @return True, if affected.
	 */
	public boolean isAffected(SystemEvent event)
	{
		boolean ret = false;
		if(event.getSource().equals(beliefset) && (!isInitIgnoring() || !event.isInit()))
		{
			if(ISystemEventTypes.Subtypes.isSubtype(event.getType(), eventtypes))
			{
				// For derived facts changed events check if value really changed.
				if(event.isDerived() && event.getType().equals(ISystemEventTypes.BSFACTS_CHANGED))
				{
					Object newvals = beliefset.getFacts();
					ret = !SUtil.arrayEquals(oldvals, newvals);
					if(ret)
						oldvals = newvals;
				}
				else
				{
					ret = true;
				}
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
