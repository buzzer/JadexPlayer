package jadex.model.jibximpl;

import java.util.*;
import jadex.model.*;
import jadex.util.collection.SCollection;

/**
 *  Model element representing an initial belief set.
 */
public class MConfigBeliefSet extends MConfigReferenceableElement implements IMConfigBeliefSet
{
	//-------- xml attributes --------

	/** The initial facts. */
	protected ArrayList initialfacts;

	/** The initial facts expression. */
	protected MExpression initialfactsexp;

	//-------- constructors --------

	/**
	 *  The setup is called by a configuration code object.
	 */
	protected void init()
	{
		super.init();

		if(getInitialFactsExpression()!=null && getInitialFactsExpression().getEvaluationMode()==null)
		{
			// todo: hack! Cannot consider update rate of original.
			getInitialFactsExpression().setEvaluationMode(IMExpression.MODE_STATIC);
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
		if(initialfacts!=null)
			ret.addAll(initialfacts);
		if(getInitialFactsExpression()!=null)
			ret.add(getInitialFactsExpression());
		return ret;
	}

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);
		
		if(getReference()!=null && initialfacts!=null && initialfacts.size()>0)
		{
			IMElement	orig	= null;
			try
			{
				orig	= findOriginalElement();
			}
			catch(Exception e) {}
		
			Class	clazz	= null;
			if(orig instanceof IMBeliefSet)
			{
				clazz	= ((IMBeliefSet)orig).getClazz();
			}
			else if(orig instanceof IMBeliefSetReference)
			{
				clazz	= ((IMBeliefSetReference)orig).getClazz();
			}
				
			if(clazz!=null)
			{
				for(int i=0; i<initialfacts.size(); i++)
				{
					((MExpression)initialfacts.get(i)).checkClass(clazz, report);
				}
				
				// Todo: check initialfacts expression!?
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
		return orig instanceof IMBeliefSet || orig instanceof IMBeliefSetReference;
	}

	//-------- initial facts --------

	/**
	 *  Get the initial facts.
	 *  @return	The initial facts.
	 */
	public IMExpression[]	getInitialFacts()
	{
		if(initialfacts==null)
			return MExpression.EMPTY_EXPRESSION_SET;
		return (IMExpression[])initialfacts.toArray(new IMExpression[initialfacts.size()]);
	}

	/**
	 *  Create a initial fact.
	 *  @param expression	The fact expression string.
	 *  @return the newly created fact expression.
	 */
	public IMExpression	createInitialFact(String expression)
	{
		assert expression!=null : this;
		if(initialfacts==null)
			initialfacts = SCollection.createArrayList();

		MExpression ret = new MExpression();
		ret.setExpressionText(expression);
		ret.setOwner(this);
		ret.init();
		initialfacts.add(ret);
		return ret;
	}

	/**
	 *  Delete a initial fact.
	 *  @param fact	The fact expression.
	 */
	public void	deleteInitialFact(IMExpression fact)
	{
		if(!initialfacts.remove(fact))
			throw new RuntimeException("Initial fact not found: "+fact);
	}


	//-------- initial facts expression --------

	/**
	 *  Get the initial facts, when represented as a single expression (returning a collection).
	 *  @return	The initial facts.
	 */
	public IMExpression	getInitialFactsExpression()
	{
		return this.initialfactsexp;
	}

	/**
	 *  Create the initial facts expression.
	 *  @param expression	The facts expression string.
	 *  @param mode	The evaluation mode.
	 *  @return the newly created facts expression.
	 */
	public IMExpression	createInitialFactsExpression(String expression, String mode)
	{
		assert expression!=null : this;
		this.initialfactsexp = new MExpression();
		initialfactsexp.setExpressionText(expression);
		initialfactsexp.setEvaluationMode(mode);
		initialfactsexp.setOwner(this);
		initialfactsexp.init();
		return initialfactsexp;
	}

	/**
	 *  Delete the initial facts expression.
	 */
	public void	deleteInitialFactsExpression()
	{
		initialfactsexp = null;
	}

	//-------- jibx related --------

	/**
	 *  Add a initialfact.
	 *  @param initialfact The initialfact.
	 */
	public void addInitialFact(MExpression initialfact)
	{
		if(initialfacts==null)
			initialfacts = SCollection.createArrayList();
		initialfacts.add(initialfact);
	}

	/**
	 *  Get an iterator for all initialfacts.
	 *  @return The iterator.
	 */
	public Iterator iterInitialFacts()
	{
		return initialfacts==null? Collections.EMPTY_LIST.iterator(): initialfacts.iterator();
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MConfigBeliefSet clone = (MConfigBeliefSet)cl;
		if(initialfactsexp!=null)
			clone.initialfactsexp = (MExpression)initialfactsexp.clone();
		if(initialfacts!=null)
		{
			clone.initialfacts = SCollection.createArrayList();
			for(int i=0; i<initialfacts.size(); i++)
				clone.initialfacts.add(((MElement)initialfacts.get(i)).clone());
		}
	}
}
