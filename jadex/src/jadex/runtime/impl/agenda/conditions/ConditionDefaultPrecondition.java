package jadex.runtime.impl.agenda.conditions;

import jadex.runtime.impl.agenda.IAgendaActionPrecondition;
import jadex.runtime.impl.*;

/**
 *  The default precondition for conditions.
 */
public class ConditionDefaultPrecondition implements IAgendaActionPrecondition, java.io.Serializable
{
	//-------- attributes --------

	/** The condition. */
	protected RCondition condition;

	//-------- constructors --------

	/**
	 *  Create a new precondition.
	 *  @param condition The condition
	 */
	public ConditionDefaultPrecondition(RCondition condition)
	{
		this.condition = condition;
	}

	//-------- methods --------

	/**
	 *  Perform the check.
	 *  @return True, if condition is valid.
	 */
	public boolean check()
	{
		// For deciding if the agenda action is still relevant.
		// It is not relevant when the user disabled the condition.
		// Otherwise the trace mode is maybe reset to never by the
		// condition itself.
		return !condition.isDisabled();
		//return !condition.isDisabled() && !isTransitivelyCleanedUp();
	}

	/**
	 *  Is transitivelay cleaned up.
	 *  @return True, if condition or some
	 *    parent is already cleaned up.
	 * /
	protected boolean isTransitivelyCleanedUp()
	{
		boolean ret = false;
		RElement tst = condition;
		while(tst!=null && !ret)
		{
			if(tst.isCleanedup())
				ret = true;
			tst = tst.getOwner();
		}
		return ret;
	}*/
}
