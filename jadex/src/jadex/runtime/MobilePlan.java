package jadex.runtime;

import java.util.List;

import jadex.runtime.impl.*;
import jadex.runtime.planwrapper.*;
import jadex.model.ISystemEventTypes;

/**
 *  A plan (in our context more a plan body) contains
 *  actions for accomplishing a target state.
 *  Additionally to plan belongs (stored in plan info):
 *    - filters (waitqueuefilter, planfilter)
 *
 *  Subclasses of plan have to implement the action method.
 */
public abstract class MobilePlan extends AbstractPlan
{
	//-------- methods --------

	/**
	 *  The action methods is called on the
	 *  instatiated plan instance from the scheduler.
	 */
	public abstract void	action(IEvent event);

	/**
	 *  The exception method is called when an exception
	 *  occurred during plan processing. The default behaviour
	 *  is that the exception will be thrown and the plan fails.
	 *  Method can be overridden for handling the exception.
	 */
	public void	exception(Exception exception) throws Exception
	{
		throw exception;
	}

	/**
	 *  @deprecated Replaced by passed(event).
	 */
	public final void	passed()
	{
	}

	/**
	 *  @deprecated Replaced by failed(event).
	 */
	public final void	failed()
	{
	}

	/**
	 *  @deprecated Replaced by aborted(event).
	 */
	public final void aborted()
	{
	}

	/**
	 *  The passed method is called on plan success.
	 *  
	 *  @param event	The event (for subsequent calls, initially null). 
	 */
	public void	passed(IEvent event)
	{
	}

	/**
	 *  The failed method is called on plan failure/abort.
	 *  
	 *  @param event	The event (for subsequent calls, initially null). 
	 */
	public void	failed(IEvent event)
	{
	}

	/**
	 *  The plan was aborted (because of conditional goal
	 *  success or termination from outside).
	 *  
	 *  @param event	The event (for subsequent calls, initially null). 
	 */
	public void aborted(IEvent event)
	{
	}

	/**
	 *  Wait for a some time.
	 *  @param duration The duration.
	 */
	public IFilter	waitFor(long duration)
	{
		getCapability().checkThreadAccess();
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.setTimeout(duration, getRPlan());
		return getRPlan().waitFor(wa);
	}

	/**
	 *  Wait for a condition to be satisfied.
	 *  @param condition The condition.
	 */
	public IFilter	waitForCondition(ICondition condition)
	{
		return waitForCondition(condition, -1);
	}

	/**
	 *  Wait for a condition or until the timeout occurs.
	 *  @param condition The condition.
	 *  @param timeout The timeout.
	 */
	public IFilter waitForCondition(ICondition condition, long timeout)
	{
		getCapability().checkThreadAccess();
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addCondition(((ConditionWrapper)condition).getOriginalCondition());
		wa.setTimeout(timeout, getRPlan());
		return getRPlan().waitFor(wa);
	}

	/**
	 *  Wait for a condition to be satisfied.
	 *  @param condition The condition.
	 */
	public IFilter	waitForCondition(String condition)
	{
		return waitForCondition(condition, -1);
	}

	/**
	 *  Wait for a condition to be satisfied.
	 *  @param condition The condition.
	 */
	public IFilter	waitForCondition(String condition, long timeout)
	{
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addCondition(condition, ICondition.TRACE_ONCE);
		wa.setTimeout(timeout, getRPlan());
		return getRPlan().waitFor(wa);
	}

	/**
	 *  A shortcut for dispatching a subgoal,
	 *  and waiting for the subgoal to be finished (without timout).
	 *  @param subgoal The new subgoal.
	 *  @return The eventfilter for identifying the result event.
	 */
	public IFilter dispatchSubgoalAndWait(IGoal subgoal)
	{
		return dispatchSubgoalAndWait(subgoal, -1);
	}

	/**
	 *  A shortcut for dispatching a subgoal,
	 *  and waiting for the subgoal to be finished (with timout).
	 *  @param subgoal The new subgoal.
	 *  @param timeout The timeout.
	 *  @return The eventfilter for identifying the result event.
	 */
	public IFilter dispatchSubgoalAndWait(IGoal subgoal, long timeout)
	{
		dispatchSubgoal(subgoal);
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addGoal((IRGoal)((GoalWrapper)subgoal).unwrap());
		wa.setTimeout(timeout, getRPlan());
		return getRPlan().waitFor(wa);
	}

	/**
	 *  Wait for an internal event.
	 *  @param type The internal event type.
	 */
	public IFilter waitForInternalEvent(String type)
	{
		return waitForInternalEvent(type, -1);
	}

	/**
	 *  Wait for an internal event.
	 *  @param type The internal event type.
	 *  @param timeout The timeout.
	 */
	public IFilter waitForInternalEvent(String type, long timeout)
	{
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addInternalEvent(type, null);	// Todo: also support match expression in API?
		wa.setTimeout(timeout, getRPlan());
		return getRPlan().waitFor(wa);
	}

	/**
	 *  Send a message and wait for the answer.
	 *  @param me The message event.
	 *  @return The eventfilter for identifying the result event.
	 */
	public IFilter sendMessageAndWait(IMessageEvent me)
	{
		return sendMessageAndWait(me, -1);
	}

	/**
	 *  Send a message and wait for the answer.
	 *  Adds a reply-with entry if not present, for tracking the conversation.
	 *  @param me The message event.
	 *  @param timeout The timeout.
	 *  @return The eventfilter for identifying the result event.
	 */
	public IFilter sendMessageAndWait(IMessageEvent me, long timeout)
	{
		// Timeout is used for keeping track of conversations.
		//((IRMessageEvent)((ElementWrapper)me).unwrap()).setTimeout(timeout);
		sendMessage(me);
		return waitForReply(me, timeout);
	}

	/**
	 *  Wait for a message event.
	 *  @param type The message event type.
	 */
	public IFilter waitForMessageEvent(String type)
	{
		return waitForMessageEvent(type, -1);
	}

	/**
	 *  Wait for a message event.
	 *  @param type The message event type.
	 *  @param timeout The timeout.
	 */
	public IFilter waitForMessageEvent(String type, long timeout)
	{
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addMessageEvent(type, null);	// Todo: also support match expression in API?
		wa.setTimeout(timeout, getRPlan());
		return getRPlan().waitFor(wa);
	}

	/**
	 *  Wait for a message.
	 *  @param msgevent The message event.
	 */
	public IFilter	waitForReply(IMessageEvent msgevent)
	{
		return waitForReply(msgevent, -1);
	}

	/**
	 *  Wait for a message.
	 *  @param msgevent The message event.
	 */
	public IFilter	waitForReply(IMessageEvent msgevent, long timeout)
	{
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.setTimeout(timeout, getRPlan());
		wa.addMessageEvent((IRMessageEvent)((MessageEventWrapper)msgevent).unwrap());
		return getRPlan().waitFor(wa);
	}

	/**
	 *  Wait for a goal.
	 *  @param type The goal type.
	 */
	public IFilter waitForGoal(String type)
	{
		return waitForGoal(type, -1);
	}

	/**
	 *  Wait for a goal.
	 *  @param type The goal type.
	 *  @param timeout The timeout.
	 */
	public IFilter waitForGoal(String type, long timeout)
	{
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addGoal(type, null);	// Todo: also support match expression in API?
		wa.setTimeout(timeout, getRPlan());
		return getRPlan().waitFor(wa);
	}

	/**
	 *  Wait for a goal.
	 *  @param goal The goal.
	 */
	public IFilter waitForSubgoal(IGoal goal)
	{
		return waitForSubgoal(goal, -1);
	}

	/**
	 *  Wait for a goal.
	 *  @param goal The goal.
	 *  @param timeout The timeout.
	 */
	public IFilter waitForSubgoal(IGoal goal, long timeout)
	{
		IRGoal	rgoal	= (IRGoal)((GoalWrapper)goal).unwrap();
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addGoal(rgoal);
		wa.setTimeout(timeout, getRPlan());
		IFilter	filter	= getRPlan().waitFor(wa);
		
		// Directly assign event for finished goal (hack???).
		if(goal.isFinished())
		{
			IREvent	info	= null;
			RGoal	orig	= (RGoal)rgoal.getOriginalElement();
			List	events	= orig.getScope().getEventbase().createGoalEvent(orig, true).getAllOccurrences();
			for(int i=0; i<events.size(); i++)
			{
				IREvent	event	= (IREvent)events.get(i); 
				if(event.getScope().equals(rgoal.getScope()))
					info	= event;
			}
			assert info!=null : this;
			getRPlan().assignNewEvent(info);
			getRPlan().schedule(info);
		}
		
		return filter;
	}

	/**
	 *  Wait for a belief change.
	 *  @param type The internal event type.
	 *  todo: returns a condition triggered event? or new BeliefChanged event?
	 */
	public IFilter waitForBeliefChange(String type)
	{
		return waitForBeliefChange(type, -1);
	}

	/**
	 *  Wait for a belief change.
	 *  @param type The belief type.
	 *  @param timeout The
	 *  todo: returns a condition triggered event? or new BeliefChanged event?
	 */
	public IFilter waitForBeliefChange(String type, long timeout)
	{
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addBelief(type, ICondition.TRACE_ONCE);
		wa.setTimeout(timeout, getRPlan());
		return getRPlan().waitFor(wa);
	}

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  todo: returns a condition triggered event? or new BeliefChanged event?
	 */
	public IFilter waitForBeliefSetChange(String type)
	{
		return waitForBeliefSetChange(type, -1);
	}

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  @param timeout The timeout.
	 *  todo: returns a condition triggered event? or new BeliefChanged event?
	 */
	public IFilter waitForBeliefSetChange(String type, long timeout)
	{
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addBeliefSet(type, new String[]{ISystemEventTypes.BSFACT_ADDED, 
			ISystemEventTypes.BSFACT_REMOVED, ISystemEventTypes.BSFACTS_CHANGED},
			ICondition.TRACE_ONCE);
		wa.setTimeout(timeout, getRPlan());
		return getRPlan().waitFor(wa);
	}

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  todo: returns a condition triggered event? or new BeliefChanged event?
	 */
	public IFilter waitForFactAddedOrRemoved(String type)
	{
		return waitForFactAddedOrRemoved(type, -1);
	}

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  @param timeout The timeout.
	 *  todo: returns a condition triggered event? or new BeliefChanged event?
	 */
	public IFilter waitForFactAddedOrRemoved(String type, long timeout)
	{
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addBeliefSet(type, new String[]{ISystemEventTypes.BSFACT_ADDED, 
			ISystemEventTypes.BSFACT_REMOVED}, ICondition.TRACE_ONCE);
		wa.setTimeout(timeout, getRPlan());
		return getRPlan().waitFor(wa);
	}

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  @return The fact that was added.
	 */
	public IFilter waitForFactAdded(String type)
	{
		return waitForFactAdded(type, -1);
	}

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  @param timeout The timeout.
	 *  @return The fact that was added.
	 */
	public IFilter waitForFactAdded(String type, long timeout)
	{
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addBeliefSet(type, new String[]{ISystemEventTypes.BSFACT_ADDED},
			ICondition.TRACE_ONCE);
		wa.setTimeout(timeout, getRPlan());
		return getRPlan().waitFor(wa);
	}

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  @return The fact that was added.
	 */
	public IFilter waitForFactRemoved(String type)
	{
		return waitForFactAdded(type, -1);
	}

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  @param timeout The timeout.
	 *  @return The fact that was added.
	 */
	public IFilter waitForFactRemoved(String type, long timeout)
	{
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addBeliefSet(type, new String[]{ISystemEventTypes.BSFACT_REMOVED},
			ICondition.TRACE_ONCE);
		wa.setTimeout(timeout, getRPlan());
		return getRPlan().waitFor(wa);
	}

	//-------- deprecated methods ---------

	/**
	 *  Wait for an event.
	 *  @param filter The event filter.
	 *  @deprecated
	 */
	public IFilter	waitFor(IFilter filter)
	{
		return waitFor(filter, -1);
	}

	/**
	 *  Wait for an event or until the timeout occurs.
	 *  @param filter The event filter.
	 *  @param timeout The timeout.
	 *  @deprecated
	 */
	public IFilter waitFor(IFilter filter, long timeout)
	{
		getCapability().checkThreadAccess();
		WaitAbstraction wa = new WaitAbstraction(getRCapability());
		wa.addFilter(filter);
		wa.setTimeout(timeout, getRPlan());
		return getRPlan().waitFor(wa);
	}

	/**
	 *  Wait for an event or until  the condition is satisfied.
	 *  @param filter The event filter.
	 *  @param condition The condition.
	 * /
	public IFilter waitFor(IFilter filter, ICondition condition)
	{
		return internalWaitFor(filter, condition, -1);
	}*/

	/**
	 *  Wait for an event or until the condition is satisfied or the timeout occurs.
	 *  @param filter The event filter.
	 *  @param timeout The timeout.
	 * /
	public IFilter internalWaitFor(IFilter filter, ICondition condition, long timeout)
	{
		getCapability().checkThreadAccess();
		return internalWaitFor(filter, condition, timeout);
	}*/
}
