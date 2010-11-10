package jadex.model.jibximpl;

import jadex.model.*;

import java.util.List;

/**
 *  Model element for a plan.
 */
public class MPlan extends MParameterElement implements IMPlan
{
	//-------- xml attributes --------

	/** The plan body. */
	protected MPlanBody body;

	/** The plan trigger. */
	protected MPlanTrigger trigger;

	/** The precondition. */
	protected MExpression precondition;

	/** The context condition. */
	protected MCondition contextcondition;

	/** The waitqueue trigger. */
	protected MTrigger waitqueue;

	/** The priority. */
	protected int priority	= 0;

	//-------- constructors --------

	/**
	 *  The setup is called by a configuration code object.
	 */
	protected void init()
	{
		super.init();

		// Set the default trigger type of the context condition
		// when nothing else is specified.
		if(getContextCondition()!=null && getContextCondition().getTrigger()==null) // todo: support other trigger types!!!
			getContextCondition().setTrigger(IMCondition.TRIGGER_CHANGES_TO_FALSE);
	}

	/**
	 *  Method called on first setup run.
	 *  Overwritten in elements that have
	 *  child elements.
	 *  @return The list of direct child elements.
	 */
	public List	getChildren()
	{
		List ret = super.getChildren();
		ret.add(body);
		if(trigger!=null)
			ret.add(trigger);
		if(precondition!=null)
			ret.add(precondition);
		if(contextcondition!=null)
			ret.add(contextcondition);
		if(waitqueue!=null)
			ret.add(waitqueue);
		return ret;
	}
	
	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);
		
		if(precondition!=null)
		{
			precondition.checkClass(Boolean.class, report);
		}
	}

	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMPlanReference;
	}

	//-------- priority --------

	/**
	 *  Get the plan priotity.
	 *  @return The priority used when selecting this plan.
	 */
	public int	getPriority()
	{
		return this.priority;
	}

	/**
	 *  Set the plan priotity.
	 *  @param priority	The priority used when selecting this plan.
	 */
	public void	setPriority(int priority)
	{
		this.priority = priority;
	}


	//-------- body --------

	/**
	 *  Get the body expression.
	 *  @return The plan body expression.
	 */
	public IMPlanBody	getBody()
	{
		return this.body;
	}

	/**
	 *  Create the body expression.
	 *  @param expression	The expression string.
	 *  @param type	The plan body type.
	 *  @return The new plan body expression.
	 */
	public IMPlanBody	createBody(String expression, String type)
	{
		this.body = new MPlanBody();
		body.setExpressionText(expression);
		body.setType(type);
		body.setOwner(this);
		body.init();
		return body;
	}

	/**
	 *  Delete the plan body expression.
	 */
	public void	deleteBody()
	{
		body = null;
	}

	//-------- trigger --------

	/**
	 *  Get the trigger of the plan (if any).
	 *  @return The trigger.
	 */
	public IMPlanTrigger	getTrigger()
	{
		return trigger;
	}

	/**
	 *  Create new the trigger for the plan.
	 *  @return The trigger.
	 */
	public IMPlanTrigger	createTrigger()
	{
		trigger = new MPlanTrigger();
		trigger.setOwner(this);
		return trigger;
	}

	/**
	 *  Delete the trigger of the plan.
	 */
	public void	deleteTrigger()
	{
		trigger = null;
	}

	//-------- waitqueue --------

	/**
	 *  Get the waitqueue of the plan (if any).
	 *  @return The waitqueue.
	 */
	public IMTrigger	getWaitqueue()
	{
		return waitqueue;
	}

	/**
	 *  Create new the waitqueue for the plan.
	 *  @return The waitqueue.
	 */
	public IMTrigger	createWaitqueue()
	{
		waitqueue = new MTrigger();
		return waitqueue;
	}

	/**
	 *  Delete the waitqueue of the plan.
	 */
	public void	deleteWaitqueue()
	{
		waitqueue = null;
	}


	//-------- precondition --------

	/**
	 *  Get the precondition of the plan.
	 *  @return The precondition (if any).
	 */
	public IMExpression	getPrecondition()
	{
		return precondition;
	}

	/**
	 *  Create a precondition for the plan.
	 *  @param expression	The expression string.
	 *  @return The new precondition.
	 */
	public IMExpression	createPrecondition(String expression)
	{
		assert expression!=null : this;
		this.precondition = new MExpression();
		precondition.setExpressionText(expression);
		precondition.setOwner(this);
		precondition.init();
		return precondition;
	}

	/**
	 *  Delete the precondition of the plan.
	 */
	public void	deletePrecondition()
	{
		precondition = null;
	}


	//-------- context condition --------

	/**
	 *  Get the context condition of the plan.
	 *  @return The context condition (if any).
	 */
	public IMCondition	getContextCondition()
	{
		return contextcondition;
	}

	/**
	 *  Create a context condition for the plan.
	 *  @param expression	The expression string.
	 *  @return The new context condition.
	 */
	public IMCondition	createContextCondition(String expression)
	{
		contextcondition = new MCondition();
		contextcondition.setExpressionText(expression);
		contextcondition.setTrigger(IMCondition.TRIGGER_CHANGES_TO_FALSE);
		contextcondition.setOwner(this);
		contextcondition.init();
		return contextcondition;
	}

	/**
	 *  Delete the context condition of the plan.
	 */
	public void	deleteContextCondition()
	{
		contextcondition = null;
	}

	/**
	 *  Get the expression parameters.
	 *  If this element has no local parameters, will return
	 *  the parameters of the owner, or null if the element
	 *  has no owner.
	 */
	public List	getSystemExpressionParameters()
	{
		List copy = super.getSystemExpressionParameters();
		copy.add(new ExpressionParameterInfo("$plan", this, "jadex.runtime.impl.RPlan"));
		copy.add(new ExpressionParameterInfo("$addedfact", null, "java.lang.Object"));
		copy.add(new ExpressionParameterInfo("$removedfact", null, "java.lang.Object"));
		return copy;
	}
	
	//-------- plan parameters --------

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
		String expression, String mode, String[] ies, String[] mes, String[] goals)
	{
		MPlanParameter ret = new MPlanParameter();
		ret.setName(name);
		ret.setClazz(clazz);
		ret.setDirection(direction);
		ret.setUpdateRate(updaterate);
		if(expression!=null)
			ret.createDefaultValue(expression, mode);
		for(int i=0; ies!=null && i<ies.length; i++)
			ret.createInternalEventMapping(ies[i]);
		for(int i=0; mes!=null && i<mes.length; i++)
			ret.createMessageEventMapping(mes[i]);
		for(int i=0; goals!=null && i<goals.length; i++)
			ret.createGoalMapping(goals[i]);
		ret.setOwner(this);
		ret.init();
		addParameter(ret);
		return ret;
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MPlan clone = (MPlan)cl;
		if(body!=null)
			clone.body = (MPlanBody)body.clone();
		if(trigger!=null)
			clone.trigger = (MPlanTrigger)trigger.clone();
		if(precondition!=null)
			clone.precondition = (MExpression)precondition.clone();
		if(contextcondition!=null)
			clone.contextcondition = (MCondition)contextcondition.clone();
		if(waitqueue!=null)
			clone.waitqueue = (MTrigger)waitqueue.clone();
	}

}
