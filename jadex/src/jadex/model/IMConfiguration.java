package jadex.model;

/**
 *  The configuration element.
 */
public interface IMConfiguration extends IMElement
{
	//-------- bases --------
	
	/**
	 *  Get the belief base.
	 *  @return The belief base.
	 */
	public IMConfigBeliefbase getBeliefbase();

	/**
	 *  Get the goal base.
	 *  @return The goal base.
	 */
	public IMConfigGoalbase getGoalbase();

	/**
	 *  Get the plan base.
	 *  @return The plan base.
	 */
	public IMConfigPlanbase getPlanbase();

	/**
	 *  Get the event base.
	 *  @return The event base.
	 */
	public IMConfigEventbase getEventbase();

	/**
	 *  Get the capability base.
	 *  @return The capability base.
	 */
	public IMConfigCapabilitybase getCapabilitybase();

	
	//-------- not xml related --------

	/**
	 *  Get the initial capability of the given
	 *  capability reference.
	 */
	public IMConfigCapability	getInitialCapability(IMCapabilityReference cap);
}
