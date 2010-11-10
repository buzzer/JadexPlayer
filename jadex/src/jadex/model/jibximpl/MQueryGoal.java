package jadex.model.jibximpl;

import java.util.List;
import jadex.model.*;

/**
 *  The query goal type.
 */
public class MQueryGoal extends MGoal implements IMQueryGoal
{
	//-------- attributes --------

	/** The target condition. */
	protected MCondition targetcondition;

	/** The failure condition. */
	protected MCondition failurecondition;

	//-------- constructors --------

	/**
	 *  Called when the element is setup.
	 */
	protected void init()
	{
		super.init();

		// todo: remove target and failure condition from query goal!

		if(getFailureCondition()!=null && getFailureCondition().getTrigger()==null)
			getFailureCondition().setTrigger(IMCondition.TRIGGER_CHANGES_TO_TRUE);

		if(getTargetCondition()!=null && getTargetCondition().getTrigger()==null)
			getTargetCondition().setTrigger(IMCondition.TRIGGER_CHANGES_TO_TRUE);

		if(getTargetCondition()==null)
		{
			// Install the standard implicit target condition.
			// That is the goal is succeeded when all (required)results are set.
			// HACK!!! todo: remove somehow?!
			IMCondition tc = createTargetCondition("((jadex.runtime.impl.RQueryGoal)$goal).checkResults()");
			IMParameter[] params = getParameters();
			for(int i=0; i<params.length; i++)
			{
				if(params[i].getDirection().equals(IMParameter.DIRECTION_OUT)
					|| params[i].getDirection().equals(IMParameter.DIRECTION_INOUT))
				{
					tc.createRelevantParameter(params[i].getName(), null);
				}
			}
			IMParameterSet[] paramsets = getParameterSets();
			for(int i=0; i<paramsets.length; i++)
			{
				if(paramsets[i].getDirection().equals(IMParameterSet.DIRECTION_OUT)
					|| paramsets[i].getDirection().equals(IMParameterSet.DIRECTION_INOUT))
				{
					tc.createRelevantParameterSet(paramsets[i].getName(), null);
				}
			}
		}
	}

	/**
	 *  Method called on first setup run.
	 *  Overwritten in elements that have
	 *  child elements.
	 *  @return The list of direct child elements.
	 */
	public List	getChildren()
	{
		List ret = super.getChildren();
		if(getTargetCondition()!=null)
			ret.add(getTargetCondition());
		if(getFailureCondition()!=null)
			ret.add(getFailureCondition());
		return ret;
	}

	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMQueryGoalReference;
	}

	//-------- target condition --------

	/**
	 *  Get the target condition of the goal.
	 *  @return The target condition (if any).
	 */
	public IMCondition	getTargetCondition()
	{
		return this.targetcondition;
	}

	/**
	 *  Create a target condition for the goal.
	 *  @param expression	The expression string.
	 *  @return The new target condition.
	 */
	public IMCondition	createTargetCondition(String expression)
	{
		targetcondition = new MCondition();
		targetcondition.setExpressionText(expression);
		targetcondition.setTrigger(IMCondition.TRIGGER_CHANGES_TO_TRUE);
		targetcondition.setOwner(this);
		targetcondition.init();
		return targetcondition;
	}

	/**
	 *  Delete the target condition of the goal.
	 */
	public void	deleteTargetCondition()
	{
		targetcondition = null;
	}


	//-------- failure condition --------

	/**
	 *  Get the failure condition of the goal.
	 *  @return The failure condition (if any).
	 */
	public IMCondition	getFailureCondition()
	{
		return this.failurecondition;
	}

	/**
	 *  Create a failure condition for the goal.
	 *  @param expression	The expression string.
	 *  @return The new failure condition.
	 */
	public IMCondition	createFailureCondition(String expression)
	{
		failurecondition = new MCondition();
		failurecondition.setExpressionText(expression);
		failurecondition.setTrigger(IMCondition.TRIGGER_CHANGES_TO_TRUE);
		failurecondition.setOwner(this);
		failurecondition.init();
		return failurecondition;
	}

	/**
	 *  Delete the failure condition of the goal.
	 */
	public void	deleteFailureCondition()
	{
		failurecondition = null;
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MQueryGoal clone = (MQueryGoal)cl;
		if(targetcondition!=null)
			clone.targetcondition = (MCondition)targetcondition.clone();
		if(failurecondition!=null)
			clone.failurecondition = (MCondition)failurecondition.clone();
	}
}
