package jadex.model;

/**
 *  Model element of a plan.
 */
public interface IMPlan extends IMParameterElement
{

	//-------- priority --------
	
	/**
	 *  Get the plan priotity.
	 *  @return The priority used when selecting this plan.
	 */
	public int	getPriority();

	/**
	 *  Set the plan priotity.
	 *  @param priority	The priority used when selecting this plan.
	 */
	public void	setPriority(int priority);


	//-------- body --------
	
	/**
	 *  Get the body expression.
	 *  @return The plan body expression.
	 */
	public IMPlanBody	getBody();

	/**
	 *  Create the body expression.
	 *  @param expression	The expression string.
	 *  @param type	The plan body type.
	 *  @return The new plan body expression.
	 */
	public IMPlanBody	createBody(String expression, String type);

	/**
	 *  Delete the plan body expression.
	 */
	public void	deleteBody();


	//-------- trigger --------

	/**
	 *  Get the trigger of the plan (if any).
	 *  @return The trigger.
	 */
	public IMPlanTrigger	getTrigger();

	/**
	 *  Create new the trigger for the plan.
	 *  @return The trigger.
	 */
	public IMPlanTrigger	createTrigger();

	/**
	 *  Delete the trigger of the plan.
	 */
	public void	deleteTrigger();


	//-------- waitqueue --------

	/**
	 *  Get the waitqueue of the plan (if any).
	 *  @return The waitqueue.
	 */
	public IMTrigger	getWaitqueue();

	/**
	 *  Create new the waitqueue for the plan.
	 *  @return The waitqueue.
	 */
	public IMTrigger	createWaitqueue();

	/**
	 *  Delete the waitqueue of the plan.
	 */
	public void	deleteWaitqueue();


	//-------- precondition --------

	/**
	 *  Get the precondition of the plan.
	 *  @return The precondition (if any).
	 */
	public IMExpression	getPrecondition();

	/**
	 *  Create a precondition for the plan.
	 *  @param expression	The expression string.
	 *  @return The new precondition.
	 */
	public IMExpression	createPrecondition(String expression);

	/**
	 *  Delete the precondition of the plan.
	 */
	public void	deletePrecondition();


	//-------- context condition --------

	/**
	 *  Get the context condition of the plan.
	 *  @return The context condition (if any).
	 */
	public IMCondition	getContextCondition();

	/**
	 *  Create a context condition for the plan.
	 *  @param expression	The expression string.
	 *  @return The new context condition.
	 */
	public IMCondition	createContextCondition(String expression);

	/**
	 *  Delete the context condition of the plan.
	 */
	public void	deleteContextCondition();

	/**
	 *  Create a new plan parameter.
	 *  @param name	The name of the parameter.
	 *  @param clazz	The class for values.
	 *  @param updaterate	The updaterate (or -1 for none).
	 *  @param expression	The default value expression (if any).
	 *  @param mode	The evaluation mode.
	 *  @param ies The internal event parameter mappings.
	 *  @param mes The message event parameter mappings.
	 *  @param goals The goal parameter mappings.
	 *  @return	The newly created plan parameter.
	 */
	public IMPlanParameter	createPlanParameter(String name, Class clazz, String direction, long updaterate,
		String expression, String mode, String[] ies, String[] mes, String[] goals);

}
