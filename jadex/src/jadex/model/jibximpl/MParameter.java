package jadex.model.jibximpl;

import jadex.model.*;
import jadex.util.SReflect;

import java.util.List;

/**
 *  The parameter type.
 */
public class MParameter extends MTypedElement implements IMParameter
{
	//-------- attributes --------

	/** The default value. */
	protected MExpression defaultvalue;

	/** The binding options. */
	protected MExpression bindingoptions;

	/** The optional flag. */
	protected boolean optional	= false;

	/** The direction. */
	protected String direction	= DIRECTION_IN;

	//-------- constructors --------

	/**
	 *  The setup is called by a configuration code object.
	 */
	protected void init()
	{
		super.init();

		// The default value must not null, when a basic type is declared.
		// Hence a new default value is created.
		if(getDefaultValue()==null && getClazz()!=null && SReflect.isBasicType(getClazz()))
		{
			if(getClazz()==boolean.class)
				createDefaultValue("false", IMExpression.MODE_STATIC);
			else if(getClazz()==byte.class)
				createDefaultValue("0", IMExpression.MODE_STATIC);
			else if(getClazz()==char.class)
				createDefaultValue("0", IMExpression.MODE_STATIC);
			else if(getClazz()==short.class)
				createDefaultValue("0", IMExpression.MODE_STATIC);
			else if(getClazz()==double.class)
				createDefaultValue("0", IMExpression.MODE_STATIC);
			else if(getClazz()==float.class)
				createDefaultValue("0", IMExpression.MODE_STATIC);
			else if(getClazz()==long.class)
				createDefaultValue("0", IMExpression.MODE_STATIC);
			else if(getClazz()==int.class)
				createDefaultValue("0", IMExpression.MODE_STATIC);
		}

		if(getDefaultValue()!=null)
		{
			if(getDefaultValue().getEvaluationMode()==null)
			{
				getDefaultValue().setEvaluationMode(
						getUpdateRate()!=0 ? IMExpression.MODE_DYNAMIC : IMExpression.MODE_STATIC);
			}
			getDefaultValue().setClazz(getClazz());
		}

		if(getBindingOptions()!=null && getBindingOptions().getEvaluationMode()==null)
		{
			getBindingOptions().setEvaluationMode(IMExpression.MODE_DYNAMIC);
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
		if(defaultvalue!=null)
			ret.add(defaultvalue);
		if(bindingoptions!=null)
			ret.add(bindingoptions);
		return ret;
	}
	
	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);
		
		if(defaultvalue!=null)
		{
			defaultvalue.checkClass(getClazz(), report);
		}	

		// Todo: check binding options!?
		
//		String[] astos = getAssignTos();
//		for(int i=0; i<astos.length; i++)
//		{
//			IMReferenceableElement	refelem = findReferencedElement(astos[i]);
//			...
//		}
	}
	
	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMParameterReference;
	}

	//-------- direction --------

	/**
	 *  Get the direction (in/out).
	 *  @return The direction of the parameter.
	 */
	public String	getDirection()
	{
		return direction;
	}

	/**
	 *  Set the direction (in/out).
	 *  @param direction	The direction of the parameter.
	 */
	public void	setDirection(String direction)
	{
		this.direction = direction;
	}


	//-------- optional --------

	/**
	 *  Get the optional flag.
	 *  @return True, if a value for this parameter is optional.
	 */
	public boolean	isOptional()
	{
		return optional;
	}

	/**
	 *  Set the optional flag.
	 *  @param optional	True, if a value for this parameter is optional.
	 */
	public void	setOptional(boolean optional)
	{
		this.optional = optional;
	}


	//-------- value --------

	/**
	 *  Get the default value of the parameter.
	 *  @return The default value expression (if any).
	 */
	public IMExpression	getDefaultValue()
	{
		return defaultvalue;
	}

	/**
	 *  Create the default value for the parameter.
	 *  @param expression	The expression string.
	 *  @param mode	The evaluation mode.
	 *  @return The new value expression.
	 */
	public IMExpression	createDefaultValue(String expression, String mode)
	{
		assert expression!=null : this;
		this.defaultvalue = new MExpression();
		defaultvalue.setExpressionText(expression);
		defaultvalue.setEvaluationMode(mode);
		defaultvalue.setOwner(this);
		defaultvalue.init();
		return defaultvalue;
	}

	/**
	 *  Delete the default value of the parameter.
	 */
	public void	deleteDefaultValue()
	{
		this.defaultvalue = null;
	}


	//-------- binding --------

	/**
	 *  Get the binding options of the parameter (i.e. a collection of possible values).
	 *  @return The binding options expression (if any).
	 */
	public IMExpression	getBindingOptions()
	{
		return bindingoptions;
	}

	/**
	 *  Create the binding options for the parameter (i.e. a collection of possible values).
	 *  @param expression	The expression string.
	 *  @param mode	The evaluation mode.
	 *  @return The new value expression.
	 */
	public IMExpression	createBindingOptions(String expression, String mode)
	{
		assert expression!=null : this;
		this.bindingoptions = new MExpression();
		bindingoptions.setExpressionText(expression);
		bindingoptions.setEvaluationMode(mode);
		bindingoptions.setOwner(this);
		bindingoptions.init();
		return bindingoptions;
	}

	/**
	 *  Delete the binding options of the parameter (i.e. a collection of possible values).
	 */
	public void	deleteBindingOptions()
	{
		bindingoptions = null;
	}

	//-------- additional methods --------

	/**
	 *  Init the assign to elements.
	 *  todo: method supports unqualified assignto names
	 *  instead "capa.elem.param" -> param
	 *  assumes that assigntos in elem are in same order
	 *  problem: doCheck calls also findRefereceableElement :-(
	 * /
	protected void initAssignToElements()
	{
		assert !isChecking() : this;
		assert getOwner()!=null;

		// It is assumed that assigntos of element and parameter occur
		// in the same order. Otherwise fully qualified names are necessary.
		String[] ownerasstos = ((IMReferenceableElement)getOwner()).getAssignTos();
		String[] asstos = getAssignTos();

		assert ownerasstos.length==asstos.length;

		for(int i=0; i<asstos.length; i++)
		{
			String refname = asstos[i];
			// Prepend corresponding ownerref. todo: is this a litlle hack?!
			if(refname.indexOf(".")==-1)
				refname = ownerasstos[i]+"."+refname;

			IMReferenceableElement refelem = findReferencedElement(refname);
			if(refelem==null)
			{
				System.out.println(getName());
				refelem = findReferencedElement(refname);
			}
			assert refelem!=null;
			addAssignToElement(refelem);
		}
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
		String baseelemname = refname.substring(0, idx);
		String refelemname = refname.substring(idx+1);
		IMReferenceableElement ownerref = ((MReferenceableElement)getOwner()).findReferencedElement(baseelemname);

		if(ownerref instanceof IMParameterElement)
		{
			ret	= ((IMParameterElement)ownerref).getParameter(refelemname);
		}
		else if(ownerref instanceof IMParameterElementReference)
		{
			ret	= ((IMParameterElementReference)ownerref).getParameterReference(refelemname);
		}

		return ret;
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MParameter clone = (MParameter)cl;
		if(defaultvalue!=null)
			clone.defaultvalue = (MExpression)defaultvalue.clone();
		if(bindingoptions!=null)
			clone.bindingoptions = (MExpression)bindingoptions.clone();
	}

}
