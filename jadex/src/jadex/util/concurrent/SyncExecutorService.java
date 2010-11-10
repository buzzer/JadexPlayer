package jadex.util.concurrent;

import jadex.util.collection.SCollection;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 *  The synchronous executor service that executes all tasks in one thread.
 */
public class SyncExecutorService extends AbstractExecutorService
{
	//-------- attributes --------
	
	/** The currently waiting tasks. */
	protected Set wanttorun;
	
	/** The currently running tasks. */
	protected Map running;
	
	/** The currently cancelled tasks. */
	protected Set cancelled;
	
	/** The executor. */
	protected Executor executor;
	
	/** Flag, indicating if executor is suspended. */
	protected boolean suspended;

	//-------- constructors --------
	
	/**
	 *  Create a new synchronous executor service. 
	 */
	public SyncExecutorService()
	{
		this.wanttorun = Collections.synchronizedSet(SCollection.createLinkedHashSet());
		this.running = Collections.synchronizedMap(SCollection.createLinkedHashMap());
		this.cancelled = Collections.synchronizedSet(SCollection.createLinkedHashSet());
		this.executor = new Executor(new IExecutable()
		{
			public boolean execute()
			{
				// Execute the tasks (depends on type of executor)
				IExecutable tasks[] = (IExecutable[])wanttorun.toArray(new IExecutable[wanttorun.size()]);
				for(int i=0; i<tasks.length; i++)
				{
					//System.out.println("executing: "+tasks[i]);

					running.put(tasks[i], Boolean.FALSE);
						
					boolean again = tasks[i].execute();
						
					again |= ((Boolean)running.remove(tasks[i])).booleanValue();
						
					if(!again || cancelled.contains(tasks[i]))
						wanttorun.remove(tasks[i]);
					cancelled.remove(tasks[i]);
				}
				
				// System.out.println("again: "+(wanttorun.size()>0));
				return wanttorun.size()>0;
			}
		});
	}

	//-------- methods --------
	
	/**
	 *  Execute a task in its own thread.
	 *  @param task The task to execute.
	 *  (called from arbitrary threads)
	 */
	public void doExecute(IExecutable task)
	{	
		//System.out.println("execute called: "+task);
		// Do something if it was not cancelled 
		// and (it is a new want to runner or a known one that runs)
		// as the task was added in a previous step
		
		if(!cancelled.contains(task) 
			&& (!wanttorun.contains(task) || running.containsKey(task)))
		{	
			// If task is running mark it as being rescheduled
			Boolean wtr = (Boolean)running.get(task);
			if(wtr!=null && !wtr.booleanValue())
			{
				running.put(task, Boolean.TRUE);
			}
			// Else the task is new, then add it as want to runner
			else
			{
				wanttorun.add(task);
			}
	
			// Wake up the main executor for executing tasks
			if(!suspended) 
				executor.execute();
		}
	}
	
	/**
	 *  Cancel a task. Triggers the task to
	 *  be not executed in future. 
	 *  @param task The task to execute.
	 */
	public void doCancel(IExecutable task)
	{
		// Remove from waiting tasks.
		wanttorun.remove(task);
		
		// Mark task as cancelled to avoid that the task
		// reschedules itself via calling execute()
		if(running.containsKey(task))
			cancelled.add(task);
	}
	
	/**
	 *  Suspend the task.
	 *  @param task The task.
	 */
	public void doSuspend(IExecutable task)
	{
		doCancel(task);
	}
	
	/**
	 *  Suspend all tasks. The tasks will not
	 *  be executed until resume is called. 
	 */
	public synchronized void suspend()
	{
		if(isShutdowned())
			return;
		
		suspended = true;
	}
	
	/**
	 *  Resume all tasks. Triggers the tasks to
	 *  be not executed again. 
	 */
	public synchronized void resume()
	{
		if(isShutdowned())
			return;
		
		suspended = false;
		// Wake up the main executor for executing tasks
		executor.execute();
	}
	
	/**
	 *  Shutdown the executor service.
	 */
	public void shutdown()
	{
		if(isShutdowned())
			return; 
		
		super.shutdown();
		wanttorun = null;
		running = null;
		cancelled = null;
		executor.shutdown();
	}
}
