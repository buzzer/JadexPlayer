package jadex.model;

/**
 *  Configuration settings for an included capability.
 */
public interface IMConfigCapability extends IMConfigReferenceableElement
{
	//-------- configuration --------

	/**
	 *  Get the configuration.
	 *  @return The name of the configuration to use.
	 */
	public String	getConfiguration();

	/**
	 *  Set the configuration.
	 *  @param configuration	The name of the configuration to use.
	 */
	public void	setConfiguration(String configuration);
}
