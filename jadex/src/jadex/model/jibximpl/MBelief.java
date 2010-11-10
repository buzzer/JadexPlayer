package jadex.model.jibximpl;

import jadex.model.*;
import jadex.util.SReflect;

import java.util.List;

/**
 *  The belief model element is the java representation
 *  of a belief description (e.g. from the xml definition).
 */
public class MBelief extends MTypedElement implements IMBelief
{
	//-------- xml attributes --------

	/** The default fact. */
	protected MExpression defaultfact;

	//-------- constructors --------

	/**
	 *  The setup is called by a configuration code object.
	 */
	protected void init()
	{
		super.init();

		// The default value must not null, when a basic type is declared.
		// Hence a new default value is created.
		if(getDefaultFact()==null && getClazz()!=null && SReflect.isBasicType(getClazz()))
		{
			if(getClazz()==boolean.class)
				createDefaultFact("false", IMExpression.MODE_STATIC);
			else if(getClazz()==byte.class)
				createDefaultFact("0", IMExpression.MODE_STATIC);
			else if(getClazz()==char.class)
				createDefaultFact("0", IMExpression.MODE_STATIC);
			else if(getClazz()==short.class)
				createDefaultFact("0", IMExpression.MODE_STATIC);
			else if(getClazz()==double.class)
				createDefaultFact("0", IMExpression.MODE_STATIC);
			else if(getClazz()==float.class)
				createDefaultFact("0", IMExpression.MODE_STATIC);
			else if(getClazz()==long.class)
				createDefaultFact("0", IMExpression.MODE_STATIC);
			else if(getClazz()==int.class)
				createDefaultFact("0", IMExpression.MODE_STATIC);
		}

		if(getDefaultFact()!=null)
		{
			getDefaultFact().setClazz(getClazz());
			if(getDefaultFact().getEvaluationMode()==null)
			{
				getDefaultFact().setEvaluationMode(
					getUpdateRate()!=0 ? IMExpression.MODE_DYNAMIC : IMExpression.MODE_STATIC);
			}
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
		if(defaultfact!=null)
			ret.add(defaultfact);
		return ret;
	}

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);
		
		if(defaultfact!=null)
		{
			defaultfact.checkClass(getClazz(), report);
		}	

		String[] astos = getAssignTos();
		for(int i=0; i<astos.length; i++)
		{
			IMReferenceableElement	refelem = findReferencedElement(astos[i]);
			if(refelem instanceof IMBeliefReference)
			{
				Class	clazz	= ((IMBeliefReference)refelem).getClazz();
				// The abstract element must be supertype of original element.
				if(clazz!=null && getClazz()!=null && !SReflect.isSupertype(clazz, getClazz()))
					report.addEntry(this, "Abstract belief class is not supertype of original: "
						+getName()+" "+clazz+" "+getClazz());
			}
		}
	}

	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMBeliefReference;
	}

	//-------- fact --------

	/**
	 *  Get the default fact.
	 *  @return The default fact.
	 */
	public IMExpression getDefaultFact()
	{
		return this.defaultfact;
	}

	/**
	 *  Create the default fact for the belief.
	 *  @param expression	The expression string.
	 *  @param mode	The evaluation mode.
	 *  @return The new fact expression.
	 */
	public IMExpression	createDefaultFact(String expression, String mode)
	{
		assert expression!=null : this;
		this.defaultfact = new MExpression();
		defaultfact.setExpressionText(expression);
		defaultfact.setEvaluationMode(mode);
		defaultfact.setOwner(this);
		defaultfact.init();
		return defaultfact;
	}

	/**
	 *  Delete the default fact of the belief.
	 */
	public void	deleteDefaultFact()
	{
		this.defaultfact = null;
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MBelief clone = (MBelief)cl;
		if(defaultfact!=null)
			clone.defaultfact = (MExpression)defaultfact.clone();
	}
}
