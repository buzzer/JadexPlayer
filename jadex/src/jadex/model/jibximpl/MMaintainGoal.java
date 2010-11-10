package jadex.model.jibximpl;

import java.util.List;
import jadex.model.*;

/**
 *  The maintain goal type.
 */
public class MMaintainGoal extends MGoal implements IMMaintainGoal
{
	//-------- xml attributes --------

	/** The maintain consition. */
	protected MCondition maintaincondition;

	/** The target consition. */
	protected MCondition targetcondition;

	//-------- constructors --------

	/**
	 *  Called when the element is setup.
	 */
	protected void init()
	{
		super.init();

		if(getMaintainCondition().getTrigger()==null)
			getMaintainCondition().setTrigger(IMCondition.TRIGGER_CHANGES);

		if(getTargetCondition()==null)
			createTargetCondition(getMaintainCondition().getExpressionText());
		
		if(getTargetCondition().getTrigger()==null)
			getTargetCondition().setTrigger(IMCondition.TRIGGER_CHANGES_TO_TRUE);
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
		if(getMaintainCondition()!=null)
			ret.add(getMaintainCondition());
		return ret;
	}

	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMMaintainGoalReference;
	}

	//-------- target condition --------

	/**
	 *  Get the target condition of the goal.
	 *  @return The target condition (if any).
	 */
	public IMCondition	getTargetCondition()
	{
		return targetcondition;
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


	//-------- maintain condition --------

	/**
	 *  Get the maintain condition of the goal.
	 *  @return The maintain condition.
	 */
	public IMCondition	getMaintainCondition()
	{
		return maintaincondition;
	}

	/**
	 *  Create a maintain condition for the goal.
	 *  @param expression	The expression string.
	 *  @return The new maintain condition.
	 */
	public IMCondition	createMaintainCondition(String expression)
	{
		maintaincondition = new MCondition();
		maintaincondition.setExpressionText(expression);
		maintaincondition.setTrigger(IMCondition.TRIGGER_CHANGES);
		maintaincondition.setOwner(this);
		maintaincondition.init();
		return targetcondition;
	}

	/**
	 *  Delete the maintain condition of the goal.
	 */
	public void	deleteMaintainCondition()
	{
		maintaincondition = null;
	}

	
	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MMaintainGoal clone = (MMaintainGoal)cl;
		if(maintaincondition!=null)
			clone.maintaincondition = (MCondition)maintaincondition.clone();
		if(targetcondition!=null)
			clone.targetcondition = (MCondition)targetcondition.clone();
	}

}
