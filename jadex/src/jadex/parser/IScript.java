package jadex.parser;

import java.util.*;


/**
 *  Represents a script that can be executed.
 */
// Todo: use this interface
public interface IScript
{
	/**
	 *  Execute the script with respect to given parameters.
	 *  @param params	The parameters representing the execution context (string, value pairs), if any.
	 */
	public void	execute(Map params);
}