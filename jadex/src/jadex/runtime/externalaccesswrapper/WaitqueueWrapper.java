package jadex.runtime.externalaccesswrapper;

import jadex.runtime.*;
import jadex.runtime.impl.*;

/**
 *  The wrapper for the waitqueue from plans.
 */
public class WaitqueueWrapper implements IWaitqueue
{
	//-------- attributes --------

	/** The original event. */
	protected Waitqueue waitqueue;

	/** The agent. */
	private RBDIAgent agent;

	//-------- constructors --------

	/**
	 *  Create a new goalbase wrapper.
	 */
	public WaitqueueWrapper(RBDIAgent agent, Waitqueue waitqueue)
	{
		this.agent = agent;
		this.waitqueue = waitqueue;
	}

	//-------- methods --------

	/**
	 * Get all waitqueue events.
	 * @return The suitable events.
	 */
	public IEvent[] getEvents()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				oarray = waitqueue.getEvents();
			}
		};
		IREvent[] evs = (IREvent[])exe.oarray;
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
	public IEvent[] getEvents(final IFilter filter)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				oarray = waitqueue.getEvents(filter);

			}
		};
		IREvent[] evs = (IREvent[])exe.oarray;
		IEvent[] ret = new IEvent[evs.length];
		for(int i=0; i<evs.length; i++)
			ret[i] = EventbaseWrapper.wrap(evs[i]);
		return ret;
	}

	/**
	 * Add a message event.
	 * @param type The type.
	 */
	public void addMessageEvent(final String type)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.addMessageEvent(type, null);	// Todo: also support match expression in API?
			}
		};
	}

	/**
	 * Add a message event reply.
	 * @param me The message event.
	 */
	public void addReply(IMessageEvent me)
	{
		//checkThreadAccess();
		final IRMessageEvent ime = (IRMessageEvent)((MessageEventWrapper)me).unwrap();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.addMessageEvent(ime);
			}
		};
	}

	/**
	 * Add an internal event.
	 * @param type The type.
	 */
	public void addInternalEvent(final String type)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.addInternalEvent(type, null);	// Todo: also support match expression in API?
			}
		};
	}

	/**
	 * Add a goal.
	 * @param type The type.
	 */
	public void addGoal(final String type)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.addGoal(type, null); 	// Todo: also support match expression in API?
			}
		};
	}

	/**
	 * Add a subgoal.
	 * @param subgoal The subgoal.
	 */
	public void addSubgoal(final IGoal subgoal)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.addGoal((IRGoal)((GoalWrapper)subgoal).unwrap());
			}
		};
	}

	/**
	 * Add a belief.
	 * @param type The type.
	 */
	public void addBelief(final String type)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.addBelief(type, ICondition.TRACE_ALWAYS);
			}
		};
	}
	
	/**
	 * Add a belief.
	 * @param type The type.
	 */
	public void addBelief(final IBelief bel)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.addBelief((IRBelief)((BeliefWrapper)bel).unwrap()
					, ICondition.TRACE_ALWAYS);
			}
		};
	}

	/**
	 *  Add a belief set.
	 *  @param type The type.
	 *  @param eventtype The eventtype.
	 */
	public void addBeliefSet(final String type, final String[] eventtype)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.addBeliefSet(type, eventtype, ICondition.TRACE_ALWAYS);
			}
		};
	}
	
	/**
	 *  Add a belief set.
	 *  @param bel set The belief set.
	 *  @param eventtype The eventtype.
	 */
	public void addBeliefSet(final IBeliefSet belset, final String[] eventtype)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.addBeliefSet((IRBeliefSet)((BeliefSetWrapper)belset).unwrap(), 
					eventtype, ICondition.TRACE_ALWAYS);
			}
		};
	}

	/**
	 * Add a user filter.
	 * @param filter The user filter.
	 */
	public void addFilter(final IFilter filter)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.addFilter(filter);
			}
		};
	}

	/**
	 * Add a condition.
	 * @param condition The condition.
	 */
	public void addCondition(final String condition)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.addCondition(condition, ICondition.TRACE_ALWAYS);
			}
		};
	}

	/**
	 * Add a condition.
	 * @param condition The condition.
	 */
	public void addCondition(final ICondition condition)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.addCondition((IRCondition)((ConditionWrapper)condition).unwrap());
			}
		};
	}

	/**
	 * Remove a message event.
	 * @param type The type.
	 */
	public void removeMessageEvent(final String type)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.removeMessageEvent(type);
			}
		};
	}

	/**
	 * Add a message event reply.
	 * @param me The message event.
	 */
	public void removeReply(IMessageEvent me)
	{
		//checkThreadAccess();
		final IRMessageEvent ime = (IRMessageEvent)((MessageEventWrapper)me).unwrap();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.removeMessageEvent(ime);
			}
		};
	}

	/**
	 * Remove an internal event.
	 * @param type The type.
	 */
	public void removeInternalEvent(final String type)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.removeInternalEvent(type);
			}
		};
	}

	/**
	 * Remove a goal.
	 * @param type The type.
	 */
	public void removeGoal(final String type)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.removeGoal(type);
			}
		};
	}

	/**
	 * Remove a goal.
	 * @param goal The goal.
	 */
	public void removeGoal(final IGoal goal)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.removeGoal((IRGoal)((GoalWrapper)goal).unwrap());
			}
		};
	}

	/**
	 * Remove a belief.
	 * @param type The type.
	 */
	public void removeBelief(final String type)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.removeBelief(type);
			}
		};
	}
	
	/**
	 * Remove a belief.
	 * @param bel The bel.
	 */
	public void removeBelief(final IRBelief bel)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.removeBelief(bel);
			}
		};
	}

	/**
	 * Remove a belief set.
	 * @param type The type.
	 */
	public void removeBeliefSet(final String type)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.removeBeliefSet(type);
			}
		};
	}
	
	/**
	 * Remove a belief set.
	 * @param belset The belief set.
	 */
	public void removeBeliefSet(final IRBeliefSet belset)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.removeBeliefSet(belset);
			}
		};
	}

	/**
	 * Remove a user filter.
	 * @param filter The user filter.
	 */
	public void removeFilter(final IFilter filter)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.removeFilter(filter);
			}
		};
	}

	/**
	 * Remove a condition.
	 * @param condition The condition.
	 */
	public void removeCondition(final String condition)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.removeCondition(condition);
			}
		};
	}

	/**
	 * Remove a condition.
	 * @param condition The condition.
	 */
	public void removeCondition(final ICondition condition)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				waitqueue.removeCondition((IRCondition)((ConditionWrapper)condition).unwrap());
			}
		};
	}

	/**
	 *  Get the number of events in the waitqueue.
	 */
	public int	size()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				integer = waitqueue.size();
			}
		};
		return exe.integer;
	}

	/**
	 *  Test if the waitqueue is empty.
	 */
	public boolean	isEmpty()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				bool = waitqueue.isEmpty();
			}
		};
		return exe.bool;
	}

	//-------- helpers --------

	// todo: remove somehow! copied from ElementWrapper?!

	/**
	 *  Check if the plan thread is accessing.
	 *  @return True, if access is ok.
	 * /
	public boolean checkThreadAccess0()
	{
		RPlan	rp	= agent.getCurrentPlan();
		Object pt = rp!=null ? rp.getThread() : null;
		return Thread.currentThread()!=pt && !agent.isAgentThread();
	}

	/**
	 *  Check if the plan thread is accessing.
	 *  @throws RuntimeException when wrong thread (e.g. GUI) is calling agent methods.
	 * / 
	public void checkThreadAccess()
	{
		if(!checkThreadAccess0())
		{
			throw new RuntimeException("Plan or agent thread are "
				+"not allowed to call gui interface: "+Thread.currentThread());
		}
	}*/

	/**
	 *  An action to be executed on the agent thread.
	 *  Provides predefined variables to store results.
	 *  Directly invokes agenda in construcor.
	 */
	public abstract class AgentInvocation	implements Runnable
	{
		//-------- attributes --------

		/** The object result variable. */
		protected Object	object;

		/** The string result variable. */
		protected String	string;

		/** The int result variable. */
		protected int	integer;

		/** The long result variable. */
		protected long	longint;

		/** The boolean result variable. */
		protected boolean	bool;

		/** The object array result variable. */
		protected Object[]	oarray;

		/** The string result variable. */
		protected String[]	sarray;

		/** The class result variable. */
		protected Class	clazz;


		//-------- constructors --------

		/**
		 *  Create an action to be executed in sync with the agent thread.
		 */
		public AgentInvocation()
		{
			//agenda.invokeAndWait(this);
			
			if(agent.getInterpreter().isExternalThread())
			{
				agent.getInterpreter().invokeSynchronized(this);
			}
			else
			{
				run();
			}
		}
	}
}
