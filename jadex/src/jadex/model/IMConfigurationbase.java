package jadex.model;

/**
 *  Container for configurations.
 */
public interface IMConfigurationbase extends IMConfigBase
{
	//-------- configurations --------

	/**
	 *  Get all configurations.
	 *  @return The configurations.
	 */
	public IMConfiguration[] getConfigurations();

	/**
	 * Get an configuration per name.
	 * @param name The name.
	 * @return The configuration.
	 */
	public IMConfiguration getConfiguration(String name);

	/**
	 *  Create an configuration.
	 *  @param name	The configuration name.
	 *  @return The configuration.
	 */
	public IMConfiguration createConfiguration(String name);

	/**
	 *  Delete a configuration.
	 *  @param state	The configuration.
	 */
	public void	deleteConfiguration(IMConfiguration state);

	/**
	 *  Get the default state name.
	 *  @return The default state name.
	 */
	public String getDefaultConfigurationName();

	/**
	 *  Set the deafult state name.
	 *  @param defaultstate The default state name.
	 */
	public void setDefaultConfigurationName(String defaultstate);

	//-------- non xml related methods --------

	/**
	 *  Get default configuration.
	 *  @return The default configuration (If no default, the first will be returned)
	 */
	public IMConfiguration getDefaultConfiguration();
	
}
