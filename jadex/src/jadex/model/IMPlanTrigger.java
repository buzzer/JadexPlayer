package jadex.model;


/**
 *  The plan trigger identifier elements which trigger execution of a plan.
 */
public interface IMPlanTrigger extends IMTrigger
{
	//-------- goals --------

	/**
	 *  Get the goals.
	 *  @return The goals.
	 */
	public IMReference[] getGoals();

	/**
	 *  Create a goal.
	 *  @param ref	The referenced goal.
	 *  @return The new goal.
	 */
	public IMReference createGoal(String ref);

	/**
	 *  Delete a goal.
	 *  @param ref	The goal.
	 */
	public void deleteGoal(IMReference ref);
	

	//-------- condition --------

	/**
	 *  Get the creation condition of the plan.
	 *  @return The creation condition (if any).
	 */
	public IMCondition	getCondition();

	/**
	 *  Create a creation condition for the plan.
	 *  @param expression	The expression string.
	 *  @return The new creation condition.
	 */
	public IMCondition	createCondition(String expression);

	/**
	 *  Delete the creation condition of the plan.
	 */
	public void	deleteCondition();


	//-------- belief changes --------

	/**
	 *  Get the belief changes.
	 *  @return The belief changes.
	 */
	public String[] getBeliefChanges();

	/**
	 *  Create a belief changes.
	 *  @param ref	The belief change.
	 *  //@return The new belief change.
	 */
	public void createBeliefChange(String ref);

	/**
	 *  Delete a belief changes.
	 *  @param ref	The belief change.
	 */
	public void deleteBeliefChange(String ref);

	//-------- beliefset changes --------

	/**
	 *  Get the beliefset changes.
	 *  @return The beliefset changes.
	 */
	public String[] getBeliefSetChanges();

	/**
	 *  Create a beliefset changes.
	 *  @param ref	The beliefset change.
	 *  //@return The new beliefset change.
	 */
	public void createBeliefSetChange(String ref);

	/**
	 *  Delete a beliefset changes.
	 *  @param ref	The beliefset change.
	 */
	public void deleteBeliefSetChange(String ref);

	//-------- beliefset fact added changes --------

	/**
	 *  Get the belief set fact added triggers.
	 *  @return The belief set fact added.
	 */
	public String[] getFactAddedTriggers();

	/**
	 *  Create a fact added trigger.
	 *  @param ref	The belief set.
	 */
	public void createFactAddedTrigger(String ref);

	/**
	 *  Delete a fact added trigger.
	 *  @param ref	The belief set.
	 */
	public void deleteFactAddedTrigger(String ref);


	//-------- beliefset fact removed changes --------

	/**
	 *  Get the belief set changes.
	 *  @return The belief set changes.
	 */
	public String[] getFactRemovedTriggers();

	/**
	 *  Create a fact removed trigger.
	 *  @param ref	The belief set.
	 */
	public void createFactRemovedTrigger(String ref);

	/**
	 *  Delete a fact removed trigger.
	 *  @param ref	The belief set.
	 */
	public void deleteFactRemovedTrigger(String ref);
}
