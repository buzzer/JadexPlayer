package jadex.model.jibximpl;

import jadex.model.*;
import jadex.util.SReflect;
import jadex.util.collection.SCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *  The belief set type.
 */
public class MBeliefSet extends MTypedElementSet implements IMBeliefSet
{
	//-------- xml attributes --------

	/** The default facts. */
	protected ArrayList defaultfacts;

	/** The default facts expression. */
	protected MExpression defaultfactsexp;

	//-------- constructors --------

	/**
	 *  The setup is called by a configuration code object.
	 */
	protected void init()
	{
		super.init();

		IMExpression[] vals = getDefaultFacts();
		for(int i=0; i<vals.length; i++)
		{
			vals[i].setClazz(getClazz());
		}

		if(getDefaultFactsExpression()!=null && getDefaultFactsExpression().getEvaluationMode()==null)
		{
			getDefaultFactsExpression().setEvaluationMode(
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
		if(defaultfacts!=null)
			ret.addAll(defaultfacts);
		if(defaultfactsexp!=null)
			ret.add(defaultfactsexp);
		return ret;
	}

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);
		
		for(int i=0; defaultfacts!=null && getClazz()!=null && i<defaultfacts.size(); i++)
		{
			((MExpression)defaultfacts.get(i)).checkClass(getClazz(), report);
		}
		
		// Todo: check defaultfacts expression!?

		String[] astos = getAssignTos();
		for(int i=0; i<astos.length; i++)
		{
			IMReferenceableElement	refelem = findReferencedElement(astos[i]);
			if(refelem instanceof IMBeliefSetReference)
			{
				Class	clazz	= ((IMBeliefSetReference)refelem).getClazz();
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
		return ref instanceof IMBeliefSetReference;
	}

	//-------- default facts --------

	/**
	 *  Get the default facts.
	 *  @return	The default facts.
	 */
	public IMExpression[]	getDefaultFacts()
	{
		if(defaultfacts==null)
			return MExpression.EMPTY_EXPRESSION_SET;
		return (IMExpression[])defaultfacts.toArray(new MExpression[defaultfacts.size()]);
	}

	/**
	 *  Create a default fact.
	 *  @param expression	The fact expression string.
	 *  @return the newly created fact expression.
	 */
	public IMExpression	createDefaultFact(String expression)
	{
		assert expression!=null : this;

		if(defaultfacts==null)
			defaultfacts = SCollection.createArrayList();

		MExpression ret = new MExpression();
		ret.setExpressionText(expression);
		ret.setEvaluationMode(IMExpression.MODE_STATIC);
		ret.setOwner(this);
		ret.init();
		defaultfacts.add(ret);
		return ret;
	}

	/**
	 *  Delete a default fact.
	 *  @param fact	The fact expression.
	 */
	public void	deleteDefaultFact(IMExpression fact)
	{
		if(!defaultfacts.remove(fact))
			throw new RuntimeException("Default fact not found: "+fact);
	}


	//-------- default facts expression --------

	/**
	 *  Get the default facts, when represented as a single expression (returning a collection).
	 *  @return	The default facts.
	 */
	public IMExpression getDefaultFactsExpression()
	{
		return this.defaultfactsexp;
	}

	/**
	 *  Create the default facts expression.
	 *  @param expression	The facts expression string.
	 *  @param mode	The evaluation mode.
	 *  @return the newly created facts expression.
	 */
	public IMExpression	createDefaultFactsExpression(String expression, String mode)
	{
		assert expression!=null : this;
		this.defaultfactsexp = new MExpression();
		defaultfactsexp.setExpressionText(expression);
		defaultfactsexp.setEvaluationMode(mode);
		defaultfactsexp.setOwner(this);
		defaultfactsexp.init();
		return defaultfactsexp;
	}

	/**
	 *  Delete the default facts expression.
	 */
	public void	deleteDefaultFactsExpression()
	{
		this.defaultfactsexp = null;
	}

	//-------- jibx related --------

	/**
	 *  Add a defaultfact.
	 *  @param defaultfact The defaultfact.
	 */
	public void addDefaultFact(MExpression defaultfact)
	{
		if(defaultfacts==null)
			defaultfacts = SCollection.createArrayList();
		defaultfacts.add(defaultfact);
	}

	/**
	 *  Get an iterator for all defaultfacts.
	 *  @return The iterator.
	 */
	public Iterator iterDefaultFacts()
	{
		return defaultfacts==null? Collections.EMPTY_LIST.iterator(): defaultfacts.iterator();
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MBeliefSet clone = (MBeliefSet)cl;
		if(defaultfactsexp!=null)
			clone.defaultfactsexp = (MExpression)defaultfactsexp.clone();
		if(defaultfacts!=null)
		{
			clone.defaultfacts = SCollection.createArrayList();
			for(int i=0; i<defaultfacts.size(); i++)
				clone.defaultfacts.add(((MElement)defaultfacts.get(i)).clone());
		}
	}
}
