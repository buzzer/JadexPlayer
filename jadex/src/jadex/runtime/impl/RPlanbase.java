package jadex.runtime.impl;

import jadex.model.*;
import jadex.runtime.*;
import jadex.runtime.impl.agenda.conditions.ConditionDefaultPrecondition;
import jadex.runtime.impl.agenda.plans.ExecutePlanStepAction;
import jadex.runtime.impl.agenda.plans.PlanCreationAction;
import jadex.util.SReflect;
import jadex.util.collection.IndexMap;
import jadex.util.collection.MultiCollection;
import jadex.util.collection.SCollection;

import java.util.*;

/**
 *  A planbase instance containing running plans.
 */
public class RPlanbase	extends RBase
{
	//-------- attributes --------

	/** The running plans. */
    protected IndexMap	plans;

	/** The creation filters for passive plans. */
	protected Map	actfilters;

	/** The plan executors (lazy initialisation). */
	protected Map	executors;

	/** The bindings for (m)plans. */
	protected Map	bindings;

    /** The internal creation conditions (stored for later cleanup). */
    protected MultiCollection	conditions;

    //-------- constructors --------

	/**
	 *  Create a new planbase instance.
	 *  @param model	The planbase model.
	 *  @param owner	The owner of the instance.
	 */
	protected RPlanbase(IMPlanbase model, RElement owner)
	{
		super(null, model, owner);
		this.plans	= SCollection.createIndexMap();
		this.actfilters = SCollection.createHashMap();
		this.executors = SCollection.createHashMap();
		this.bindings = SCollection.createHashMap();
		this.conditions	= SCollection.createMultiCollection();
	}

	/**
	 *  Initialize the plan base.
	 */
	protected void	init(int level)
	{
		// On constructor initialization, create creation conditions for the plan model elements.
		if(level==0)
		{
			IMPlanbase model	= (IMPlanbase)getModelElement();
			IMPlan[] mplans = model.getPlans();
			for(int i=0; i<mplans.length; i++)
			{
				registerPlan(mplans[i]);
			}
		}

		// On action init, create and schedule initial plans.
		if(level==1)
		{
			// todo: create RInitial elements!
			IMConfiguration is = getScope().getConfiguration();
			if(is!=null)
			{
				IMConfigPlanbase	initialbase	= is.getPlanbase();
	
				if(initialbase!=null)
				{
					instantiateConfigPlans(initialbase.getInitialPlans());
				}
			}
		}
	}

	/**
	 *  Perform any necessary clean up, when this element is
	 *  no longer used.
	 *  Subclasses may override this method to do their
	 *  cleanup operations.
	 */
	public void	cleanup()
	{
		if(cleanedup)
			return;

		//System.out.println("Cleanup: "+getName());
		super.cleanup();
		for(int i=0; i<plans.size(); i++)
		{
			((RPlan)plans.get(i)).cleanup();
		}

		// Cleanup bindings.
		for(Iterator i=bindings.values().iterator(); i.hasNext(); )
		{
			((BindingHelper)i.next()).cleanup();
		}

		// Cleanup conditions.
		IInterpreterCondition[] conds = (IInterpreterCondition[])conditions.getObjects(IInterpreterCondition.class);
		for(int i=0; i<conds.length; i++)
		{
			conds[i].cleanup();
		}
	}

	//-------- methods --------

	/**
	 *  Get all running plans of this planbase.
	 *  @return The plans.
	 */
	public RPlan[] getPlans()
	{
		return (RPlan[])plans.getObjects(RPlan.class);
	}

	/**
	 *  Get a plan by name.
	 *  @param name	The plan name.
	 *  @return The plan with that name (if any).
	 */
	public RPlan	getPlan(String name)
	{
		return (RPlan)plans.get(name);
	}

	/**
	 *  Get all plans of a specified type (=model element name).
	 *  @param type The plan type.
	 *  @return All plans of the specified type.
	 */
	public RPlan[] getPlans(String type)
	{
		ArrayList ret = SCollection.createArrayList();
		for(int i=0; i<plans.size(); i++)
		{
			RPlan tmp = (RPlan)plans.get(i);
			if(tmp.getType().equals(type))
				ret.add(tmp);
		}
		//System.out.println("found for "+type+" : "+ret);
		return (RPlan[])ret.toArray(new RPlan[ret.size()]);
	}

	/**
	 *  Get the state of this planbase.
	 *  encoded in a set of corresponding change events.
	 *  @param types	The system event types, the caller is interested in.
	 */
	public List	getState(String[] types)
	{
  		ArrayList	ret	= SCollection.createArrayList();

		// Add events for plan instances.
		if(ISystemEventTypes.Subtypes.isSubtype(SystemEvent.PLAN_ADDED, types))
		{
			RPlan[]	pls	= getPlans();
			for(int i=0; i<pls.length; i++)
			{
				ret.add(new SystemEvent(SystemEvent.PLAN_ADDED, pls[i]));
			}
		}

		return ret;
	}

	/**
	 *  Register a new plan.
	 *  a) Evaluates trigger to a filter and registers the filter.
	 *  b) Creates a binding helper if binding variables were found.
	 *  c) Creates and traces the plan's creation condition.
	 */
	public void registerPlan(final IMPlan mplan)
	{
		IMPlanTrigger trigger = mplan.getTrigger();

		// Create filter expressions for passive plans.
		// Todo: store wait abstraction instead of filter.
		IFilter filter = new WaitAbstraction(getScope()).createFilter(trigger);
		if(filter!=null)
		{
			actfilters.put(mplan, filter);
		}

		// Create bindings.
		BindingHelper	binding = null;
		if(mplan.getBindingParameters().length>0)
		{
			binding	= new BindingHelper(mplan, this, trigger!=null && trigger.getCondition()!=null);
			bindings.put(mplan, binding);
		}

		// Create and monitor trigger conditions.
		if(trigger!=null)
		{
			IMCondition cond = trigger.getCondition();

			if(cond!=null)
			{
				// Create condition for checking trigger.
				final RBindingCondition creation = (RBindingCondition)getScope().getExpressionbase()
					.createInternalCondition(cond, this, null, binding);
				conditions.put(mplan, creation);

				// Create action for condition.
				creation.setAction(new PlanCreationAction(this, new ConditionDefaultPrecondition(creation), mplan));

				// Activate tracing: May be triggered, when atomic agent init block ends.
				creation.setTraceMode(ICondition.TRACE_ALWAYS);
			}

			String[] belchanges = trigger.getBeliefChanges();
			for(int i=0; i<belchanges.length; i++)
			{
				IRBelief bel = getScope().getBeliefbase().getBelief(belchanges[i]);
				// todo: check at model time
				if(bel==null)
					throw new RuntimeException("Belief not found: "+belchanges[i]);

				PlanCreationAction pcact = new PlanCreationAction(this, null, mplan); // todo: prec
				BeliefChangeCondition rcond = new BeliefChangeCondition(bel, bel.getFact(), pcact);
				rcond.setTraceMode(ICondition.TRACE_ALWAYS);

				conditions.put(mplan, rcond);
			}

			MultiCollection eventtypes = SCollection.createMultiCollection();
			Map oldvals = SCollection.createHashMap();

			String[] belsetchanges = trigger.getBeliefSetChanges();
			for(int i=0; i<belsetchanges.length; i++)
			{
				IRBeliefSet belset = getScope().getBeliefbase().getBeliefSet(belsetchanges[i]);
				// todo: check at model time
				if(belset==null)
					throw new RuntimeException("Beliefset not found: "+belsetchanges[i]);
				eventtypes.put(belset, ISystemEventTypes.BSFACTS_CHANGED);
				oldvals.put(belset, belset.getFacts());
			}

			String[] factadditions = trigger.getFactAddedTriggers();
			for(int i=0; i<factadditions.length; i++)
			{
				IRBeliefSet belset = getScope().getBeliefbase().getBeliefSet(factadditions[i]);
				// todo: check at model time
				if(belset==null)
					throw new RuntimeException("Beliefset not found: "+factadditions[i]);
				eventtypes.put(belset, ISystemEventTypes.BSFACT_ADDED);
			}

			String[] factremovals = trigger.getFactRemovedTriggers();
			for(int i=0; i<factremovals.length; i++)
			{
				IRBeliefSet belset = getScope().getBeliefbase().getBeliefSet(factremovals[i]);
				// todo: check at model time
				if(belset==null)
					throw new RuntimeException("Beliefset not found: "+factremovals[i]);
				eventtypes.put(belset, ISystemEventTypes.BSFACT_REMOVED);
			}

			IRBeliefSet[] keys = (IRBeliefSet[])eventtypes.getKeys(IRBeliefSet.class);
			for(int i=0; i<keys.length; i++)
			{
				Collection col = (Collection)eventtypes.get(keys[i]);
				PlanCreationAction act = new PlanCreationAction(this, null, mplan); // todo: prec
				BeliefSetChangeCondition rcond = new BeliefSetChangeCondition(keys[i], (Object[])oldvals.get(keys[i]),
					(String[])col.toArray(new String[col.size()]), act);
				rcond.setTraceMode(ICondition.TRACE_ALWAYS);

				conditions.put(mplan, rcond);
			}
		}
	}

	/**
	 *  Deregister a plan.
	 *  a) Removes the trigger filter.
	 *  b) Removes the binding helper.
	 *  c) Stops tracing the creation condition.
	 */
	public void deregisterPlan(IMPlan mplan)
	{
		// Deactivate the plan.
		leaveRunningState(mplan);
		
		// Prevent plan from being selected for events.
		actfilters.remove(mplan);
	}

	//-------- helper methods --------

	/**
	 *  Check if the pre- and contextcondition of a plan candidate holds
	 *  and the plan is not excluded.
	 * @param cand	The plan candidate. 
	 * @param event	The event.
	 * @param excludes The exclude list.
	 */
	public boolean checkPlanApplicability(PlanInfo cand, IREvent event, Set excludes)
	{
		boolean	ret	= false;
		// Test if the candidate is excluded.
		if(excludes==null || !excludes.contains(cand))
		{
			IMPlan plan	= cand.getPlanModel();

			// Hack!!! Avoid event to be part of original binding.
			// Otherwise exclude would not work as the event always differs.
			Map params	= cand.getBinding()==null? SCollection.createNestedMap(getExpressionParameters())
				: SCollection.createNestedMap(new Map[]{cand.getBinding(), getExpressionParameters()});
			params.put("$event", event);
			if(event instanceof IRGoalEvent)
				params.put("$goal", ((IRGoalEvent)event).getGoal());

			// Test if the precondition holds.
			try
			{
				if(plan.getPrecondition()==null
					// Hack!!! Should use RExpressionbase.evaluateInternalExpression()
					|| ((Boolean)plan.getPrecondition().getTerm().getValue(params)).booleanValue())
				{
					// The context is checked after plan instantiation
					// Otherwise the $plan would not be available in the expression.
					ret	= true;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				getScope().getLogger().severe("Precondition could not be evaluated: "+plan.getPrecondition());
			}
		}

		return ret;
	}

	/**
	 *  Generate binding candidates for an MPlan.
	 *  @param config The configuration.
	 *  @param plan	The plan.
	 *  @param event	The event(if any).
	 *  @param excludes	The exclude list (if any).
	 *  @return A list of all binding plan infos.
	 */
	protected List	generateBindingCandidates(IMConfigPlan config, IMPlan plan, IREvent event, Set excludes)
	{
		List	candidates	= SCollection.createArrayList();
		BindingHelper	bh	= (BindingHelper)this.bindings.get(plan);
		if(bh!=null)
		{
			String[] names = null;
			Object[] vals = null;
			if(config!=null)
			{
				IMConfigParameter[] inips =  config.getParameters();
				names = new String[inips.length];
				vals = new Object[inips.length];
				for(int i=0; i<inips.length; i++)
				{
					// todo: how can params be accessed from expressions?
					//Object value = inips[i].getInitialValue().getValue(getUserExpressionParameters());
					Object value = getScope().getExpressionbase().evaluateInternalExpression(inips[i].getInitialValue(), this);
					names[i] = inips[i].getOriginalElement().getName();
					vals[i] = new Object[]{value};
				}
			}

			// Calculate bindings for plan.
			List	bindings	= bh.calculateBindings(event, names, vals);
			for(Iterator j=bindings.iterator(); j.hasNext(); )
			{
				// Create candidate containing current binding.
				Map	binding	= (Map)j.next();
				PlanInfo cand = new PlanInfo(config, event, plan, binding);

				// Test if the plan conditions hold.
				if(checkPlanApplicability(cand, event, excludes))
				{
					candidates.add(cand);
				}
			}
		}
		else
		{
			PlanInfo	cand	= new PlanInfo(config, event, plan, null);

			// Test if the plan conditions hold.
			if(checkPlanApplicability(cand, event, excludes))
			{
				candidates.add(cand);
			}
		}
		return candidates;
	}

	//-------- internal methods --------

	/**
	 *  Get the runtime element for a model element.
	 *  Depending on the type it might have to be created (e.g. a goal)
	 *  or might be already there (e.g. belief).
	 *  @param melement	The model of the element to be retrieved.
	 *  @param creator	The creator of the element (e.g. a reference).
	 */
	protected RReferenceableElement	getElementInstance(
			IMReferenceableElement melement, RReferenceableElement creator)
	{
		throw new RuntimeException("Base does not support referenceable elements!: "+getName());
	}

	/**
	 *  Get the corresponding base of a given scope.
	 *  Depending on the type of this base (e.g. beliefbase),
	 *  the corresponding base of the given scope is returned.
	 *  @param scope	The scope.
	 *  @return	The corresponding base.
	 */
	protected RBase	getCorrespondingBase(RCapability scope)
	{
		return scope.getPlanbase();
	}

	/**
	 *  Add a plan to the planbase.
	 *  @param plan	The plan to add.
	 */
	protected void	addPlan(RPlan plan)
	{
        this.plans.add(plan.getName(), plan);
	}

	/**
	 *  Remove a plan from the planbase.
	 *  @param plan	The plan to add.
	 */
	public void	removePlan(RPlan plan)
	{
		plan	= (RPlan)this.plans.removeKey(plan.getName());
		assert plan!=null;
		plan.throwSystemEvent(SystemEvent.PLAN_REMOVED);
		// Hack!!! Have to throw event first, otherwise it isn't propagated.		
		plan.cleanup();
	}

	/**
	 *  Get all the plans including subcapabilities.
	 *  @return All plans.
	 * /
	protected RPlan[] getAllPlans()
	{
		RPlan[] ret = getPlans();
		RCapability[] subcaps = getScope().getChildren();
		for(int i=0; i<subcaps.length; i++)
		{
			ret = (RPlan[])SUtil.joinArrays(ret, subcaps[i].getPlanbase().getAllPlans());
		}
		return ret;
	}*/

	/**
	 *  Create a string representation of this element.
	 *  @return	A string representing this element.
	 */
	public String	toString()
	{
		StringBuffer	sb	= new StringBuffer();
		sb.append(SReflect.getInnerClassName(this.getClass()));
		sb.append("(name=");
		sb.append(name);
		sb.append("\nplans=\n");
		for(int i=0; i<plans.size(); i++)
		{
			sb.append("  ");
			sb.append(plans.get(i));
			sb.append("\n");
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 *  Create a plan instance.
	 *  @param name The name.
	 *  @param plan	The plan model.
	 *  @param config The configuration.
	 *  @param binding	The binding parameters.
	 *  @param processgoal	The rootgoal of the plan.
	 *  @return	The instantiated plan.
	 */
	public RPlan createPlan(String name, IMPlan plan, IMConfigPlan config, Map binding, RProcessGoal processgoal, IREvent event)
	{
		return	new RPlan(name, plan, this, config, null, binding, processgoal, event);
		//this.addPlan(ret);
		//return ret;
	}

	/**
	 *  Get the activation filter of a passive plan.
	 *  @param mplan The plan model.
	 *  @return The activation filter.
	 */
	public IFilter getPassivePlanActivationFilter(IMPlan mplan)
	{
		return (IFilter)actfilters.get(mplan);
	}

	/**
	 *  Get the bindings of a passive plan.
	 *  @param mplan The plan model.
	 *  @return The bindings.
	 */
	public BindingHelper getPassivePlanBindings(IMPlan mplan)
	{
		return (BindingHelper)bindings.get(mplan);
	}

	/**
	 *  Schedule a candidate for execution.
	 *  @param cand The candidate to execute.
	 *  @return	The execute plan instance.
	 */
	public static RPlan	scheduleCandidate(CandidateInfo cand)
	{
		assert cand!=null;
		assert cand instanceof PlanInfo || cand instanceof PlanInstanceInfo
			|| cand instanceof WaitqueueInfo;

		// Resolve candidate to plan instance rplan.
		// May be: PlanInfo, PlanInstanceInfo, WaitqueueInfo.
		RPlan rplan;
		IREvent	event = cand.getEventInstance();
	
		if(cand instanceof PlanInfo)
		{
			rplan = cand.getPlanInstance();
			rplan.adopt();
			rplan.getScope().getGoalbase().addProcessGoal(rplan.getRootGoal());
		}
		else if(cand instanceof PlanInstanceInfo)
		{
			if(event instanceof IRGoalEvent && !((IRGoalEvent)event).isInfo())
				event.getScope().getLogger().severe("Dispatching goal to running plan not allowed: "+cand+", "+event);
			rplan = cand.getPlanInstance();
			// Set the plans initial event.
			rplan.assignNewEvent(event);
		}
		else //if(cand instanceof WaitqueueInfo)
		{
			if(event instanceof IRGoalEvent && !((IRGoalEvent)event).isInfo())
				event.getScope().getLogger().severe("Dispatching goal to waitqueue not allowed: "
					+cand+", "+event);
			WaitqueueInfo winfo = (WaitqueueInfo)cand;
			rplan = winfo.getPlanInstance();
			rplan.getWaitqueue().addEvent(winfo.getEventInstance());
			//System.out.println("REvent added to wait queue: "+agent.getName()+" "+rplan);
		}

		// Do not execute plan step when dispatching to waitqueue.
		if(!(cand instanceof WaitqueueInfo))
		{
			// Add a execute plan step action.
			rplan.schedule(null);	//this); // todo: cause
			//System.out.println("Dispatched: "+rplan);
		}

		//System.out.println("Executing: "+cand+" for goal: "+mypropgoal
		//	+ " and processgoal: "+processgoal);
		return	rplan;
	}

	/**
	 *  Exit the running state.
	 *  Terminates all running plans.
	 */
	public void exitRunningState()
	{
		// Deactivate all plans.
		IMPlan[]	plans	= ((IMPlanbase)getModelElement()).getPlans();
		for(int i=0; i<plans.length; i++)
		{
			leaveRunningState(plans[i]);
		}
	}

	/**
	 *  Activate the end state of the planbase.
	 *  Creates the plans specified in the end state.
	 */
	public void activateEndState()
	{
		// Instantiate plans from end state.
		IMConfiguration config = getScope().getConfiguration();
		if(config!=null)
		{
			IMConfigPlanbase	planbase	= config.getPlanbase();

			if(planbase!=null)
			{
				instantiateConfigPlans(planbase.getEndPlans());
			}
		}
	}

	/**
	 *  Instantiate plans from the configuration
	 */
	protected void instantiateConfigPlans(IMConfigPlan[] configplans)
	{
		for(int i=0; i<configplans.length; i++)
		{
			IMPlan mplan = (IMPlan)configplans[i].getOriginalElement();
			List candidates	= generateBindingCandidates(configplans[i], mplan, null, null);
			RCapability cap = getScope();
			for(int j=0; j<candidates.size(); j++)
			{
				// todo: code is very similar to code in PlanCreationAction -> unify
				IMInternalEvent mevent = ((IMCapability)cap.getModelElement())
					.getEventbase().getInternalEvent(IMEventbase.TYPE_EXECUTEPLAN);
				RInternalEvent event = getScope().getEventbase().createInternalEvent(mevent);
				
				/*event.getParameter("candidate").setValue(candidates.get(j));
				((PlanInfo)candidates.get(j)).setEvent(event);	// Hack!!!
				cap.dispatchEvent(event, null);*/ // todo: set cause?
				
				((PlanInfo)candidates.get(j)).setEvent(event);
				scheduleCandidate((PlanInfo)candidates.get(j));
			}
		}
	}
	
	/**
	 *  Deactivate everything related to a plan type.
	 */
	public void	leaveRunningState(IMPlan plan)
	{
		// Terminate running plans if they are not protected.
		RPlan[]	plans	= getPlans(plan.getName());
		for(int i=0; i<plans.length; i++)
		{
			if(!isProtected(plans[i]))
				plans[i].terminate();
		}
		
		// Disable plan conditions of model.
		BindingHelper bh = (BindingHelper)bindings.remove(plan);
		if(bh!=null)
			bh.cleanup();
		List pcs = (List)conditions.remove(plan);
		for(int i=0; pcs!=null && i<pcs.size(); i++)
			((IInterpreterCondition)pcs.get(i)).cleanup();
	}

	/**
	 *  Check if the end state of this base is terminated.
	 *  Checks if no more plans are running.
	 *  @return true, when the agent can be safely deleted.
	 */
	public boolean isEndStateTerminated()
	{
//		if(plans.size()>0)
//			System.out.println("plans: "+plans);
		return plans.size()==0;
	}
}
