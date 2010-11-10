package jadex.runtime;

import jadex.model.IMCapability;
import jadex.model.IMEventbase;
import jadex.model.IMPlan;
import jadex.model.IMPlanBody;
import jadex.runtime.impl.IREvent;
import jadex.runtime.impl.IRGoalEvent;
import jadex.runtime.impl.RPlan;
import jadex.runtime.impl.WaitAbstraction;
import jadex.runtime.planwrapper.EventbaseWrapper;
import jadex.runtime.planwrapper.GoalWrapper;
import jadex.util.collection.SCollection;

import java.io.Serializable;
import java.util.Map;


/**
 *  A plan executor for JADE-behaviour style plans
 *  (with action method called for each step).
 *  Plan bodies have to inherit from @link{MobilePlan}.
 */
public class JavaMobilePlanExecutor implements IPlanExecutor, Serializable
{
	//-------- attributes --------

	/** The executing thread. */
	protected transient Thread thread;

	/** The cache for parsed inline plan body types. */
	protected transient Map  planbodies;

	//-------- constructor --------

	/**
	 *  Create a new threadbased plan executor.
	 */
	public JavaMobilePlanExecutor()
	{
	}

	//-------- IPlanExecutor interface --------

	/**
	 *  Create the body of a plan.
	 *  @param plan The plan.
	 *  @return	The created body.
	 *  May throw any kind of exception, when the body creation fails
	 */
	public Object	createPlanBody(RPlan plan) throws Exception
	{
		//return AbstractPlan.createPlanBody(plan);

		// Create plan body object.
		// Hack!!! Not an elegant way by using a static hashtable!
		// Needed for passing the rplan to the abstract plan instance.
		String refname= ""+Thread.currentThread()+"_"+Thread.currentThread().hashCode();
		AbstractPlan.planinit.put(refname, plan);

		IMPlanBody	bodyexp	= ((IMPlan)plan.getModelElement()).getBody();

		// todo: If term is reflect node, activate class reloading. Hack!!!
		/*if(bodyexp.getTerm() instanceof ReflectNode)
		{
			((ReflectNode)bodyexp.getTerm()).setReloading(true);
		}*/

		Object	body = null;
		Class tmp = planbodies==null ? null : (Class)planbodies.get(plan.getModelElement());
		if(tmp!=null)
		{
			body = tmp.newInstance();
		}
		else if(bodyexp.isInline())
		{
			// Create an inline plan by creating a new class with the
			// body code from the bodyexp.
			IMCapability mscope = (IMCapability)plan.getScope().getModelElement();
			StringBuffer sb = new StringBuffer("public void action(jadex.runtime.IEvent event){");
			sb.append(bodyexp.getExpressionText());
			sb.append("}");
			String passed = bodyexp.getPassedCode();
			if(passed!=null && passed.length()!=0)
			{
				sb.append("public void passed(){");
				sb.append(passed);
				sb.append("}");
			}
			String failed = bodyexp.getFailedCode();
			if(failed!=null && failed.length()!=0)
			{
				sb.append("public void failed(){");
				sb.append(failed);
				sb.append("}");
			}
			String aborted = bodyexp.getAbortedCode();
			if(aborted!=null && aborted.length()!=0)
			{
				sb.append("public void aborted(){");
				sb.append(aborted);
				sb.append("}");
			}
			body = mscope.getParser().parseClass(sb.toString(), MobilePlan.class);
			if(planbodies==null)
				this.planbodies = SCollection.createWeakHashMap();
			planbodies.put(plan.getModelElement(), body.getClass());
		}
		else if(bodyexp.getClazz()!=null)
		{
			try
			{
				body = bodyexp.getClazz().newInstance();
			}
			catch(Exception e)
			{
				// Use only RuntimeException from below
			}
		}
		else
		{
			// Create a normal class plan.
			body = plan.getScope().getExpressionbase().evaluateInternalExpression(bodyexp, plan);
		}

		if(body==null)
			throw new RuntimeException("Plan body could not be created: "+bodyexp.getExpressionText());

		AbstractPlan.planinit.remove(refname);

		return body;
	}

	/**
	 *  Execute a step of a plan.
	 *  Executing a step should cause the latest event to be handled.
	 *  Will be called by the scheduler for every event to be handled.
	 *  May throw any kind of exception, when the plan execution fails
	 */
	protected boolean	executeStep(RPlan plan, String type)	throws Exception
	{
		// Save the execution thread for this task.
		this.thread = Thread.currentThread();
		
		// Get plan body object.
		Object tmp = plan.getBody();
		if(tmp==null)
		{
			tmp	= plan.createBody();
		}
		if(!(tmp instanceof MobilePlan))
		{
			throw new RuntimeException("Mobile plan body not of type MobilePlan.");
		}
		MobilePlan	pi	= (MobilePlan)tmp;
		
		// Get BDI exception (if any).
		BDIFailureException	exe	= null;
		IREvent oevent = plan.getLatestEvent();
		if(oevent!=null && IMEventbase.TYPE_TIMEOUT.equals(oevent.getType()))
		{
			exe = (BDIFailureException)oevent.getParameter("exception").getValue();
			// todo: if timeout for subgoal, drop the subgoal
		}
		else if(oevent instanceof IRGoalEvent)
		{
			IRGoalEvent ge = (IRGoalEvent)oevent;
			if(ge.isInfo() && ge.getGoal().isFailed() && ge.getGoal().isSubgoal())	// todo: only subgoals?
			{
				exe = new GoalFailureException(new GoalWrapper(ge.getGoal()), ge.getGoal().getException());
			}
		}
		
		// On exception (e.g. goal failure or timout) call exception handler).
		if(exe!=null)
		{
			pi.exception(exe);
		}
		// Otherwise, execute corresponding plan methods (action, passed, failed or aborted).
		else
		{
			// Create wrapper for event. Hack??? Executor should only get wrapped event?
			IEvent	event	= oevent!=null? EventbaseWrapper.wrap(oevent): null;
			
			if(type.equals(RPlan.STATE_BODY))
			{
				pi.action(event);
			}
			else if(type.equals(RPlan.STATE_PASSED))
			{				
				pi.passed(event);
			}
			else if(type.equals(RPlan.STATE_FAILED))
			{				
				pi.failed(event);
			}
			else if(type.equals(RPlan.STATE_ABORTED))
			{				
				pi.aborted(event);
			}
		}
		
		// Reset thread.
		this.thread	= null;
		
		// Interrupts not supported.
		return false;
	}

	/**
	 *  Execute a step of a plan.
	 *  Executing a step should cause the latest event to be handled.
	 *  
	 *  Will be called by the scheduler for every event to be handled.
	 *  May throw any kind of exception, when the plan execution fails
	 *  @return True, if plan was interrupted (micro plan step).
	 */
	public boolean	executePlanStep(RPlan plan)	throws Exception
	{
		return executeStep(plan, RPlan.STATE_BODY);
	}

	/**
	 *  Execute a step of the plans passed() code.
	 *  This method is called, after the plan has finished
	 *  successfully (i.e. without exception).
	 *  
	 *  Will be called by the scheduler for the first time and 
	 *  every subsequent event to be handled.
	 *  May throw any kind of exception, when the execution fails
	 *  @return True, if execution was interrupted (micro plan step).
	 */
	public boolean	executePassedStep(RPlan plan)	throws Exception
	{
		return executeStep(plan, RPlan.STATE_PASSED);
	}

	/**
	 *  Execute a step of the plans failed() code.
	 *  This method is called, when the plan has failed
	 *  (i.e. due to an exception occurring in the plan body).
	 *  
	 *  Will be called by the scheduler for the first time and 
	 *  every subsequent event to be handled.
	 *  May throw any kind of exception, when the execution fails
	 *  @return True, if execution was interrupted (micro plan step).
	 */
	public boolean	executeFailedStep(RPlan plan)	throws Exception
	{
		return executeStep(plan, RPlan.STATE_FAILED);		
	}

	/**
	 *  Execute a step of the plans aborted() code.
	 *  This method is called, when the plan is terminated
	 *  from the outside (e.g. when the corresponding goal is
	 *  dropped or the context condition of the plan becomes invalid)
	 *  
	 *  Will be called by the scheduler for the first time and 
	 *  every subsequent event to be handled.
	 *  May throw any kind of exception, when the execution fails
	 *  @return True, if execution was interrupted (micro plan step).
	 */
	public boolean	executeAbortedStep(RPlan plan)	throws Exception
	{
		return executeStep(plan, RPlan.STATE_ABORTED);		
	}

	/**
	 *  Interrupt a plan step during execution.
	 *  The plan is requested to stop the execution to allow
	 *  consequences of performed plan actions (like belief changes)
	 *  taking place. If the method is not implemented the
	 *  plan step will be NOT be interrupted.
	 */
	public void	interruptPlanStep(RPlan plan)
	{
		// Cannot be supported because of single thread model.
	}

	/**
	 *  Called on termination of a plan.
	 *  Free all associated ressources, stop threads, etc.
	 */
	public void cleanup(RPlan rplan)
	{
	}

	/**
	 *  Get the executing thread of a plan.
	 *  @param rplan The plan.
	 *  @return The executing thread (if any).
	 */
	public Thread getExecutionThread(RPlan rplan)
	{
		return this.thread;
	}

	/**
	 *  Block a plan until an event matching the wait abstraction
	 *  occurs.
	 *  Only used for standard plans, which block during execution.
	 */
	public IREvent eventWaitFor(RPlan rplan, WaitAbstraction wa)
	{
		throw new UnsupportedOperationException("eventWaitFor not possible for mobile plans.");
	}
}
