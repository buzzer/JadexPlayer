package jadex.model;

/**
 *  The interface for plan parameters that add
 *  the possibility for mappings.
 */
public interface IMPlanParameter extends IMParameter
{
	//-------- internal event mappings --------

	/**
	 *  Create an internal event mapping.
	 *  @param name The mapping name.
	 */
	public void createInternalEventMapping(String name);

	/**
	 *  Delete an internal event mapping.
	 *  @param name The mapping name.
	 */
	public void deleteInternalEventMapping(String name);

	/**
	 *  Get all parameter internal event mappings.
	 *  @return All mappings.
	 */
	public String[] getInternalEventMappings();


	//-------- message event mappings --------

	/**
	 *  Create an message event mapping.
	 *  @param name The mapping name.
	 */
	public void createMessageEventMapping(String name);

	/**
	 *  Delete an message event mapping.
	 *  @param name The mapping name.
	 */
	public void deleteMessageEventMapping(String name);

	/**
	 *  Get all parameter message event mappings.
	 *  @return All mappings.
	 */
	public String[] getMessageEventMappings();


	//-------- goal event mappings --------

	/**
	 *  Create a goal event mapping.
	 *  @param name The mapping name.
	 */
	public void createGoalMapping(String name);

	/**
	 *  Delete a goal event mapping.
	 *  @param name The mapping name.
	 */
	public void deleteGoalMapping(String name);

	/**
	 *  Get all parameter goal mappings.
	 *  @return All mappings.
	 */
	public String[] getGoalMappings();
}
