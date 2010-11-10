package jadex.runtime.externalaccesswrapper;

import jadex.model.ISystemEventTypes;
import jadex.runtime.*;
import jadex.runtime.impl.*;
import jadex.runtime.impl.agenda.IAgendaAction;

/**
 *  Provides 'plan'-similar methods for agent access from non-agent threads.
 *  Cannot provide subgoal related methods as there is no plan context.
 *  The context is a certain capability, which's elements can be accessed.
 */
public class ExternalAccess extends CapabilityWrapper implements IExternalAccess
{	
	//-------- constructors --------

	/**
	 *  Create a new CapabilityWrapper.
	 *  @param cap	The original capability.
	 */
	public ExternalAccess(RCapability cap)
	{
		super(cap);
	}
	
	//-------- goalbase shortcut methods --------

	/**
	 *  Dispatch a new top-level goal.
	 *  @param goal The new goal.
	 *  Note: plan step is interrupted after call.
	 */
	public void dispatchTopLevelGoal(IGoal goal)
	{
		getGoalbase().dispatchTopLevelGoal(goal);
	}

	/**
	 *  Create a goal from a template goal.
	 *  To be processed, the goal has to be dispatched as subgoal
	 *  or adopted as top-level goal.
	 *  @param type	The template goal name as specified in the ADF.
	 *  @return The created goal.
	 */
	public IGoal createGoal(String type)
	{
		return getGoalbase().createGoal(type);
	}

	//-------- eventbase shortcut methods --------

	/**
	 *  Send a message after some delay.
	 *  @param me	The message event.
	 *  @return The filter to wait for an answer.
	 */
	public IFilter	sendMessage(IMessageEvent me)
	{
		return getEventbase().sendMessage(me);
	}

	/**
	 *  Dispatch an internal event.
	 *  @param event The event.
	 *  Note: plan step is interrupted after call.
	 */
	public void dispatchInternalEvent(IInternalEvent event)
	{
		getEventbase().dispatchInternalEvent(event);
	}

	/**
	 *  Create a new message event.
	 *  @return The new message event.
	 */
	public IMessageEvent createMessageEvent(String type)
	{
		return getEventbase().createMessageEvent(type);
	}

	/**
	 *  Create a new intenal event.
	 *  @return The new intenal event.
	 */
	public IInternalEvent createInternalEvent(String type)
	{
		return getEventbase().createInternalEvent(type);
	}

	/**
	 *  Create a new intenal event.
	 *  @return The new intenal event.
	 *  @deprecated Convenience method for easy conversion to new explicit internal events.
	 *  Will be removed in later releases.
	 */
	public IInternalEvent createInternalEvent(String type, Object content)
	{
		return getEventbase().createInternalEvent(type, content);
	}

	//-------- methods --------

	/**
	 *  Wait for a some time.
	 *  @param duration The duration.
	 */
	public void	waitFor(long duration)
	{
		performWait(duration, null);
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
	public void waitForCondition(final ICondition condition, final long timeout)
	{
		IRCondition cond = (IRCondition)((ConditionWrapper)condition).unwrap();
		IFilter filter = new SystemEventFilter(new String[]{
			ISystemEventTypes.CONDITION_TRIGGERED, ISystemEventTypes.AGENT_DIED}, cond);
		SynchronousSystemEventListener listener = new SynchronousSystemEventListener();
		addListener(listener, filter);

		boolean changed = false;
		if(cond.getTraceMode().equals(ICondition.TRACE_NEVER))
		{
			cond.traceOnce();
			changed = true;
		}
		performWait(timeout, listener);
		if(changed)
			cond.setTraceMode(ICondition.TRACE_NEVER);
		evaluateListenerResult(listener);
	}

	/**
	 *  Wait for a condition to be satisfied.
	 *  @param condition The condition.
	 */
	public void waitForCondition(String condition)
	{
		waitForCondition(condition, -1);
	}

	/**
	 *  Wait for a condition to be satisfied.
	 *  @param condtext The condition.
	 */
	public void waitForCondition(String condtext, long timeout)
	{
		IRCondition cond = getCapability().getExpressionbase().createCondition(condtext);
		IFilter filter = new SystemEventFilter(new String[]{
			ISystemEventTypes.CONDITION_TRIGGERED, ISystemEventTypes.AGENT_DIED}, cond);
		SynchronousSystemEventListener listener = new SynchronousSystemEventListener();
		addListener(listener, filter);
		if(cond.getTraceMode().equals(ICondition.TRACE_NEVER))
			cond.traceOnce();
		performWait(timeout, listener);
		cond.cleanup();
		evaluateListenerResult(listener);
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
	public IInternalEvent waitForInternalEvent(final String type, final long timeout)
	{
		IFilter filter = new SystemEventFilter(new String[]{
			ISystemEventTypes.INTERNAL_EVENT_OCCURRED, ISystemEventTypes.AGENT_DIED}, type);
		SynchronousSystemEventListener listener = new SynchronousSystemEventListener();
		addListener(listener, filter);
		SystemEvent se = eventWaitFor(timeout, listener);
		return new InternalEventWrapper((IRInternalEvent)se.getSource());
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
		IFilter filter = new SystemEventMessageFilter((IRMessageEvent)((MessageEventWrapper)me).unwrap());
		SynchronousSystemEventListener listener = new SynchronousSystemEventListener();
		addListener(listener, filter);
		getEventbase().sendMessage(me);
		SystemEvent se = eventWaitFor(timeout, listener);
		return new MessageEventWrapper(((IRMessageEvent)se.getSource()));
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
		IFilter filter = new SystemEventFilter(new String[]{
			ISystemEventTypes.MESSAGE_RECEIVED, ISystemEventTypes.AGENT_DIED}, type);
		SynchronousSystemEventListener listener = new SynchronousSystemEventListener();
		addListener(listener, filter);
		SystemEvent se = eventWaitFor(timeout, listener);
		return new MessageEventWrapper(((IRMessageEvent)se.getSource()));
	}

	/**
	 *  Wait for a message.
	 *  @param msgevent The message event.
	 */
	public IMessageEvent	waitForReply(IMessageEvent me)
	{
		return waitForReply(me, -1);
	}

	/**
	 *  Wait for a message.
	 *  @param msgevent The message event.
	 */
	public IMessageEvent	waitForReply(IMessageEvent me, long timeout)
	{
		IFilter filter = new SystemEventMessageFilter((IRMessageEvent)((MessageEventWrapper)me).unwrap());
		SynchronousSystemEventListener listener = new SynchronousSystemEventListener();
		addListener(listener, filter);
		SystemEvent se = eventWaitFor(timeout, listener);
		return new MessageEventWrapper(((IRMessageEvent)se.getSource()));
	}

	/**
	 * Dispatch a top level goal and wait for the result.
	 * @param goal The goal.
	 */
	public void dispatchTopLevelGoalAndWait(final IGoal goal)
	{
		dispatchTopLevelGoalAndWait(goal, -1);
	}

	/**
	 *  Dispatch a top level goal and wait for the result.
	 *  @param goal The goal.
	 */
	public void dispatchTopLevelGoalAndWait(IGoal goal, long timeout)
	{
		IFilter filter = new SystemEventFilter(new String[]{
			ISystemEventTypes.GOAL_REMOVED, ISystemEventTypes.AGENT_DIED}, ((GoalWrapper)goal).unwrap());
		IRGoal rgoal = (IRGoal)((GoalWrapper)goal).unwrap();
		SynchronousSystemEventListener listener = new SynchronousSystemEventListener();
		addListener(listener, filter);
		dispatchTopLevelGoal(goal);
		eventWaitFor(timeout, listener);
		
		// Drop the top-level goal if not pursued until now.
		if(!rgoal.isFinished())
			rgoal.drop();

		// When goal failed, throw goal failure exception.
		if(rgoal.isFailed())
			throw new GoalFailureException(goal, rgoal.getException());
	}

	/**
	 *  Wait for a goal.
	 *  @param type The goal type.
	 */
	public void waitForGoal(String type)
	{
		waitForGoal(type, -1);
	}

	/**
	 *  Wait for a goal.
	 *  @param type The goal type.
	 *  @param timeout The timeout.
	 */
	public void waitForGoal(String type, long timeout)
	{
		IFilter filter = new SystemEventFilter(new String[]{
			ISystemEventTypes.GOAL_REMOVED, ISystemEventTypes.AGENT_DIED}, type);
		SynchronousSystemEventListener listener = new SynchronousSystemEventListener();
		addListener(listener, filter);
		eventWaitFor(timeout, listener);
	}

	/**
	 *  Wait for a belief change.
	 *  @param type The internal event type.
	 *  @return The new fact.
	 */
	public Object waitForBeliefChange(String type)
	{
		return waitForBeliefChange(type, -1);
	}

	/**
	 *  Wait for a belief change.
	 *  @param type The belief type.
	 *  @param timeout The
	 *  @return The new fact.
	 */
	public Object waitForBeliefChange(String type, long timeout)
	{
		IFilter filter = new SystemEventFilter(
			new String[]{ISystemEventTypes.FACT_CHANGED, ISystemEventTypes.AGENT_DIED}, type);
		SynchronousSystemEventListener listener = new SynchronousSystemEventListener();
		addListener(listener, filter);
		SystemEvent se = eventWaitFor(timeout, listener);
		return se.getValue();
	}

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  todo: returns a condition triggered event? or new BeliefChanged event?
	 */
	public void  waitForBeliefSetChange(String type)
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
		IFilter filter = new SystemEventFilter(new String[]{ISystemEventTypes.BSFACT_ADDED,
			ISystemEventTypes.BSFACT_REMOVED, ISystemEventTypes.BSFACTS_CHANGED, ISystemEventTypes.AGENT_DIED}, type);
		SynchronousSystemEventListener listener = new SynchronousSystemEventListener();
		addListener(listener, filter);
		eventWaitFor(timeout, listener);
	}

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  @return The added or removed fact.
	 */
	public Object waitForFactAddedOrRemoved(String type)
	{
		return waitForFactAddedOrRemoved(type, -1);
	}

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  @param timeout The timeout.
	 *  @return The added or removed fact.
	 */
	public Object waitForFactAddedOrRemoved(final String type, final long timeout)
	{
		IFilter filter = new SystemEventFilter(new String[]{ISystemEventTypes.BSFACT_ADDED,
			ISystemEventTypes.BSFACT_REMOVED, ISystemEventTypes.AGENT_DIED}, type);
		SynchronousSystemEventListener listener = new SynchronousSystemEventListener();
		addListener(listener, filter);
		SystemEvent se = eventWaitFor(timeout, listener);
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
		IFilter filter = new SystemEventFilter(new String[]{
			ISystemEventTypes.BSFACT_ADDED, ISystemEventTypes.AGENT_DIED}, type);
		SynchronousSystemEventListener listener = new SynchronousSystemEventListener();
		addListener(listener, filter);
		SystemEvent se = eventWaitFor(timeout, listener);
		return se.getValue();
	}

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  @return The fact that was added.
	 */
	public Object waitForFactRemoved(String type)
	{
		return waitForFactRemoved(type, -1);
	}

	/**
	 *  Wait for a belief set change.
	 *  @param type The belief set type.
	 *  @param timeout The timeout.
	 *  @return The fact that was added.
	 */
	public Object waitForFactRemoved(String type, long timeout)
	{
		IFilter filter = new SystemEventFilter(new String[]{
			ISystemEventTypes.BSFACT_REMOVED, ISystemEventTypes.AGENT_DIED}, type);
		SynchronousSystemEventListener listener = new SynchronousSystemEventListener();
		addListener(listener, filter);
		SystemEvent se = eventWaitFor(timeout, listener);
		return se.getValue();
	}

	/**
	 *  Wait for the agent to terminate.
	 */
	public void	waitForAgentTerminating()
	{
		waitForAgentTerminating(-1);
	}

	/**
	 *  Wait for the agent to terminate.
	 */
	public void	waitForAgentTerminating(long timeout)
	{
		IFilter filter = new SystemEventFilter(new String[]{
			ISystemEventTypes.AGENT_TERMINATING, ISystemEventTypes.AGENT_DIED});
		SynchronousSystemEventListener listener = new SynchronousSystemEventListener();
		addListener(listener, filter);
		eventWaitFor(timeout, listener);
	}
	
	//-------- helper methods --------
	
	/**
	 *  Wait for events specified by the wait abstraction.
	 *  @param wa The wait abstraction.
	 *  @param listener The listener.
	 */
	protected SystemEvent eventWaitFor(long timeout, SynchronousSystemEventListener listener)
	{
		//checkThreadAccess();
		performWait(timeout, listener);
		return evaluateListenerResult(listener);
	}	
	
	/**
	 *  Perform the waiting.
	 */
	protected void performWait(long timeout, SynchronousSystemEventListener listener)
	{
		// Check if external thread (otherwise waiting would cause deadlock).
		if(!getAgent().getInterpreter().isExternalThread())
		{
			if(listener!=null)
			{
				removeListener(listener);
			}
			throw new RuntimeException("Calling blocking methods on external access is only allowed from external threads.");
		}
		
		// Create dummy listener for just waiting for time.
		// Little hack, but we need an object as monitor anyways and SynchronousSystemEventListener is pretty lightweight.
		if(listener==null)
		{
			listener	= new SynchronousSystemEventListener();
		}
		
		// Add a timetable entry for the timeout.
		if(timeout!=-1)
		{
			// Create timetable entry to notify listener. 
			final SynchronousSystemEventListener	thelistener	= listener;
			final TimetableData	entry	= new TimetableData(timeout, new IAgendaAction()
			{
				public void execute()
				{
					if(!thelistener.isNotified())
					{
						synchronized(thelistener)
						{
							thelistener.notify();
						}
					}
				}
				
				public boolean isValid()
				{
					return !thelistener.isNotified();
				}
			});
			
			// Add timetable entry to agent.
			new AgentInvocation()
			{
				public void run()
				{
					getCapability().getAgent().addTimetableEntry(entry);
				}
			};
		}
		
		// Wait for event or timeout.
		try
		{
			synchronized(listener)
			{
				if(!listener.isNotified())
				{
					listener.wait();
				}
			}
		}
		catch(InterruptedException ex)
		{
		}
	}
	
	/** 
	 *  Evaluate the listener result.
	 *  @param listener The listener.
	 *  @return The system event.
	 *  @throws AgentDeathException when agent died during wait.
	 *  @throws TimeoutException if timeout during wait.
	 */
	protected SystemEvent evaluateListenerResult(final SynchronousSystemEventListener listener)
	{
		// Some cleanup code.
		removeListener(listener);

		// When agent died, throw agent death exception.
		if(listener.isAgentDied())
			throw new AgentDeathException(getCapability().getAgent());

		// When no event was received, throw timeout exception.
		else if(listener.getEvent()==null && listener!=null)
			throw new TimeoutException();
		
		return listener.getEvent();
	}
	
	/**
	 *  Add a listener to the agent.
	 */	
	protected void	addListener(final SynchronousSystemEventListener listener, final IFilter filter)
	{
		new AgentInvocation()
		{
			public void run()
			{
				getCapability().addSystemEventListener(listener, filter);
			}
		};
	}

	/**
	 *  Remove a listener from the agent.
	 */	
	protected void	removeListener(final SynchronousSystemEventListener listener)
	{
		new AgentInvocation()
		{
			public void run()
			{
				getCapability().removeSystemEventListener(listener);
			}
		};
	}
}
