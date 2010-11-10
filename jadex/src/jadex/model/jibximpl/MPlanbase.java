package jadex.model.jibximpl;

import java.util.*;
import jadex.model.*;
import jadex.util.SUtil;
import jadex.util.collection.SCollection;

/**
 *  The model of a planbase containing known plans.
 */
public class MPlanbase extends MBase implements IMPlanbase
{
	//-------- xml attributes --------

	/** The plan list. */
	protected ArrayList plans;

	//-------- plans --------

	/**
	 *  Get all known plans.
	 *  @return The plans.
	 */
	public IMPlan[] getPlans()
	{
		if(plans==null)
			return new IMPlan[0];
		return (IMPlan[])plans.toArray(new IMPlan[plans.size()]);
	}

	/**
	 *  Get a plan by name.
	 *  @param name	The plan name.
	 *  @return The plan with that name (if any).
	 */
	public IMPlan	getPlan(String name)
	{
		assert name!=null;

		IMPlan ret = null;
		for(int i=0; plans!=null && i<plans.size(); i++)
		{
			IMPlan test = (IMPlan)plans.get(i);
			if(test.getName().equals(name))
				ret = test;
		}
		return ret;
	}

	/**
	 *  Create a new plan.
	 *  @param name	The plan name.
	 *  @param priority	The plan priority.
	 *  @param body The body expression.
	 *  @param type	The body type.
	 *  @return The new plan.
	 */
	public IMPlan	createPlan(String name, int priority, String body, String type)
	{
		if(plans==null)
			plans = SCollection.createArrayList();

		MPlan ret = new MPlan();
		ret.setName(name);
		ret.setPriority(priority);
		ret.createBody(body, type);
		ret.setOwner(this);
		ret.init();
		plans.add(ret);
		return ret;
	}

	/**
	 *  Delete a plan.
	 *  @param plan	The plan.
	 */
	public void	deletePlan(IMPlan plan)
	{
		if(!plans.remove(plan))
			throw new RuntimeException("Plan not found: "+plan);
	}

	//-------- plan references --------

	/**
	 *  Get a plan reference.
	 *  @return The plan reference.
	 */
	public IMPlanReference[] getPlanReferences()
	{
		// todo: implement me
		return new IMPlanReference[0];
	}

	/**
	 *  Get a plan reference.
	 *  @param name The name.
	 *  @return The plan reference.
	 */
	public IMPlanReference getPlanReference(String name)
	{
		// todo: implement me
		return null;
	}

	//-------- methods --------

	/**
	 *  Get the element name.
	 *  @return The element name.
	 */
	public String	getName()
	{
		return "planbase";
	}

	/**
	 *  Get the corresponding base of a given scope.
	 *  Depending on the type of this base (e.g. beliefbase),
	 *  the corresponding base of the given scope is returned.
	 *  @param scope	The scope.
	 *  @return	The corresponding base.
	 */
	public IMBase	getCorrespondingBase(IMCapability scope)
	{
		return scope.getPlanbase();
	}

	/**
	 *  Get the elements contained in the base.
	 *  @return The elements.
	 */
	public IMReferenceableElement[]	getReferenceableElements()
	{
		Object	ret	= new IMReferenceableElement[0];
		ret	= SUtil.joinArrays(ret, getPlans());
		ret	= SUtil.joinArrays(ret, getPlanReferences());
		return (IMReferenceableElement[])ret;
	}

	/**
	 *  Delete a referenceable element per name.
	 *  @param elem The element.
	 */
	public void deleteReferenceableElement(IMReferenceableElement elem)
	{
		assert elem!=null;

		deletePlan((IMPlan)elem);

		// todo: what about plan references
	}

	//-------- jibx related --------

	/**
	 *  Add a plan.
	 *  @param plan The plan.
	 */
	public void addPlan(MPlan plan)
	{
		if(plans==null)
			plans = SCollection.createArrayList();
		plans.add(plan);
	}

	/**
	 *  Get an iterator for all plans.
	 *  @return The iterator.
	 */
	public Iterator iterPlans()
	{
		return plans==null? Collections.EMPTY_LIST.iterator(): plans.iterator();
	}


	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MPlanbase clone = (MPlanbase)cl;
		if(plans!=null)
		{
			clone.plans = SCollection.createArrayList();
			for(int i=0; i<plans.size(); i++)
				clone.plans.add(((MElement)plans.get(i)).clone());
		}
	}
}
