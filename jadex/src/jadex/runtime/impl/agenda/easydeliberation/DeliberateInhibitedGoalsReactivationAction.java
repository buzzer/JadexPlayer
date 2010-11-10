package jadex.runtime.impl.agenda.easydeliberation;

import java.util.*;

import jadex.runtime.IGoal;
import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.agenda.goals.GoalLifecycleStatePrecondition;
import jadex.runtime.impl.*;
import jadex.util.*;
import jadex.util.collection.SCollection;
import jadex.model.IMDeliberation;

/**
 *  This action has the task to decide if other goals can be reactivated
 *  because this goal allows this (because it is not active any longer).
 */
public class DeliberateInhibitedGoalsReactivationAction extends AbstractAgendaAction implements java.io.Serializable
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
	public DeliberateInhibitedGoalsReactivationAction(IAgendaActionPrecondition precond, final IRGoal goal)
	{
		//super(precond);
		super(new ComposedPrecondition(new IAgendaActionPrecondition()
		{
			public boolean check()
			{
				// Todo: refactor all preconditions for agent kill.
				return !goal.getScope().getAgent().isCleanedup();
			}
		}, precond));
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

		assert goal.getLifecycleState().equals(IGoal.LIFECYCLESTATE_SUSPENDED)
			|| goal.getLifecycleState().equals(IGoal.LIFECYCLESTATE_DROPPING)
			|| goal.getLifecycleState().equals(IGoal.LIFECYCLESTATE_DROPPED)
			|| (goal.getLifecycleState().equals(IGoal.LIFECYCLESTATE_ACTIVE) && !goal.isInProcess())
			: goal.getName()+", "+goal.getLifecycleState();

		Set	options	= SCollection.createHashSet();
		computeReactivatableOptions(goal, null, options);
		for(Iterator it=options.iterator(); it.hasNext();)
		{
			// For each option add deliberation entry.
			final IRGoal	tmp	= (IRGoal)it.next();
			tmp.getScope().getAgent().getInterpreter().addAgendaEntry(
				new DeliberateGoalActivationAction(
					new GoalLifecycleStatePrecondition(tmp, IGoal.LIFECYCLESTATE_OPTION), tmp), goal);
//			System.out.println("(Re)Activating: "+tmp);
		}

		// Calculate time to execute action (hack, used for evaluation).
		timer	+= System.currentTimeMillis()-starttime;
	}

	/**
	 *  When goal is inactivated (suspended or dropped),
	 *  check if other goals can be (re)activated.
	 */
	protected void	computeReactivatableOptions(IRGoal goal, IRGoal source, Set options)
	{
		assert !goal.isInProcess() : goal.getName();

		IMDeliberation	delib	= goal.getDeliberationInfo();
		if(delib!=null)
		{
			// Deliberate goal cardinality.
			if(delib.getCardinality()!=-1)
			{
				// Try to activate options of same type.
				IRGoal[]	siblings	= goal.getScope().getGoalbase().getOptions(goal.getType());
				for(int i=0; i<siblings.length; i++)
				{
					options.add(siblings[i]);
				}
			}

			// Try to activate previously inhibited goals.
			IRGoal[]	goals	= goal.getScope().getGoalbase().getOptions();
			for(int i=0; i<goals.length; i++)
			{
				// When goal is still active (but no longer in process)
				// only reactivate goals inhibited by when_in_process tags.
				if(SEasyDeliberation.inhibits(goal, goals[i], !goal.isActive(), true))
				{
					options.add(goals[i]);
				}
			}
		}

		// Recursively deliberate references.
		List	refs	= ((RReferenceableElement)goal).getReferences();
		for(int i=0; i<refs.size(); i++)
		{
			if(refs.get(i)!=source)
				computeReactivatableOptions((IRGoal)refs.get(i), goal, options);
		}

		// Recursively deliberate referenced element (if any).
		if(goal instanceof RElementReference)
		{
			RReferenceableElement	reffed	= ((RElementReference)goal).getReferencedElement();
			if(reffed!=source)
				computeReactivatableOptions((IRGoal)reffed, goal, options);
		}
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