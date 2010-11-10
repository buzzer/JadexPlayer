package jadex.model;

import jadex.parser.ITerm;
import jadex.util.collection.MultiCollection;

/**
 *  An expression is a string that is parsed and later evaluated.
 */
public interface IMExpression extends IMReferenceableElement
{
	
	//-------- constants --------

	/** The evaluation mode for dynamic values. */
	public static String	MODE_DYNAMIC = "dynamic";

	/** The evaluation mode for static values. */
	public static String	MODE_STATIC = "static";

	/** The any element indicator (for expressions affected by system events regardless of the originating element). */
	public static final String	ANY_ELEMENT = "any_element_indicator";

	//-------- evaluation mode --------

	/**
	 *  Get the evaluation mode.
	 *  @return	The evaluation mode.
	 */
	public String	getEvaluationMode();

	/**
	 *  Set the evaluation mode.
	 *  @param eva	The evaluation mode.
	 */
	public void setEvaluationMode(String eva);


	//-------- expression text --------
	
	/**
	 *  Get the expression text.
	 *  @return The expression text.
	 */
	public String getExpressionText();

	/**
	 *  Set the expression text.
	 *  @param expression	The expression text.
	 */
	public void setExpressionText(String expression);


	//-------- expression parameters --------

	/**
	 *  Get all expression parameters.
	 *  @return The expression parameters.
	 */
	public IMExpressionParameter[] getExpressionParameters();

	/**
	 *  Create an expression parameter.
	 *  @param name The name.
	 *  @param clazz The clazz.
	 */
	public IMExpressionParameter createExpressionParameter(String name, Class clazz);

	/**
	 *  Delete an expression parameter.
	 *  @param param The expression parameter.
	 */
	public void deleteExpressionParameter(IMExpressionParameter param);


	//-------- relevant elements --------

	/**
	 *  Get the relevant beliefs.
	 *  @return The relevant beliefs.
	 */
	public IMRelevantElement[]	getRelevantBeliefs();

	/**
	 *  Add a relevant belief.
	 *  @param ref	The referenced belief.
	 *  @param event	The relevant system event (or null for default).
	 */
	public IMRelevantElement	createRelevantBelief(String ref, String event);

	/**
	 *  Delete a relevant belief.
	 *  @param ref	The referenced belief.
	 */
	public void	deleteRelevantBelief(IMRelevantElement ref);

	/**
	 *  Get the relevant belief sets.
	 *  @return The relevant belief sets.
	 */
	public IMRelevantElement[]	getRelevantBeliefSets();

	/**
	 *  Add a relevant belief set.
	 *  @param ref	The referenced belief set.
	 *  @param event	The relevant system event (or null for default).
	 */
	public IMRelevantElement	createRelevantBeliefSet(String ref, String event);

	/**
	 *  Delete a relevant belief set.
	 *  @param ref	The referenced belief set.
	 */
	public void	deleteRelevantBeliefSet(IMRelevantElement ref);


	/**
	 *  Get the relevant goals.
	 *  @return The relevant goals.
	 */
	public IMRelevantElement[]	getRelevantGoals();

	/**
	 *  Add a relevant goal.
	 *  @param ref	The referenced goal.
	 *  @param event	The relevant system event (or null for default).
	 */
	public IMRelevantElement	createRelevantGoal(String ref, String event);

	/**
	 *  Delete a relevant goal.
	 *  @param ref	The referenced goal.
	 */
	public void	deleteRelevantGoal(IMRelevantElement ref);


	/**
	 *  Get the relevant parameters.
	 *  @return The relevant parameters.
	 */
	public IMRelevantElement[]	getRelevantParameters();

	/**
	 *  Add a relevant parameter.
	 *  @param ref	The referenced parameter.
	 *  @param event	The relevant system event (or null for default).
	 */
	public IMRelevantElement	createRelevantParameter(String ref, String event);

	/**
	 *  Delete a relevant parameter.
	 *  @param ref	The referenced parameter.
	 */
	public void	deleteRelevantParameter(IMRelevantElement ref);


	/**
	 *  Get the relevant parameter sets.
	 *  @return The relevant parameter sets.
	 */
	public IMRelevantElement[]	getRelevantParameterSets();

	/**
	 *  Add a relevant parameter set.
	 *  @param ref	The referenced parameter set.
	 *  @param event	The relevant system event (or null for default).
	 */
	public IMRelevantElement	createRelevantParameterSet(String ref, String event);

	/**
	 *  Delete a relevant parameter set.
	 *  @param ref	The referenced parameter set.
	 */
	public void	deleteRelevantParameterSet(IMRelevantElement ref);

	/**
	 *  Get the expected value type.
	 *  @return The expected value type.
	 */
	public Class	getClazz();

	/**
	 *  Set the expected value type.
	 *  @param clazz	The expected value type.
	 */
	public void	setClazz(Class clazz);

	
	//-------- not xml related --------

	/**
	 *  Get the relevant list.
	 *  Hack!!! todo: remove or change?!
	 */
	public MultiCollection getRelevantList();

	/**
	 *  Evaluate the expression.
	 *  @param params	The expression parameters (if any).
	 *  @return The value.
	 * /
	public Object	getValue(Map params)	throws Exception;
	*/

	/**
	 *  Get the term.
	 *  @return The term.
	 */
	// Shouldn't be accesible to the outside, only to RExpression ???
	public ITerm getTerm();

	/**
	 *  Get the static type.
	 *  If no information about the return type of an expression
	 *  is available (e.g. because it depends on the evaluation context),
	 *  the static type is Object.
	 *  @return The static type.
	 * /
	public Class	getStaticType();*/
}
