package jadex.runtime.externalaccesswrapper;

import jadex.model.ISystemEventTypes;
import jadex.runtime.IFilter;
import jadex.runtime.IGoal;
import jadex.runtime.IGoalListener;
import jadex.runtime.ISystemEventListener;
import jadex.runtime.impl.AsynchronousSystemEventListener;
import jadex.runtime.impl.IRGoal;
import jadex.runtime.impl.SystemEventFilter;
import jadex.util.Tuple;

/**
 *  The wrapper for all goal types (perform, achieve, query, maintain).
 */
public class GoalWrapper	extends ParameterElementWrapper	implements IGoal
{
	//-------- attributes --------

	/** The original goal. */
	protected IRGoal goal;

	//-------- constructors --------

	/**
	 *  Create a new goal wrapper.
	 */
	public GoalWrapper(IRGoal goal) // todo: make protected
	{
		super(goal);
		this.goal = goal;
	}

	//-------- BDI event properties --------

	/**
	 *  Is it a post-to-all event.
	 *  @return True, if post-to-all is set.
	 */
	public boolean isPostToAll()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				bool = goal.isPostToAll();
			}
		};
		return exe.bool;
	}

	/**
	 *  Get the random selection flag.
	 *  @return True, when applicable
	 *  selection is random style.
	 */
	public boolean	isRandomSelection()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				bool =  goal.isRandomSelection();
			}
		};
		return exe.bool;
	}


	/**
	 * Set the post-to-all flag.
	 * @param flag The post-to-all flag.
	 */
	public void setPostToAll(final boolean flag)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				goal.setPostToAll(flag);
			}
		};
	}

	/**
	 * Set the random selection flag.
	 * @param flag The random selection flag.
	 */
	public void setRandomSelection(final boolean flag)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				goal.setRandomSelection(flag);
			}
		};
	}

	/**
	 * Get the retry flag.
	 */
	public boolean isRetry()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				bool = goal.isRetry();
			}
		};
		return exe.bool;
	}

	/**
	 *  Get the retry delay (if any).
	 */
	public long getRetryDelay()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				longint = goal.getRetryDelay();
			}
		};
		return exe.longint;
	}

	/**
	 * Get the exclude mode.
	 * @return The exclude mode.
	 */
	public String getExcludeMode()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				string = goal.getExcludeMode();
			}
		};
		return exe.string;
	}

	/**
	 * Set the retry flag.
	 * @param flag The retry flag.
	 */
	public void setRetry(final boolean flag)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				goal.setRetry(flag);
			}
		};
	}

	/**
	 * Set the retry delay (in millis).
	 * @param delay The delay.
	 */
	public void setRetryDelay(final long delay)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				goal.setRetryDelay(delay);
			}
		};
	}

	/**
	 * Set the exclude flag.
	 * @param exclude The exclude mode.
	 * @see IGoal for constant definitions.
	 */
	public void setExcludeMode(final String exclude)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				goal.setExcludeMode(exclude);
			}
		};
	}

	/**
	 *  Get the recur flag.
	 */
	public boolean	isRecur()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				bool = goal.isRecur();
			}
		};
		return exe.bool;
	}

	/**
	 *  Get the recur delay expression (if any).
	 */
	public long	getRecurDelay()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				longint = goal.getRecurDelay();
			}
		};
		return exe.longint;
	}

	/**
	 *  Set the recur flag.
	 *  @param flag The recur flag.
	 */
	public void	setRecur(final boolean flag)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				goal.setRecur(flag);
			}
		};
	}

	/**
	 *  Set the recur delay (in millis).
	 *  @param delay The delay.
	 */
	public void	setRecurDelay(final long delay)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				goal.setRecurDelay(delay);
			}
		};
	}

	/**
	 * Get the activation state.
	 * @return True, if the goal is active.
	 */
	public boolean isActive()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				bool = goal.isActive();
			}
		};
		return exe.bool;
	}

	/**
	 * Check if goal is adopted
	 * @return True, if the goal is adopted.
	 */
	public boolean isAdopted()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				bool = goal.isAdopted();
			}
		};
		return exe.bool;
	}

	/**
	 * Get the lifecycle state.
	 * @return The current lifecycle state (e.g. new, active, dropped).
	 */
	public String getLifecycleState()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				string = goal.getLifecycleState();
			}
		};
		return exe.string;
	}

	//-------- goal methods --------

	/**
	 *  Drop this goal.
	 *  Causes all associated process goals
	 *  and subgoals to be dropped.
	 */
	public void drop()
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void	run()
			{
				if(goal.isAdopted())
					goal.drop();
			}
		};
	}

	/**
	 *  Test if a goal is finished.
	 *  @return True, if goal is finished.
	 */
	public boolean isFinished()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				bool	= goal.isFinished();
			}
		};
		return exe.bool;
	}

	/**
	 *  Test if a goal is succeeded.
	 *  This has different meanings for the different goal types.
	 *  @return True, if goal is succeeded.
	 */
	public boolean isSucceeded()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				bool	= goal.isSucceeded();
			}
		};
		return exe.bool;
	}

	/**
	 *  Test if a goal is failed.
	 *  This has different meanings for the different goal types.
	 *  @return True, if goal has failed.
	 */
	public boolean isFailed()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				bool	= goal.isFailed();
			}
		};
		return exe.bool;
	}

	/**
	 *  Get the goal type.
	 *  @return The goal type.
	 */
	public String	getType()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				string	= goal.getType();
			}
		};
		return exe.string;
	}

	/**
	 *  Get the result of the goal.
	 *  This is a convenience method, as the goal result
	 *  is stored as property.
	 *  @return The result value.
	 */
	public Object	getResult()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				object	= goal.getResult();
			}
		};
		return exe.object;
	}

	/**
	 *  Set the result for the goal.
	 *  This is a convenience method, as the goal result
	 *  is stored as property.
	 *  @param result The result.
	 */
	public void	setResult(final Object result)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void	run()
			{
				goal.setResult(result);
			}
		};
	}

	/**
	 *  Get the filter to wait for an info event.
	 *  @return The filter.
	 */
	public IFilter getFilter()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object = goal.getFilter();
			}
		};
		return (IFilter)exe.object;
	}
	
	/**
	 *  Get the exception (if any).
	 *  When the goal has failed, the exception can be inspected.
	 *  If more than one plan has been executed for a goal
	 *  only the last exception will be available.
	 */
	public Exception	getException()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object = goal.getException();
			}
		};
		return (Exception)exe.object;
	}
	
	//-------- listeners --------
	
	/**
	 *  Add a goal listener.
	 *  @param listener The goal listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addGoalListener(final IGoalListener userlistener, final boolean async)
	{
		new AgentInvocation()
		{
			public void run()
			{
				IFilter filter = new SystemEventFilter(new String[]{ISystemEventTypes.GOAL_REMOVED}, unwrap());
				final AsynchronousSystemEventListener listener 
					= new AsynchronousSystemEventListener(userlistener, new Tuple(userlistener, goal));
				getCapability().addSystemEventListener(listener, filter, true, async);
			}
		};
	}
	
	/**
	 *  Remove a goal listener.
	 *  @param listener The goal listener.
	 */
	public void removeGoalListener(final IGoalListener userlistener)
	{
		new AgentInvocation()
		{
			public void run()
			{
				Object	identifier	= new Tuple(userlistener, goal);
				ISystemEventListener[] listeners = getAgent().getSystemEventListeners();
				for(int i=0; i<listeners.length; i++)
				{
					if((listeners[i] instanceof AsynchronousSystemEventListener) 
						&& ((AsynchronousSystemEventListener)listeners[i]).getIdentifier().equals(identifier))
					{
						getCapability().removeSystemEventListener(listeners[i]);
						break;
					}
				}
			}
		};
	}
}
