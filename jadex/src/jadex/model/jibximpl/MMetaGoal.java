package jadex.model.jibximpl;

import java.util.List;
import jadex.model.*;

/**
 *  The meta goal type for meta-level deliberation.
 */
public class MMetaGoal extends MQueryGoal implements IMMetaGoal
{
	//-------- xml attributes --------

	/** The trigger. */
	protected MMetaGoalTrigger trigger;

	//-------- constructors --------

	/**
	 *  Method called on first setup run.
	 *  Overwritten in elements that have
	 *  child elements.
	 *  @return The list of direct child elements.
	 */
	public List	getChildren()
	{
		List ret = super.getChildren();
		if(trigger!=null)
			ret.add(trigger);
		return ret;
	}

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);

		IMTrigger trigger = getTrigger();
		if(trigger==null)
		{
			report.addEntry(this, "No trigger specififed.");
		}
	}
	
	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMMetaGoalReference;
	}

	//-------- trigger --------

	/**
	 *  Get the trigger of the goal.
	 *  @return The trigger.
	 */
	public IMMetaGoalTrigger	getTrigger()
	{
		return this.trigger;
	}

	/**
	 *  Create new the trigger for the goal.
	 *  @return The trigger.
	 */
	public IMMetaGoalTrigger	createTrigger()
	{
		trigger = new MMetaGoalTrigger();
		trigger.setOwner(this);
		trigger.init();
		return trigger;
	}

	/**
	 *  Delete the trigger of the goal.
	 */
	public void	deleteTrigger()
	{
		trigger = null;
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MMetaGoal clone = (MMetaGoal)cl;
		if(trigger!=null)
			clone.trigger = (MMetaGoalTrigger)trigger.clone();
	}

}
