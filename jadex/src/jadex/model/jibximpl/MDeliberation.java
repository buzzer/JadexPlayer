package jadex.model.jibximpl;

import java.util.*;
import jadex.model.*;
import jadex.util.collection.SCollection;

/**
 *  The deliberation settings of a goal.
 */
public class MDeliberation extends MElement implements IMDeliberation
{
	//-------- xml attributes --------

	/** The cardinality. */
	protected int cardinality = -1;

	/** The inhibited goals. */
	protected ArrayList inhibits;

	/**
	 *  Method called on first setup run.
	 *  Overwritten in elements that have
	 *  child elements.
	 *  @return The list of direct child elements.
	 */
	public List	getChildren()
	{
		List ret = super.getChildren();
		if(inhibits!=null)
			ret.addAll(inhibits);
		return ret;
	}

	//-------- xml methods --------

	//-------- cardinality --------

	/**
	 *  Get the cardinality.
	 *  @return The cardinality.
	 */
	public int getCardinality()
	{
		return this.cardinality;
	}

	/**
	 *  Set the cardinality.
	 *  @param cardinality The cardiniality.
	 */
	public void setCardinality(int cardinality)
	{
		this.cardinality = cardinality;
	}

	//-------- inhibits --------

	/**
	 *  Get all inhibition links.
	 *  @return The inhibition links originating from the enclosing goal.
	 */
	public IMInhibits[]	getInhibits()
	{
		if(inhibits==null)
			return new IMInhibits[0];
		return (IMInhibits[])inhibits.toArray(new MInhibits[inhibits.size()]);
	}

	/**
	 *  Create a new inhibition link.
	 *  @param ref	The goal type to be inhibited.
	 *  @param expression	An optional boolean expression specifying the context in which the inhibition link is active.
	 *  @param inhibit	An optional identifier for the goal's lifecycle state in which the inhibition link is active.
	 */
	public IMInhibits	createInhibits(String ref, String expression, String inhibit)
	{
		if(inhibits==null)
			inhibits = SCollection.createArrayList();

		MInhibits ret = new MInhibits();
		ret.setExpressionText(expression);
		ret.setInhibit(inhibit);
		ret.setOwner(this);
		ret.init();
		inhibits.add(ret);
		return ret;
	}

	/**
	 *  Delete an inhibition link.
	 *  @param inhibits The inhibition link.
	 */
	public void	deleteInhibits(IMInhibits inhibits)
	{
		if(!this.inhibits.remove(inhibits))
			throw new RuntimeException("Inhibit not found: "+inhibits);
	}

	//-------- jibx related --------

	/**
	 *  Add a inhibits.
	 *  @param inhibit The inhibits.
	 */
	public void addInhibits(MInhibits inhibit)
	{
		if(inhibits==null)
			inhibits = SCollection.createArrayList();
		inhibits.add(inhibit);
	}

	/**
	 *  Get an iterator for all inhibitss.
	 *  @return The iterator.
	 */
	public Iterator iterInhibits()
	{
		return inhibits==null? Collections.EMPTY_LIST.iterator(): inhibits.iterator();
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MDeliberation clone = (MDeliberation)cl;
		if(inhibits!=null)
		{
			clone.inhibits = SCollection.createArrayList();
			for(int i=0; i<inhibits.size(); i++)
				clone.inhibits.add(((MElement)inhibits.get(i)).clone());
		}
	}
}
