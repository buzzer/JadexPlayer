package jadex.runtime.impl.agenda.easydeliberation;

import java.util.*;

import jadex.runtime.IGoal;
import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.*;
import jadex.util.*;
import jadex.model.*;

/**
 *  This action has the target to decide if the given goal
 *  can be activated in the current situation.
 */
public class DeliberateGoalActivationAction extends AbstractElementAgendaAction implements java.io.Serializable
{
	/** Static timer used for evaluation (hack). */
	public static long	timer;

	//-------- attributes --------

	/** The goal to deliberate on. */
	protected IRGoal	goal;

	//-------- constructors --------

	/**
	 *  Create a deliberation action.
	 */
	public DeliberateGoalActivationAction(IAgendaActionPrecondition precondition, IRGoal goal)
	{
		super(goal, precondition);
		this.goal	= goal;
	}

	//-------- methods --------

	/**
	 *  Execute the action.
	 */
	public void execute()
	{
		// Calculate time to execute action (hack, used for evaluation).
		long	starttime	= System.currentTimeMillis();

		assert goal.getLifecycleState().equals(IGoal.LIFECYCLESTATE_OPTION)
			: goal.getName()+", "+goal.getLifecycleState();

		deliberateNewOption();

		// Calculate time to execute action (hack, used for evaluation).
		timer	+= System.currentTimeMillis()-starttime;
	}

	/**
	 *  When goal is an option, check if goal can be activated
	 *  maybe at the cost of other active goals.
	 */
	protected void	deliberateNewOption()
	{
//		if(goal.getName().indexOf("take")!=-1)
//			System.out.println("aa: "+goal.getName()+", "+goal.getLifecycleState());

		boolean activate = true;
		List occs = goal.getAllOccurrences();

		// Check in all capabilities if
		// a) cardinality is ok (may vary)
		// b) there are no other active goals inhibiting the new option.
		for(int i=0; i<occs.size() && activate; i++)
		{
			IRGoal occ = (IRGoal)occs.get(i);
			activate = checkCardinality(occ) && checkInhibitingGoals(occ);
		}

		// When this goal will be activated:
		// (a) check inhibitions and deactivate these goals, done in enterActiveState()).
		// b) activate the goal.
		if(activate)
			goal.changeLifecycleState(IGoal.LIFECYCLESTATE_ACTIVE);
//		else
//			System.out.println("not activated: "+goal);
	}

	/**
	 *  Check the cardinality for a goal.
	 *  @return True, if cardinality is ok.
	 */
	protected boolean checkCardinality(IRGoal goal)
	{
		boolean ret = true;
		IMDeliberation	delib	= goal.getDeliberationInfo();

		// Check if cardinality is ok.
		if(delib!=null && delib.getCardinality()!=-1)
		{
			IRGoal[] samegoals= goal.getScope().getGoalbase().getActiveGoals(goal.getType());
			ret = samegoals.length < delib.getCardinality();
			if(!ret)
			{
				int exactgoalcnt = 0;
				for(int j=0; j<samegoals.length; j++)
				{
//					if(!goal.inhibits(samegoals[j], true, false))
					if(!SEasyDeliberation.inhibits(goal, samegoals[j], true, false))
						exactgoalcnt++;
				}
				ret = exactgoalcnt < delib.getCardinality();
			}
		}

		return ret;
	}

	/**
	 *  Check there are active goals inhibiting the goal.
	 *  @return True, if no inhibitors found.
	 */
	protected boolean checkInhibitingGoals(IRGoal goal)
	{
		// Check if there are no active goals that inhibit my activation.
		boolean ret = true;
		IRGoal[] allgoals = goal.getScope().getGoalbase().getActiveGoals();
		for(int j=0; j<allgoals.length && ret; j++)
		{
			ret	= !SEasyDeliberation.inhibits(allgoals[j], goal, allgoals[j].isActive(), allgoals[j].isInProcess());
		}
		return ret;
	}

	/**
	 *  Get the goal.
	 */
	protected IRGoal getGoal()
	{
		return goal;
	}
  
	/**
	 * @return the "cause" of this action
	 * @see jadex.runtime.impl.agenda.IAgendaAction#getCause()
	 * /
	public Object getCause()
	{
		return goal.getName();
	}*/
  
	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public  String toString()
	{
		return SReflect.getInnerClassName(this.getClass())+" (goal="+goal.getName()+")";
	}
}