package jadex.runtime.impl.agenda.goals;

import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.*;
import jadex.model.IMGoal;
import jadex.util.SReflect;

/**
 *  Action to create a goal with possibly a binding.
 */
public class GoalCreationAction extends BindingAction
{
	//-------- attributes --------

	/** The goalbase. */
	protected RGoalbase goalbase;

	/** The goal model. */
	protected IMGoal	goal;

	/** The goal instance. */
	protected IRGoal	newgoal;	

	//-------- constructors --------

	/**
	 *  Create a goal binding action.
	 */
	public GoalCreationAction(RGoalbase goalbase, IAgendaActionPrecondition precond, IMGoal goal)
	{
		super(precond);
		this.goalbase = goalbase;
		this.goal = goal;
	}

	//-------- methods --------

	/**
	 *  Execute the action.
	 */
	public void	execute()
	{
		// Hack!!! Done when condition is triggered, to make goal
		// accesible from getCreatedGoals() before agenda entry
		// is executed.
		//System.out.println("Wants to create rgoal for: "+goal.getName());
		//System.out.println("Binding is: "+getBinding());
		newgoal = goalbase.createGoal(null, goal, null, null, getBinding());

		// Add a new top-level goal
		goalbase.adoptGoal(newgoal);
		//System.out.println("New toplevel goal created: "+newgoal);
	}

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public  String toString()
	{
		return SReflect.getInnerClassName(this.getClass())+" (goal="+goal.getName()+")";
	}

	/**
	 *  Hack!!! Already create goal, when condition is triggered.
	 * /
	protected BindingAction createBindingInstance(Map binding)
	{
		GoalCreationAction	clone	= (GoalCreationAction)super.createBindingInstance(binding);
		clone.newgoal = createGoal(null, goal, null, binding);
		return clone;
	}*/
}
