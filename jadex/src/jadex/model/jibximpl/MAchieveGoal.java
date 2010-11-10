package jadex.model.jibximpl;

import java.util.List;
import jadex.model.*;

/**
 *  The achieve goal type.
 */
public class MAchieveGoal extends MGoal implements IMAchieveGoal
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

		if(getCreationCondition()!=null)
			getCreationCondition().setName(getName()+"_creation");

		// Set the default trigger mode
		// when nothing else is specified.
		if(getTargetCondition()!=null && getTargetCondition().getTrigger()==null)
			getTargetCondition().setTrigger(IMCondition.TRIGGER_CHANGES_TO_TRUE);
		if(getFailureCondition()!=null && getFailureCondition().getTrigger()==null)
			getFailureCondition().setTrigger(IMCondition.TRIGGER_CHANGES_TO_TRUE);
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
		return ref instanceof IMAchieveGoalReference;
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
		MAchieveGoal clone = (MAchieveGoal)cl;
		if(targetcondition!=null)
			clone.targetcondition = (MCondition)targetcondition.clone();
		if(failurecondition!=null)
			clone.failurecondition = (MCondition)failurecondition.clone();
	}
}
