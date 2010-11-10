package jadex.model.jibximpl;

import java.util.*;
import jadex.model.*;
import jadex.util.collection.SCollection;

/**
 *  Configuration for the goalbase.
 */
public class MConfigGoalbase extends MConfigBase implements IMConfigGoalbase
{
	//-------- xml attributes --------

	/** The initial goals. */
	protected ArrayList initialgoals;

	/** The end goals. */
	protected ArrayList endgoals;

	//-------- constructors --------

	/**
	 *  Method called on first setup run.
	 *  Overwritten in elements that have
	 *  child elements.
	 *  @return The list of direct child elements.
	 */
	public List	getChildren()
	{
		List ret = super.getChildren();
		if(initialgoals!=null)
			ret.addAll(initialgoals);
		if(endgoals!=null)
			ret.addAll(endgoals);
		return ret;
	}

	//-------- initial goals --------

	/**
	 *  Get all known initial goals.
	 *  @return The initial goals.
	 */
	public IMConfigGoal[] getInitialGoals()
	{
		if(initialgoals==null)
			return new IMConfigGoal[0];
		return (IMConfigGoal[])initialgoals.toArray(new IMConfigGoal[initialgoals.size()]);
	}

	/**
	 *  Create a initial goal.
	 *  @param ref	The name of the referenced goal.
	 *  @return The initial goal.
	 */
	public IMConfigGoal createInitialGoal(String ref)
	{
		if(initialgoals==null)
			initialgoals = SCollection.createArrayList();

		MConfigGoal ret = new MConfigGoal();
		ret.setReference(ref);
		ret.setOwner(this);
		ret.init();
		initialgoals.add(ret);
		return ret;
	}

	/**
	 *  Delete a initial goal.
	 *  @param goal	The initial goal.
	 */
	public void	deleteInitialGoal(IMConfigGoal goal)
	{
		if(!initialgoals.remove(goal))
			throw new RuntimeException("Goal not found: "+goal);
	}

	//-------- end goals --------

	/**
	 *  Get all known end goals.
	 *  @return The end goals.
	 */
	public IMConfigGoal[] getEndGoals()
	{
		if(endgoals==null)
			return new IMConfigGoal[0];
		return (IMConfigGoal[])endgoals.toArray(new IMConfigGoal[endgoals.size()]);
	}

	/**
	 *  Create a end goal.
	 *  @param ref	The name of the referenced goal.
	 *  @return The end goal.
	 */
	public IMConfigGoal createEndGoal(String ref)
	{
		if(endgoals==null)
			endgoals = SCollection.createArrayList();

		MConfigGoal ret = new MConfigGoal();
		ret.setReference(ref);
		ret.setOwner(this);
		ret.init();
		endgoals.add(ret);
		return ret;
	}

	/**
	 *  Delete a end goal.
	 *  @param goal	The end goal.
	 */
	public void	deleteEndGoal(IMConfigGoal goal)
	{
		if(!endgoals.remove(goal))
			throw new RuntimeException("Goal not found: "+goal);
	}

	//-------- methods ---------

	/**
	 *  Resolve the reference to the original element.
	 *  @return The original element.
	 */
	protected IMElement findOriginalElement()
	{
		return getScope().getGoalbase();
	}

	//-------- jibx related --------

	/**
	 *  Add a initialgoal.
	 *  @param initialgoal The initialgoal.
	 */
	public void addInitialGoal(MConfigGoal initialgoal)
	{
		if(initialgoals==null)
			initialgoals = SCollection.createArrayList();
		initialgoals.add(initialgoal);
	}

	/**
	 *  Get an iterator for all initialgoals.
	 *  @return The iterator.
	 */
	public Iterator iterInitialGoals()
	{
		return initialgoals==null? Collections.EMPTY_LIST.iterator(): initialgoals.iterator();
	}

	/**
	 *  Add a endgoal.
	 *  @param endgoal The endgoal.
	 */
	public void addEndGoal(MConfigGoal endgoal)
	{
		if(endgoals==null)
			endgoals = SCollection.createArrayList();
		endgoals.add(endgoal);
	}

	/**
	 *  Get an iterator for all endgoals.
	 *  @return The iterator.
	 */
	public Iterator iterEndGoals()
	{
		return endgoals==null? Collections.EMPTY_LIST.iterator(): endgoals.iterator();
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MConfigGoalbase clone = (MConfigGoalbase)cl;
		if(initialgoals!=null)
		{
			clone.initialgoals = SCollection.createArrayList();
			for(int i=0; i<initialgoals.size(); i++)
				clone.initialgoals.add(((MElement)initialgoals.get(i)).clone());
		}
		if(endgoals!=null)
		{
			clone.endgoals = SCollection.createArrayList();
			for(int i=0; i<endgoals.size(); i++)
				clone.endgoals.add(((MElement)endgoals.get(i)).clone());
		}
	}
}
