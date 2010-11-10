package jadex.runtime.planwrapper;

import jadex.runtime.*;
import jadex.runtime.impl.*;

/**
 *  The process goal wrapper accessible from within plans.
 */
public class ProcessGoalWrapper extends ParameterElementWrapper implements IProcessGoal
{
	//-------- attributes --------

	/** The original goal. */
	protected RProcessGoal goal;

	//-------- constructors --------

	/**
	 *  Create a new goal wrapper.
	 */
	public ProcessGoalWrapper(RProcessGoal goal)
	{
		super(goal);
		this.goal = goal;
	}

	/**
	 *  Drop this goal.
	 *  Causes all associated process goals
	 *  and subgoals to be dropped.
	 */
	public void abort()
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{goal.abort(false);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

		/**
	 *  Check if the corresponding plan was aborted because the
	 *  proprietary goal succeeded during the plan was running.
	 *  @return True, if the goal was aborted on success of the proprietary goal.
	 */
	public boolean isAbortedOnSuccess()
	{
		checkThreadAccess();
		return goal.isAbortedOnSuccess();	
	}


	/**
	 *  Test if a goal is succeeded.
	 *  This has different meanings for the different goal types.
	 *  @return True, if goal is succeeded.
	 * /
	public boolean isSucceeded()
	{
		checkThreadAccess();
		return goal.isSucceeded();
	}*/

	//-------- abstract goal methods --------

	/**
	 *  Get the goal type.
	 *  @return The goal type.
	 */
	public String	getType()
	{
		checkThreadAccess();
		return goal.getType();
	}

	//-------- parameter handling --------

	/**
	 *  Get the result of the goal.
	 *  This is a convenience method, as the goal result
	 *  is stored as property.
	 *  @return The result value.
	 * /
	public Object	getResult()
	{
		checkThreadAccess();
		return goal.getResult();
	}*/

	/**
	 *  Set the result for the goal.
	 *  This is a convenience method, as the goal result
	 *  is stored as property.
	 *  @param result The result.
	 *  @deprecated
	 */
	public void	setResult(Object result)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{goal.setResult(result);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Get the result parameter.
	 *  @return The result parameter.
	 *  @deprecated
	 */
	public IParameter	getResultParameter()
	{
		checkThreadAccess();
		IRParameter res = goal.findResultParemeter();
		return res==null? null: new ParameterWrapper(res);
	}

	/**
	 *  Get the process goal.
	 *  @return The proprietary goal.
	 */
	public RProcessGoal getGoal()
	{
		return goal;
	}
}
