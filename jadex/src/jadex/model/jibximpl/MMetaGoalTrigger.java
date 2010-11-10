package jadex.model.jibximpl;

import java.util.*;
import jadex.model.*;
import jadex.util.collection.SCollection;

/**
 *  The trigger for plans.
 */
public class MMetaGoalTrigger extends MTrigger implements IMMetaGoalTrigger
{
	//-------- attributes --------

	/** The goals. */
	protected ArrayList goals;

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
		if(goals!=null)
			ret.addAll(goals);
		return ret;
	}
	
	//-------- goals --------

	/**
	 *  Get the goals.
	 *  @return The goals.
	 */
	public IMReference[] getGoals()
	{
		if(goals==null)
			return new IMReference[0];
		return (IMReference[])goals.toArray(new IMReference[goals.size()]);
	}

	/**
	 *  Create a goal.
	 *  @param ref	The referenced goal.
	 *  @return The new goal.
	 */
	public IMReference createGoal(String ref)
	{
		if(goals==null)
			goals = SCollection.createArrayList();

		MReference ret = new MReference();
		ret.setReference(ref);
		ret.setOwner(this);
		ret.init();
		goals.add(ret);
		return ret;
	}

	/**
	 *  Delete a goal.
	 *  @param ref	The goal.
	 */
	public void deleteGoal(IMReference ref)
	{
		if(!goals.remove(ref))
			throw new RuntimeException("Goal ref not found: "+ref);
	}

	//-------- jibx related --------

	/**
	 *  Add a goals.
	 *  @param goal The goals.
	 */
	public void addGoal(MReference goal)
	{
		if(goals==null)
			goals = SCollection.createArrayList();
		goals.add(goal);
	}

	/**
	 *  Get an iterator for all goals.
	 *  @return The iterator.
	 */
	public Iterator iterGoals()
	{
		return goals==null? Collections.EMPTY_LIST.iterator(): goals.iterator();
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MMetaGoalTrigger clone = (MMetaGoalTrigger)cl;
		if(goals!=null)
		{
			clone.goals = SCollection.createArrayList();
			for(int i=0; i<goals.size(); i++)
				clone.goals.add(((MElement)goals.get(i)).clone());
		}
	}
}
