package jadex.runtime.externalaccesswrapper;

import jadex.runtime.IParameter;
import jadex.runtime.IProcessGoal;
import jadex.runtime.impl.IRParameter;
import jadex.runtime.impl.RProcessGoal;

/**
 *  The process goal wrapper accessible from within plans.
 */
public class ProcessGoalWrapper	extends ParameterElementWrapper	implements IProcessGoal
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

	//-------- goal methods --------

	/**
	 *  abort this goal.
	 *  Causes process goal and subgoals to be dropped.
	 */
	public void abort()
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void	run()
			{
				goal.abort(false);
			}
		};
	}

	/**
	 *  Check if the corresponding plan was aborted because the
	 *  proprietary goal succeeded during the plan was running.
	 *  @return True, if the goal was aborted on success of the proprietary goal.
	 */
	public boolean isAbortedOnSuccess()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				bool	= goal.isAbortedOnSuccess();
			}
		};
		return exe.bool;
	}

	/**
	 *  Test if a goal is succeeded.
	 *  This has different meanings for the different goal types.
	 *  @return True, if goal is succeeded.
	 * /
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
	}*/

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
	 * /
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
	}*/

	/**
	 *  Set the result for the goal.
	 *  This is a convenience method, as the goal result
	 *  is stored as property.
	 *  @param result The result.
	 *  @deprecated
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
	 *  Get the result parameter.
	 *  @return The result parameter.
	 *  @deprecated
	 */
	public IParameter	getResultParameter()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				object = goal.findResultParemeter();
			}
		};
		return exe.object==null? null: new ParameterWrapper((IRParameter)exe.object);
	}
}
