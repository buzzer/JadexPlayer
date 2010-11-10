package jadex.runtime.impl.agenda.eventprocessing;

import jadex.model.IMEvent;
import jadex.model.IMGoal;
import jadex.model.IMMetaGoal;
import jadex.runtime.ICandidateInfo;
import jadex.runtime.impl.*;
import jadex.runtime.impl.agenda.AbstractElementAgendaAction;
import jadex.runtime.impl.agenda.IAgendaActionPrecondition;
import jadex.runtime.impl.agenda.plans.ExecutePlanStepAction;
import jadex.util.SReflect;
import jadex.util.SUtil;
import jadex.util.collection.SCollection;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Level;

/**
 *  The action for processing an event.
 *  For execution speed unifies the three event processing actions
 *  (FindApplicableCandidates, SelectCandidates, ScheduleCandidates)
 */
public class ProcessEventAction extends AbstractElementAgendaAction implements java.io.Serializable
{
	//-------- attributes --------

	/** The agent. */
	protected RBDIAgent agent;

	/** The event. */
	protected IREvent event;

	/** The default meta-level reasoner. */
	protected DefaultMetaLevelReasoner reasoner;

	//-------- constructors --------

	/**
	 *  Create a new process event action.
	 */
	public ProcessEventAction(IAgendaActionPrecondition precond, RBDIAgent agent, IREvent event)
	{
		super(event, precond);
		this.agent = agent;
		this.event = event;

		this.reasoner = DefaultMetaLevelReasoner.getInstance();
	}

	//-------- methods --------

	/**
	 *  The action.
	 */
	public void execute()
	{
		// When info event of meta-level goal, continue
		// original event processing with ScheduleCandidateAction.
		if(event instanceof RGoalEvent && ((RGoalEvent)event).isInfo()
			&& ((RGoalEvent)event).getGoal() instanceof RMetaGoal)
		{
			IRGoal goal = ((RGoalEvent)event).getGoal();
			this.event = ((CandidateInfo)goal.getParameterSet("applicables").getValues()[0]).getEventInstance();
			List candidates;
			if(goal.isSucceeded())
			{
				ICandidateInfo[] cs = (ICandidateInfo[])goal.getParameterSet("result").getValues();
				if(cs.length==0)
					agent.getLogger().severe("Meta-level reasoning did not return a non-empty candidate list: "+SUtil.arrayToString(cs));
				candidates = SUtil.arrayToList(cs);
			}
			else
			{
				String	msg	= "";
				Exception	ex	= goal.getException();
				if(ex!=null)
				{
					StringWriter	sw	= new StringWriter();
					ex.printStackTrace(new PrintWriter(sw));
					msg	= "\n"+sw.toString();
				}
				
				agent.getLogger().log(Level.SEVERE, " meta goal failed: " +agent.getName() + " " + goal + " " + event + msg);

				candidates	= SCollection.createArrayList();
				Object[]	cands	= goal.getParameterSet("applicables").getValues();
				for(int i=0; i< cands.length; i++)
					candidates.add(cands[i]);
				candidates = reasoner.reason(event, candidates);
			}

			// todo: what precondition should be used?
			agent.getInterpreter().addAgendaEntry(new ScheduleCandidatesAction(getPrecondition(), agent, event, candidates), this);
		}

		// Hack!!! should not access mode???
		else if(agent.getInterpreter().getExecutionMode().equals(JadexInterpreter.EXECUTION_MODE_STEP))
		{
			agent.getInterpreter().addAgendaEntry(new FindApplicableCandidatesAction(getPrecondition(), agent, event), this);
		}
		else
		{
			//List applicables = findApplicableCandidates(event);
			List applicables = event.getApplicableCandidatesList().getCandidates();
			if(applicables.size()!=0)
			{
				boolean ml = false;
				if(applicables.size()>1)
				{
					ml = initiateMetaLevelReasoning(event, applicables);
				}
				if(!ml)
				{
					List candidates = selectCandidates(event, applicables);
					scheduleCandidates(event, candidates);
				}
			}
			else
			{
				eventNotHandled(event);
				/*if(event instanceof IRMessageEvent)
				{
					applicables = event.getApplicableCandidatesList().getCandidates();
					System.out.println("app: "+applicables);
				}*/
			}
		}
	}

	/**
	 *  Get the event.
	 *  @return The event.
	 */
	public IREvent getEvent()
	{
		return event;
	}
  
	/**
	 *  @return the "cause" of this action
	 *  @see jadex.runtime.impl.agenda.IAgendaAction#getCause()
	 * /
	public Object getCause()
	{
		return event==null? null: event.getName();
	}*/

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public  String toString()
	{
		return SReflect.getInnerClassName(this.getClass())+"( event="+(event==null? "null":event.getName())+")";
	}

	//-------- helper methods --------

	/**
	 *  Find applicable candidates. Candidates are plans,
	 *  plan instances and waitqueues.
	 *  @return The applicable candidates.
	 * /
	protected List findApplicableCandidates(IREvent event)
	{
		List candidates	= SCollection.createArrayList();
		List	occurrences	= event.getAllOccurrences();

		for(int j=0; j<occurrences.size(); j++)
		{
			RReferenceableElement	elm	= (RReferenceableElement)occurrences.get(j);
			List	cands	= elm.getScope().getPlanbase().generateCandidateList((IREvent)elm);
			candidates.addAll(cands);
		}

		return candidates;
	}*/

	/**
	 *  Initiate meta-level reasoning.
	 */
	protected boolean initiateMetaLevelReasoning(IREvent event, List applicables)
	{
		assert event!=null;
		assert applicables!=null;
		assert applicables.size()>1;

		// Get the meta-level goal type (if any).
		IMGoal metagoal	= null;
		List	occs	= event.getAllOccurrences();
		for(int i=0; i<occs.size(); i++)
		{
			IREvent	occ	= (IREvent)occs.get(i);
			IMMetaGoal	metagoal2	= occ.getScope().getGoalbase().getMetaGoals(occ);
			if(metagoal!=null && metagoal2!=null)
			{
				throw new RuntimeException("Multiple meta goals matching event: "+event+", "+metagoal+", "+metagoal2);
			}
			else if(metagoal2!=null)
			{
				metagoal	= metagoal2;
			}
		}
		if(metagoal!=null)
		{
			RReferenceableElement	orig	= event.getOriginalElement(); 
			RCapability scope = orig.getScope();
			IRGoal mlgoal = (IRGoal)scope.getEventbase().createElementStructure(orig,
				new GoalCreator(scope.getGoalbase(), metagoal));
			//mlgoal.getParameter("event").setValue(event);
			for(int i=0; i<applicables.size(); i++)
				mlgoal.getParameterSet("applicables").addValue(applicables.get(i));
			scope.getGoalbase().dispatchTopLevelGoal(mlgoal);
		}
		return metagoal!=null;
	}

	/**
	 *  Select candidate from candidate list.
	 *  Here the meta-level reasoning is done.
	 *  (Represents the select option function. SO)
	 *  @param event The event.
	 *  @param list The candidate list.
	 *  @return The selected candidates.
	 */
	protected List selectCandidates(IREvent event, List list)
	{
		return reasoner.reason(event, list);
	}

	/**
	 *  Schedule all candidates for execution.
	 *  @param event The event.
	 *  @param candidates The candidate to execute.
	 */
	protected void	scheduleCandidates(IREvent event, List candidates)
	{
		List procgoals = SCollection.createArrayList();
		for(int i = 0; i < candidates.size(); i++)
		{
			// Execute the selected candidate using correct event occurrence.
			CandidateInfo	cand	= (CandidateInfo)candidates.get(i);
			procgoals.add(agent.getPlanbase().scheduleCandidate(cand).getRootGoal());
		}

		// When event is of kind process event
		// inform the goal of the newly created process goals
		if(event instanceof RGoalEvent && !((IRGoalEvent)event).isInfo())
			((RGoal)((IRGoalEvent)event).getGoal().getOriginalElement()).processGoalsCreated(procgoals);
	}

	/**
	 *  Called when no plans found for an event.
	 *  If no candidates found cleanup event and issue warning.
	 *  @param event	The event.
	 */
	protected void eventNotHandled(IREvent event)
	{
		// Check for some special cases where no warning has to be produced.
		boolean warn	= true;
		if(event instanceof IRGoalEvent)
		{
			IRGoalEvent	ge	= (IRGoalEvent)event; 
			if(ge.isInfo() && ge.getGoal().isSucceeded() && !ge.getGoal().isSubgoal())
			{
				// No warning for top-level info events of succeeded goals.
				//System.out.println("no warning: "+ge.getGoal());
				warn	= false;
			}
			if(ge.isInfo() && agent.isCleanedup())
			{
				// No warning for info events during agent cleanup.
				//System.out.println("no warning: "+ge.getGoal());
				warn	= false;
			}
			else if(!ge.isInfo() && ge.getGoal().wasProcessed())
			{
				// No warning for retried process events.
				//System.out.println("no warning: "+ge.getGoal());
				warn	= false;
			}
		}
		
		// When event is of kind process event inform the goal.
		if(event instanceof RGoalEvent && !((IRGoalEvent)event).isInfo())
		{
			((RGoal)((IRGoalEvent)event).getGoal().getOriginalElement()).processGoalsCreated(SCollection.createArrayList());
		}

		// Generate warning for unhandled event (except for the special cases).
		if(warn)
		{
			agent.getLogger().warning("Event not handled: " +agent.getName() + " " + event);
		}

		event.cleanup();
	}

	/**
	 *  Schedule a candidate for execution.
	 *  @param cand The candidate to execute.
	 *  @return	The execute plan instance.
	 * /
	protected RPlan	scheduleCandidate(CandidateInfo cand)
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
			/*RCapability	scope	= event.getScope();
			PlanInfo mcand = (PlanInfo)cand;
			IMPlan plan = mcand.getPlanModel();
			Map	binding = mcand.getBinding();
			// Hack!!! Avoid event to be part of original binding.
			binding	= binding!=null ? new HashMap(binding) : SCollection.createHashMap();
			binding.put("$event", event);
			// Create (maybe dummy) root goal for new plan instance.
			RProcessGoal processgoal = scope.getGoalbase().createProcessGoal(propgoal, cand);

			// Create the plan instance rplan
			rplan = scope.getPlanbase().createPlan(plan, mcand.getInitialState(), binding, processgoal, event);
			processgoal.setPlanInstance(rplan);* /
			rplan = cand.getPlanInstance();
			rplan.adopt();
			rplan.getScope().getGoalbase().addProcessGoal(rplan.getRootGoal());
		}
		else if(cand instanceof PlanInstanceInfo)
		{
			if(event instanceof IRGoalEvent && !((IRGoalEvent)event).isInfo())
				agent.getLogger().severe("Dispatching goal to running plan not allowed: "+cand+", "+event);
			rplan = cand.getPlanInstance();
			// Set the plans initial event.
			rplan.assignNewEvent(event);
		}
		else //if(cand instanceof WaitqueueInfo)
		{
			if(event instanceof IRGoalEvent && !((IRGoalEvent)event).isInfo())
				agent.getLogger().severe("Dispatching goal to waitqueue not allowed: "
					+cand+", "+event);
			WaitqueueInfo winfo = (WaitqueueInfo)cand;
			rplan = winfo.getPlanInstance();
			rplan.getWaitqueue().addEvent(winfo.getEventInstance());
			//System.out.println("REvent added to wait queue: "+agent.getName()+" "+rplan);
		}

		// Prepare the event for the plan. For process goal events,
		// don't set original goal, but process goal.
		// todo: clone event for process goals.
		/*if(event instanceof RGoalEvent)
		{
			((RGoalEvent)event).setGoal(processgoal);
		}* /

		// Do not execute plan step when dispatching to waitqueue.
		if(!(cand instanceof WaitqueueInfo))
		{
			// Add a execute plan step action.
			agent.getAgenda().add(new ExecutePlanStepAction(rplan), this);
			//System.out.println("Dispatched: "+rplan);
		}

		//System.out.println("Executing: "+cand+" for goal: "+mypropgoal
		//	+ " and processgoal: "+processgoal);
		return	rplan;
	}*/
}

