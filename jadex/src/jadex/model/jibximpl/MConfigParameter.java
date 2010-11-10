package jadex.model.jibximpl;

import jadex.model.*;

import java.util.List;

/**
 *  A config parameter.
 */
public class MConfigParameter extends MConfigReferenceableElement implements IMConfigParameter
{
	//-------- xml attributes --------

	/** The initial value. */
	protected MExpression initialvalue;

	//-------- constructors --------

	/**
	 *  The setup is called by a configuration code object.
	 */
	protected void init()
	{
		super.init();

		// Set the default expression evaluation type
		// when nothing is specified.
		if(getInitialValue()!=null && getInitialValue().getEvaluationMode()==null)
			getInitialValue().setEvaluationMode(IMExpression.MODE_STATIC);
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
		if(getInitialValue()!=null)
			ret.add(getInitialValue());
		return ret;
	}

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);
		
		if(getReference()!=null && initialvalue!=null)
		{
			IMElement	orig	= null;
			try
			{
				orig	= findOriginalElement();
			}
			catch(Exception e) {}
		
			Class	clazz	= null;
			if(orig instanceof IMParameter)
			{
				clazz	= ((IMParameter)orig).getClazz();
			}
			else if(orig instanceof IMParameterReference)
			{
				clazz	= ((IMParameterReference)orig).getClazz();
			}
				
			initialvalue.checkClass(clazz, report);				
		}
	}

	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., an initialbeliefsetreference can only be a reference to a beliefset(reference),
	 *  and not a belief(reference).
	 */
	protected boolean assignable(IMElement orig)
	{
		return orig instanceof IMParameter || orig instanceof IMParameterReference;
	}

	//-------- value --------

	/**
	 *  Get the initial value of the parameter.
	 *  @return The initial value expression (if any).
	 */
	public IMExpression	getInitialValue()
	{
		return this.initialvalue;
	}

	/**
	 *  Create the initial value for the parameter.
	 *  @param expression	The expression string.
	 *  @param mode	The evaluation mode.
	 *  @return The new value expression.
	 */
	public IMExpression	createInitialValue(String expression, String mode)
	{
		assert expression!=null : this;
		this.initialvalue = new MExpression();
		initialvalue.setExpressionText(expression);
		initialvalue.setEvaluationMode(mode);
		initialvalue.setOwner(this);
		initialvalue.init();
		return initialvalue;
	}

	/**
	 *  Delete the initial value of the parameter.
	 */
	public void	deleteInitialValue()
	{
		initialvalue = null;
	}

	//-------- other methods --------

	/**
	 *  Resolve the reference to the original element.
	 */
	protected IMElement findOriginalElement()
	{
		IMReferenceableElement	ref	= null;

		IMElement	elemref = ((MConfigParameterElement)getOwner()).getOriginalElement();
		if(elemref instanceof IMParameterElement)
		{
			ref = ((IMParameterElement)elemref).getParameter(getReference());
		}
		else if(elemref instanceof IMParameterElementReference)
		{
			ref = ((IMParameterElementReference)elemref).getParameterReference(getReference());
			// Search for implicitly added reference parameters. (hack???)
			while(ref==null && isChecking() && (elemref instanceof IMParameterElementReference))
			{
				elemref	= ((IMParameterElementReference)elemref).getReferencedElement();
				if(elemref instanceof IMParameterElement)
				{
					ref = ((IMParameterElement)elemref).getParameter(getReference());
				}
				else if(elemref instanceof IMParameterElementReference)
				{
					ref = ((IMParameterElementReference)elemref).getParameterReference(getReference());
				}
			}
		}
		return ref;
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MConfigParameter clone = (MConfigParameter)cl;
		if(initialvalue!=null)
			clone.initialvalue = (MExpression)initialvalue.clone();
	}
}
