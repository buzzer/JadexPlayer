package jadex.runtime.planwrapper;

import jadex.runtime.*;
import jadex.runtime.externalaccesswrapper.BeliefSetWrapper;
import jadex.runtime.impl.*;

/**
 *  The wrapper for the waitqueue from plans.
 */
public class WaitqueueWrapper implements IWaitqueue
{
	//-------- attributes --------

	/** The original wait queue. */
	protected Waitqueue waitqueue;

	//-------- constructors --------

	/**
	 *  Create a new goalbase wrapper.
	 */
	public WaitqueueWrapper(Waitqueue waitqueue)
	{
		this.waitqueue = waitqueue;
	}

	//-------- methods --------

	/**
	 * Get all waitqueue events.
	 * @return The suitable events.
	 */
	public IEvent[] getEvents()
	{
		checkThreadAccess();
		IREvent[] evs = waitqueue.getEvents();
		IEvent[] ret = new IEvent[evs.length];
		for(int i=0; i<evs.length; i++)
			ret[i] = EventbaseWrapper.wrap(evs[i]);
		return ret;
	}

	/**
	 * Get all waitqueue events that match the filter.
	 * Removes the matching events from the waitqueue.
	 * @param filter The filter.
	 * @return The suitable events.
	 */
	public IEvent[] getEvents(IFilter filter)
	{
		checkThreadAccess();
		IREvent[] evs = waitqueue.getEvents(filter);
		IEvent[] ret = new IEvent[evs.length];
		for(int i=0; i<evs.length; i++)
			ret[i] = EventbaseWrapper.wrap(evs[i]);
		return ret;
	}

	/**
	 * Add a message event.
	 * @param type The type.
	 */
	public void addMessageEvent(String type)
	{
		checkThreadAccess();
		waitqueue.addMessageEvent(type, null);	// Todo: also support match expression in API?
	}

	/**
	 * Add a message event reply.
	 * @param me The message event.
	 */
	public void addReply(IMessageEvent me)
	{
		checkThreadAccess();
		IRMessageEvent ime = (IRMessageEvent)((MessageEventWrapper)me).unwrap();
		waitqueue.addMessageEvent(ime);
	}

	/**
	 * Add an internal event.
	 * @param type The type.
	 */
	public void addInternalEvent(String type)
	{
		checkThreadAccess();
		waitqueue.addInternalEvent(type, null);	// Todo: also support match expression in API?
	}

	/**
	 * Add a goal.
	 * @param type The type.
	 */
	public void addGoal(String type)
	{
		checkThreadAccess();
		waitqueue.addGoal(type, null);	// Todo: also support match expression in API?
	}

	/**
	 * Add a subgoal.
	 * @param subgoal The subgoal.
	 */
	public void addSubgoal(IGoal subgoal)
	{
		checkThreadAccess();
		waitqueue.addGoal((IRGoal)((GoalWrapper)subgoal).unwrap());
	}

	/**
	 * Add a belief.
	 * @param type The type.
	 */
	public void addBelief(String type)
	{
		checkThreadAccess();
		waitqueue.addBelief(type, ICondition.TRACE_ALWAYS);
	}
	
	/**
	 * Add a belief.
	 * @param type The type.
	 */
	public void addBelief(IBelief bel)
	{
		checkThreadAccess();
		waitqueue.addBelief((IRBelief)((BeliefWrapper)bel).unwrap(), ICondition.TRACE_ALWAYS);
	}

	/**
	 *  Add a belief set.
	 *  @param type The type.
	 *  @param eventtype The event type.
	 */
	public void addBeliefSet(String type, String[] eventtype)
	{
		checkThreadAccess();
		waitqueue.addBeliefSet(type, eventtype, ICondition.TRACE_ALWAYS);
	}
	
	/**
	 *  Add a belief set.
	 *  @param bel set The belief set.
	 *  @param eventtype The eventtype.
	 */
	public void addBeliefSet(IBeliefSet belset, String[] eventtype)
	{
		checkThreadAccess();
		waitqueue.addBeliefSet((IRBeliefSet)((BeliefSetWrapper)belset).unwrap(), 
			eventtype, ICondition.TRACE_ALWAYS);
	}

	/**
	 * Add a user filter.
	 * @param filter The user filter.
	 */
	public void addFilter(IFilter filter)
	{
		checkThreadAccess();
		waitqueue.addFilter(filter);
	}

	/**
	 * Add a condition.
	 * @param condition The condition.
	 */
	public void addCondition(String condition)
	{
		checkThreadAccess();
		waitqueue.addCondition(condition, ICondition.TRACE_ALWAYS);
	}

	/**
	 * Add a condition.
	 * @param condition The condition.
	 */
	public void addCondition(ICondition condition)
	{
		checkThreadAccess();
		waitqueue.addCondition((IRCondition)((ConditionWrapper)condition).unwrap());
	}

	/**
	 * Remove a message event.
	 * @param type The type.
	 */
	public void removeMessageEvent(String type)
	{
		checkThreadAccess();
		waitqueue.removeMessageEvent(type);
	}

	/**
	 *  Remove a message event reply.
	 *  @param me The message event.
	 */
	public void removeReply(IMessageEvent me)
	{
		checkThreadAccess();
		IRMessageEvent ime = (IRMessageEvent)((MessageEventWrapper)me).unwrap();
		waitqueue.removeMessageEvent(ime);
	}

	/**
	 * Remove an internal event.
	 * @param type The type.
	 */
	public void removeInternalEvent(String type)
	{
		checkThreadAccess();
		waitqueue.removeInternalEvent(type);
	}

	/**
	 * Remove a goal.
	 * @param type The type.
	 */
	public void removeGoal(String type)
	{
		checkThreadAccess();
		waitqueue.removeGoal(type);
	}

	/**
	 * Remove a goal.
	 * @param goal The goal.
	 */
	public void removeGoal(IGoal goal)
	{
		checkThreadAccess();
		waitqueue.removeGoal((IRGoal)((GoalWrapper)goal).unwrap());
	}

	/**
	 * Remove a belief.
	 * @param type The type.
	 */
	public void removeBelief(String type)
	{
		checkThreadAccess();
		waitqueue.removeBelief(type);
	}
	

	/**
	 * Remove a belief.
	 * @param bel The bel.
	 */
	public void removeBelief(IRBelief bel)
	{
		checkThreadAccess();
		waitqueue.removeBelief(bel);
	}

	/**
	 * Remove a belief set.
	 * @param type The type.
	 */
	public void removeBeliefSet(String type)
	{
		checkThreadAccess();
		waitqueue.removeBeliefSet(type);
	}
	
	/**
	 * Remove a belief set.
	 * @param belset The belief set.
	 */
	public void removeBeliefSet(IRBeliefSet belset)
	{
		checkThreadAccess();
		waitqueue.removeBeliefSet(belset);
	}

	/**
	 * Remove a user filter.
	 * @param filter The user filter.
	 */
	public void removeFilter(IFilter filter)
	{
		checkThreadAccess();
		waitqueue.removeFilter(filter);
	}

	/**
	 * Remove a condition.
	 * @param condition The condition.
	 */
	public void removeCondition(String condition)
	{
		checkThreadAccess();
		waitqueue.removeCondition(condition);
	}

	/**
	 * Remove a condition.
	 * @param condition The condition.
	 */
	public void removeCondition(ICondition condition)
	{
		checkThreadAccess();
		waitqueue.removeCondition((IRCondition)((ConditionWrapper)condition).unwrap());
	}

	/**
	 *  Get the number of events in the waitqueue.
	 */
	public int	size()
	{
		checkThreadAccess();
		return waitqueue.size();
	}

	/**
	 *  Test if the waitqueue is empty.
	 */
	public boolean	isEmpty()
	{
		checkThreadAccess();
		return waitqueue.isEmpty();
	}

	//-------- helpers --------

	/**
	 *  Check if the plan thread is accessing.
	 *  @return True, if access is ok.
	 */
	public boolean checkThreadAccess0()
	{
		return !waitqueue.getCapability().getAgent().getInterpreter().isExternalThread();
	}

	/**
	 *  Check if the plan thread is accessing.
	 *  @throws RuntimeException when wrong thread (e.g. GUI) is calling agent methods.
	 */
	public void checkThreadAccess()
	{
		if(!checkThreadAccess0())
		{
			throw new RuntimeException("Wrong thread calling plan interface: "+Thread.currentThread());
		}
	}
}
