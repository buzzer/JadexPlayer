package jadex.model;

/**
 *  The expression base interface.
 */
public interface IMExpressionbase extends IMBase
{
	//-------- constants --------

	/** The standard model element for internal expression references (e.g. for initial values). */
	public static final String	STANDARD_EXPRESSION_REFERENCE	= "standard_expression_reference";

	
	//-------- expressions --------

	/**
	 *  Get all defined expressions.
	 *  @return The expressions.
	 */
	public IMExpression[] getExpressions();

	/**
	 *  Get a expression by name.
	 *  @param name	The expression name.
	 *  @return The expression with that name (if any).
	 */
	public IMExpression	getExpression(String name);

	/**
	 *  Create a new expression.
	 *  @param name	The expression name.
	 *  @param expression	The expression string.
	 *  @param clazz	The expected type for values.
	 *  @param exported	Flag indicating if this expression may be referenced from outside capabilities.
	 *  @param paramnames	The names of the parameters.
	 *  @param paramtypes	The types of the parameters.
	 *  @return The modelelement of the expression.
	 */
	public IMExpression	createExpression(String name, String expression, Class clazz, String exported, String[] paramnames, Class[] paramtypes);

	/**
	 *  Delete an expression.
	 *  @param expression	The expression.
	 */
	public void	deleteExpression(IMExpression expression);

	
	//-------- expression references --------

	/**
	 *  Get all expression references.
	 *  @return The expression references.
	 */
	public IMExpressionReference[] getExpressionReferences();

	/**
	 *  Get an expression reference.
	 *  @param name The name.
	 *  @return The expression reference.
	 */
	public IMExpressionReference getExpressionReference(String name);

	/**
	 *  Create a new expression reference.
	 *  @param name The name.
	 *  @param exported	Flag indicating if this element may be referenced from outside capabilities.
	 *  @param ref	The referenced expression (or null for abstract).
	 *  @return The modelelement of the expression reference.
	 */
	public IMExpressionReference	createExpressionReference(String name, String exported, String ref);

	/**
	 *  Delete an expression reference.
	 *  @param reference	The expression reference.
	 */
	public void	deleteExpressionReference(IMExpressionReference reference);

	
	//-------- conditions --------
	
	/**
	 *  Get all defined conditions.
	 *  @return The conditions.
	 */
	public IMCondition[] getConditions();

	/**
	 *  Get a condition by name.
	 *  @param name	The condition name.
	 *  @return The condition with that name (if any).
	 */
	public IMCondition	getCondition(String name);

	/**
	 *  Create a new condition.
	 *  @param name The name.
	 *  @param expression The expression string.
	 *  @param trigger The trigger type (or null for default).
	 *  @param exported	Flag indicating if this event reference may be referenced from outside capabilities.
	 *  @return The new condition model element.
	 */
	public IMCondition createCondition(String name, String expression, String trigger, String exported, String[] paramnames, Class[] paramtypes);

	/**
	 *  Delete a condition.
	 *  @param condition	The condition.
	 */
	public void	deleteCondition(IMCondition condition);

	
	//-------- condition references --------
	
	/**
	 *  Get all condition references.
	 *  @return The condition references.
	 */
	public IMConditionReference[] getConditionReferences();

	/**
	 *  Get a condition reference.
	 *  @param name The name.
	 *  @return The condition reference.
	 */
	public IMConditionReference getConditionReference(String name);

	/**
	 *  Create a new condition reference.
	 *  @param name The name.
	 *  @param exported	Flag indicating if this element may be referenced from outside capabilities.
	 *  @param ref	The referenced condition (or null for abstract).
	 *  @return The modelelement of the condition reference.
	 */
	public IMConditionReference	createConditionReference(String name, String exported, String ref);

	/**
	 *  Delete an condition reference.
	 *  @param reference	The condition reference.
	 */
	public void	deleteConditionReference(IMConditionReference reference);
}
