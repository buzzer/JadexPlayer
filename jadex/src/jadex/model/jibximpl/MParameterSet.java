package jadex.model.jibximpl;

import jadex.model.*;
import jadex.util.collection.SCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *  The parameter set type.
 */
public class MParameterSet extends MTypedElementSet implements IMParameterSet
{
	//-------- xml attributes --------

	/** The default values. */
	protected ArrayList defaultvalues;

	/** The default values expression. */
	protected MExpression defaultvaluesexp;

	/** The optional flag. */
	protected boolean optional	= false;

	/** The direction flag. */
	protected String direction	= DIRECTION_IN;

	//-------- constructors --------

	/**
	 *  The setup is called by a configuration code object.
	 */
	protected void init()
	{
		super.init();
		
		IMExpression[] vals = getDefaultValues();
		for(int i=0; i<vals.length; i++)
		{
			vals[i].setClazz(getClazz());
		}

		if(getDefaultValuesExpression()!=null && getDefaultValuesExpression().getEvaluationMode()==null)
		{
			getDefaultValuesExpression().setEvaluationMode(
				getUpdateRate()!=0 ? IMExpression.MODE_DYNAMIC : IMExpression.MODE_STATIC);
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
		if(defaultvalues!=null)
			ret.addAll(defaultvalues);
		if(getDefaultValuesExpression()!=null)
			ret.add(getDefaultValuesExpression());
		return ret;
	}

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);

		for(int i=0; defaultvalues!=null && getClazz()!=null && i<defaultvalues.size(); i++)
		{
			((MExpression)defaultvalues.get(i)).checkClass(getClazz(), report);
		}
	}
	
	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMParameterSetReference;
	}

	//-------- direction --------

	/**
	 *  Get the direction (in/out).
	 *  @return The direction of the parameter set.
	 */
	public String	getDirection()
	{
		return direction;
	}

	/**
	 *  Set the direction (in/out).
	 *  @param direction	The direction of the parameter set.
	 */
	public void	setDirection(String direction)
	{
		this.direction = direction;
	}


	//-------- optional --------

	/**
	 *  Get the optional flag.
	 *  @return True, if values for this parameter set are optional.
	 */
	public boolean	isOptional()
	{
		return optional;
	}

	/**
	 *  Set the optional flag.
	 *  @param optional	True, if values for this parameter set are optional.
	 */
	public void	setOptional(boolean optional)
	{
		this.optional = optional;
	}


	//-------- values --------

	/**
	 *  Get the default values of the parameter.
	 *  @return The default values.
	 */
	public IMExpression[]	getDefaultValues()
	{
		if(defaultvalues==null)
			return MExpression.EMPTY_EXPRESSION_SET;
		return (IMExpression[])defaultvalues.toArray(new IMExpression[defaultvalues.size()]);
	}

	/**
	 *  Create a default values for the parameter.
	 *  @param expression	The expression string.
	 *  @return The new value expression.
	 */
	public IMExpression	createDefaultValue(String expression)
	{
		if(defaultvalues==null)
			defaultvalues = SCollection.createArrayList();
		
		assert expression!=null : this;
		MExpression ret = new MExpression();
		ret.setExpressionText(expression);
		ret.setOwner(this);
		ret.init();
		defaultvalues.add(ret);
		return ret;
	}

	/**
	 *  Delete the default values of the parameter.
	 *  @param value	The value expression.
	 */
	public void	deleteDefaultValue(IMExpression value)
	{
		if(!defaultvalues.remove(value))
			throw new RuntimeException("Default value not found: "+value);
	}


	//-------- default values expression --------

	/**
	 *  Get the default values expression of the parameter (returning a collection of default values).
	 *  @return The default values expression expression (if any).
	 */
	public IMExpression	getDefaultValuesExpression()
	{
		return this.defaultvaluesexp;
	}

	/**
	 *  Create the default values expression for the parameter (returning a collection of default values).
	 *  @param expression	The expression string.
	 *  @param mode	The evaluation mode.
	 *  @return The new value expression.
	 */
	public IMExpression	createDefaultValuesExpression(String expression, String mode)
	{
		assert expression!=null : this;
		this.defaultvaluesexp = new MExpression();
		defaultvaluesexp.setExpressionText(expression);
		defaultvaluesexp.setEvaluationMode(mode);
		defaultvaluesexp.setOwner(this);
		defaultvaluesexp.init();
		return defaultvaluesexp;
	}

	/**
	 *  Delete the default values expression of the parameter (returning a collection of default values).
	 */
	public void	deleteDefaultValuesExpression()
	{
		this.defaultvaluesexp = null;
	}

	//-------- additional methods --------

	/**
	 *  Init the assign to elements.
	 *  todo: only necessary if we want to support unqualified names
	 *  like "paramname". See also MParameter
	 * /
	protected void initAssignToElements()
	{
		System.out.println("Init assignto on parameterset: "+getName());		
	}*/

	/**
	 *  Init the assign from element.
	 */
	protected IMReferenceableElement	findReferencedElement(String refname)
	{
		IMReferenceableElement ret	= null;

		// assure that there are exactly two "." characters.
		assert refname.indexOf(".")!=-1 && refname.lastIndexOf(".")!=-1
			&& refname.indexOf(".")!=refname.lastIndexOf(".");

		int idx = refname.lastIndexOf(".");
		int lidx = refname.lastIndexOf(".");
		String baseelemname = refname.substring(0, idx);
		String refelemname = refname.substring(lidx+1);
		IMReferenceableElement ownerref = ((MReferenceableElement)getOwner()).findReferencedElement(baseelemname);

		if(ownerref instanceof IMParameterElement)
		{
			ret	= ((IMParameterElement)ownerref).getParameterSet(refelemname);
		}
		else if(ownerref instanceof IMParameterElementReference)
		{
			ret	= ((IMParameterElementReference)ownerref).getParameterSetReference(refelemname);
		}

		return ret;
	}

	//-------- jibx related --------

	/**
	 *  Add a defaultvalue.
	 *  @param defaultvalue The defaultvalue.
	 */
	public void addDefaultValue(MExpression defaultvalue)
	{
		if(defaultvalues==null)
			defaultvalues = SCollection.createArrayList();
		defaultvalues.add(defaultvalue);
	}

	/**
	 *  Get an iterator for all defaultvalues.
	 *  @return The iterator.
	 */
	public Iterator iterDefaultValues()
	{
		return defaultvalues==null? Collections.EMPTY_LIST.iterator(): defaultvalues.iterator();
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MParameterSet clone = (MParameterSet)cl;
		if(defaultvaluesexp!=null)
			clone.defaultvaluesexp = (MExpression)defaultvaluesexp.clone();
		if(defaultvalues!=null)
		{
			clone.defaultvalues = SCollection.createArrayList();
			for(int i=0; i<defaultvalues.size(); i++)
				clone.defaultvalues.add(((MElement)defaultvalues.get(i)).clone());
		}
	}
}
