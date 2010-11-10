package jadex.model.jibximpl;

import jadex.model.*;
import jadex.parser.ITerm;

import java.util.List;

/**
 *  The element containing information about inhibiting another goal instance.
 */
public class MInhibits extends MExpression implements IMInhibits
{
	//-------- xml attributes --------

	/** The reference. */
	protected String reference;

	/** The inhibition type. */
	protected String inhibit	= WHEN_ACTIVE;

	//-------- constructors --------

	/**
	 *  Called when the element is setup.
	 */
	protected void init()
	{
		super.init();

		if(getEvaluationMode()==null)
			setEvaluationMode(IMExpression.MODE_DYNAMIC);
	}

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);
		
		checkClass(Boolean.class, report);
		
		if(getReference()==null)
		{
			report.addEntry(this, "Name of inhibited goal required.");
		}
		else if(getScope().getGoalbase().getReferenceableElement(getReference())==null)
		{
			report.addEntry(this, "Inhibited goal '"+getReference()+"' not found.");
		}
	}
	
	//-------- inhibit --------

	/**
	 *  Get the inhibition type, i.e. an identifier for the goal's
	 *  lifecycle state in which the inhibition link is active.
	 *  @return	The inhibition type.
	 */
	public String	getInhibit()
	{
		return this.inhibit;
	}

	/**
	 *  Set the inhibition type, i.e. an identifier for the goal's
	 *  lifecycle state in which the inhibition link is active.
	 *  @param type	The inhibition type.
	 */
	public void	setInhibit(String type)
	{
		this.inhibit = type;
	}

	//-------- reference --------

	/**
	 *  Get the reference to the inhibited goal type.
	 *  @return	The inhibited goal type.
	 */
	public String	getReference()
	{
		return this.reference;
	}

	/**
	 *  Set the reference to the inhibited goal type.
	 *  @param reference	The inhibited goal type.
	 */
	public void	setReference(String reference)
	{
		this.reference = reference;
	}

	//--------- attributes --------

	/** The goals with inhibition expressions. */
	protected IMReferenceableElement goal;

	//-------- methods --------

	/**
	 *  Get the inhibited goals.
	 *  @return The inhibited goal.
	 */
	public IMReferenceableElement	getInhibitedGoal()
	{
		if(goal==null)
		{
			goal = getScope().getGoalbase().getReferenceableElement(getReference());
		}
		return goal;
	}

	/**
	 *  Get the inhibiting expression.
	 *  @return The inhibiting expression.
	 */
	public IMExpression	getInhibitingExpression()
	{
		return exptext!=null? this: null;
	}

	/**
	 *  todo: jbix bug remove me!!! jibx overwrites with "" instead of null or nothing :-(
	 *  Set the expression text.
	 *  @param expression	The expression text.
	 */
	public void setExpressionText(String expression)
	{
		if(expression.length()==0)
			return;
		else
			super.setExpressionText(expression);
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
		copy.add(new ExpressionParameterInfo("$ref", getInhibitedGoal(), "jadex.runtime.impl.IRGoal"));
		return copy;
	}

	/**
	 *  Get the term.
	 *  @return The term.
	 */
	public ITerm	getTerm()
	{
		return exptext!=null? super.getTerm(): null;
	}
}
