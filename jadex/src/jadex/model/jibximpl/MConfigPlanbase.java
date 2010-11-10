package jadex.model.jibximpl;

import java.util.*;
import jadex.model.*;
import jadex.util.collection.SCollection;

/**
 *  The configuration for the planbase.
 */
public class MConfigPlanbase extends MConfigBase implements IMConfigPlanbase
{
	//-------- xml attributes --------

	/** The initial plans. */
	protected ArrayList initialplans;

	/** The end plans. */
	protected ArrayList endplans;

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
		if(initialplans!=null)
			ret.addAll(initialplans);
		if(endplans!=null)
			ret.addAll(endplans);
		return ret;
	}

	//-------- initial plans --------

	/**
	 *  Get all defined Plans.
	 *  @return The Plans.
	 */
	public IMConfigPlan[] getInitialPlans()
	{
		if(initialplans==null)
			return new IMConfigPlan[0];
		return (IMConfigPlan[])initialplans
			.toArray(new IMConfigPlan[initialplans.size()]);
	}

	/**
	 *  Create a initial plan.
	 *  @param ref	The name of the referenced plan.
	 *  @return The initial plan.
	 */
	public IMConfigPlan	createInitialPlan(String ref)
	{
		if(initialplans==null)
			initialplans = SCollection.createArrayList();

		MConfigPlan ret = new MConfigPlan();
		ret.setReference(ref);
		ret.setOwner(this);
		ret.init();
		initialplans.add(ret);
		return ret;
	}

	/**
	 *  Delete a initial plan.
	 *  @param plan	The initial plan.
	 */
	public void	deleteInitialPlan(IMConfigPlan plan)
	{
		if(!initialplans.remove(plan))
			throw new RuntimeException("Could not find: "+plan);
	}

	//-------- end plans --------

	/**
	 *  Get all defined Plans.
	 *  @return The Plans.
	 */
	public IMConfigPlan[] getEndPlans()
	{
		if(endplans==null)
			return new IMConfigPlan[0];
		return (IMConfigPlan[])endplans
			.toArray(new IMConfigPlan[endplans.size()]);
	}

	/**
	 *  Create a end plan.
	 *  @param ref	The name of the referenced plan.
	 *  @return The end plan.
	 */
	public IMConfigPlan	createEndPlan(String ref)
	{
		if(endplans==null)
			endplans = SCollection.createArrayList();

		MConfigPlan ret = new MConfigPlan();
		ret.setReference(ref);
		ret.setOwner(this);
		ret.init();
		endplans.add(ret);
		return ret;
	}

	/**
	 *  Delete a end plan.
	 *  @param plan	The end plan.
	 */
	public void	deleteEndPlan(IMConfigPlan plan)
	{
		if(!endplans.remove(plan))
			throw new RuntimeException("Could not find: "+plan);
	}

	//-------- methods --------

	/**
	 *  Resolve the reference to the original element.
	 *  @return The original element.
	 */
	protected IMElement findOriginalElement()
	{
		return getScope().getPlanbase();
	}

	//-------- jibx related --------

	/**
	 *  Add a initialplan.
	 *  @param initialplan The initialplan.
	 */
	public void addInitialPlan(MConfigPlan initialplan)
	{
		if(initialplans==null)
			initialplans = SCollection.createArrayList();
		initialplans.add(initialplan);
	}

	/**
	 *  Get an iterator for all initialplans.
	 *  @return The iterator.
	 */
	public Iterator iterInitialPlans()
	{
		return initialplans==null? Collections.EMPTY_LIST.iterator(): initialplans.iterator();
	}

	/**
	 *  Add a endplan.
	 *  @param endplan The endplan.
	 */
	public void addEndPlan(MConfigPlan endplan)
	{
		if(endplans==null)
			endplans = SCollection.createArrayList();
		endplans.add(endplan);
	}

	/**
	 *  Get an iterator for all endplans.
	 *  @return The iterator.
	 */
	public Iterator iterEndPlans()
	{
		return endplans==null? Collections.EMPTY_LIST.iterator(): endplans.iterator();
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MConfigPlanbase clone = (MConfigPlanbase)cl;
		if(initialplans!=null)
		{
			clone.initialplans = SCollection.createArrayList();
			for(int i=0; i<initialplans.size(); i++)
				clone.initialplans.add(((MElement)initialplans.get(i)).clone());
		}
		if(endplans!=null)
		{
			clone.endplans = SCollection.createArrayList();
			for(int i=0; i<endplans.size(); i++)
				clone.endplans.add(((MElement)endplans.get(i)).clone());
		}
	}
}
