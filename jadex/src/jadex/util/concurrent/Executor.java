package jadex.util.concurrent;

import jadex.util.concurrent.ThreadPoolFactory;


/**
 *  A helper class for running a single instance
 *  of code using the thread pool.
 *  The code to be executed has to be placed in the
 *  code() method.
 *  Once created, the execute() method may be called
 *  as often as desired. When no thread is currently
 *  executing the object, a new thread is used from the
 *  thread pool. Otherwise, the already existing thread
 *  continues execution.
 *  After shutdown() is called, the executor stops
 *  execution, even when execute() is called afterwards.
 */
public class Executor implements Runnable
{
	//-------- attributes --------

	/** Flag indicating if the thread is running. */
	protected boolean	running;

	/** Flag indicating if the thread wants to run. */
	protected boolean	wanttorun;

	/** Flag indicating that the executor shuts down. */
	protected boolean	shutdown;
	
	/** Flag indicating that the executor is suspended. */
	protected boolean	suspended;

	/** The executable. */
	protected IExecutable executable;
	
	//--------- constructors --------

	/**
	 *  Create an executor object.
	 */
	public Executor()
	{
	}
	
	/**
	 *  Create an executor object.
	 */
	public Executor(IExecutable executable)
	{
		this.executable = executable;
	}
		
	//-------- methods --------

	/**
	 *  Execute the code. 
	 */
	public void run()
	{
		//this.thread	= thread.currentThread();
		boolean	iwanttorun	= true;
		while(iwanttorun && !shutdown && !suspended)
		{
			iwanttorun	=	code();

			// Setting flags in synchronized block assures,
			// that execute is not called in between.
			// Separating running and myrunning allows that this thread
			// may terminate (myrunning==false) while a new thread
			// is already starting (running==true).
			synchronized(this)
			{
				//if(iwanttorun)	System.out.println("continuing: "+this);
				//else if(wanttorun)	System.out.println("forced to continue: "+this);
				iwanttorun	= iwanttorun || wanttorun;
				running	= iwanttorun;
				wanttorun	= false;	// reset until execute() is called again.
			}
			
//			try
//			{
//				Thread.sleep(10);
//			}
//			catch(InterruptedException e)
//			{
//				throw new RuntimeException(e);
//			}
		}
		running = false;
		//this.thread	= null;
		//System.out.println("exited: "+this);
	}

	/**
	 *  Make sure a thread is executing the code.
	 */
	public synchronized void	execute()
	{
		//System.out.println("executing: "+this+" "+running);
		// Indicate that thread should continue to run (if running).
		wanttorun	= true;

		if(!running && !shutdown)
		{
			running	= true;
			// Invoke the code of the executor object using the thread pool,
			// which allows thread to be shared, when code is idle.
			ThreadPoolFactory.getThreadPool().execute(this);
		}
	}
	
	/**
	 *  Suspend a task. The task will not
	 *  be executed until resume is called. 
	 */
	public void suspend()
	{
		suspended = true;
	}
	
	/**
	 *  Resume a task. Triggers the task to
	 *  be not executed again. 
	 */
	public void resume()
	{
		suspended = false;
		execute();
	}

	/**
	 *  Shutdown the executor.
	 */
	public void	shutdown()
	{
		shutdown	= true;
	}
	
	/**
	 *  The code to be run.
	 *  @return True, when execution should continue.
	 */
	protected boolean	code()
	{
		return executable.execute();
	}
	
}