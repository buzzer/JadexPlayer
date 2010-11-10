package jadex.util.concurrent;

import jadex.util.collection.SCollection;

import java.util.Collections;
import java.util.Map;

/**
 *  The asynchronous executor service that executes all tasks in separate executors.
 */
public class AsyncExecutorService extends AbstractExecutorService
{
	//-------- attributes --------
	
	/** The currently waiting tasks. */
	protected Map executors;
	
	/** The closed flag. */
	protected boolean shutdown;
	
	//-------- constructors --------
	
	/**
	 *  Create a new asynchronous executor service. 
	 */
	public AsyncExecutorService()
	{
		this.executors = Collections.synchronizedMap(SCollection.createHashMap());
	}

	//-------- methods --------
	
	/**
	 *  Execute a task in its own thread.
	 *  @param task The task to execute.
	 *  (called from arbitrary threads)
	 */
	public void doExecute(final IExecutable task)
	{	
		//System.out.println("execute called: "+task);
		
		Executor exe = (Executor)executors.get(task);

		if(exe==null)
		{
			exe = new Executor()
			{
				protected boolean code()
				{
					return task.execute();
				}
				
				public String	toString()
				{
					return "AsyncExecutable("+task.toString()+")";
				}
				
			};
			executors.put(task, exe);
		}
		
		exe.execute();
	}
	
	/**
	 *  Cancel a task. Triggers the task to
	 *  be not executed in future. 
	 *  @param task The task to execute.
	 */
	public void doCancel(IExecutable task)
	{
		Executor exe = (Executor)executors.get(task);
		if(exe!=null)
		{
			exe.shutdown();
			executors.remove(task);
		}
	}
	
	/**
	 *  Suspend all tasks. The tasks will not
	 *  be executed until resume is called. 
	 */
	public synchronized void suspend()
	{
		if(isShutdowned())
			return;
		
		IExecutable[] keys = (IExecutable[])executors.keySet()
			.toArray(new IExecutable[executors.size()]);
		for(int i=0; i<keys.length; i++)
			suspend(keys[i]);
	}
	
	/**
	 *  Resume all tasks. Triggers the tasks to
	 *  be not executed again. 
	 */
	public synchronized void resume()
	{
		if(isShutdowned())
			return;
		
		IExecutable[] keys = (IExecutable[])executors.keySet()
			.toArray(new IExecutable[executors.size()]);
		for(int i=0; i<keys.length; i++)
			resume(keys[i]);
	}
	
	/**
	 *  Suspend the task.
	 */
	public void doSuspend(IExecutable task)
	{
		Executor exe = (Executor)executors.get(task);
		if(exe!=null)
			exe.suspend();
	}
	
	/**
	 *  Resume the task. 
	 */
	public void doResume(IExecutable task)
	{
		Executor exe = (Executor)executors.get(task);
		if(exe!=null)
			exe.resume();
	}

	/**
	 *  Shutdown the executor service.
	 */
	public synchronized void shutdown()
	{
		if(isShutdowned())
			return;
		
		shutdown = true;
		
		IExecutable[] keys = (IExecutable[])executors.keySet()
			.toArray(new IExecutable[executors.size()]);
		for(int i=0; i<keys.length; i++)
		{
			Executor exe = (Executor)executors.get(keys[i]);
			if(exe!=null)
				exe.shutdown();
		}
		executors = null;
	}
}
