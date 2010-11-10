package jadex.model;

/**
 *  The base model element for all goal types.
 */
public interface IMGoal extends IMParameterElement
{
	//-------- constants --------

	/** Never exclude plan candidates from apl. */
	public static final String EXCLUDE_NEVER = "never";

	/** Exclude failed plan candidates from apl. */
	public static final String EXCLUDE_WHEN_FAILED = "when_failed";

	/** Exclude succeeded plan candidates from apl. */
	public static final String EXCLUDE_WHEN_SUCCEEDED = "when_succeeded";

	/** Exclude tried plan candidates from apl. */ 
	public static final String EXCLUDE_WHEN_TRIED = "when_tried";


	//-------- creation condition --------

	/**
	 *  Get the creation condition of the goal.
	 *  @return The creation condition (if any).
	 */
	public IMCondition	getCreationCondition();

	/**
	 *  Create a creation condition for the goal.
	 *  @param expression	The expression string.
	 *  @return The new creation condition.
	 */
	public IMCondition	createCreationCondition(String expression);

	/**
	 *  Delete the creation condition of the goal.
	 */
	public void	deleteCreationCondition();


	//-------- context condition --------

	/**
	 *  Get the context condition of the goal.
	 *  @return The context condition (if any).
	 */
	public IMCondition	getContextCondition();

	/**
	 *  Create a context condition for the goal.
	 *  @param expression	The expression string.
	 *  @return The new context condition.
	 */
	public IMCondition	createContextCondition(String expression);

	/**
	 *  Delete the context condition of the goal.
	 */
	public void	deleteContextCondition();

	
	//-------- drop condition --------

	/**
	 *  Get the drop condition of the goal.
	 *  @return The drop condition (if any).
	 */
	public IMCondition	getDropCondition();

	/**
	 *  Create a drop condition for the goal.
	 *  @param expression	The expression string.
	 *  @return The new drop condition.
	 */
	public IMCondition	createDropCondition(String expression);

	/**
	 *  Delete the drop condition of the goal.
	 */
	public void	deleteDropCondition();

		
	//-------- unique --------

	/**
	 *  Get the uniqueness properties of the goal (if any).
	 *  @return The uniqueness properties.
	 */
	public IMUnique	getUnique();

	/**
	 *  Create new the uniqueness properties for the goal.
	 *  @return The uniqueness properties.
	 */
	public IMUnique	createUnique();

	/**
	 *  Delete the uniqueness properties of the goal.
	 */
	public void	deleteUnique();

		
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

	
	//-------- bdi flags --------

	/**
	 *  Get the retry flag.
	 *  @return The flag indicating if this goal should be retried when the first plan fails.
	 */
	public boolean	isRetry();

	/**
	 *  Set the retry flag.
	 *  @param retry	The flag indicating if this goal should be retried when the first plan fails.
	 */
	public void	setRetry(boolean retry);


	/**
	 *  Get the retry delay.
	 *  @return The delay between retries of the goal (in milliseconds) or -1 for no delay.
	 */
	public long	getRetryDelay();

	/**
	 *  Set the retry delay flag.
	 *  @param retrydelay	The delay between retries of the goal (in milliseconds) or -1 for no delay.
	 */
	public void	setRetryDelay(long retrydelay);


	/**
	 *  Get the exclude mode.
	 *  @return The mode indicating which plans should be excluded after they have been tried.
	 */
	public String	getExcludeMode();

	/**
	 *  Set the exclude mode.
	 *  @param exclude	The mode indicating which plans should be excluded after they have been tried.
	 */
	public void	setExcludeMode(String exclude);


	/**
	 *  Get the random selection flag.
	 *  @return The flag indicating if plans should be selected at random or by prominence.
	 */
	public boolean	isRandomSelection();

	/**
	 *  Set the random selection flag.
	 *  @param randomselection	The flag indicating if plans should be selected at random or by prominence.
	 */
	public void	setRandomSelection(boolean randomselection);


	/**
	 *  Get the post-to-all flag.
	 *  @return The flag indicating if all applicable plans should be executed at once.
	 */
	public boolean	isPostToAll();

	/**
	 *  Set the post-to-all flag.
	 *  @param posttoall	The flag indicating if all applicable plans should be executed at once.
	 */
	public void	setPostToAll(boolean posttoall);


	/**
	 *  Get the recalculate applicable candidates list (apl) state.
	 *  @return True, if should be recalculated eacht time.
	 */
	public boolean isRecalculating();

	/**
	 *  Set the recalculate applicable candidates list (apl) state.
	 *  @param recalculate True, if should be recalculated eacht time.
	 */
	public void setRecalculating(boolean recalculate);

	/**
	 *  Get the recur flag.
	 *  @return The flag indicating if this goal should be retried when the first plan fails.
	 */
	public boolean	isRecur();

	/**
	 *  Set the recur flag.
	 *  @param recur	The flag indicating if this goal should be retried when the first plan fails.
	 */
	public void	setRecur(boolean recur);


	/**
	 *  Get the recur delay.
	 *  @return The delay between retries of the goal (in milliseconds) or -1 for no delay.
	 */
	public long	getRecurDelay();

	/**
	 *  Set the recur delay flag.
	 *  @param recurdelay	The delay between retries of the goal (in milliseconds) or -1 for no delay.
	 */
	public void	setRecurDelay(long recurdelay);

	
	//-------- methods --------

	/**
	 *  Get the goal event.
	 */
	public IMGoalEvent	getGoalEvent(); 

	/**
	 *  Get the parameters which are relevant for comparing goals.
	 */
	public IMParameter[]	getRelevantParameters(); 

	/**
	 *  Get the parameter sets which are relevant for comparing goals.
	 */
	public IMParameterSet[]	getRelevantParameterSets();
}
