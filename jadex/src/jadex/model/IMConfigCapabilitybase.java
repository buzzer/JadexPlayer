package jadex.model;

/**
 *  Configuration settings for included capabilities.
 */
public interface IMConfigCapabilitybase extends IMConfigBase
{
	//-------- capability configurations --------

	/**
	 *  Get all defined capabilities.
	 *  @return The capability configurations.
	 */
	public IMConfigCapability[] getCapabilityConfigurations();

	/**
	 *  Create a new capability configuration.
	 *  @param ref	The name of the referenced capability.
	 *  @param config	The configuration.
	 *  @return	The newly created capability configuration.
	 */
	public IMConfigCapability	createCapabilityConfiguration(String ref, String config);

	/**
	 *  Delete a capability configuration.
	 *  @param capability	The capability configuration to delete.
	 */
	public void	deleteCapabilityConfiguration(IMConfigCapability capability);
	
	//-------- not xml related --------
	
	/**
	 *  Get the configuration for a given capability.
	 *  @param subcap	The subcapability.
	 *  @return	The capability configuration.
	 */
	public IMConfigCapability	getCapabilityConfiguration(IMCapabilityReference subcap);


}
