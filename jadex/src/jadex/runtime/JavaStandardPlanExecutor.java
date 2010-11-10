package jadex.runtime;

import jadex.model.IMCapability;
import jadex.model.IMEventbase;
import jadex.model.IMPlan;
import jadex.model.IMPlanBody;
import jadex.runtime.impl.IREvent;
import jadex.runtime.impl.RBDIAgent;
import jadex.runtime.impl.RPlan;
import jadex.runtime.impl.WaitAbstraction;
import jadex.util.collection.SCollection;
import jadex.util.concurrent.ThreadPoolFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;

/**
 *  A plan executor for plans that run on their own thread
 *  and therefore may perform blocking wait operations.
 *  Plan bodies have to inherit from @link{Plan}.
 */
public class JavaStandardPlanExecutor	implements IPlanExecutor, Serializable
{
	//-------- constants --------

	public static final String MAX_PLANSTEP_TIME = "max_planstep_time";

	//-------- attributes --------

	/** The agent. */
	protected RBDIAgent	agent;

	/** The maximum execution time per plan step in millis. */
	protected Number	maxexetime;

	/** The pool for the planinstances -> execution tasks. */
	protected Map	tasks;

	/** The cache for parsed inline plan body types. */
	protected transient Map  planbodies;

	//-------- constructor --------

	/**
	 *  Create a new threadbased plan executor.
	 */
	public JavaStandardPlanExecutor(RBDIAgent agent)
	{
		this.agent	= agent;
		this.tasks = SCollection.createHashMap();
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
			StringBuffer sb = new StringBuffer("public void body(){");
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
			body = mscope.getParser().parseClass(sb.toString(), Plan.class);
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
				// e.printStackTrace();
			}
		}
		else
		{
			// Create a normal class plan.
			body = plan.getScope().getExpressionbase().evaluateInternalExpression(bodyexp, plan);
		}

		AbstractPlan.planinit.remove(refname);

		if(body==null)
			throw new RuntimeException("Plan body could not be created: "+bodyexp.getExpressionText());

		return body;
	}

	/**
	 *  Execute a step of a plan.
	 *  Executing a step should cause the latest event to be handled.
	 *  Will be called by the scheduler for every event to be handled.
	 *  May throw any kind of exception, when the plan execution fails
	 *  @return True, if the plan step was interrupted (interrupted flag).
	 */
	public boolean	executeStep(RPlan plan, String steptype)	throws Exception
	{
		// Get or create new a thread for the plan instance info.
		boolean newthread = false;
		PlanExecutionTask task = (PlanExecutionTask)tasks.get(plan);
		if(task==null)
		{
			task = new PlanExecutionTask(plan);
			tasks.put(plan, task);
			newthread = true;
		}
		Object monitor = task.getMonitor();

		// Lock the pool monitor and start it.
		// Because it needs its monitor to run, it starts
		// not until the scheduler has called wait().
		//System.out.println("now scheduling: "+plinfo);
		synchronized(monitor)
		{
//		boolean haslock = false;
//		try{haslock = task.lock.tryLock(task.LOCKTIME, java.util.concurrent.TimeUnit.MILLISECONDS);}
//		catch(InterruptedException e){e.printStackTrace();}
//		if(!haslock)
//			throw new RuntimeException("Could not get lock: "+this);

			task.setStepType(steptype);
			task.setState(PlanExecutionTask.STATE_RUNNING);
			if(newthread)
			{
				// It must be avoided that the new thread
				// immediately starts. Therefore its first
				// instruction is synchronized(monitor){}
				ThreadPoolFactory.getThreadPool().execute(task);
			}
			else
			{
				monitor.notify();
//				task.cond.signal();
			}

			assert agent.getInterpreter().agent_executing: this;
			assert !agent.getInterpreter().plan_executing1: this;
			assert !agent.getInterpreter().plan_executing2: this;
			agent.getInterpreter().plan_executing1	= true;
			
			try
			{
				// Wait causes to free the monitor
				// and awakens the plan thread which needs
				// the monitor to execute
				if(getMaxExecutionTime()==0)
					monitor.wait();
//					if(!task.cond.await(task.LOCKTIME, java.util.concurrent.TimeUnit.MILLISECONDS))
//						throw new RuntimeException("Condition not signalled: "+this);
				else
					monitor.wait(getMaxExecutionTime());
//					task.cond.await(getMaxExecutionTime(), java.util.concurrent.TimeUnit.MILLISECONDS);
			}
			catch(InterruptedException e)
			{
				// Shouldn't happen (agent thread shouldn't be interrupted)
				System.err.println("Warning, agent thread was interrupted");
				e.printStackTrace(System.err);
			}
			
			assert agent.getInterpreter().agent_executing: this;
			assert agent.getInterpreter().plan_executing1: this;
			assert !agent.getInterpreter().plan_executing2: this;
			agent.getInterpreter().plan_executing1	= false;

			if(PlanExecutionTask.STATE_RUNNING.equals(task.getState()))
			{
				agent.getLogger().warning(" plan step is running longer than maximum " +
						"execution time, plan will be terminated: "+agent+" "+task);
				task.getPlan().getRootGoal().fail(null);
				// Todo: wait for plan termination
				// (otherwise there are two threads running at once).
			}
//		}
//		task.lock.unlock();

//        if(task.getState().equals(PlanExecutionTask.STATE_TERMINATED))
//        {
            //plan.setCleanupFinished(true);
        	if(task.getThrowable() instanceof Error)
        		throw (Error)task.getThrowable();
        	else if(task.getThrowable() instanceof Exception)
        		throw (Exception)task.getThrowable();
        	else if(task.getThrowable()!=null)
        		// Neither Error nor Exception !?
        		throw new RuntimeException("Unexpected Throwable type: "+task.getThrowable().getClass(), task.getThrowable());
        }

		return task.getState().equals(PlanExecutionTask.STATE_INTERRUPTED);
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
		PlanExecutionTask task = (PlanExecutionTask)tasks.get(plan);
		assert task.getExecutionThread()==Thread.currentThread() : plan+", "+Thread.currentThread();
		task.giveBackControl(PlanExecutionTask.STATE_INTERRUPTED);
	}

	/**
	 *  Called on termination of a plan.
	 *  Free all associated ressources, stop threads, etc.
	 */
	public void cleanup(RPlan rplan)
	{
		PlanExecutionTask task = (PlanExecutionTask)tasks.get(rplan);
		if(task!=null)
		{
			Object monitor = task.getMonitor();

			// Because plan thread needs its monitor to run, it starts
			// not until the executor has called wait().
			synchronized(monitor)
			{
//			boolean haslock = false;
//			try{haslock = task.lock.tryLock(task.LOCKTIME, java.util.concurrent.TimeUnit.MILLISECONDS);}
//			catch(InterruptedException e){e.printStackTrace();}
//			if(!haslock)
//				throw new RuntimeException("Could not get lock: "+this);

				task.setState(PlanExecutionTask.STATE_RUNNING);
				task.setTerminate(true);
				monitor.notify();

				assert agent.getInterpreter().agent_executing: this;
				assert !agent.getInterpreter().plan_executing1: this;
				assert !agent.getInterpreter().plan_executing2: this;
				agent.getInterpreter().plan_executing1	= true;

				try
				{
					// Wait causes to free the monitor
					// and awakens the plan thread which needs
					// the monitor to execute
					if(getMaxExecutionTime()==0)
						monitor.wait();
//						if(!task.cond.await(task.LOCKTIME, java.util.concurrent.TimeUnit.MILLISECONDS))
//							throw new RuntimeException("Condition not signalled: "+this);
					else
						monitor.wait(getMaxExecutionTime());
//						task.cond.await(getMaxExecutionTime(), java.util.concurrent.TimeUnit.MILLISECONDS);
				}
				catch(InterruptedException e)
				{
					// Shouldn't happen (agent thread shouldn't be interrupted)
					System.err.println("Warning, agent thread was interrupted");
					e.printStackTrace(System.err);
				}

				assert agent.getInterpreter().agent_executing: this;
				assert agent.getInterpreter().plan_executing1: this;
				assert !agent.getInterpreter().plan_executing2: this;
				agent.getInterpreter().plan_executing1	= false;

				if(PlanExecutionTask.STATE_RUNNING.equals(task.getState()))
				{
					agent.getLogger().warning(" plan step is running longer than maximum " +
							"execution time, plan will be terminated: "+agent+" "+task);
					task.getPlan().getRootGoal().fail(null);
					// Todo: wait for plan termination
					// (otherwise there are two threads running at once).
				}
			}
//			task.lock.unlock();
		}
	}

	/**
	 *  Get the executing thread of a plan.
	 *  @param rplan The plan.
	 *  @return The executing thread (if any).
	 */
	public Thread getExecutionThread(RPlan rplan)
	{
		PlanExecutionTask task =  (PlanExecutionTask)tasks.get(rplan);
		return task==null? null: task.getExecutionThread();
	}

	/**
	 *  Called from a plan.
	 *  Registers the plan to wait for a event.
	 *  Blocks plan when body method is finished.
	 *  Note: This method cannot be synchronized, because when
	 *  a thread comes in and waits it still owns the
	 *  BDIAgent lock.
	 *  @param rplan The planinstance.
	 *  @param wa The wait abstraction.
	 */
	public IREvent	eventWaitFor(RPlan rplan, WaitAbstraction wa)
	{
		if(agent.isAtomic())
			throw new RuntimeException("WaitFor not allowed in atomic block.");

		AbstractPlan body = (AbstractPlan)rplan.getBody();
		if(body==null)
			throw new RuntimeException("Plan body null. waitFor() calls from plan constructors not allowed.");

		// Set wait filter settings in plan.
		rplan.waitFor(wa);
		
		IREvent ret = null;
		boolean failure = false;
		PlanExecutionTask task = (PlanExecutionTask)tasks.get(rplan);
     	if(task.getExecutionThread()==Thread.currentThread())
		{
     		// Transfer execution to agent thread and wait until the plan is scheduled again.
			task.giveBackControl(PlanExecutionTask.STATE_WAITING);

			// When timout event occurred, throw as TimeoutException.
			ret = rplan.getLatestEvent();
			if(ret!=null && IMEventbase.TYPE_TIMEOUT.equals(ret.getType())
				&& ret.getParameter("exception").getValue()!=null)
			{
				throw (TimeoutException)ret.getParameter("exception").getValue();
			}
		}
		else
		{
			failure = true;
		}

		//System.out.println(":::"+myid+" "+this);

		if(failure)
			agent.getLogger().log(Level.SEVERE, "ThreadedWaitFor error, not plan thread: "+Thread.currentThread());

		assert ret!=null: rplan.getName();
		return ret;
	}

	/**
	 *  Get the maximum execution time.
	 *  0 indicates no maximum execution time.
	 *  @return The max execution time.
	 */
	protected long getMaxExecutionTime()
	{
		if(maxexetime==null)
		{
			maxexetime = (Number)agent.getPropertybase().getProperty(MAX_PLANSTEP_TIME);
			if(maxexetime==null)
				maxexetime = new Long(0);
		}
		return maxexetime.longValue();
	}

	//-------- The thread for a plan instance ---------

	/**
	 *  The task for executing a plan instance. Will
	 *  be executed in its own thread.
	 */
	protected class PlanExecutionTask implements Runnable
	{
		//-------- constants --------

		public static final String STATE_RUNNING = "running";
		public static final String STATE_WAITING = "waiting";
        public static final String STATE_INTERRUPTED = "interrupted";
		public static final String STATE_TERMINATED = "terminated";

		//-------- attributes --------

		/** The plan. */
		protected RPlan rplan;

		/** The thread to wakeup. */
		protected final Object monitor;

//		/** Lock for debugging with 1.5. */ 
//		java.util.concurrent.locks.ReentrantLock lock = new java.util.concurrent.locks.ReentrantLock();
//		/** Condition for debugging with 1.5. */ 
//		java.util.concurrent.locks.Condition cond = lock.newCondition();
//		/** Time to wait for a lock. */
//		long LOCKTIME	= 20000; 

		/** The plan thread's state. */
		protected String state;

		/** The plan step type to execute (body/passed/failed/aborted). */
		protected String steptype;

		/** The execution result (when a problem occurred). */
		protected Throwable throwable;

		/** The thread executing this task. */
		protected Thread thread;
		
		/** Flag indicating that the plan should terminate immediately (set from agent thread). */
		protected boolean	terminate;

		//-------- constructors --------

		/**
		 *  Create a new plan exeution thread.
		 *  @param rplan The plan instance info.
		 */
		public PlanExecutionTask(RPlan rplan)
		{
			this.rplan = rplan;
			this.monitor = new Object();
		}

		//-------- methods --------

		/**
		 *  The thread method.
		 */
		public void run()
		{
			// When the thread is new it has to wait till the
			// scheduler is finished (called wait).
			// The plan is not allowed to hold the monitor for
			// the whole plan execution because this would not
			// allow the scheduler (agent) thread to wakeup
			// whenever the planstep execution time of the plan
			// thread exceeds.
			synchronized(monitor)
			{
				assert agent.getInterpreter().agent_executing: this;
				assert agent.getInterpreter().plan_executing1: this;
				assert !agent.getInterpreter().plan_executing2: this;
				agent.getInterpreter().plan_executing2	= true;				
			}
						
//			boolean haslock = false;
//			try{haslock = lock.tryLock(LOCKTIME, java.util.concurrent.TimeUnit.MILLISECONDS);}
//			catch(InterruptedException e){e.printStackTrace();}
//			if(!haslock)
//				throw new RuntimeException("Could not get lock: "+this);
//			lock.unlock();
			

			// Save the execution thread for this task.
			this.thread = Thread.currentThread();

			// Execute the plan (interrupted by pause() calls).
			// While the plan is executing its plan steps
			// it does not hold the monitor! Therefore another
			// task can grab the monitor and call notify on
			// it. This wakes up the agent thread.
			Plan pi = null;
			boolean	aborted	= false;	// Body is aborted, continue to aborted() method.
			boolean	interrupted	= false;	// Plan is interrupted, exit plan thread.
			try
			{
				// Thread is only executed once, so first step
				// will always be the body.
				assert	steptype.equals(RPlan.STATE_BODY) : steptype+", "+rplan;
				assert	rplan.getState().equals(RPlan.STATE_BODY) : rplan.getState()+", "+rplan;

				Object tmp = rplan.getBody();
				if(tmp==null)
					tmp = rplan.createBody();
				if(!(tmp instanceof Plan))
					throw new RuntimeException("User plan has wrong baseclass. Expected jadex.runtime.Plan for standard plan.");
				pi = (Plan)tmp;
				pi.body();
			}
			catch(BodyAborted e)
			{
				// The body method has been interrupted by the plan step action for abort.
				aborted	= true;
			}
			catch(PlanTerminated e)
			{
				// Plan is interrupted (e.g. due to excessive step length)
				// -> ignore and cleanup.
				interrupted	= true;
			}
			catch(Throwable t)
			{
				// Throwable in plan thread will be rethrown in agent thread.
				this.throwable	= t;
			}

			// Skip plan cleanup code, when plan is interrupted.
			if(!interrupted)
			{
				// Wait until scheduler calls plan cleanup.
				// Hack!!! Abort already happens on plan step action for abort, therefore no extra wait
				if(!aborted)
				{
					// Debug termination issue (todo: remove)
					if(!agent.getInterpreter().agent_executing)
					{
//						System.err.println("plan execution inconsistency: "+rplan);
//						if(throwable!=null)
//							throwable.printStackTrace();
						return;
					}
						
					giveBackControl(STATE_WAITING);
				}
				
				assert	steptype.equals(RPlan.STATE_PASSED)
					|| steptype.equals(RPlan.STATE_FAILED)
					|| steptype.equals(RPlan.STATE_ABORTED) : steptype+", "+rplan;
	
				// Execute cleanup code.
				throwable	= null;
				try
				{
					if(steptype.equals(RPlan.STATE_PASSED))
					{
						pi.passed();
					}
					else if(steptype.equals(RPlan.STATE_FAILED))
					{
						// Check if body has been created (can be null when body creation fails).
						if(pi!=null)
						{
							pi.failed();
						}
					}
					else if(steptype.equals(RPlan.STATE_ABORTED))
					{
						pi.aborted();
					}
				}
				catch(PlanTerminated e)
				{
					// Plan is interrupted (e.g. due to excessive step length)
					// -> ignore and cleanup.
				}
				catch(Throwable t)
				{
					// Throwable in plan thread will be rethrown in agent thread.
					this.throwable	= t;
				}
			}

			// Cleanup the plan execution thread.
			tasks.remove(rplan);
			this.state	= PlanExecutionTask.STATE_TERMINATED;

			// Finally, transfer execution back to agent thread.
			synchronized(monitor)
			{
				// Debug termination issue (todo: remove)
				if(!agent.getInterpreter().agent_executing)
				{
//					System.err.println("plan execution inconsistency: "+rplan);
//					if(throwable!=null)
//						throwable.printStackTrace();
					return;
				}

				assert agent.getInterpreter().agent_executing: this;
				assert agent.getInterpreter().plan_executing1: this;
				assert agent.getInterpreter().plan_executing2: this+", "+interrupted+", "+aborted;
				agent.getInterpreter().plan_executing2	= false;

				monitor.notify();
				//System.out.println("Execution of plan finished: "+type+", "+this+": "+rplan);
			}

//			haslock = false;
//			try{haslock = lock.tryLock(LOCKTIME, java.util.concurrent.TimeUnit.MILLISECONDS);}
//			catch(InterruptedException e){e.printStackTrace();}
//			if(!haslock)
//				throw new RuntimeException("Could not get lock: "+this);
//			cond.signal();			
//			lock.unlock();
		}

		/**
		 *  Get the pool monitor.
		 *  @return The monitor.
		 */
		public Object getMonitor()
		{
			return monitor;
		}

		/**
		 *  Interrupt the plan execution.
		 *  Stop and notify the scheduler.
		 *  Continues when monitor is notified from the scheduler again.
		 */
		public void	giveBackControl(String state)
		{
			//System.out.println("waiting for lock: "+n);
			synchronized(monitor)
			{
				//System.out.println("aquired for lock: "+n++);
							
//			boolean haslock = false;
//			try{haslock = lock.tryLock(LOCKTIME, java.util.concurrent.TimeUnit.MILLISECONDS);}
//			catch(InterruptedException e){e.printStackTrace();}
//			if(!haslock)
//				throw new RuntimeException("Could not get lock: "+this);

				//System.out.println(":::Wf0 "+agent+" "+rplan.getPlanbody());
				//System.out.println(":::Wf1 "+agent+" "+rplan.getPlanbody());
				// When the thread was set to be not alive anymore, terminate.
				// Alive is false by the terminate method of the rplan.
				assert	rplan.isAlive() : rplan;
				
				// Remember current step type (might get overwritten from agent thread).
				String planstate	= steptype;

				assert agent.getInterpreter().agent_executing: this;
				assert agent.getInterpreter().plan_executing1: this;
				assert agent.getInterpreter().plan_executing2: this;
				agent.getInterpreter().plan_executing2	= false;				
				
				// Transfer execution from plan thread to agent thread using notify/wait pair.
				this.state	= state;
				monitor.notify();
//				cond.signal();
				try
				{
					monitor.wait();
//					if(!cond.await(LOCKTIME, java.util.concurrent.TimeUnit.MILLISECONDS))
//						throw new RuntimeException("Condition not signalled: "+this+", locked="+lock.isLocked()+", owner="+lock.isHeldByCurrentThread());					
				}
				catch(InterruptedException e)
				{
					// Shouldn't happen (plan thread shouldn't be interrupted)
					System.err.println("Warning, plan thread was interrupted: "+rplan);
					e.printStackTrace(System.err);
				}

				assert agent.getInterpreter().agent_executing: this;
				assert agent.getInterpreter().plan_executing1: this;
				assert !agent.getInterpreter().plan_executing2: this;
				agent.getInterpreter().plan_executing2	= true;

				// Execution continues when the executors executeStep() transfers
				// execution from agent thread to plan thread (using another notify/wait pair).
				
				// When plan must be terminated unconditionally stop execution.
				if(terminate)
				{
					throw new PlanTerminated();
				}
				
				// When planstate has changed from body to aborted, leave body method
				// by throwing an error, which is catched by plan execution task.
				else if(planstate.equals(RPlan.STATE_BODY) && rplan.getState().equals(RPlan.STATE_ABORTED))
				{
					throw new BodyAborted();
				}
			}
//			lock.unlock();
		}

		/**
		 *  Get the plan.
		 *  @return The plan.
		 */
		public RPlan getPlan()
		{
			return this.rplan;
		}

		/**
		 *  Get the plan thread state.
		 *  @return The plan thread state.
		 */
		public String getState()
		{
			return this.state;
		}

		/**
		 *  Set the plan thread state.
		 *  @param state The plan thread state.
		 */
		public void setState(String state)
		{
			this.state = state;
		}

		/**
		 *  Set the plan step type.
		 *  @param type The plan step type (body/passed/failed/aborted).
		 */
		public void setStepType(String type)
		{
			this.steptype = type;
		}

		/**
		 *  Get the execution result.
		 *  @return The execution result.
		 */
		public Throwable getThrowable()
		{
			return throwable;
		}

		/**
		 *  Get the execution thread.
		 *  @return thread The thread.
		 */
		public Thread getExecutionThread()
		{
			return this.thread;
		}
		
		/**
		 *  Set the terminate flag.
		 */
		public void	setTerminate(boolean terminate)
		{
			this.terminate	= terminate;
		}

		/**
		 *  Create a string representation of this element.
		 */
		public String	toString()
		{
			return "PlanExecutionTask("+rplan+")";
		}
	}
	
	/**
	 *  An error thrown to abort the execution of the plan body.
	 */
	public static class BodyAborted	extends	ThreadDeath 
	{
//		public BodyAborted()
//		{
//			System.err.print(this+": ");
//			Thread.dumpStack();
//		}
//		
//		public String toString()
//		{
//			return super.toString()+"@"+hashCode();
//		}
	}
	
	/**
	 *  An error allowing the agent to terminate the execution of a plan.
	 */
	public static class PlanTerminated	extends	ThreadDeath 
	{
	}
	
	// todo remove me
	public static int n;
}
