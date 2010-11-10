package jadex.runtime.impl;

import jadex.model.IMConfigPlan;
import jadex.model.IMPlan;
import jadex.runtime.BDIFailureException;
import jadex.runtime.ICondition;
import jadex.runtime.IFilter;
import jadex.runtime.IPlanExecutor;
import jadex.runtime.SystemEvent;
import jadex.runtime.impl.agenda.IAgendaActionPrecondition;
import jadex.runtime.impl.agenda.plans.ExecutePlanStepAction;
import jadex.runtime.impl.agenda.plans.PlanTerminationAction;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;

/**
 *  The runtime element for a plan.
 */
public class RPlan	extends RParameterElement
{
	//-------- constants --------

	/** The planexecutor property identifier. */
	public static final String PROPERTY_PLAN_EXECUTOR = "planexecutor";

	/** The exception level. */
	public static final String PROPERTY_LOGGING_LEVEL_EXCEPTIONS = "logging.level.exceptions";

	/** The initial state before any execution. */
	public static final String	STATE_INITIAL	= "initial";
	
	/** The state, indicating the execution of the plan body. */
	public static final String	STATE_BODY	= "body";
	
	/** The state, indicating the execution of the passed code. */
	public static final String	STATE_PASSED	= "passed";
	
	/** The state, indicating the execution of the failed code. */
	public static final String	STATE_FAILED	= "failed";
	
	/** The state, indicating the execution of the aborted. */
	public static final String	STATE_ABORTED	= "aborted";
	
	//-------- attributes --------

	/** The plan body (programmers instantiated plan object). */
	protected Object	body;

	/** The plan executor. */
	protected IPlanExecutor	exe;

	/** The initial event (if any). */
	protected IREvent	initialevent;

	/** The latest event, dispatched for this plan instance (if any). */
	protected IREvent	event;

	/** The goal for which the plan was instantiated. */
	protected RProcessGoal rootgoal;

	/** The context condition (if any). */
	protected RCondition	contextcondition;

	/** The current filter for the plan instance (if any). */
	//protected IFilter	filter;
	protected WaitAbstraction waitabs;
	// Hack!!! needed for mobile plan to remember that it was a subgoal wait.
	protected WaitAbstraction lastwaitabs;

	/** The plan waitqueue. */
	protected Waitqueue waitqueue;

	/** The alive status of the plan. */
	protected boolean alive;

	/** The exception during execution of the plan body (if any). */
	protected Exception	exception;

	/** The execution state (body/passed/failed/aborted). */
	protected String	state;
	
	/** Flag indicating that a plan has be scheduled. */
	boolean	scheduled;
	
	//-------- constructor --------

	/**
	 *  Create a new plan.
	 *  @param plan The model element.
	 *  @param owner The owner.
	 *  @param exparams The expression parameters.
	 *  @param goal The goal.
	 */
	protected RPlan(String name, IMPlan plan, RElement owner, IMConfigPlan state,
		RReferenceableElement creator, Map exparams, RProcessGoal goal, IREvent initialevent)
	{
		// todo: refactor name!!!
		//super(plan.getName()+"#"+instanceCount(plan)+"p", plan, state, owner, creator, exparams);
		super(name, plan, state, owner, creator, exparams);

		//if (name!=null && name.toLowerCase().indexOf("plan")<0) this.name+="p"; // todo: what is that?
		
		this.rootgoal	= goal;
		this.initialevent = initialevent;
		this.state	= STATE_INITIAL;
		this.setExpressionParameter("$plan", this);
		this.setExpressionParameter("$event", initialevent);
		if(initialevent instanceof IRGoalEvent)
			this.setExpressionParameter("$goal", ((IRGoalEvent)initialevent).getGoal());
		assignNewEvent(initialevent);
		
		// Connects mapped parameter(set)s with the initial event resp. rootgoal.
		IRParameterElement target;
		if(initialevent instanceof IRMessageEvent || initialevent instanceof IRInternalEvent)
			target = initialevent;
		else
			target = goal;
		if(target!=null)
		{
			IRParameter[] params = getParameters();
			for(int i=0; i<params.length; i++)
			{
				if(params[i] instanceof RPlanParameter) // todo: remove check
				{
					if(((RPlanParameter)params[i]).isConnectable()) // todo: remove cast
						((RPlanParameter)params[i]).connect(target);
				}
			}
			IRParameterSet[] paramsets = getParameterSets();
			for(int i=0; i<paramsets.length; i++)
			{
				if(paramsets[i] instanceof RPlanParameterSet) // todo: remove check
				{
					if(((RPlanParameterSet)paramsets[i]).isConnectable()) // todo: remove cast
						((RPlanParameterSet)paramsets[i]).connect(target);
				}
			}
		}

		// Plan access is always protected in processing mode
		setParameterProtectionMode(ACCESS_PROTECTION_PROCESSING);
	}

	/**
	 *  Adopt a plan. Means that it is considered for execution.
	 *  The plan is added to the planbase. Context condition is traced.
	 */
	public void adopt()
	{
		getScope().getPlanbase().addPlan(this);

		//this.event	= null;
		IMPlan plan = (IMPlan)getModelElement();

		if(plan.getContextCondition()!=null)
		{
			this.contextcondition = getScope().getExpressionbase()
				.createInternalCondition(plan.getContextCondition(), this, new PlanTerminationAction(this,
					new IAgendaActionPrecondition()
					{
						public boolean check()
						{
							return state.equals(STATE_INITIAL) || state.equals(STATE_BODY);
						}
					}), null);

			//Boolean	context	= this.contextcondition.evaluate();
			//assert context!=null && context.booleanValue() : "Plan context is "+context;
			this.contextcondition.setTraceMode(ICondition.TRACE_ONCE);
		}
		this.alive	= true;

		String	type	= plan.getBody().getType();
		this.exe	= (IPlanExecutor)getScope().getPropertybase().getProperty
			(PROPERTY_PLAN_EXECUTOR+"."+type);
		if(exe==null)
			throw new RuntimeException("No plan executor found for plan: "+plan+", type="+type);

		if(plan.getWaitqueue()!=null)
		{
			// Todo: initialize waitqueue with trigger from model!?
			getWaitqueue().addFilter(getWaitqueue().createFilter(plan.getWaitqueue()));
		}

		throwSystemEvent(SystemEvent.PLAN_ADDED);
	}

	/**
	 *  Perform any necessary clean up, when this element is
	 *  no longer used.
	 *  Subclasses may override this method to do their
	 *  cleanup operations. They must call super.cleanup
	 *  to ensure that the cleanedup property is set.
	 */
	public void	cleanup()
	{
		if(cleanedup)
			return;

		if(exe!=null)
			exe.cleanup(this);
		
		// Cleanup event.
		assignNewEvent(null);
		
		// When plan is cleaned up, agenda actions will be removed due to invalid precondition.
		this.scheduled	= false;

		// Cleanup context condition.
		if(contextcondition!=null)
			contextcondition.cleanup();
		
		// Cleanup liveness state and inform root goal.
		if(alive)	// Might not be alive when candidate but never scheduled. 
		{
			alive	= false;
			getRootGoal().planOrSubgoalFinished();
		}

		super.cleanup();
	}

	//-------- methods --------

	/**
	 *  Get the context condition.
	 *  @return	The context condition.
	 */
	public RCondition	getContextCondition()
	{
		return this.contextcondition;
	}

	/**
	 *  Get the plan body.
	 *  May throw any kind of exception, when the body creation fails
	 *  @return	The plan body.
	 */
	public Object	createBody()	throws Exception
	{
		//assert this.body==null: this;
		if(!this.state.equals(STATE_BODY))
			throw new RuntimeException("Plan body must be created in the first plan step: "+this+", "+this.getState());
		if(this.body!=null)
			throw new RuntimeException("Plan body already exists: "+this);
		this.body	= exe.createPlanBody(this);
		throwSystemEvent(SystemEvent.PLAN_CHANGED);
		//System.out.println("################# inited: "+this.getName()+" "+this.body);
		return this.body;
	}

	/**
	 *  Get the plan body.
	 *  @return	The plan body.
	 */
	public Object	getBody()
	{
		return this.body;
	}
	
	/**
	 *  Get the plan executor.
	 */
	public IPlanExecutor	getPlanExecutor()
	{
		return this.exe;
	}
	
	/**
	 *  Get the exception that occurred in the plan body (if any).
	 */
	public Exception	getException()
	{
		return this.exception;
	}
	
	/**
	 *  Get the execution state (body/passed/failed/aborted).
	 */
	public String	getState()
	{
		return this.state;
	}
	
	/**
	 *  Execute a step of this plan.
	 *  Causes the latest event to be handled.
	 *  Should only be called by the scheduler.
	 */
	public void	executePlanStep()
	{
		RCapability cap=getScope();
		boolean interrupted = false;
		Exception	ex	= null;

		assert cap.getAgent().getCurrentPlan()==null;
	   	cap.getAgent().setCurrentPlan(this);
		try
		{
			// Reset scheduled flag on beginning of each step.
			this.scheduled	= false;

			// If first step, change state from initial to body.
			if(state.equals(STATE_INITIAL))
				this.state	= STATE_BODY;

			if(state.equals(STATE_BODY))
			{
				interrupted = exe.executePlanStep(this);
			}
			else if(state.equals(STATE_PASSED))
			{				
				interrupted = exe.executePassedStep(this);
			}
			else if(state.equals(STATE_FAILED))
			{				
				interrupted = exe.executeFailedStep(this);
			}
			else if(state.equals(STATE_ABORTED))
			{				
				interrupted = exe.executeAbortedStep(this);
			}
		}
		catch(Exception e)
		{
			ex	= e;
			
			// Log user-level exception (i.e. not BDI exceptions).
			if(!(e instanceof BDIFailureException))
			{
				StringWriter	sw	= new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				Level level = (Level)cap.getPropertybase().getProperty(PROPERTY_LOGGING_LEVEL_EXCEPTIONS);
				cap.getLogger().log(level, cap.getAgent().getName()+
					": Exception while executing: "+this+"\n"+sw);
				//System.out.println(cap.getAgent().getName()+": Exception while executing: "+this);
				//e.printStackTrace();
			}
			
			// Log BDI exceptions of top-level plans (because otherwise no one would notice).
			else if(getRootGoal().getProprietaryGoal()==null)
			{
				StringWriter	sw	= new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				// Todo: allow finetuning of exception logging levels.
				cap.getLogger().warning(cap.getAgent().getName()+
					": Exception while executing: "+this+"\n"+sw);
			}
		}
		
		// Cleanup after plan step.
		cap.getAgent().setCurrentPlan(null);
		// Reset the event, when the plan is not scheduled.
		// The ready list can contain the plan again when
		// it called pause and an event from its waitqueue
		// was dispatched immediatly.
		if(!isScheduled())
			this.event = null;
			// assignNewEvent(null); // not possible because it sets wait filter to null

		// Detect wrong use of atomic blocks.
		if(cap.getAgent().isAtomic())
		{
			assert !interrupted : "When atomic, plan shouldn't be interrupted.";
			if(ex==null)
			{
				cap.getLogger().log(Level.SEVERE, cap.getAgent().getName()+
					": Atomic block not finished: "+this);
				ex	= new RuntimeException("Atomic block not finished.");
			}
			cap.getAgent().endAtomic();
		}



		// Cases for end of plan step:
		// - exception in body	-> move state to failed
		// - interrupted	-> add new agenda entry for next microplanstep
		// - not waiting and not scheduled after body	-> move state to passed
		// - not waiting and not scheduled after passed/failed/aborted -> finalize plan
		if(ex!=null && state.equals(STATE_BODY))
		{
			assert getWaitAbstraction()==null && !isScheduled(): "When exception, plan shouldn't be waiting: "+ex+", "+getWaitAbstraction()+", "+isScheduled();
			assert !interrupted : "When exception, plan shouldn't be interrupted: "+ex;
			// When exception in plan body -> plan failed.
			exception	= ex;
			state	= STATE_FAILED;
			// Disable condition.
			if(contextcondition!=null)
				contextcondition.setTraceMode(ICondition.TRACE_NEVER);
			schedule(null);	// Hack!!! No event?
		}
		else if(interrupted && !isScheduled())
		{
			// Interrupted, micro plan step (no new event assignment necessary)
			schedule(null);	// Hack!!! No event?
		}
		else if(getWaitAbstraction()==null && !isScheduled())
		{
			// Body finished.
			if(state.equals(RPlan.STATE_BODY))
			{
				state	= STATE_PASSED;
				// Disable condition.
				if(contextcondition!=null)
					contextcondition.setTraceMode(ICondition.TRACE_NEVER);
				schedule(null);	// Hack!!! No event?
			}
			
			// Finally finished after passed/failed/aborted.
			else
			{
				// Finish goal when plan has failed or succeeded.
				if(!getRootGoal().isFinished())
				{
					// When exception occurred plan has failed.
					if(exception!=null)
						getRootGoal().fail(exception);
	
					// When plan is completed without exception -> success.
					else
						getRootGoal().succeed();
				}

				getScope().getPlanbase().removePlan(this);
			}
		}
	}

	/**
	 *  Interrupt a plan step.
	 */
	public void interruptPlanStep()
	{
		// Plan must be running.
		assert !this.state.equals(STATE_INITIAL) : this;
		exe.interruptPlanStep(this);
	}
	
	/**
	 *  Schedule the plan.
	 *  @param event	The event causing the scheduling.
	 */
	public void	schedule(Object event)
	{
		this.scheduled	= true;
		// Hack!!! Use plan when no cause?
		getScope().getAgent().getInterpreter().addAgendaEntry(new ExecutePlanStepAction(this), event!=null ? event : this);		
	}

	/**
	 *  Wait for a wait abstraction.
	 *  a) if the wait abstraction contains some filter -> possibly dispatch from wait queue
	 *  b) otherwise possibly mofify wa and set the plans wa accordingly.
	 *  @return The filter. todo: remove return value?!
	 */
	public IFilter	waitFor(WaitAbstraction wa)
	{
		// Hack!!! Already removed plans cannot wait.
		if(!isAlive())
			return null;

		//System.out.println("pause "+rplan+" "+filter+" "+timeout+" "+condition);

		// Try dispatch from wait queue (hack???).
		boolean dispatched	= false;
		if(wa.getFilter()!=null)
		{
			IREvent[] events = getWaitqueue().getEvents();
			for(int i=0; i<events.length && !dispatched; i++)
			{
				//System.out.println("Found waitqueue event: "+events[i]);
				if(getScope().getAgent().applyFilter(wa.getFilter(), events[i]))
				{
					//System.out.println("Matched: "+events[i]);
					getWaitqueue().removeEvent(events[i]);
					assignNewEvent(events[i]);

					RPlan.this.schedule(events[i]);

					dispatched	= true;
					//System.out.println("--- dispatch from waitqueue --- "+winfos[i].getEvent());
					//System.out.println(getName()+": REvent dispatched from wait queue: "+rplan);
				}
			}
		}

		if(!dispatched)
		{
			// Set the wait filter for the plan.
			setWaitAbstraction(wa);
			//System.out.println("Setting filter: "+filter+" for plan "+rplan);
		}

		return wa.getFilter();
	}

	/**
	 *  Get the root goal.
	 *  @return	The root goal.
	 */
	public RProcessGoal	getRootGoal()
	{
		return this.rootgoal;
	}

	/**
	 *  Get the initial event.
	 *  @return	The last event for the plan instance.
	 */
	public IREvent	getInitialEvent()
	{
		return this.initialevent;
	}

	/**
	 *  Get the latest event for the plan instance.
	 *  @return	The last event for the plan instance.
	 */
	public IREvent	getLatestEvent()
	{
		return this.event;
	}

	/**
	 *  Set the latest event for the plan instance.
	 *  This is the event to be processed next, or that is just processed
	 *  (if the plan is running).
	 *  @param event	The latest event for the plan instance.
	 */
	public void	assignNewEvent(IREvent event)
	{
		// Cleanup event that is no longer used.
		// todo: Hack!!! what about lifetime of post-to-all events???
		if(this.event!=null && !this.event.isCleanedup())
			this.event.cleanup();

		this.event	= event;

		// Probably remove obsolete timetable and target entry
		// when a pause() was used.
		// Wait filter is null for first step of passive plan.
		//cleanupWaitFor();

		// Update plan instance rplan and schedule plan instance for execution.
        setWaitAbstraction(null);
		throwSystemEvent(SystemEvent.PLAN_CHANGED);
	}

	/**
     *  Get the wait abstraction.
     *  @return The wait abstraction.
	 */
	public WaitAbstraction	getWaitAbstraction()
	{
		return this.waitabs;
	}

	/**
	 *  Set the wait abstraction.
	 *  @param waitabs The wait abstraction.
	 */
	public void	setWaitAbstraction(WaitAbstraction waitabs)
	{
		this.lastwaitabs = this.waitabs;
    	this.waitabs	= waitabs;

		// Cleanup last wait abstraction.
		if(this.lastwaitabs!=null)
			this.lastwaitabs.cleanup();
		throwSystemEvent(SystemEvent.PLAN_CHANGED);
	}

	/**
	 *  Hack! todo: remove somehow
     *  Get the last wait abstraction.
     *  @return The last wait abstraction.
	 */
	public WaitAbstraction	getLastWaitAbstraction()
	{
		return this.lastwaitabs;
	}

	/**
	 *  Get the waitqueue.
	 *  @return The waitqueue.
	 */
	public Waitqueue getWaitqueue()
	{
		if(waitqueue==null)
			waitqueue = new Waitqueue(this);
		return waitqueue;
	}

	/**
	 *  Get the alive status of this plan.
	 *  A plan is alive once it has been created until
	 *  it is terminated or terminates by itself.
	 */
	public boolean	isAlive()
	{
		return alive;
	}

	/**
	 *  Is this plan finished.
	 *  @return True, if the plan is finished.
	 */
	public boolean isFinished()
	{
		// Test if
		// -- a) root goal is finished
		// -- b) proprietary goal is deactivated
		// c) the plan does not wait and is not in ready list (and does not run currently)
		// d) the context condition is not satisfied any longer
		// getRootGoal().isFinished()
		//	|| (getRootGoal().getProprietaryGoal()!=null &&
		//		!getRootGoal().getProprietaryGoal().isActive())
		// todo: what here?
		//return (getWaitFilter()==null && !getScope().getAgent().getReadyList().contains(this))
		//	|| (getContextCondition()!=null && !getContextCondition().getBooleanValue());
		return getWaitAbstraction()==null && !isScheduled();
			//|| (getContextCondition()!=null && !getContextCondition().getBooleanValue());
	}

	/**
	 *  Test if body execution is finished.
	 * /
	public boolean	isBodyFinished()
	{
		return bodyfin;
	}

	/**
	 *  Set the body finished state.
	 * /
	public void	setBodyFinished(boolean bodyfin)
	{
		this.bodyfin = bodyfin;
	}*/

	/**
	 *  Terminate this plan.
	 */
	public void	terminate()
	{
		// Plan should not be finished.
		assert isAlive();
		
		// Disable condition.
		if(contextcondition!=null)
			contextcondition.setTraceMode(ICondition.TRACE_NEVER);

		// When process goal is not finished, terminate by dropping the goal.
		// Will call rplan.terminate() again, when the goal is dropped.
		if(!getRootGoal().isFinished())
		{
			// Hack!!! Need new state "aborted w/o success/failure".
			getRootGoal().drop(RProcessGoal.PROCESS_STATE_ABORTED_ON_FAILURE);
		}

		// When root goal already finished, terminate the plan.
		else
		{
			// Only abort execution of plan body (passed/failed will not be aborted).
			if(state.equals(RPlan.STATE_BODY))
			{
				// Move to aborted state and schedule the plan (will cause mircoplanstep if plan terminates itself).
				state	= RPlan.STATE_ABORTED;
				schedule(null);	// Hack!!! No event?
				
				// Cleanup wait for.
				setWaitAbstraction(null);
			}
			// When plan did not run at all, do cleanup here (instead of executePlanStep()).
			else if(state.equals(STATE_INITIAL))
			{
				getScope().getPlanbase().removePlan(this);
			}
		}
	}

	/**
	 *  Test if this plan is scheduled for execution.
	 *  @return True, if scheduled.
	 */
	// Todo: flag should only be used in executePlanStep()?
	public boolean isScheduled()
	{
//		boolean	ret	= false;
//
//		IAgenda agenda = getScope().getAgent().getAgenda();
//		List exes = agenda.getUnprocessedEntries();
//		for(int i=0; i<exes.size() && !ret; i++)
//		{
//			IAgendaAction action = ((AgendaEntry)exes.get(i)).getAction();
//			if(action instanceof ExecutePlanStepAction)
//			{
//				if(((ExecutePlanStepAction)action).getPlan()==RPlan.this)
//				{
//					ret = true;
//				}
//			}
//		}

		return scheduled;
	}

	/**
	 *  Get the thread.
	 */
	public Thread getThread()
	{
		return exe.getExecutionThread(this);
	}

	//-------- overridings --------

	/**
	 *  Test if this plan instances is equal to another object.
	 *  Two plan instance infos are equal,
	 *  if the plan instance is null they are equal, only if they
	 *  are identical.
	 *  @param object	The object against which to test equality.
	 *  @return	True, when the object is equal to this plan instance info.
	 * /
	// Do we need this ???
	public boolean	equals(Object object)
	{
		boolean eq = false;
		if(this==object ||
			((object instanceof RPlan)
			&& this.body==((RPlan)object).getPlanInstance()
			&& this.body!=null))
		{
			eq = true;
		}
		return eq;
	}
*/
	/**
	 *  Create a string representation of this element.
	 *  @return	This element represented as string.
	 */
	public String	toString()
	{
		StringBuffer	sb	= new StringBuffer();
		sb.append("RPlan(name=");
		sb.append(getName());
		/*sb.append(", rootgoal=");
		sb.append(rootgoal!=null ? rootgoal.getName() : "null");
		sb.append(", activegoal=");
		sb.append(activegoal!=null ? activegoal.getName() : "null");
		sb.append(", event=");
		sb.append(event);
		sb.append(", filter=");
		sb.append(filter);
		sb.append(", filters=");
		sb.append(filters);
		sb.append(", waitqueue=");
		sb.append(waitqueue);*/
		sb.append(")");
		return sb.toString();
	}

	//-------- helper methods --------

	/**
	 *  Generate a property representation for encoding this element
	 *  (eg to SL).
	 *  @return A properties object representaing this element.
	 */
	public Map getEncodableRepresentation()
	{
		Map	representation	= super.getEncodableRepresentation();
		representation.put("rootgoal", getRootGoal().getName());
		if(getRootGoal()!=null && getRootGoal().getProprietaryGoal()!=null)
		{
			RReferenceableElement	propgoal	= getRootGoal().getProprietaryGoal().getOriginalElement();
			representation.put("proprietarygoal", propgoal.getName());
			representation.put("proprietarygoalscope", propgoal.getScope().getDetailName());
		}
		// Active goals no longer supported (0.93beta clean membrane)
		//representation.put("activegoal", getActiveGoal()==null ? "null" : getActiveGoal().getName());
		representation.put("latestevent", getLatestEvent()==null? (Object)"null":
			((RReferenceableElement)getLatestEvent()).getEncodableRepresentation());
		IFilter filter = getWaitAbstraction()==null? null: getWaitAbstraction().getFilter();
		representation.put("filter", filter==null? (Object)"null":
			(filter instanceof IEncodable? (Object)((IEncodable)filter).getEncodableRepresentation(): ""+filter));
		representation.put("state", ""+this.state);
		representation.put("body", ""+this.body);
		representation.put("waitqueue", ""+waitqueue);
		//representation.put("waitqueuefilters", SUtil.arrayToString(getWaitqueueFilters()));
		return representation;
	}

	/**
	 *  Generate a change event for this element
	 *  using the current representation.
	 *  @param event	The event.
	 */
	// Hack ???
	public void	throwSystemEvent(String event)
	{
		if(isAlive())
		{
//			if(event.equals(SystemEvent.PLAN_ADDED) && getScope().getAgent().getName().indexOf("Willy")!=-1)
//				System.out.println(event+", "+this);
			super.throwSystemEvent(event);
		}
	}

	//-------- actions --------

}
