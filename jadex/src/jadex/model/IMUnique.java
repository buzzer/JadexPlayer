package jadex.model;

/**
 *  Uniqueness properties of a goal.
 *  Defines which parameters are considered, to test if two goals
 *  are equal.
 */
public interface IMUnique	extends IMElement 
{

	//-------- excludes --------

	/**
	 *  Get the excluded parameters (as reference string).
	 *  @return	The excluded parameters.
	 */
	public String[]	getExcludes();

	/**
	 *  Create an excluded parameter (as reference string).
	 *  @param ref	The reference.
	 */
	public void	createExclude(String ref);

	/**
	 *  Delete an excluded parameter (as reference string).
	 *  @param ref	The reference.
	 */
	public void	deleteExclude(String ref);
}
