package jadex.model;

/**
 *  A reference to a goal.
 */
public interface IMGoalReference extends IMParameterElementReference
{

	//-------- deliberation --------

	/**
	 *  Get the deliberation properties of the goal (if any).
	 *  @return The deliberation properties.
	 */
	public IMDeliberation	getDeliberation();

	/**
	 *  Create new the deliberation properties for the goal.
	 *  @param cardinality	The cardinality (i.e. number of concurrently active goals) of this type.
	 *  @return The deliberation properties.
	 */
	public IMDeliberation	createDeliberation(int cardinality);

	/**
	 *  Delete the deliberation properties of the goal.
	 */
	public void	deleteDeliberation();
}
