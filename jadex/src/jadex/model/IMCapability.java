package jadex.model;

import jadex.parser.IParser;

/**
 *  The capability model element.
 */
public interface IMCapability extends IMElement
{
	//-------- package --------

	/**
	 *  Get the package.
	 *  @return The package.
	 */
	public String getPackage();

	/**
	 *  Set the package.
	 *  @param pkg The package.
	 */
	public void setPackage(String pkg);

	//-------- imports --------

	/**
	 *  Get the import declarations.
	 *  @return The import statements.
	 */
	public String[] getImports();
	
	/**
	 *  Create an import declaration.
	 *  @param exp The import statement.
	 */
	public void	createImport(String exp);

	/**
	 *  Delete an import declaration.
	 *  @param exp The import statement.
	 */
	public void	deleteImport(String exp);
	

	//-------- capability references --------

	/**
	 *  Get all capability references.
	 *  @return The capability references.
	 */
	public IMCapabilityReference[] getCapabilityReferences();

	/**
	 *  Get a capability reference.
	 *  @param name The capability reference name.
	 *  @return The capability reference.
	 */
	public IMCapabilityReference getCapabilityReference(String name);

	/**
	 *  Create a capability reference.
	 *  @param name	The capability reference name.
	 *  @param file	The file or identifier of the referenced capability.
	 *  @return The capability reference.
	 */
	public IMCapabilityReference createCapabilityReference(String name, String file);

	/**
	 *  Delete a capability reference.
	 *  @param reference	The capability reference.
	 */
	public void	deleteCapabilityReference(IMCapabilityReference reference);


	//-------- abstract --------

	/**
	 *  Test if the capability is abstract.
	 *  @return True, if abstract.
	 */
	public boolean isAbstract();

	/**
	 *  Set the abstract state.
	 *  @param abs The state.
	 */
	public void setAbstract(boolean abs);


	//-------- bases --------
	
	/**
	 *  Get the belief base.
	 *  @return The belief base.
	 */
	public IMBeliefbase getBeliefbase();

	/**
	 *  Get the goal base.
	 *  @return The goal base.
	 */
	public IMGoalbase getGoalbase();

	/**
	 *  Get the plan base.
	 *  @return The plan base.
	 */
	public IMPlanbase getPlanbase();

	/**
	 *  Get the event base.
	 *  @return The event base.
	 */
	public IMEventbase getEventbase();

	/**
	 * Get the expression base.
	 * @return The expression base.
	 */
	public IMExpressionbase getExpressionbase();

	/**
	 * Get the property base.
	 * @return The property base.
	 */
	public IMPropertybase getPropertybase();

	/**
	 * Get the configuration base.
	 * @return The configuration base.
	 */
	public IMConfigurationbase getConfigurationbase();

	//-------- not xml related --------

	/**
	 *  Get all import declarations (including package).
	 *  @return The import statements.
	 */
	public String[] getFullImports();

	/**
	 *  Get the parser for this document.
	 *  @return The parser.
	 */
	public IParser getParser();

	/**
	 *  Get the filename.
	 *  @return The file name.
	 */
	public String getFilename();

	/**
	 *  Set the filename.
	 *  @param filename The file name.
	 */
	public void setFilename(String filename);

	/**
	 *  Get the last modified date.
	 *  @return The last modified date.
	 */
	public long getLastModified();

	/**
	 *  Set the last modified date.
	 *  @param lastmodified The last modified date.
	 */
	public void setLastModified(long lastmodified);

	/**
	 *  Get the fully qualified name package+"."+typename of a capability.
	 *  @return The fully qualified name.
	 */
	public String getFullName();
}
