package jadex.runtime.impl;

import jadex.model.IMEventbase;
import jadex.model.IMGoal;
import jadex.model.IMPlan;
import jadex.model.IMPlanbase;
import jadex.runtime.ICandidateInfo;
import jadex.runtime.IFilter;
import jadex.util.collection.SCollection;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

/**
 *  The applicable candidates list. Implements functionality to
 *  generate and keep track of candidates (plans) for some event.
 */
public class ApplicableCandidateList	implements Serializable
{
	//-------- constants --------

	/** Never exclude plan candidates from apl. */
	//public static final String EXCLUDE_NEVER = "never";

	/** Exclude failed plan candidates from apl. */
	//public static final String EXCLUDE_WHEN_FAILED = "when_failed";

	/** Exclude succeeded plan candidates from apl. */
	//public static final String EXCLUDE_WHEN_SUCCEEDED = "when_succeeded";

	/** Exclude tried plan candidates from apl. */
	//public static final String EXCLUDE_WHEN_TRIED = "when_tried";

	//-------- attributes --------

	/** The event. */
	protected IREvent event;

	/** The list of candidates. */
	protected List candidates;

	/** The set of already tried plans. */
	protected Set excludeset;

	/** The result list (candidates -  excludeset). */
	protected List result;

	//-------- constructors --------

	/**
	 *  Create a new applicable candidate list.
	 */
	public ApplicableCandidateList(IREvent event)
	{
		this.event = event;
	}

	//-------- method --------

	/**
	 *  Get the candidates. Note, that if recalculation is
	 *  on, the applicable candidates list will be calculated
	 *  on each access of this method.
	 *  @return A list of the candidates.
	 */
	public List getCandidates()
	{
		if(candidates == null || isRecalculating())
		{
			calculateCandidates();
		}
		return result;
	}

	/**
	 *  Set the event.
	 *  @param event The event.
	 */
	public void setEvent(IREvent event)
	{
		this.event = event;
	}

	/**
	 *  Clear the apl and the excludeset.
	 * /
	public void clear()
	{
		this.candidates = null;
		this.excludeset = null;
	}*/

	/**
	 *  Clear the excludeset.
	 */
	public void clearExcludeSet()
	{
		this.excludeset = null;
	}

	/**
	 *  Test, if the APL should be recalculated on every access.
	 *  @return True, if recalculating.
	 */
	public boolean isRecalculating()
	{
		// Assumes that only goals are intesresting enough for apl recalculation.
		boolean ret = false;
		if(event instanceof IRGoalEvent)
		{
			ret = ((IRGoalEvent)event).getGoal().isRecalculating();
		}
		return ret;
		//return true;
	}

	/**
	 *  Get the exclude mode.
	 *  @return The exclude mode.
	 *  // todo: make exclude mode more general?!
	 */
	public String getExcludeMode()
	{
		String ret = IMGoal.EXCLUDE_WHEN_TRIED;
		if(event instanceof IRGoalEvent)
		{
			ret = ((IRGoalEvent)event).getGoal().getExcludeMode();
		}
		return ret;
	}
	
	/**
	 *  Get executed candidates.
	 */
	// Hack??? Used for goal regression test.
	public Set	getExecutedCandidates()
	{
		return Collections.unmodifiableSet(getExcludeSet());
	}

	/**
	 *  Add an exclude element.
	 *  @param exclude The exclude element.
	 */
	protected void addExcludeCandidate(ICandidateInfo exclude)
	{
		assert exclude!=null;

		if(!getExcludeMode().equals(IMGoal.EXCLUDE_NEVER) && !getExcludeSet().contains(exclude))
		{
			getExcludeSet().add(exclude);
		}

		recalculateResult();
		//result.remove(exclude);
	}

	/**
	 *  Get the exclude set.
	 */
	protected Set getExcludeSet()
	{
		if(excludeset==null)
		{
			if(!getExcludeMode().equals(IMGoal.EXCLUDE_NEVER))
			{
				excludeset = SCollection.createHashSet();
			}
			else
			{
				excludeset = Collections.EMPTY_SET;
			}
		}
		return excludeset;
	}

	/**
	 *  Recalculate the result.
	 */
	protected void recalculateResult()
	{
		if(candidates==null || candidates.size()==0)
		{
			result = Collections.EMPTY_LIST;
		}
		else
		{
			result = (List)((ArrayList)candidates).clone();
			result.removeAll(excludeset);
		}
	}

	/**
	 *  Add a candidate to the list.
	 * /
	public void addCandidate(ICandidateInfo cand)
	{
		candidates.add(cand);
	}*/

	/**
	 *  Add candidates to the list.
	 * /
	public void addCandidates(List cands)
	{
		candidates.addAll(cands);
	}*/

	/**
	 *  Calculate the current candidates for the event.
	 */
	protected void calculateCandidates()
	{
		candidates	= SCollection.createArrayList();
		List	occurrences	= event.getAllOccurrences();

		for(int j=0; j<occurrences.size(); j++)
		{
			RReferenceableElement	elm	= (RReferenceableElement)occurrences.get(j);
			generateCandidateList((IREvent)elm);
		}
		recalculateResult();
	}

	/**
	 *  Generate candidate list.
	 *  @param event The event occurrence.
	 */
	protected void generateCandidateList(IREvent event)
	{
		RPlanbase planbase = event.getScope().getPlanbase();
		
		// Now execute planstep actions are directly created.
		// When execute event the declared plan will be instantiated.
		// No meta-level reasoning has to be done.
		/*if(event instanceof RInternalEvent
			&& event.getType().equals(IMEventbase.TYPE_EXECUTEPLAN))
		{
			PlanInfo cand = (PlanInfo)event.getParameter("candidate").getValue();
			if(planbase.checkPlanApplicability(cand, event, getExcludeSet()))
			{
				/* addCandidate(cand); * /
				candidates.add(cand);
			}
		}
		else
		{*/
			// Search for running plans that called pause(filter).
			// or plans that have waitqueue filters.
			//System.out.println("available: "+SUtil.arrayToString(instances));
			//if(instances.length==1)
			//	System.out.println(getScope().getAgent().applyFilter(instances[0].getWaitFilter(), event));
			RPlan[] plans = planbase.getPlans();
			RBDIAgent agent = event.getScope().getAgent();
			for(int i=0; i<plans.length; i++)
			{
				PlanInstanceInfo	cand	= new PlanInstanceInfo(event, plans[i]);
				// Test if the plan instance is allowed.
				if(!getExcludeSet().contains(cand))
				{
					// Test if the context condition holds.
					if(plans[i].getContextCondition()==null
						|| ((Boolean)plans[i].getContextCondition().getValue()).booleanValue())
					{
						// Test if the event type matches the wait filter.
						IFilter filter = plans[i].getWaitAbstraction()!=null? plans[i].getWaitAbstraction().getFilter(): null;
						if(agent.applyFilter(filter, event))
						{
							//addCandidate(cand);
							candidates.add(cand);
							//System.out.println("Filter matched: "+instances[i].getWaitFilter()+" to "+instances[i]);
						}
						else
						{
							if(agent.applyFilter(plans[i].getWaitqueue().getFilter(), event))
							{
								candidates.add(new WaitqueueInfo(event, plans[i], plans[i].getWaitqueue().getFilter()));
								//addCandidate(new WaitqueueInfo(event, plans[i], plans[i].getWaitqueue().getFilter()));
							}
						}
					}
				}
			}

			// Search for plan templates.
			IMPlan[] mplans	= ((IMPlanbase)planbase.getModelElement()).getPlans();
			//System.out.println("Plans in "+getName()+" --- "+SUtil.arrayToString(plans));

			for(int i=0; i<mplans.length; i++)
			{
				// Evaluate filter expression.
				//RExpression	fexp	= (RExpression)actfilters.get(plans[i]);
				//IFilter filter = fexp!=null ? (IFilter)fexp.getValue() : null;
				IFilter filter = planbase.getPassivePlanActivationFilter(mplans[i]);

				// Test if the event type is relevant.
				if(event.getScope().getAgent().applyFilter(filter, event))
				{
					// Add candidates for all available bindings.
					// If no binding is specified, exactly one candidate is added.
					//addCandidates(generateBindingCandidates(null, mplans[i], event));
					candidates.addAll(planbase.generateBindingCandidates(null, mplans[i], event, getExcludeSet()));
				}
			}
		//}
	}
}
