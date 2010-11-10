package jadex.runtime.impl.agenda.easydeliberation;

import java.util.*;

import jadex.runtime.IGoal;
import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.*;
import jadex.util.*;

/**
 *  This action hast the task to deactivate (optionize)
 *  all goals that are inhitited by the given goal.
 */
public class DeactivateInhibitedGoalsAction extends AbstractElementAgendaAction implements java.io.Serializable
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
	public DeactivateInhibitedGoalsAction(IAgendaActionPrecondition precondition, IRGoal goal)
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

		assert goal.getLifecycleState().equals(IGoal.LIFECYCLESTATE_ACTIVE)
			: goal.getName()+", "+goal.getLifecycleState();

		//List occs = RGoalbase.getOriginalGoal(goal).getAllOccurrences();
		List occs = goal.getAllOccurrences();
		for(int i=0; i<occs.size(); i++)
		{
			IRGoal occ = (IRGoal)occs.get(i);
			IRGoal[] goals = occ.getScope().getGoalbase().getActiveGoals();
			for(int j=0; j<goals.length; j++)
			{
				if(SEasyDeliberation.inhibits(occ, goals[j], occ.isActive(), occ.isInProcess()))
				{
					goals[j].changeLifecycleState(IGoal.LIFECYCLESTATE_OPTION);
//					System.out.println("Deactivating: "+goals[j]);
				}
			}
		}

		// Calculate time to execute action (hack, used for evaluation).
		timer	+= System.currentTimeMillis()-starttime;
	}  
  
	/**
	 *  Get the cause for an action.
	 *  @return The "cause" for this action.
	 *  @see jadex.runtime.impl.agenda.IAgendaAction#getCause()
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