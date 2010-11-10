package jadex.model.jibximpl;

import jadex.model.*;

import java.util.List;

/**
 *  Model element representing an initial belief.
 */
public class MConfigBelief extends MConfigReferenceableElement implements IMConfigBelief
{
	//-------- xml attributes --------

	/** The initial fact. */
	protected MExpression initialfact;

	//-------- constructors --------

	/**
	 *  The setup is called by a configuration code object.
	 */
	protected void init()
	{
		super.init();

		// todo: hack! Cannot consider update rate of original.
		if(getInitialFact()!=null && getInitialFact().getEvaluationMode()==null)
			getInitialFact().setEvaluationMode(IMExpression.MODE_STATIC);
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
		if(getInitialFact()!=null)
			ret.add(getInitialFact());
		return ret;
	}
	
	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);
		
		if(getReference()!=null && initialfact!=null)
		{
			IMElement	orig	= null;
			try
			{
				orig	= findOriginalElement();
			}
			catch(Exception e) {}
		
			Class	clazz	= null;
			if(orig instanceof IMBelief)
			{
				clazz	= ((IMBelief)orig).getClazz();
			}
			else if(orig instanceof IMBeliefReference)
			{
				clazz	= ((IMBeliefReference)orig).getClazz();
			}
				
			initialfact.checkClass(clazz, report);				
		}
	}

	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., an initialbeliefsetreference can only be a reference to a beliefset(reference),
	 *  and not a belief(reference).
	 */
	protected boolean assignable(IMElement orig)
	{
		return orig instanceof IMBelief || orig instanceof IMBeliefReference;
	}

	//-------- fact --------

	/**
	 *  Get the initial fact of the belief.
	 *  @return The initial fact expression (if any).
	 */
	public IMExpression	getInitialFact()
	{
		return this.initialfact;
	}

	/**
	 *  Create the initial fact for the belief.
	 *  @param expression	The expression string.
	 *  @param mode	The evaluation mode.
	 *  @return The new fact expression.
	 */
	public IMExpression	createInitialFact(String expression, String mode)
	{
		assert expression!=null : this;
		this.initialfact = new MExpression();
		initialfact.setExpressionText(expression);
		initialfact.setEvaluationMode(mode);
		initialfact.setOwner(this);
		initialfact.init();
		return initialfact;
	}

	/**
	 *  Delete the initial fact of the belief.
	 */
	public void	deleteInitialFact()
	{
		initialfact = null;
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MConfigBelief clone = (MConfigBelief)cl;
		if(initialfact!=null)
			clone.initialfact = (MExpression)initialfact.clone();
	}
}
