package jadex.util.concurrent;

/**
 *  Common interface for different executor services.
 */
public interface IExecutorService
{
	/**
	 *  Execute a task. Triggers the task to
	 *  be executed in future. 
	 *  @param task The task to execute.
	 */
	public void execute(IExecutable task);
	
	/**
	 *  Cancel a task. Triggers the task to
	 *  be not executed in future. 
	 *  @param task The task to execute.
	 */
	public void cancel(IExecutable task);

	/**
	 *  Suspend a task. The task will not
	 *  be executed until resume is called. 
	 *  @param task The task to execute.
	 */
	public void suspend(IExecutable task);
	
	/**
	 *  Resume a task. Triggers the task to
	 *  be not executed again. 
	 *  @param task The task to execute.
	 */
	public void resume(IExecutable task);
	
	/**
	 *  Suspend the service (all tasks).
	 */
	public void suspend();
	
	/**
	 *  Resume the service (all tasks).  
	 */
	public void resume();
	
	/**
	 *  Shutdown the executor service.
	 */
	public void shutdown();
}
