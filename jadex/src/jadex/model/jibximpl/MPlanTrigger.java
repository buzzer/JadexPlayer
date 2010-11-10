package jadex.model.jibximpl;

import java.util.*;
import jadex.model.*;
import jadex.util.collection.SCollection;

/**
 *  The trigger for plans.
 */
public class MPlanTrigger extends MTrigger implements IMPlanTrigger
{
	//-------- attributes --------

	/** The goals. */
	protected ArrayList goals;

	/** The condition. */
	protected MBindingCondition condition;

	/** The belief changes. */
	protected ArrayList beliefchanges;

	/** The belief changes. */
	protected ArrayList beliefsetchanges;

	/** The belief set fact additions. */
	protected ArrayList factadditions;

	/** The belief set fact additions. */
	protected ArrayList factremovals;

	//-------- constructors --------

	/**
	 *  The setup is called by a configuration code object.
	 */
	protected void init()
	{
		super.init();

		// Set the default trigger type of the trigger condition
		// when nothing else is specified.
		if(getCondition()!=null && getCondition().getTrigger()==null)
			getCondition().setTrigger(IMCondition.TRIGGER_IS_TRUE);
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
		if(goals!=null)
			ret.addAll(goals);
		if(condition!=null)
			ret.add(condition);
		return ret;
	}

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);
		
		// Check goal triggers.
		IMReference[] refs = getGoals();
		for(int i=0; i<refs.length; i++)
		{
			String goalname = refs[i].getReference();
			IMReferenceableElement	target = getScope().getGoalbase().getReferenceableElement(goalname);
			if(target==null)
			{
				report.addEntry(refs[i], "Referenced goal not found '"+goalname+"'.");
			}
			/*else
			{
				IMReferenceParameter[]	params	= refs[i].getParameters();
				for(int p=0; p<params.length; p++)
				{
					Object	para	= target instanceof IMParameterElement
						? (Object)((IMParameterElement)target).getParameter(params[i].getReference())
						: (Object)((IMParameterElementReference)target).getParameterReference(params[i].getReference());
					if(para==null)
					{
						report.addEntry(this, "Referenced parameter '"+params[i].getReference()+"' not found in referenced element "+target);
					}
				}
			}*/
		}

		// Check beliefbase related triggers.
		String[]	triggers	= getBeliefChanges();
		for(int i=0; i<triggers.length; i++)
		{
			IMReferenceableElement	refelem	= getScope().getBeliefbase().getReferenceableElement(triggers[i]);
			if(!(refelem instanceof IMBelief || refelem instanceof IMBeliefReference))
			{
				report.addEntry(this, "Cannot find triggering belief '"+triggers[i]+"'.");
			}
		}
		triggers	= getBeliefSetChanges();
		for(int i=0; i<triggers.length; i++)
		{
			IMReferenceableElement	refelem	= getScope().getBeliefbase().getReferenceableElement(triggers[i]);
			if(!(refelem instanceof IMBeliefSet || refelem instanceof IMBeliefSetReference))
			{
				report.addEntry(this, "Cannot find triggering beliefset '"+triggers[i]+"'.");
			}
		}
		triggers	= getFactAddedTriggers();
		for(int i=0; i<triggers.length; i++)
		{
			IMReferenceableElement	refelem	= getScope().getBeliefbase().getReferenceableElement(triggers[i]);
			if(!(refelem instanceof IMBeliefSet || refelem instanceof IMBeliefSetReference))
			{
				report.addEntry(this, "Cannot find triggering beliefset '"+triggers[i]+"'.");
			}
		}
		triggers	= getFactRemovedTriggers();
		for(int i=0; i<triggers.length; i++)
		{
			IMReferenceableElement	refelem	= getScope().getBeliefbase().getReferenceableElement(triggers[i]);
			if(!(refelem instanceof IMBeliefSet || refelem instanceof IMBeliefSetReference))
			{
				report.addEntry(this, "Cannot find triggering beliefset '"+triggers[i]+"'.");
			}
		}
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

	//-------- condition --------

	/**
	 *  Get the creation condition of the plan.
	 *  @return The creation condition (if any).
	 */
	public IMCondition	getCondition()
	{
		return this.condition;
	}

	/**
	 *  Create a creation condition for the plan.
	 *  @param expression	The expression string.
	 *  @return The new creation condition.
	 */
	public IMCondition	createCondition(String expression)
	{
		this.condition = new MBindingCondition();
		condition.setExpressionText(expression);
		condition.setTrigger(IMCondition.TRIGGER_IS_TRUE);
		condition.setOwner(this);
		condition.init();
		return condition;
	}

	/**
	 *  Delete the creation condition of the plan.
	 */
	public void	deleteCondition()
	{
		condition = null;
	}

	//-------- belief changes --------

	/**
	 *  Get the internal events.
	 *  @return The internal events.
	 */
	public String[] getBeliefChanges()
	{
		if(beliefchanges==null)
			return new String[0];
		return (String[])beliefchanges.toArray(new String[beliefchanges.size()]);
	}

	/**
	 *  Create a belief changes.
	 *  @param ref	The belief change.
	 */
	public void createBeliefChange(String ref)
	{
		if(beliefchanges==null)
			beliefchanges = SCollection.createArrayList();
		beliefchanges.add(ref);
	}

	/**
	 *  Delete a belief changes.
	 *  @param ref	The belief change.
	 */
	public void deleteBeliefChange(String ref)
	{
		beliefchanges.remove(ref);
	}

	//-------- beliefset changes --------

	/**
	 *  Get the beliefset changes.
	 *  @return The beliefset changes.
	 */
	public String[] getBeliefSetChanges()
	{
		if(beliefsetchanges==null)
			return new String[0];
		return (String[])beliefsetchanges.toArray(new String[beliefsetchanges.size()]);
	}

	/**
	 *  Create a beliefset changes.
	 *  @param ref	The beliefset change.
	 *  //@return The new beliefset change.
	 */
	public void createBeliefSetChange(String ref)
	{
		if(beliefsetchanges==null)
			beliefsetchanges = SCollection.createArrayList();
		beliefsetchanges.add(ref);
	}

	/**
	 *  Delete a beliefset changes.
	 *  @param ref	The beliefset change.
	 */
	public void deleteBeliefSetChange(String ref)
	{
		beliefsetchanges.remove(ref);
	}

	//-------- beliefset fact added changes --------

	/**
	 *  Get the belief set fact added triggers.
	 *  @return The belief set fact added.
	 */
	public String[] getFactAddedTriggers()
	{
		if(factadditions==null)
			return new String[0];
		return (String[])factadditions.toArray(new String[factadditions.size()]);
	}

	/**
	 *  Create a fact added trigger.
	 *  @param ref	The belief set.
	 */
	public void createFactAddedTrigger(String ref)
	{
		if(factadditions==null)
			factadditions = SCollection.createArrayList();
		factadditions.add(ref);
	}

	/**
	 *  Delete a fact added trigger.
	 *  @param ref	The belief set.
	 */
	public void deleteFactAddedTrigger(String ref)
	{
		factadditions.remove(ref);
	}

	//-------- beliefset fact removed changes --------

	/**
	 *  Get the belief set changes.
	 *  @return The belief set changes.
	 */
	public String[] getFactRemovedTriggers()
	{
		if(factremovals==null)
			return new String[0];
		return (String[])factremovals.toArray(new String[factremovals.size()]);
	}

	/**
	 *  Create a fact removed trigger.
	 *  @param ref	The belief set.
	 */
	public void createFactRemovedTrigger(String ref)
	{
		if(factremovals==null)
			factremovals = SCollection.createArrayList();
		factremovals.add(ref);
	}

	/**
	 *  Delete a fact removed trigger.
	 *  @param ref	The belief set.
	 */
	public void deleteFactRemovedTrigger(String ref)
	{
		factremovals.remove(ref);
	}

	//-------- jibx related --------

	/**
	 *  Add a goal.
	 *  @param goal The goal.
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
	 *  Get an iterator for all belief changes.
	 *  @return The iterator.
	 */
	public Iterator iterBeliefChanges()
	{
		return beliefchanges==null? Collections.EMPTY_LIST.iterator(): beliefchanges.iterator();
	}

	/**
	 *  Get an iterator for all beliefset changes.
	 *  @return The iterator.
	 */
	public Iterator iterBeliefSetChanges()
	{
		return beliefsetchanges==null? Collections.EMPTY_LIST.iterator(): beliefsetchanges.iterator();
	}

	/**
	 *  Get an iterator for all fact added triggers.
	 *  @return The iterator.
	 */
	public Iterator iterFactAddedTriggers()
	{
		return factadditions==null? Collections.EMPTY_LIST.iterator(): factadditions.iterator();
	}

	/**
	 *  Get an iterator for all fact removed triggers.
	 *  @return The iterator.
	 */
	public Iterator iterFactRemovedTriggers()
	{
		return factremovals==null? Collections.EMPTY_LIST.iterator(): factremovals.iterator();
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MPlanTrigger clone = (MPlanTrigger)cl;
		if(goals!=null)
		{
			clone.goals = SCollection.createArrayList();
			for(int i=0; i<goals.size(); i++)
				clone.goals.add(((MElement)goals.get(i)).clone());
		}
		if(condition!=null)
			clone.condition = (MBindingCondition)condition.clone();
		if(beliefchanges!=null)
			clone.beliefchanges = (ArrayList)beliefchanges.clone();
		if(factadditions!=null)
			clone.factadditions = (ArrayList)factadditions.clone();
		if(factremovals!=null)
			clone.factremovals = (ArrayList)factremovals.clone();
	}
}
