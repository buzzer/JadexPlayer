package jadex.model;

import java.util.Map;

/**
 *  The bdi agent is the root model element of any jadex agent.
 */
public interface IMBDIAgent extends IMCapability
{
	/**
	 *  Get the property file name.
	 *  @return The property file name.
	 */
	public String getPropertyFile();

	/**
	 *  Set the property file name.
	 *  @param propertyfile The property file.
	 */
	public void setPropertyFile(String propertyfile);

	/**
	 *  Get the possible command-line argument names.
	 *  @return The possible command-line argument names. 
	 */
	public String[] getArgumentNames();

	/**
	 *  Check if the given arguments match the beliefs in the model. 
	 */
	public void checkArguments(Map arguments);
}
