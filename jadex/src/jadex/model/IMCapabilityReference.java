package jadex.model;

/**
 *  A reference to a capability.
 */
public interface IMCapabilityReference extends IMElement
{
	/**
	 *  Set the file name.
	 *  The file name specifies the capability to include. The file can
	 *  be either referenced by an unqualified or qualified name, e.g.,
	 *  name or package.name, or by a filename relative to the classpath, e.g.,
	 *  package/name.capability.xml. Abstract capabilities like the DF capability
	 *  have to be referenced by the fully qualified name (i.e., jadex.planlib.DF),
	 *  because these capabilities are resolved by this identifier using the
	 *  platform configuration.
	 *  @param file The file.
	 */
	public void setFile(String file);

	/**
	 *  Get the file.
	 *  The file name specifies the capability to include. The file can
	 *  be either referenced by an unqualified or qualified name, e.g.,
	 *  name or package.name, or by a filename relative to the classpath, e.g.,
	 *  package/name.capability.xml. Abstract capabilities like the DF capability
	 *  have to be referenced by the fully qualified name (i.e., jadex.planlib.DF),
	 *  because these capabilities are resolved by this identifier using the
	 *  platform configuration.
	 *  @return The file.
	 */
	public String getFile();

	//-------- not xml related --------
	
	/**
	 *  Get the referenced capability.
	 *  @return The referenced capability.
	 */
	public IMCapability getCapability();
}
