package jadex.runtime.impl;

import jadex.model.*;
import jadex.runtime.ComposedFilter;
import jadex.runtime.GoalEventFilter;
import jadex.runtime.ICondition;
import jadex.runtime.IFilter;
import jadex.runtime.InternalEventFilter;
import jadex.runtime.MessageEventFilter;
import jadex.runtime.TimeoutException;
import jadex.runtime.impl.agenda.ComposedPrecondition;
import jadex.runtime.impl.agenda.IAgendaActionPrecondition;
import jadex.runtime.impl.agenda.eventprocessing.ScheduleCandidatesAction;
import jadex.runtime.impl.agenda.plans.DefaultPlanActionPrecondition;
import jadex.util.Tuple;
import jadex.util.collection.SCollection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  The wait abstraction contains all stuff related to waiting for something.
 *  Used only internally.
 */
public class WaitAbstraction	implements Serializable, ICleanable
{
	//-------- constants --------
	
	/** The condition constant. */
	private static String CONDITION = "condition";
	
	/** The internal event constant. */
	private static String INTERNALEVENT = "internalevent";

	/** The message event constant. */
	private static String MESSAGEEVENT = "messageevent";

	/** The goal constant for goal finished occurrences. */
	private static String GOAL = "goal";

	/** The goal constant for goal process occurrences. */
	private static String GOALPROCESSING = "goal";

	/** The belief constant. */
	private static String BELIEF = "belief";
	
	/** The beliefset constant. */
	private static String BELIEFSET = "beliefset";

	/** The filter constant. */
	private static String FILTER = "filter";
	
	//-------- attributes --------

	/** The capability. */
	protected RCapability capability;

	/** The timeout. */
	protected long timeout;

	/** The corresponding timetable entry. */
	protected TimetableData td;

	/** The constructed filter. */
	protected IFilter filter;

	/** The filter map (creation reason -> filter). */
	protected Map filters;

	/** Created_conditions to clean up (creation reason -> condition). */
	protected Map cleanup_conditions;

	/** Turned on conditions to turn off (creation reason -> condition). */
	protected Map turnoff_conditions;

	/** Flag to indicate that the wait abstraction has been cleaned up. */
	protected boolean	cleanedup;
	
	//-------- constructors --------

	/**
	 *  Create a new wait abstraction.
	 */
	public WaitAbstraction(RCapability capability)
	{
		this.capability = capability;
		this.cleanup_conditions = SCollection.createHashMap();
		this.turnoff_conditions = SCollection.createHashMap();
		this.filters = SCollection.createHashMap();
		this.timeout = -1;
	}

	/**
	 *  Perform any necessary clean up, when this element is
	 *  no longer used.
	 *  Subclasses may override this method to do their
	 *  cleanup operations.
	 */
	public void	cleanup()
	{
		if(!cleanedup)
		{
			cleanedup	= true;

			capability.getAgent().removeTimetableEntry(td);

			Object[] keys = turnoff_conditions.keySet().toArray();
			for(int i=0; i<keys.length; i++)
			{
				Object cond = turnoff_conditions.get(keys[i]);
				if(cond instanceof IRCondition)
					((IRCondition)cond).setTraceMode(ICondition.TRACE_NEVER);
				else if(cond instanceof InterpreterCondition)
					((InterpreterCondition)cond).setTraceMode(ICondition.TRACE_NEVER);
			}
			
			keys = cleanup_conditions.keySet().toArray();
			for(int i=0; i<keys.length; i++)
			{
				((ICleanable)cleanup_conditions.get(keys[i])).cleanup();
			}
	
			// help gc.
			cleanup_conditions = null;
			turnoff_conditions = null;
		}
	}
	
	/**
	 *  Called when garbage collector decides to discard the element.
	 */
	protected void finalize() throws Throwable
	{
		super.finalize();
		if(!cleanedup)
		{
//			if(RElement.finalized_classes.add(this.getClass()))
//				System.out.println("finalize(): "+this.getClass());

			capability.getAgent().scheduleGarbageCollection(this);
//			cleanup();	// Hack!!! Uses wrong thread.
		}
	}

	//-------- accessor methods --------

	/**
	 *  Get the timeout.
	 *  @return The timeout.
	 */
	public long getTimeout()
	{
		return timeout;
	}

	/**
	 *  Test if it is a timeout wait. This is the case when
	 *  it is waited for something in combination with a timeout.
	 *  @return True, if timeout wait (in contrast to a duration wait = waitFor(time)).
	 */
	public boolean isTimeoutWait()
	{
		return timeout!=-1 && getFilter()!=null;
	}

    /**
     *  Get the filter.
     *  @return The filter.
     */
    public IFilter getFilter()
    {
		if(filter==null && filters.size()>0)
		{
			if(filters.size()==1)
			{
				filter = (IFilter)filters.values().toArray()[0];
			}
			else
			{
				filter = new ComposedFilter((IFilter[])filters.values()
					.toArray(new IFilter[filters.size()]), ComposedFilter.OR);
			}
		}
        return filter;
    }

	/**
     *  Get a dynamic filter (reflects changes made to the wait abstraction).
     *  @return The filter.
     */
    public IFilter getDynamicFilter()
    {
		return new WaitAbstractionFilter();
    }

	//-------- setter/adder methods --------

	/**
	 *  Add a message event.
	 *  @param type The type.
	 *  @param match	The match expression.
	 */
	public void addMessageEvent(String type, IMExpression match)
	{
		MessageEventFilter	mef	= new MessageEventFilter(type);
		if(match!=null)
			mef.setMatchExpression(match);
		addFilter(new Tuple(MESSAGEEVENT, type), mef);
	}

	/**
	 *  Add a message event to wait for a reply.
	 *  @param mevent The message.
	 */
	public void addMessageEvent(IRMessageEvent mevent)
	{
		//addFilter(new Tuple(new String[]{"messageevent", type, replyname}),
		//	new MessageEventFilter(type, replyname));
		//addFilter(new Tuple(new Object[]{REPLY, mevent}), mevent.getFilter());
		addFilter(new Tuple(new Object[]{MESSAGEEVENT, mevent}), mevent.getFilter());
	}

	/**
	 *  Add an internal event.
	 *  @param type The type.
	 *  @param match	The match expression (if any).
	 */
	public void addInternalEvent(String type, IMExpression match)
	{
		InternalEventFilter	ief	= new InternalEventFilter(type);
		if(match!=null)
			ief.setMatchExpression(match);
		addFilter(new Tuple(INTERNALEVENT, type), ief);
	}

	/**
	 *  Add a goal type.
	 *  Only waits for process events (e.g. when the goal is adopted)!
	 *  @param type The type.
	 *  @param match The match expression.
	 */
	public void addGoalProcess(String type, IMExpression match)
	{
		GoalEventFilter	gef	= new GoalEventFilter(type, false);
		if(match!=null)
			gef.setMatchExpression(match);
		addFilter(new Tuple(GOALPROCESSING, type), gef);
	}

	/**
	 *  Add a goal type.
	 *  Only waits for info events (i.e. when the goal is finished)!
	 *  @param type The type.
	 *  @param match The match expression.
	 */
	public void addGoal(String type, IMExpression match)
	{
		GoalEventFilter	gef	= new GoalEventFilter(type, true);
		if(match!=null)
			gef.setMatchExpression(match);
		addFilter(new Tuple(GOAL, type), gef);
	}

	/**
	 *  Add a goal.
	 *  @param goal The goal.
	 */
	public void addGoal(IRGoal goal)
	{
		addFilter(new Tuple(GOAL, goal), goal.getFilter());
	}

	/**
	 *  Add a belief.
	 *  @param type The type.
	 */
	public void addBelief(String type, String trace_mode)
	{
		IRBelief bel = capability.getBeliefbase().getBelief(type);
		if(bel==null)
			throw new RuntimeException("Belief not found: "+type);

		BeliefChangeCondition cond = new BeliefChangeCondition(bel, bel.getFact(), null);
		cond.setTraceMode(trace_mode);
		Tuple reason = new Tuple(BELIEF, type);
		turnoff_conditions.put(reason, cond);
		cleanup_conditions.put(reason, cond);
		addFilter(reason, cond.getFilter());
	}
	
	/**
	 *  Add a belief.
	 *  @param type The type.
	 */
	public void addBelief(IRBelief bel, String trace_mode)
	{
		BeliefChangeCondition cond = new BeliefChangeCondition(bel, bel.getFact(), null);
		cond.setTraceMode(trace_mode);
		Tuple reason = new Tuple(BELIEF, bel);
		turnoff_conditions.put(reason, cond);
		cleanup_conditions.put(reason, cond);
		addFilter(reason, cond.getFilter());
	}

	/**
	 *  Add a belief set.
	 *  @param type The type.
	 *  @param eventtypes The eventtypes (from ISystemEventTypes).
	 */
	public void addBeliefSet(String type, String[] eventtypes, String trace_mode)
	{
		IRBeliefSet belset = capability.getBeliefbase().getBeliefSet(type);
		if(belset==null)
			throw new RuntimeException("Belief set not found: "+type);

		Object[] oldvals = null;
		for(int i=0; i<eventtypes.length; i++)
		{
			if(eventtypes[i].equals(ISystemEventTypes.BSFACTS_CHANGED))
			{
				oldvals = belset.getFacts(); // Hack?
				break;
			}
		}
		
		BeliefSetChangeCondition cond = new BeliefSetChangeCondition(belset, oldvals, eventtypes, null);
		cond.setTraceMode(trace_mode);
		Tuple reason = new Tuple(BELIEFSET, type);
		turnoff_conditions.put(reason, cond);
		cleanup_conditions.put(reason, cond);
		addFilter(reason, cond.getFilter());
	}
	
	/**
	 *  Add a belief set.
	 *  @param The belief set The belief set.
	 *  @param eventtypes The eventtypes (from ISystemEventTypes).
	 */
	public void addBeliefSet(IRBeliefSet belset, String[] eventtypes, String trace_mode)
	{
		Object[] oldvals = null;
		for(int i=0; i<eventtypes.length; i++)
		{
			if(eventtypes[i].equals(ISystemEventTypes.BSFACTS_CHANGED))
			{
				oldvals = belset.getFacts(); // Hack?
				break;
			}
		}
		BeliefSetChangeCondition cond = new BeliefSetChangeCondition(belset, oldvals, eventtypes, null);
		cond.setTraceMode(trace_mode);
		Tuple reason = new Tuple(BELIEFSET, belset);
		turnoff_conditions.put(reason, cond);
		cleanup_conditions.put(reason, cond);
		addFilter(new Tuple(BELIEFSET, belset), cond.getFilter());
	}
	
	/**
	 *  Add a user filter.
	 *  @param filter The user filter.
	 */
	public void addFilter(IFilter filter)
	{
		addFilter(new Tuple(FILTER, filter), filter);
	}

	/**
	 *  Add a condition.
	 *  @param condition The condition.
	 */
	public void addCondition(String condition, String trace_mode)
	{
		IRCondition cond =  capability.getExpressionbase().createCondition(condition);
		if(trace_mode.equals(ICondition.TRACE_ONCE))
		{
			cond.traceOnce();
		}
		else if(trace_mode.equals(ICondition.TRACE_ALWAYS))
		{
			cond.traceAlways();
		}
		else // if(trace_mode.equals(ICondition.TRACE_NEVER))
		{
			throw new RuntimeException("Cannot use trace mode: "+trace_mode);
		}
		Tuple reason = new Tuple(CONDITION, condition);
		turnoff_conditions.put(reason, cond);
		cleanup_conditions.put(reason, cond);
		// Condition is bewared of gc, because it is contained as
		// value within the filter.
		addFilter(reason, cond.getFilter());
	}

	/**
	 *  Add a condition.
	 *  @param condition The condition.
	 */
	// todo: remove, only support add by (ADF )condition name.
	public void addCondition(IRCondition condition)
	{
		Tuple reason = new Tuple(CONDITION, condition);
		if(condition.getTraceMode().equals(ICondition.TRACE_NEVER))
		{
			condition.traceOnce();
		}
		// Condition is bewared of gc, because it is contained as
		// value within the filter.
		addFilter(reason, condition.getFilter());
	}

	/**
	 *  Set the timeout.
	 *  Creates and dispatches a new ScheduleCandidatesAction
	 *  for the future timeout timepoint.
	 *  @param timeout The timeout.
	 *  @param plan The plan. todo: hack?
	 */
	public void setTimeout(long timeout, final RPlan plan)
	{
		// Must only be called at most once.
		assert this.timeout==-1;
		if(timeout==-1) return;

		this.timeout = timeout;

		// Create internal event to dispatch when timeout occurs.
		IMInternalEvent	mevent	= ((IMCapability)capability.getModelElement())
			.getEventbase().getInternalEvent(IMEventbase.TYPE_TIMEOUT);
		final RInternalEvent event = capability.getEventbase().createInternalEvent(mevent);
		if(isTimeoutWait())
			event.getParameter("exception").setValue(new TimeoutException());
		// todo: add the goal to the event to be able to abort it in mobile?!

		List cands = SCollection.createArrayList();
		cands.add(new PlanInstanceInfo(event, plan));
		this.td	= new TimetableData(getTimeout(), new ScheduleCandidatesAction(
			new ComposedPrecondition(
				new IAgendaActionPrecondition()
				{
					public boolean check()
					{
						return !plan.isScheduled();
					}
				},
				new DefaultPlanActionPrecondition(plan)
			), capability.getAgent(), event, cands));

		capability.getAgent().addTimetableEntry(td);
	}

	//-------- remover methods --------

	/**
	 *  Remove a message event.
	 *  @param type The type.
	 */
	public void removeMessageEvent(String type)
	{
		removeFilter(new Tuple(MESSAGEEVENT, type));
	}

	/**
	 *  Remove a message event.
	 *  @param mevent The message event.
	 */
	public void removeMessageEvent(IRMessageEvent mevent)
	{
		removeFilter(new Tuple(new Object[]{MESSAGEEVENT, mevent}));
	}

	/**
	 *  Remove an internal event.
	 *  @param type The type.
	 */
	public void removeInternalEvent(String type)
	{
		removeFilter(new Tuple(INTERNALEVENT, type));
	}

	/**
	 *  Disable waiting for process events of the given goal type.
	 *  @param type The type.
	 */
	public void removeGoalProcess(String type)
	{
		removeFilter(new Tuple(GOALPROCESSING, type));
	}

	/**
	 *  Disable waiting for finished events of the given goal type.
	 *  @param type The type.
	 */
	public void removeGoal(String type)
	{
		removeFilter(new Tuple(GOAL, type));
	}
	
	/**
	 *  Remove a goal.
	 *  @param goal The goal.
	 */
	public void removeGoal(IRGoal goal)
	{
		removeFilter(new Tuple(GOAL, goal));
	}

	/**
	 *  Remove a belief.
	 *  @param type The type.
	 */
	public void removeBelief(String type)
	{
		removeReason(new Tuple(BELIEF, type));
	}
	
	/**
	 *  Remove a belief.
	 *  @param bel The belief.
	 */
	public void removeBelief(IRBelief bel)
	{
		removeReason(new Tuple(BELIEF, bel));
	}

	/**
	 *  Remove a belief set.
	 *  @param type The type.
	 */
	public void removeBeliefSet(String type)
	{
		removeReason(new Tuple(BELIEFSET, type));
	}
	
	/**
	 *  Remove a belief set.
	 *  @param bel The belief set.
	 */
	public void removeBeliefSet(IRBeliefSet belset)
	{
		removeReason(new Tuple(BELIEFSET, belset));
	}

	/**
	 *  Remove a user filter.
	 *  @param filter The user filter.
	 */
	public void removeFilter(IFilter filter)
	{
		removeFilter(new Tuple(FILTER, filter));
	}

	/**
	 *  Remove a condition.
	 *  @param condition The condition.
	 */
	public void removeCondition(String condition)
	{
		removeReason(new Tuple(CONDITION, condition));
	}

	/**
	 *  Remove a condition.
	 *  @param condition The condition.
	 */
	public void removeCondition(IRCondition condition)
	{
		removeReason(new Tuple(CONDITION, condition));
	}

	//-------- helpers --------

	/**
	 *  Remove for a reason the filter and conditions.
	 *  @param reason The reason.
	 */
	protected void removeReason(Tuple reason)
	{
		removeFilter(reason);
		Object cond = turnoff_conditions.get(reason);
		if(cond instanceof IRCondition)
			((IRCondition)cond).setTraceMode(ICondition.TRACE_NEVER);
		else if(cond instanceof InterpreterCondition)
			((InterpreterCondition)cond).setTraceMode(ICondition.TRACE_NEVER);
		ICleanable cleanable = (ICleanable)cleanup_conditions.remove(reason);
		if(cleanable!=null)
			cleanable.cleanup();
	}
	
	/**
	 *  Add a filter with a reason.
	 *  @param reason The reason.
	 *  @param filter The filter.
	 */
	protected void addFilter(Tuple reason, IFilter filter)
	{
		if(filters.containsKey(reason))
			throw new RuntimeException("Error, wait reason already contained: "+reason);
		this.filters.put(reason, filter);
		this.filter = null;
	}

	/**
	 *  Remove a filter with a reason.
	 *  @param reason The reason.
	 */
	protected void removeFilter(Tuple reason)
	{
		if(!filters.containsKey(reason))
			throw new RuntimeException("Error, wait reason not found: "+reason);
		this.filters.remove(reason);
		this.filter = null;
	}

	/**
	 *  Get the capability.
	 *  @return The capability.
	 */
	public RCapability getCapability()
	{
		return capability;
	}

	//-------- helper methods --------

	/**
	 *  Create an activation filter for a plan.
	 *  @return The filter.
	 */
	protected IFilter createFilter(IMTrigger trigger)
	{
		IFilter ret = null;
		ArrayList filters = SCollection.createArrayList();

		if(trigger!=null)
		{
			IMEventbase eventbase = ((IMCapability)capability.getModelElement()).getEventbase();
			IMReference[] refs = trigger.getInternalEvents();
			for(int i=0; i<refs.length; i++)
			{
				String name = refs[i].getReference();
				IMInternalEvent event = eventbase.getInternalEvent(name);
				if(event==null)
				{
					IMInternalEventReference eventref = eventbase.getInternalEventReference(name);
					if(eventref==null)
						throw new RuntimeException("Referenced internal event not found: "+name);
				}
				InternalEventFilter bf	= new InternalEventFilter(name);
				bf.setMatchExpression(refs[i].getMatchExpression());
				filters.add(bf);
			}

			refs = trigger.getMessageEvents();
			for(int i=0; i<refs.length; i++)
			{
				String name = refs[i].getReference();
				IMMessageEvent event = eventbase.getMessageEvent(name);
				if(event==null)
				{
					IMMessageEventReference eventref = eventbase.getMessageEventReference(name);
					if(eventref==null)
						throw new RuntimeException("Referenced message event not found: "+name);
					//event = (IMMessageEvent)eventref.getAssignFromElement();
				}
				MessageEventFilter mf	= new MessageEventFilter(name);
				mf.setMatchExpression(refs[i].getMatchExpression());
				filters.add(mf);
			}

			IMGoalbase goalbase = ((IMCapability)capability.getModelElement()).getGoalbase();
			refs = trigger.getGoalFinisheds();
			for(int i=0; i<refs.length; i++)
			{
				String name = refs[i].getReference();
                // Throws exception when cannot be resolved
                goalbase.getReferenceableElement(name);
				GoalEventFilter gf = new GoalEventFilter(name, true);
				gf.setMatchExpression(refs[i].getMatchExpression());
				filters.add(gf);
			}

			if(trigger instanceof IMPlanTrigger || trigger instanceof IMMetaGoalTrigger)
			{
				if(trigger instanceof IMPlanTrigger)
					refs = ((IMPlanTrigger)trigger).getGoals();
				else
					refs = ((IMMetaGoalTrigger)trigger).getGoals();
					
				for(int i=0; i<refs.length; i++)
				{
					String name = refs[i].getReference();
	                // Throws exception when cannot be resolved
	                goalbase.getReferenceableElement(name);
					GoalEventFilter gf = new GoalEventFilter(name);
					gf.setMatchExpression(refs[i].getMatchExpression());
					filters.add(gf);
				}
			}

			if(trigger.getFilter()!=null)
			{
				// Hack!!! todo: Use (e.g. plan-) base instead of capability as scope
				// But bases have no extra expression parameters!?
				IFilter filter = (IFilter)capability.getExpressionbase()
					.evaluateInternalExpression(trigger.getFilter(), capability);
				filters.add(filter);
			}
		}

		if(filters.size()==1)
		{
			ret = (IFilter)filters.get(0);
		}
		else if(filters.size()>1)
		{
			ret = new ComposedFilter((IFilter[])filters.toArray(new IFilter[filters.size()]), ComposedFilter.OR);
		}

		return ret;

//		// Todo: generic method for all triggers.
//		IFilter ret = null;
//
//		if(trigger!=null)
//		{
//			WaitAbstraction	wabs	= new WaitAbstraction(getScope());
//
//			if(trigger.getFilter()!=null)
//				wabs.addFilter((IFilter)getScope().getExpressionbase().evaluateInternalExpression(trigger.getFilter(), this));
//
//			IMReference[]	goalfs	= trigger.getGoalFinisheds();
//			for(int i=0; i<goalfs.length; i++)
//				wabs.addGoal(goalfs[i].getReference(), goalfs[i].getMatchExpression());
//
//			IMReference[]	ievs	= trigger.getInternalEvents();
//			for(int i=0; i<ievs.length; i++)
//				wabs.addInternalEvent(ievs[i].getReference(), ievs[i].getMatchExpression());
//
//			IMReference[]	mevs	= trigger.getMessageEvents();
//			for(int i=0; i<mevs.length; i++)
//				wabs.addMessageEvent(mevs[i].getReference(), mevs[i].getMatchExpression());
//			
//			IMReference[]	goals	= null;
//			if(trigger instanceof IMPlanTrigger)
//			{
//				goals	= ((IMPlanTrigger)trigger).getGoals();
//				
//				// Todo: additional plan triggers (currently done in registerPlan)
//				// ptrigger.getCondition();
//				// ptrigger.getBeliefChanges();
//				// ptrigger.getBeliefSetChanges();
//				// ptrigger.getFactAddedTriggers();
//				// ptrigger.getFactRemovedTriggers();
//			}
//			else if(trigger instanceof IMMetaGoalTrigger)
//			{
//				goals	= ((IMMetaGoalTrigger)trigger).getGoals();
//			}
//			for(int i=0; goals!=null && i<goals.length; i++)
//				wabs.addGoal(goals[i].getReference(), goals[i].getMatchExpression());				
//
//			ret	= wabs.getDynamicFilter();
//		}
//
//		return ret;
	}

	/**
	 *  This filter reflects changes in the underlying wait abstraction immediately.
	 */
	public class WaitAbstractionFilter implements IFilter, Serializable
	{
//		//-------- attributes --------
//		
//		/** The reason. */
//		protected Tuple reason;
		
		//-------- methods --------

		/**
		 * Match an object against the filter.
		 * Exceptions are interpreted as non-match.
		 * @param object The object.
		 * @return True, if the filter matches.
		 */
		public boolean filter(Object object) throws Exception
		{
//			boolean ret = false;
//			Tuple[] keys = (Tuple[])filters.keySet().toArray(new Tuple[filters.size()]);
//			this.reason = null;
//			for(int i=0; i<filters.size() && !ret; i++)
//			{
//				IFilter filter = (IFilter)filters.get(keys[i]);
//				ret = filter.filter(object);
//				if(ret)
//					this.reason = keys[i];
//			}
//			
//			return ret;
			return WaitAbstraction.this.getFilter().filter(object);
		}

//		/**
//		 *  Get the reason.
//		 *  @return The reason.
//		 */
//		public Tuple getReason()
//		{
//			return reason;
//		}
	}
}