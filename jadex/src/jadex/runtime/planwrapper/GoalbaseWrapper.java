package jadex.runtime.planwrapper;

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
		super(goalbase);
		this.goalbase = goalbase;
	}

	//-------- methods --------
	
	/**
	 *  Get a (proprietary) adopted goal by name.
	 *  @param name	The goal name.
	 *  @return The goal (if found).
	 */
	public IGoal getGoal(String name)
	{
		checkThreadAccess();
		IRGoal goal = goalbase.getGoal(name);
		return goal==null? null: new GoalWrapper(goal);
	}

	/**
	 *  Test if an adopted goal is already contained in the goal base.
	 *  @param goal	The goal to test.
	 *  @return True, if the goal is contained.
	 */
	public boolean containsGoal(IGoal goal)
	{
		checkThreadAccess();
		return goal==null? false: goalbase.containsGoal((IRGoal)((GoalWrapper)goal).unwrap());
	}

	/**
	 *  Get all proprietary goals of a specified type (=model element name).
	 *  @param type The goal type.
	 *  @return All proprietary goals of the specified type.
	 */
	public IGoal[] getGoals(String type)
	{
		checkThreadAccess();
		IRGoal[]	rgoals	= goalbase.getGoals(type);
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
		checkThreadAccess();
		IRGoal[]	rgoals	= goalbase.getGoals();
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
	public IGoal	createGoal(String type)
	{
		checkThreadAccess();
		return new GoalWrapper(goalbase.createGoal(type));
	}

	/**
	 *  Dispatch a new top-level goal.
	 *  @param goal The new goal.
	 */
	public IFilter dispatchTopLevelGoal(IGoal goal)
	{
		IFilter ret = null;
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try
		{
			ret = goalbase.dispatchTopLevelGoal((IRGoal)((GoalWrapper)goal).unwrap());
		}
		finally
		{
			getCapability().getAgent().endMonitorConsequences();
		}
		return ret;
	}

	/**
	 *  Register a new goal model.
	 *  @param mgoal The goal model.
	 */
	public void registerGoal(IMGoal mgoal)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{goalbase.registerGoal(mgoal);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Deregister a goal model.
	 *  @param mgoal The goal model.
	 */
	public void deregisterGoal(IMGoal mgoal)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{goalbase.deregisterGoal(mgoal);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}
	
	/**
	 *  Register a new goal reference model.
	 *  @param mgoalref The goal reference model.
	 */
	public void registerGoalReference(IMGoalReference mgoalref)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{goalbase.registerGoalReference(mgoalref);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Deregister a goal reference model.
	 *  @param mgoalref The goal reference model.
	 */
	public void deregisterGoalReference(IMGoalReference mgoalref)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{goalbase.deregisterGoalReference(mgoalref);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}
	
	//-------- listeners --------
	
	/**
	 *  Add a goal listener.
	 *  @param listener The goal listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addGoalListener(String type, IGoalListener userlistener, boolean async)
	{
		checkThreadAccess();
		IFilter filter = new SystemEventFilter(new String[]{ISystemEventTypes.GOAL_ADDED, ISystemEventTypes.GOAL_REMOVED}, type);
		final AsynchronousSystemEventListener listener 
			= new AsynchronousSystemEventListener(userlistener, new Tuple(new Object[]{userlistener, goalbase, type}));
		getCapability().addSystemEventListener(listener, filter, true, async);
	}
	
	/**
	 *  Remove a goal listener.
	 *  @param listener The goal listener.
	 */
	public void removeGoalListener(String type, IGoalListener userlistener)
	{
		checkThreadAccess();
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
}
