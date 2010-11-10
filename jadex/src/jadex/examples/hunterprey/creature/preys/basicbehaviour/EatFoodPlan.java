package jadex.examples.hunterprey.creature.preys.basicbehaviour;

import jadex.examples.hunterprey.*;
import jadex.runtime.*;

/**
 *  Try to run away from a hunter.
 */
/*  @requires goal goto_location
 *  @requires goal eat_food
 *  @requires belief forbidden_food 
 */
public class EatFoodPlan extends Plan
{
	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		Food food = (Food)getParameter("food").getValue();
		Location target = food.getLocation();
		IGoal goloc = createGoal("goto_location");
		goloc.getParameter("location").setValue(target);
		dispatchSubgoalAndWait(goloc);

		//System.out.println("Food location reached");
	    IGoal eat = createGoal("eat");
		eat.getParameter("object").setValue(food);
		dispatchSubgoalAndWait(eat);
	}

	/**
	 *  When move to location or eat food finally failed,
	 *  add food to forbidden food.
	 */
	public void	failed()
	{
	    getBeliefbase().getBeliefSet("forbidden_food")
			.addFact(getParameter("food").getValue());
	}
}
