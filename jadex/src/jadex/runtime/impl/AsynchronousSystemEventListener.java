package jadex.runtime.impl;

import java.io.Serializable;

import jadex.model.ISystemEventTypes;
import jadex.runtime.AgentEvent;
import jadex.runtime.IAgentListener;
import jadex.runtime.IBeliefListener;
import jadex.runtime.IBeliefSetListener;
import jadex.runtime.IConditionListener;
import jadex.runtime.IGoalListener;
import jadex.runtime.IInternalEventListener;
import jadex.runtime.IMessageEventListener;
import jadex.runtime.IPlanListener;
import jadex.runtime.ISystemEventListener;
import jadex.runtime.SystemEvent;
import jadex.runtime.externalaccesswrapper.*;

/**
 *  Asynchronous listener for agent events, i.e. ProcessEventActions being executed.
 *  The filter can be used to match a specific event. Hence one can wait
 *  for the occurrence of some event, e.g. a goal info event of a goal.
 */
public class AsynchronousSystemEventListener implements ISystemEventListener, Serializable
{
	//-------- attributes --------
	
	/** The agent listener to inform about relevant changes. */
	protected Object listener;
	
	/** The identifier that can be used to remove the listener. */
	protected Object identifier;
	
	//-------- constructors --------
	
	/**
	 *  Create a new listener.
	 *  @param wa The wait abstraction.
	 */
	public AsynchronousSystemEventListener(Object listener, Object identifier)
	{
		this.listener = listener;
		this.identifier	= identifier;
	}

	//-------- methods --------

	/**
	 *  Notify that system events occured.
	 *  @see jadex.runtime.SystemEvent
	 */
	public void systemEventsOccurred(SystemEvent[] events)
	{
		for(int i=0; i<events.length; i++)
		{
			// Belief set
			if(ISystemEventTypes.BSFACT_ADDED.equals(events[i].getType()))
			{
				((IBeliefSetListener)listener).factAdded(new AgentEvent(
					new BeliefSetWrapper((IRBeliefSet)events[i].getSource()), events[i].getValue()));
			}
			else if(ISystemEventTypes.BSFACT_REMOVED.equals(events[i].getType()))
			{
				((IBeliefSetListener)listener).factRemoved(new AgentEvent(
					new BeliefSetWrapper((IRBeliefSet)events[i].getSource()), events[i].getValue()));
			}
			else if(ISystemEventTypes.BSFACT_CHANGED.equals(events[i].getType())
				|| ISystemEventTypes.BSFACTS_CHANGED.equals(events[i].getType()))
			{
				((IBeliefSetListener)listener).beliefSetChanged(new AgentEvent(
					new BeliefSetWrapper((IRBeliefSet)events[i].getSource()), events[i].getValue()));
			}
			
			// Belief
			else if(ISystemEventTypes.FACT_CHANGED.equals(events[i].getType()))
			{	
				((IBeliefListener)listener).beliefChanged(new AgentEvent(
					new BeliefWrapper((IRBelief)events[i].getSource()), events[i].getValue()));
			}
			
			// Goal
			else if(ISystemEventTypes.GOAL_ADDED.equals(events[i].getType()))
			{	
				IRGoal goal = (IRGoal)events[i].getSource();
				((IGoalListener)listener).goalAdded(new AgentEvent(new GoalWrapper(goal)));
			}
			else if(ISystemEventTypes.GOAL_REMOVED.equals(events[i].getType()))
			{	
				IRGoal goal = (IRGoal)events[i].getSource();
				((IGoalListener)listener).goalFinished(new AgentEvent(new GoalWrapper(goal)));
			}
			/*else if(ISystemEventTypes.GOAL_CHANGED.equals(events[i].getType()))
			{	
				IRGoal goal = (IRGoal)events[i].getSource();
				if(goal.isFinished())
					((IGoalListener)listener).goalFinished(new AgentEvent(new GoalWrapper(goal)));
			}*/
			// todo: maintain goals
			
			// Messages
			else if(ISystemEventTypes.MESSAGE_SENT.equals(events[i].getType()))
			{	
				IRMessageEvent me = (IRMessageEvent)events[i].getSource();
				((IMessageEventListener)listener).messageEventSent(new AgentEvent(new MessageEventWrapper(me)));
			}
			else if(ISystemEventTypes.MESSAGE_RECEIVED.equals(events[i].getType()))
			{	
				IRMessageEvent me = (IRMessageEvent)events[i].getSource();
				((IMessageEventListener)listener).messageEventReceived(new AgentEvent(new MessageEventWrapper(me)));
			}
			
			// Internal event.
			else if(ISystemEventTypes.INTERNAL_EVENT_OCCURRED.equals(events[i].getType()))
			{	
				IRInternalEvent ie = (IRInternalEvent)events[i].getSource();
				((IInternalEventListener)listener).internalEventOccurred(new AgentEvent(new InternalEventWrapper(ie)));
			}
			
			// Condition triggered event.
			else if(ISystemEventTypes.CONDITION_TRIGGERED.equals(events[i].getType()))
			{	
				IRCondition cond = (IRCondition)events[i].getSource();
				((IConditionListener)listener).conditionTriggered(new AgentEvent(new ConditionWrapper(cond)));
			}
			
			// Plan events.
			else if(ISystemEventTypes.PLAN_ADDED.equals(events[i].getType()))
			{	
				RPlan plan = (RPlan)events[i].getSource();
				((IPlanListener)listener).planAdded(new AgentEvent(new PlanWrapper(plan)));
			}
			else if(ISystemEventTypes.PLAN_REMOVED.equals(events[i].getType()))
			{
				RPlan plan = (RPlan)events[i].getSource();
				((IPlanListener)listener).planFinished(new AgentEvent(new PlanWrapper(plan)));
			}
			
			// Agent events.
			else if(ISystemEventTypes.AGENT_TERMINATING.equals(events[i].getType()))
			{
				RBDIAgent agent = (RBDIAgent)events[i].getSource();
				((IAgentListener)listener).agentTerminating(new AgentEvent(new ExternalAccess(agent)));
			}
		}
	}
	
	/**
	 *  Get the listener.
	 *  @return The listener.
	 */
	public Object getIdentifier()
	{
		return identifier;
	}

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public String toString()
	{
		return "AsynchronousSystemEventListener(listener="+listener+")";
	}
}
