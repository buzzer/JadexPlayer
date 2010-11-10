package jadex.model.jibximpl;

import java.util.*;
import jadex.model.*;
import jadex.util.collection.SCollection;

/**
 *  A config parameter set.
 */
public class MConfigParameterSet extends MConfigReferenceableElement implements IMConfigParameterSet
{
	//-------- xml attributes --------

	/** The initial values. */
	protected ArrayList initialvalues;

	/** The initial values expression. */
	protected MExpression initialvaluesexp;

	//-------- constructors --------

	/**
	 *  The setup is called by a configuration code object.
	 */
	protected void init()
	{
		super.init();

		// Set the default expression evaluation type
		// when nothing is specified.
		for(int i=0; initialvalues!=null && i<initialvalues.size(); i++)
		{
			IMExpression	exp	= (IMExpression)initialvalues.get(i);
			if(exp.getEvaluationMode()==null)
				exp.setEvaluationMode(IMExpression.MODE_STATIC);
		}
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
		if(initialvalues!=null)
			ret.addAll(initialvalues);
		if(getInitialValuesExpression()!=null)
			ret.add(getInitialValuesExpression());
		return ret;
	}

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);
		
		if(getReference()!=null && initialvalues!=null && initialvalues.size()>0)
		{
			IMElement	orig	= null;
			try
			{
				orig	= findOriginalElement();
			}
			catch(Exception e) {}
		
			Class	clazz	= null;
			if(orig instanceof IMParameterSet)
			{
				clazz	= ((IMParameterSet)orig).getClazz();
			}
			else if(orig instanceof IMParameterSetReference)
			{
				clazz	= ((IMParameterSetReference)orig).getClazz();
			}
				
			if(clazz!=null)
			{
				for(int i=0; i<initialvalues.size(); i++)
				{
					((MExpression)initialvalues.get(i)).checkClass(clazz, report);
				}
				
				// Todo: check initialvalues expression!?
			}
		}
	}

	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., an initialbeliefsetreference can only be a reference to a beliefset(reference),
	 *  and not a belief(reference).
	 */
	protected boolean assignable(IMElement orig)
	{
		return orig instanceof IMParameterSet || orig instanceof IMParameterSetReference;
	}

	//-------- initial values --------

	/**
	 *  Get the initial values.
	 *  @return	The initial values.
	 */
	public IMExpression[]	getInitialValues()
	{
		if(initialvalues==null)
			return MExpression.EMPTY_EXPRESSION_SET;
		return (IMExpression[])initialvalues.toArray(new IMExpression[initialvalues.size()]);
	}

	/**
	 *  Create a initial value.
	 *  @param expression	The value expression string.
	 *  @return the newly created value expression.
	 */
	public IMExpression	createInitialValue(String expression)
	{
		if(initialvalues==null)
			initialvalues = SCollection.createArrayList();

		assert expression!=null : this;
		MExpression ret = new MExpression();
		ret.setExpressionText(expression);
		ret.setOwner(this);
		ret.init();
		initialvalues.add(ret);
		return ret;
	}

	/**
	 *  Delete a initial value.
	 *  @param value	The value expression.
	 */
	public void	deleteInitialValue(IMExpression value)
	{
		if(!initialvalues.remove(value))
			throw new RuntimeException("Could not find initial value: "+value);
	}


	//-------- initial values expression --------

	/**
	 *  Get the initial values, when represented as a single expression (returning a collection).
	 *  @return	The initial values.
	 */
	public IMExpression	getInitialValuesExpression()
	{
		return this.initialvaluesexp;
	}

	/**
	 *  Create the initial values expression.
	 *  @param expression	The values expression string.
	 *  @param mode	The evaluation mode.
	 *  @return the newly created values expression.
	 */
	public IMExpression	createInitialValuesExpression(String expression, String mode)
	{
		assert expression!=null : this;
		initialvaluesexp = new MExpression();
		initialvaluesexp.setExpressionText(expression);
		initialvaluesexp.setEvaluationMode(mode);
		initialvaluesexp.setOwner(this);
		initialvaluesexp.init();
		return initialvaluesexp;
	}

	/**
	 *  Delete the initial values expression.
	 */
	public void	deleteInitialValuesExpression()
	{
		initialvaluesexp = null;
	}

	//-------- other methods --------

	/**
	 *  Resolve the reference to the original element.
	 */
	protected IMElement findOriginalElement()
	{
		IMReferenceableElement	ref	= null;

		IMElement elemref = ((IMConfigParameterElement)getOwner()).getOriginalElement();
		if(elemref instanceof IMParameterElement)
		{
			ref = ((IMParameterElement)elemref).getParameterSet(getReference());
		}
		else if(elemref instanceof IMParameterElementReference)
		{
			ref = ((IMParameterElementReference)elemref).getParameterSetReference(getReference());
			// Search for implicitly added reference parameters. (hack???)
			while(ref==null && isChecking() && (elemref instanceof IMParameterElementReference))
			{
				elemref	= ((IMParameterElementReference)elemref).getReferencedElement();
				if(elemref instanceof IMParameterElement)
				{
					ref = ((IMParameterElement)elemref).getParameterSet(getReference());
				}
				else if(elemref instanceof IMParameterElementReference)
				{
					ref = ((IMParameterElementReference)elemref).getParameterSetReference(getReference());
				}
			}
		}
		return ref;
	}

	//-------- jibx related --------

	/**
	 *  Add a initialvalue.
	 *  @param initialvalue The initialvalue.
	 */
	public void addInitialValue(MExpression initialvalue)
	{
		if(initialvalues==null)
			initialvalues = SCollection.createArrayList();
		initialvalues.add(initialvalue);
	}

	/**
	 *  Get an iterator for all initialvalues.
	 *  @return The iterator.
	 */
	public Iterator iterInitialValues()
	{
		return initialvalues==null? Collections.EMPTY_LIST.iterator(): initialvalues.iterator();
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MConfigParameterSet clone = (MConfigParameterSet)cl;
		if(initialvaluesexp!=null)
			clone.initialvaluesexp = (MExpression)initialvaluesexp.clone();
		if(initialvalues!=null)
		{
			clone.initialvalues = SCollection.createArrayList();
			for(int i=0; i<initialvalues.size(); i++)
				clone.initialvalues.add(((MElement)initialvalues.get(i)).clone());
		}
	}
}
