package jadex.runtime;

import jadex.runtime.planwrapper.*;
import jadex.runtime.impl.*;
import jadex.model.IMEventbase;
import jadex.model.ISystemEventTypes;

/**
 *  A plan (in our context more a plan body) contains
 *  actions for accomplishing a target state.
 *  Additionally to plan belongs (stored in plan info):
 *    - filters (waitqueuefilter, planfilter)
 *
 *  Subclasses of plan have to implement the action method.
 */
public abstract class Plan extends AbstractPlan
{
	//-------- attributes --------

	/** The initial event. */
	protected IEvent initialevent;

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public Plan()
	{
		// Is set by the dispatcher when it queues the plan
		// instance info to the ready list.
		this.initialevent = EventbaseWrapper.wrap(getRPlan().getInitialEvent());
	}

	//-------- methods --------

	/**
	 *  The body method is called on the
	 *  instatiated plan instance from the scheduler.
	 */
	public abstract void	body();

	/**
	 *  The passed method is called on plan success.
	 */
	public void	passed()
	{
	}

	/**
	 *  The failed method is called on plan failure/abort.
	 */
	public void	failed()
	{
	}

	/**
	 *  The plan was aborted (because of conditional goal
	 *  success or termination from outside).
	 */
	public void aborted()
	{
	}

	/**
	 *  Get the initial event, which might differ
	 *  from the plans rootgoal.
	 */
	public IEvent getInitialEvent()
	{
		getCapability().checkThreadAccess();
		return this.initialevent;
	}

	/**
	 *  Wait for a some time.
	 *  @param duration The duration.
	 */
	public void	waitFor(long duration)
	{
		getCapability().checkThreadAccess();
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.setTimeout(duration, getRPlan());
		eventWaitFor(wa);
	}

	/**
	 *  Wait for a condition to be satisfied.
	 *  @param condition The condition.
	 */
	public void	waitForCondition(ICondition condition)
	{
		waitForCondition(condition, -1);
	}

	/**
	 *  Wait for a condition or until the timeout occurs.
	 *  @param condition The condition.
	 *  @param timeout The timeout.
	 */
	public void waitForCondition(ICondition condition, long timeout)
	{
		getCapability().checkThreadAccess();
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addCondition(((ConditionWrapper)condition).getOriginalCondition());
		wa.setTimeout(timeout, getRPlan());
		eventWaitFor(wa);
	}

	/**
	 *  Wait for a condition to be satisfied.
	 *  @param condition The condition.
	 */
	public void	waitForCondition(String condition)
	{
		waitForCondition(condition, -1);
	}

	/**
	 *  Wait for a condition to be satisfied.
	 *  @param condition The condition.
	 */
	public void	waitForCondition(String condition, long timeout)
	{
		getCapability().checkThreadAccess();
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addCondition(condition, ICondition.TRACE_ONCE);
		wa.setTimeout(timeout, getRPlan());
		eventWaitFor(wa);
	}

	/**
	 *  A shortcut for dispatching a goal as subgoal of the active goal,,
	 *  and waiting for the subgoal to be finished (without timout).
	 *  @param subgoal The new subgoal.
	 *  @throws GoalFailureException	when the goal fails.
	 */
	public void dispatchSubgoalAndWait(IGoal subgoal) throws GoalFailureException
	{
		dispatchSubgoalAndWait(subgoal, -1);
	}

	/**
	 *  A shortcut for dispatching a goal as subgoal of the active goal
	 *  and waiting for the subgoal to be finished.
	 *  Additionally the subgoal will be dropped when finished.
	 *  This differs from the dispatchSubgoal implementation.
	 *  @param subgoal The new subgoal.
	 *  @param timeout	The timeout.
	 */
	public void dispatchSubgoalAndWait(IGoal subgoal, long timeout)
	{
		getCapability().checkThreadAccess();

		getRPlan().getScope().getAgent().startMonitorConsequences();
		IRGoal	rgoal	= (IRGoal)((GoalWrapper)subgoal).unwrap();
		RuntimeException	rex	= null;
		try
		{
			dispatchSubgoal(subgoal);
			WaitAbstraction wa = new WaitAbstraction(getRCapability());
			wa.addGoal(rgoal);
			wa.setTimeout(timeout, getRPlan());
			// Hack!!! Shouldn't wait inside monitoring consequences.
			eventWaitFor(wa);
			//return (IGoalEvent)event;
		}
		catch(RuntimeException e)
		{
			rex	= e;
		}

		getRPlan().getScope().getAgent().endMonitorConsequences();

		if(rex!=null)
		{
			throw rex;
		}
		else if(!subgoal.isSucceeded())
		{
			throw new GoalFailureException(subgoal, rgoal.getException());
		}
	}

	/**
	 *  Wait for an internal event.
	 *  @param type The internal event type.
	 */
	public IInternalEvent waitForInternalEvent(String type)
	{
		return waitForInternalEvent(type, -1);
	}

	/**
	 *  Wait for an internal event.
	 *  @param type The internal event type.
	 *  @param timeout The timeout.
	 */
	public IInternalEvent waitForInternalEvent(String type, long timeout)
	{
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addInternalEvent(type, null);	// Todo: also support match expression in API?
		wa.setTimeout(timeout, getRPlan());
		return (IInternalEvent)eventWaitFor(wa);
	}

	/**
	 *  Send a message and wait for the answer.
	 *  @param me The message event.
	 *  @return The result event.
	 */
	public IMessageEvent sendMessageAndWait(IMessageEvent me)
	{
		return sendMessageAndWait(me, -1);
	}

	/**
	 *  Send a message and wait for the answer.
	 *  Adds a reply-with entry if not present, for tracking the conversation.
	 *  @param me The message event.
	 *  @param timeout The timeout.
	 *  @return The result event.
	 */
	public IMessageEvent sendMessageAndWait(IMessageEvent me, long timeout)
	{
		sendMessage(me);
		return waitForReply(me, timeout);
	}

	/**
	 *  Wait for a message event.
	 *  @param type The message event type.
	 */
	public IMessageEvent waitForMessageEvent(String type)
	{
		return waitForMessageEvent(type, -1);
	}

	/**
	 *  Wait for a message event.
	 *  @param type The message event type.
	 *  @param timeout The timeout.
	 */
	public IMessageEvent waitForMessageEvent(String type, long timeout)
	{
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addMessageEvent(type, null);	// Todo: also support match expression in API?
		wa.setTimeout(timeout, getRPlan());
		return (IMessageEvent)eventWaitFor(wa);
	}

	/**
	 *  Wait for a message.
	 *  @param msgevent The message event.
	 */
	public IMessageEvent	waitForReply(IMessageEvent msgevent)
	{
		return waitForReply(msgevent, -1);
	}

	/**
	 *  Wait for a message.
	 *  @param msgevent The message event.
	 */
	public IMessageEvent	waitForReply(IMessageEvent msgevent, long timeout)
	{
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addMessageEvent((IRMessageEvent)((MessageEventWrapper)msgevent).unwrap());
		wa.setTimeout(timeout, getRPlan());
		IEvent ev = eventWaitFor(wa);
		return (IMessageEvent)ev;
		//return (IMessageEvent)eventWaitFor(wa);
	}

	/**
	 *  Wait for a goal.
	 *  @param type The goal type.
	 */
	public IGoal waitForGoal(String type)
	{
		return waitForGoal(type, -1);
	}

	/**
	 *  Wait for a goal.
	 *  @param type The goal type.
	 *  @param timeout The timeout.
	 */
	public IGoal waitForGoal(String type, long timeout)
	{
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addGoal(type, null);	// Todo: also support match expression in API?
		wa.setTimeout(timeout, getRPlan());
		IGoalEvent	event	= (IGoalEvent)eventWaitFor(wa);
		return event.getGoal();
	}

	/**
	 *  Wait for a goal.
	 *  @param goal The goal.
	 */
	public void waitForGoal(IGoal goal)
	{
		waitForGoal(goal, -1);
	}

	/**
	 *  Wait for a goal.
	 *  @param goal The goal.
	 *  @param timeout The timeout.
	 */
	public void waitForGoal(IGoal goal, long timeout)
	{
		// Only wait for non-finished goals (Hack???).
		if(!goal.isFinished())
		{
			IRGoal	rgoal	= (IRGoal)((GoalWrapper)goal).unwrap();
			if(rgoal.getParent()==getRPlan().getRootGoal())
			{
				throw new RuntimeException("Goal is a subgoal: "+goal);
			}
			WaitAbstraction wa = new WaitAbstraction(getRCapability());
			wa.addGoal(rgoal);
			wa.setTimeout(timeout, getRPlan());
			eventWaitFor(wa);
		}
	}

	/**
	 *  Wait for a goal.
	 *  @param goal The goal.
	 */
	public void waitForSubgoal(IGoal goal)
	{
		waitForSubgoal(goal, -1);
	}

	/**
	 *  Wait for a goal.
	 *  @param goal The goal.
	 *  @param timeout The timeout.
	 */
	public void waitForSubgoal(IGoal goal, long timeout)
	{
		// Only wait for non-finished goals (Hack???).
		if(!goal.isFinished())
		{
			IRGoal	rgoal	= (IRGoal)((GoalWrapper)goal).unwrap();
			if(rgoal.getParent()!=getRPlan().getRootGoal())
			{
				throw new RuntimeException("Goal not a subgoal: "+goal);
			}
			WaitAbstraction wa = new WaitAbstraction(getRCapability());
			wa.addGoal(rgoal);
			wa.setTimeout(timeout, getRPlan());
			eventWaitFor(wa);
		}
	}

	/**
	 *  Wait for a belief change.
	 *  @param type The internal event type.
	 *  @return The changed fact value.
	 */
	public Object waitForBeliefChange(String type)
	{
		return waitForBeliefChange(type, -1);
	}

	/**
	 *  Wait for a belief change.
	 *  @param type The belief type.
	 *  @param timeout The timeout.
	 */
	public Object waitForBeliefChange(String type, long timeout)
	{
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addBelief(type, ICondition.TRACE_ONCE);
		wa.setTimeout(timeout, getRPlan());
		IInternalEvent ev = (IInternalEvent)eventWaitFor(wa);
		SystemEvent se = (SystemEvent)ev.getParameter(IMEventbase.CAUSE).getValue();
		return se.getValue();
	}

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 */
	public void waitForBeliefSetChange(String type)
	{
		waitForBeliefSetChange(type, -1);
	}

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  @param timeout The timeout.
	 */
	public void waitForBeliefSetChange(String type, long timeout)
	{
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addBeliefSet(type, new String[]{ISystemEventTypes.BSFACT_ADDED,
			ISystemEventTypes.BSFACT_REMOVED, ISystemEventTypes.BSFACTS_CHANGED},
			ICondition.TRACE_ONCE);
		wa.setTimeout(timeout, getRPlan());
		eventWaitFor(wa);
	}

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  @return The fact that was added or removed.
	 */
	public Object waitForFactAddedOrRemoved(String type)
	{
		return waitForFactAddedOrRemoved(type, -1);
	}

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  @param timeout The timeout.
	 *  @return The fact that was added or removed.
	 */
	public Object waitForFactAddedOrRemoved(String type, long timeout)
	{
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addBeliefSet(type, new String[]{ISystemEventTypes.BSFACT_ADDED, 
			ISystemEventTypes.BSFACT_REMOVED}, ICondition.TRACE_ONCE);
		wa.setTimeout(timeout, getRPlan());
		IInternalEvent ev = (IInternalEvent)eventWaitFor(wa);
		SystemEvent se = (SystemEvent)ev.getParameter(IMEventbase.CAUSE).getValue();
		return se.getValue();
	}

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  @return The fact that was added.
	 */
	public Object waitForFactAdded(String type)
	{
		return waitForFactAdded(type, -1);
	}

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  @param timeout The timeout.
	 *  @return The fact that was added.
	 */
	public Object waitForFactAdded(String type, long timeout)
	{
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addBeliefSet(type, new String[]{ISystemEventTypes.BSFACT_ADDED},
			ICondition.TRACE_ONCE);
		wa.setTimeout(timeout, getRPlan());
		IInternalEvent ev = (IInternalEvent)eventWaitFor(wa);
		SystemEvent se = (SystemEvent)ev.getParameter(IMEventbase.CAUSE).getValue();
		return se.getValue();
	}

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  @return The fact that was added.
	 */
	public Object waitForFactRemoved(String type)
	{
		return waitForFactAdded(type, -1);
	}

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  @param timeout The timeout.
	 *  @return The fact that was added.
	 */
	public Object waitForFactRemoved(String type, long timeout)
	{
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addBeliefSet(type, new String[]{ISystemEventTypes.BSFACT_REMOVED},
			ICondition.TRACE_ONCE);
		wa.setTimeout(timeout, getRPlan());
		IInternalEvent ev = (IInternalEvent)eventWaitFor(wa);
		SystemEvent se = (SystemEvent)ev.getParameter("cause").getValue();
		return se.getValue();
	}

	//-------- helper methods --------

	/**
	 *  Wait for with thread handling.
	 *  @param wa The wait abstraction.
	 *  @return The event.
	 */
	// todo: remove this HACK!
	protected IEvent	eventWaitFor(WaitAbstraction wa)
	{
		getCapability().checkThreadAccess();
		IEvent ret = null;

		IPlanExecutor exe = getRPlan().getPlanExecutor();
		IREvent	event	= exe.eventWaitFor(getRPlan(), wa);
		if(event==null)
			throw new RuntimeException("eventWaitFor failed: "+this);
		ret = EventbaseWrapper.wrap(event);
		
		return ret;
	}

	//-------- deprecated methods --------

	/**
	 *  Wait for an event.
	 *  @param filter The event filter.
	 *  //@deprecated Should be avoided but in certain cases maybe cannot
	 */
	public IEvent	waitFor(IFilter filter)
	{
		return waitFor(filter, -1);
	}

	/**
	 *  Wait for an event or until the timeout occurs.
	 *  @param filter The event filter.
	 *  @param timeout The timeout.
	 *  //@deprecated Should be avoided but in certain cases maybe cannot
	 */
	public IEvent waitFor(IFilter filter, long timeout)
	{
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addFilter(filter);
		wa.setTimeout(timeout, getRPlan());
		return eventWaitFor(wa);
	}
}
