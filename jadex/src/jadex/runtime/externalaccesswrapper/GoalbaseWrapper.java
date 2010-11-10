package jadex.runtime.externalaccesswrapper;

import jadex.runtime.*;
import jadex.runtime.externalaccesswrapper.ElementWrapper.AgentInvocation;
import jadex.runtime.impl.*;
import jadex.util.Tuple;
import jadex.model.IMGoal;
import jadex.model.IMGoalReference;
import jadex.model.ISystemEventTypes;

/**
 *  The goalbase wrapper accessible from within plans.
 */
public class GoalbaseWrapper extends ElementWrapper implements IGoalbase
{
	//-------- attributes --------

	/** The original goal base. */
	protected RGoalbase goalbase;

	//-------- constructors --------

	/**
	 *  Create a new goalbase wrapper.
	 */
	protected GoalbaseWrapper(RGoalbase goalbase)
	{
		super(goalbase.getScope().getAgent(), goalbase);
		this.goalbase = goalbase;
	}

	//-------- methods --------
	
	/**
	 *  Get a (proprietary) adopted goal by name.
	 *  @param name	The goal name.
	 *  @return The goal (if found).
	 */
	public IGoal getGoal(final String name)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object =  goalbase.getGoal(name);
			}
		};
		return exe.object==null? null: new GoalWrapper((IRGoal)exe.object);
	}

	/**
	 *  Test if an adopted goal is already contained in the goal base.
	 *  @param goal	The goal to test.
	 *  @return True, if the goal is contained.
	 */
	public boolean containsGoal(final IGoal goal)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				bool =  goal==null? false: goalbase.containsGoal((IRGoal)((GoalWrapper)goal).unwrap());
			}
		};
		return exe.bool;
	}

	/**
	 *  Get all proprietary goals of a specified type (=model element name).
	 *  @param type The goal type.
	 *  @return All proprietary goals of the specified type.
	 */
	public IGoal[] getGoals(final String type)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object =  goalbase.getGoals(type);
			}
		};
		IRGoal[]	rgoals	= (IRGoal[])exe.object;
		IGoal[]	goals	= new IGoal[rgoals.length];
		for(int i=0; i<goals.length; i++)
			goals[i]	= new GoalWrapper(rgoals[i]);
		return goals;
	}

	/**
	 *  Get all the adopted goals in this scope (including subgoals).
	 *  @return All goals and subgoals.
	 */
	public IGoal[]	getGoals()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object =  goalbase.getGoals();
			}
		};
		IRGoal[]	rgoals	= (IRGoal[])exe.object;
		IGoal[]	goals	= new IGoal[rgoals.length];
		for(int i=0; i<goals.length; i++)
			goals[i]	= new GoalWrapper(rgoals[i]);
		return goals;
	}

	/**
	 *  Create a goal from a template goal.
	 *  To be processed, the goal has to be dispatched as subgoal
	 *  or adopted as top-level goal.
	 *  @param type	The template goal name as specified in the ADF.
	 *  @return The created goal.
	 */
	public IGoal	createGoal(final String type)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object =  goalbase.createGoal(type);
			}
		};
		return new GoalWrapper((IRGoal)exe.object);
	}

	/**
	 *  Dispatch a new top-level goal.
	 *  @param goal The new goal.
	 */
	public IFilter dispatchTopLevelGoal(final IGoal goal)
	{
		//checkThreadAccess();
		AgentInvocation exe = new AgentInvocation()
		{
			public void run()
			{
				object = goalbase.dispatchTopLevelGoal((IRGoal)((GoalWrapper)goal).unwrap());
			}
		};
		return (IFilter)exe.object; // todo: no wrapper?!
	}

	/**
	 *  Register a new goal model.
	 *  @param mgoal The goal model.
	 */
	public void registerGoal(final IMGoal mgoal)
	{
		//checkThreadAccess();
		AgentInvocation exe = new AgentInvocation()
		{
			public void run()
			{
				goalbase.registerGoal(mgoal);
			}
		};
	}

	/**
	 *  Deregister a goal model.
	 *  @param mgoal The goal model.
	 */
	public void deregisterGoal(final IMGoal mgoal)
	{
		//checkThreadAccess();
		AgentInvocation exe = new AgentInvocation()
		{
			public void run()
			{
				goalbase.deregisterGoal(mgoal);
			}
		};
	}

	/**
	 *  Register a new goal reference model.
	 *  @param mgoalref The goal reference model.
	 */
	public void registerGoalReference(final IMGoalReference mgoalref)
	{
		//checkThreadAccess();
		AgentInvocation exe = new AgentInvocation()
		{
			public void run()
			{
				goalbase.registerGoalReference(mgoalref);
			}
		};
	}

	/**
	 *  Deregister a goal reference model.
	 *  @param mgoalref The goal reference model.
	 */
	public void deregisterGoalReference(final IMGoalReference mgoalref)
	{
		//checkThreadAccess();
		AgentInvocation exe = new AgentInvocation()
		{
			public void run()
			{
				goalbase.deregisterGoalReference(mgoalref);
			}
		};
	}
	
	//-------- listeners --------
	
	/**
	 *  Add a goal listener.
	 *  @param listener The goal listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addGoalListener(final String type, final IGoalListener userlistener, final boolean async)
	{
		new AgentInvocation()
		{
			public void run()
			{
				IFilter filter = new SystemEventFilter(new String[]{ISystemEventTypes.GOAL_ADDED, ISystemEventTypes.GOAL_REMOVED}, type);
				final AsynchronousSystemEventListener listener 
					= new AsynchronousSystemEventListener(userlistener,  new Tuple(new Object[]{userlistener, goalbase, type}));
				getCapability().addSystemEventListener(listener, filter, true, async);
			}
		};
	}
	
	/**
	 *  Remove a goal listener.
	 *  @param listener The goal listener.
	 */
	public void removeGoalListener(final String type, final IGoalListener userlistener)
	{
		new AgentInvocation()
		{
			public void run()
			{
				Object	identifier	= new Tuple(new Object[]{userlistener, goalbase, type});
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
