package jadex.util.concurrent;

import jadex.util.collection.SCollection;

import java.util.Collections;
import java.util.Set;

/**
 *  The abstract executor service provides the generic functionality
 *  for suspending and resuming tasks.
 */
public abstract class AbstractExecutorService implements IExecutorService
{
	//-------- attributes --------
	
	/** The suspended tasks. */
	protected Set suspended;
	
	/** The shutdown flag. */
	protected boolean shutdown;
	
	//-------- constructors --------
	
	/**
	 *  Create a new abstract executor service.
	 */
	public AbstractExecutorService()
	{
		this.suspended = Collections.synchronizedSet(SCollection.createHashSet());
	}

	//-------- methods --------
	
	/**
	 *  Execute a task. Triggers the task to
	 *  be executed in future. 
	 *  @param task The task to execute.
	 */
	public void execute(IExecutable task)
	{
		if(isShutdowned())
			return;
		
		if(!suspended.contains(task))
			doExecute(task);
	}
	
	/**
	 *  Cancel a task. Triggers the task to
	 *  be not executed in future. 
	 *  @param task The task to execute.
	 */
	public void cancel(IExecutable task)
	{
		if(isShutdowned())
			return;
		
		suspended.remove(task);
		doCancel(task);
	}
	
	/**
	 *  Suspend a task. The task will not
	 *  be executed until resume is called. 
	 *  @param task The task to execute.
	 */
	public void suspend(IExecutable task)
	{
		if(isShutdowned())
			return;
		
		if(!suspended.contains(task))
		{
			suspended.add(task);
			doSuspend(task);
		}
	}
	
	/**
	 *  Resume a task. Triggers the task to
	 *  be not executed again. 
	 *  @param task The task to execute.
	 */
	public void resume(IExecutable task)
	{
		if(isShutdowned())
			return;
		
		if(suspended.remove(task))
		{
			doResume(task);
			execute(task);
		}
	}

	/**
	 *  Suspend the task.
	 */
	public void doSuspend(IExecutable task)
	{
	}
	
	/**
	 *  Resume the task. 
	 */
	public void doResume(IExecutable task)
	{
	}
	
	/**
	 *  Test if a task is suspended.
	 *  @return True, if suspended.
	 */
	public boolean isSuspended(IExecutable task)
	{
		return suspended.contains(task);
	}
	
	/**
	 *  Shutdown the executor service.
	 */
	public void shutdown()
	{
		this.shutdown = true;
	}
	
	/**
	 *  Test if service is shutdowned.
	 *  @return True, if shutdowned.
	 */
	public boolean isShutdowned()
	{
		return this.shutdown;
	}
	
	/**
	 *  Verify that service is running.
	 * /
	public void verifyRunning()
	{
		if(isShutdowned())
			throw new RuntimeExecution("Service already shutdowned: "+this);
	}*/
	
	/**
	 *  Cancel the task execution.
	 */
	public abstract void doCancel(IExecutable task);
	
	/**
	 *  Execute the task as long as it indicates
	 *  the execution should continue.
	 *  @param task The task to execute.
	 */
	public abstract void doExecute(IExecutable task);
}
