package jadex.tools.jadexdoc.doclets;

import jadex.tools.jadexdoc.Configuration;

/**
 *
 */
public interface IJadexDoclet
{

	/**
	 * Create the configuration instance.
	 * Override this method to use a different
	 * configuration.
	 */
	//public Configuration createConfiguration();

	/**
	 * Get current configuration instance.
	 * @return Configuration The current configuration instance.
	 */
	Configuration getConfiguration();

	/**
	 * The "start" method as required by Jadexdoc.
	 * Start the generation of files. Call generate methods in the individual
	 * writers, which will in turn genrate the documentation files.
	 * @return boolean
	 */
	boolean start();

}
