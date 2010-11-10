package jadex.model.jibximpl;

import jadex.model.*;
import jadex.util.collection.SCollection;

import java.util.*;

/**
 *  Configuration for the beliefbase.
 */
public class MConfigBeliefbase extends MConfigBase implements IMConfigBeliefbase
{
	//-------- xml attributes --------

	/** The list of initial beliefs. */
	protected ArrayList beliefs;

	/** The list of initial belief sets. */
	protected ArrayList beliefsets;

	//-------- constructors --------

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);

		// Check if names of elements in base are unique.
		checkRefUniqueness(this, report, getElements());
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
		if(beliefs!=null)
			ret.addAll(beliefs);
		if(beliefsets!=null)
			ret.addAll(beliefsets);
		return ret;
	}

	//-------- beliefs --------

	/**
	 *  Get all defined beliefs.
	 *  @return The beliefs.
	 */
	public IMConfigBelief[] getInitialBeliefs()
	{
		if(beliefs==null)
			return new IMConfigBelief[0];
		return (IMConfigBelief[])beliefs
			.toArray(new IMConfigBelief[beliefs.size()]);
	}

	/**
	 *  Create a new initial belief.
	 *  @param ref	The name of the referenced belief.
	 *  @param expression	The fact expression.
	 *  @param mode	The evaluation mode.
	 *  @return	The newly created initial belief.
	 */
	public IMConfigBelief	createInitialBelief(String ref, String expression, String mode)
	{
		if(beliefs==null)
			beliefs = SCollection.createArrayList();

		MConfigBelief ret = new MConfigBelief();
		ret.setReference(ref);
		if(expression!=null)
			ret.createInitialFact(expression, mode);
		ret.setOwner(this);
		ret.init();
		beliefs.add(ret);
		return ret;
	}

	/**
	 *  Delete a belief.
	 *  @param belief	The belief to delete.
	 */
	public void	deleteInitialBelief(IMConfigBelief belief)
	{
		if(!beliefs.remove(belief))
			throw new RuntimeException("Initial belief not found: "+belief);
	}


	//-------- belief sets --------

	/**
	 *  Get all defined belief sets.
	 *  @return The belief sets.
	 */
	public IMConfigBeliefSet[] getInitialBeliefSets()
	{
		if(beliefsets==null)
			return new IMConfigBeliefSet[0];
		return (IMConfigBeliefSet[])beliefsets
			.toArray(new IMConfigBeliefSet[beliefsets.size()]);
	}

	/**
	 *  Create a new belief set.
	 *  @param ref	The name of the referenced belief.
	 *  @param expression	The initial facts expression (if any).
	 *  @return	The newly created belief set.
	 */
	public IMConfigBeliefSet	createInitialBeliefSet(String ref, String expression)
	{
		if(beliefsets==null)
			beliefsets = SCollection.createArrayList();

		MConfigBeliefSet ret = new MConfigBeliefSet();
		ret.setReference(ref);
		if(expression!=null)
			ret.createInitialFact(expression);
		ret.setOwner(this);
		ret.init();
		beliefsets.add(ret);
		return ret;
	}

	/**
	 *  Delete a belief set.
	 *  @param beliefset	The belief set to delete.
	 */
	public void	deleteInitialBeliefSet(IMConfigBeliefSet beliefset)
	{
		if(!beliefsets.remove(beliefset))
			throw new RuntimeException("Initial beliefset not found: "+beliefset);

	}


	//-------- not xml related --------

	/**
	 *  Get a defined belief.
	 *  @return The belief.
	 */
	public IMConfigBelief getInitialBelief(IMBelief elem)
	{
		IMConfigBelief	ret	= null;
		for(int i=0; beliefs!=null && i<beliefs.size(); i++)
		{
			IMConfigBelief	test	= (IMConfigBelief)beliefs.get(i);
			if(elem == test.getOriginalElement())
				ret	= test;
		}
		return ret;
	}

	/**
	 *  Get a defined belief set.
	 *  @return The belief set.
	 */
	public IMConfigBeliefSet getInitialBeliefSet(IMBeliefSet elem)
	{
		IMConfigBeliefSet	ret	= null;
		for(int i=0; beliefsets!=null && i<beliefsets.size(); i++)
		{
			IMConfigBeliefSet	test = (IMConfigBeliefSet)beliefsets.get(i);
			if(elem == test.getOriginalElement())
				ret	= test;
		}
		return ret;
	}


	/**
	 *  Get the initial configuration for a given belief reference.
	 *  @param beliefref	The belief reference.
	 *  @return	The initial belief reference configuration.
	 */
	public IMConfigBelief	getInitialBelief(IMBeliefReference beliefref)
	{
		IMConfigBelief	ret	= null;
		for(int i=0; beliefs!=null && i<beliefs.size(); i++)
		{
			IMConfigBelief	test	= (IMConfigBelief)beliefs.get(i);
			if(beliefref == test.getOriginalElement())
				ret	= test;
		}
		return ret;
	}

	/**
	 *  Get the initial configuration for a given belief set reference.
	 *  @param beliefsetref	The belief set reference.
	 *  @return	The initial belief set reference configuration.
	 */
	public IMConfigBeliefSet	getInitialBeliefSet(IMBeliefSetReference beliefsetref)
	{
		IMConfigBeliefSet	ret	= null;
		for(int i=0; beliefsets!=null && i<beliefsets.size(); i++)
		{
			IMConfigBeliefSet	test = (IMConfigBeliefSet)beliefsets.get(i);
			if(beliefsetref == test.getOriginalElement())
				ret	= test;
		}
		return ret;
	}

	//-------- methods --------

	/**
	 *  Resolve the reference to the original element.
	 *  @return The original element.
	 */
	protected IMElement findOriginalElement()
	{
		return getScope().getBeliefbase();
	}

	//-------- jibx related --------

	/**
	 *  Add a initialbelief.
	 *  @param initialbelief The initialbelief.
	 */
	public void addInitialBelief(MConfigBelief initialbelief)
	{
		if(beliefs==null)
			beliefs = SCollection.createArrayList();
		beliefs.add(initialbelief);
	}

	/**
	 *  Get an iterator for all initialbeliefs.
	 *  @return The iterator.
	 */
	public Iterator iterInitialBeliefs()
	{
		return beliefs==null? Collections.EMPTY_LIST.iterator(): beliefs.iterator();
	}

	/**
	 *  Add a initialbeliefset.
	 *  @param initialbeliefset The initialbeliefset.
	 */
	public void addInitialBeliefSet(MConfigBeliefSet initialbeliefset)
	{
		if(beliefsets==null)
			beliefsets = SCollection.createArrayList();
		beliefsets.add(initialbeliefset);
	}

	/**
	 *  Get an iterator for all initialbeliefsets.
	 *  @return The iterator.
	 */
	public Iterator iterInitialBeliefSets()
	{
		return beliefsets==null? Collections.EMPTY_LIST.iterator(): beliefsets.iterator();
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MConfigBeliefbase clone = (MConfigBeliefbase)cl;
		if(beliefs!=null)
		{
			clone.beliefs = SCollection.createArrayList();
			for(int i=0; i<beliefs.size(); i++)
				clone.beliefs.add(((MElement)beliefs.get(i)).clone());
		}
		if(beliefsets!=null)
		{
			clone.beliefsets = SCollection.createArrayList();
			for(int i=0; i<beliefsets.size(); i++)
				clone.beliefsets.add(((MElement)beliefsets.get(i)).clone());
		}
	}
}
