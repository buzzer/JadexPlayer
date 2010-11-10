package jadex.parser;

import java.util.*;


/**
 *  Represents a term that can be evaluated.
 */
public interface ITerm
{
	/**
	 *  Evaluate the term with respect to given parameters.
	 *  @param params	The parameters representing the evaluation context (string, value pairs), if any.
	 *  @return	The value of the term.
	 */
	public Object	getValue(Map params)	throws Exception;

	/**
	 *  Get relevant event types.
	 *  @return The event types in form of #
	 *  e.g. ("beliefbase", "beliefname").
	 */
	public List getRelevantEventtypes();

	/**
	 *  Get unbound parameter nodes.
	 *  @return The unbound parameter nodes.
	 * /
	public ParameterNode[]	getUnboundParameterNodes();*/

	/**
	 *  Get the static type.
	 *  If no information about the return type of an expression
	 *  is available (e.g. because it depends on the evaluation context),
	 *  the static type is null.
	 *  @return The static type.
	 */
	public Class	getStaticType();
}