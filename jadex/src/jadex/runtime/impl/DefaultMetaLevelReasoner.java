package jadex.runtime.impl;

import jadex.runtime.impl.*;
import jadex.model.*;
import jadex.util.collection.SCollection;

import java.util.*;
import java.io.Serializable;


/**
 *  The default meta level reasoner is responsible to select between
 *  different plan instance alternatives for one event.
 */
public class DefaultMetaLevelReasoner implements Serializable
{
	/** The singleton instance. */
	protected static DefaultMetaLevelReasoner instance;

	//-------- constructors --------

	/**
	 *  Get the singleton.
	 */
	public static DefaultMetaLevelReasoner getInstance()
	{
		if(instance==null)
			instance = new DefaultMetaLevelReasoner();
		return instance;
	}

	//-------- IMetaLevelResoner interface --------

	/**
	 *  The reasoning method.
     *  @param event The event.
	 *  @param candidatelist The list of candidates.
	 *  @return The selected candidates.
	 */
	public List reason(IREvent event, List candidatelist)
	{
		// Use the plan priorities to sort the candidates.
		// If the priority is the same use the following order:
		// running plan - waitque of running plan - passive plan
		ArrayList selected = SCollection.createArrayList();

		// todo: include a number of retries...
		int numcandidates = 1;
		if(event.isPostToAll())
			numcandidates = Integer.MAX_VALUE;

		for(int i=0; i<numcandidates && candidatelist.size()>0; i++)
		{
			selected.add(getNextCandidate(candidatelist,
				event.isRandomSelection()));
		}

		return selected;
	}

	//-------- helper methods --------

	/**
	 *  Get the next candidate with respect to the plan
	 *  priority and the rank of the candidate.
	 *  @param candidatelist The candidate list.
	 *  @param random The random selection flag.
	 *  @return The next candidate.
	 */
	protected Object getNextCandidate(List candidatelist, boolean random)
	{
		List finals = SCollection.createArrayList();
		finals.add(candidatelist.get(0));
		int candprio = getPriority(finals.get(0));
		for(int i=1; i<candidatelist.size(); i++)
		{
			Object tmp = candidatelist.get(i);
			int tmpprio = getPriority(tmp);
			if(tmpprio>candprio
				|| (tmpprio == candprio && getRank(tmp)>getRank(finals.get(0))))
			{
				finals.clear();
				finals.add(tmp);
				candprio = tmpprio;
			}
			else if(tmpprio==candprio && getRank(tmp)==getRank(finals.get(0)))
			{
				finals.add(tmp);
			}
		}

		Object cand;
		if(random)
		{
			int rand = (int)(Math.random()*(double)finals.size());
			cand = finals.get(rand);
			//System.out.println("Random sel: "+finals.size()+" "+rand+" "+cand);
		}
		else
		{
			//System.out.println("First sel: "+finals.size()+" "+0);
			cand = finals.get(0);
		}

		candidatelist.remove(cand);
		return cand;
	}

	/**
	 *  Get the priority of a candidate.
	 *  @return The priority of a candidate.
	 */
	protected int getPriority(Object cand)
	{
		int prio;
		if(cand instanceof PlanInstanceInfo)
			prio = ((IMPlan)((PlanInstanceInfo)cand).getPlanInstance().getModelElement()).getPriority();
		else if(cand instanceof PlanInfo)
			prio = ((PlanInfo)cand).getPlanModel().getPriority();
		else
			prio = ((IMPlan)((WaitqueueInfo)cand).getPlanInstance()
				.getModelElement()).getPriority();
		return prio;
	}

	/**
	 *  Get the rank of a candidate.
	 *  The order is as follows:
	 *  running plan -> Waitqueue -> plan.
	 *  @return The rank of a candidate.
	 */
	protected int getRank(Object cand)
	{
		int rank;
		if(cand instanceof PlanInfo) // mplan
			rank = 0;
		else if(cand instanceof WaitqueueInfo) // waitqueue
			rank = 1;
		else //if(cand instanceof PlanInstanceInfo) // running plan
			rank = 2;

		return rank;
	}
}

