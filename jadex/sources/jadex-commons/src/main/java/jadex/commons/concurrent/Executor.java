package jadex.commons.concurrent;

import jadex.commons.Future;
import jadex.commons.IFuture;

import java.util.ArrayList;
import java.util.List;


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
	
	/** Flag indicating that the executor has shutdowned. */
	protected boolean	shutdowned;
	
	/** The thread pool. */
	protected IThreadPool threadpool;
	
	/** The executable. */
	protected IExecutable executable;
	
	/** The shutdown futures. */
	protected List shutdownfutures;
		
	//--------- constructors --------

	/**
	 *  Create an executor object.
	 *  Constructor for subclasses overriding
	 *  the code() method.
	 */
	public Executor(IThreadPool threadpool)
	{
		this(threadpool, null);
	}
	
	/**
	 *  Create an executor object.
	 */
	public Executor(IThreadPool threadpool, IExecutable executable)
	{
		if(threadpool==null)
			throw new IllegalArgumentException("Threadpool must not null.");
		
		this.threadpool = threadpool;
		this.executable = executable;
		this.shutdownfutures = new ArrayList();
	}
		
	//-------- methods --------
	
	/**
	 *  Execute the code. 
	 */
	public void run()
	{
		// running is already set to true in execute()
		
		boolean	iwanttorun	= true;
		while(iwanttorun && !shutdown)
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

		// Notify shutdown listeners when execution has ended.
		Future[] futures = null;
		synchronized(this)
		{
			if(shutdown)
			{
				futures = (Future[])shutdownfutures.toArray(new Future[shutdownfutures.size()]);
				shutdownfutures.clear();
			}
		}
		if(futures!=null)
		{
			for(int i=0; i<futures.length; i++)
				futures[i].setResult(null);
			
			synchronized(this)
			{
				shutdowned = true;
			}
		}
		
		//System.out.println("exited: "+this);
	}

	/**
	 *  Make sure a thread is executing the code.
	 */
	public void execute()
	{
		boolean	execute	= false;
		
		synchronized(this)
		{
//			System.out.println("executing: "+this+" "+running);
//			Thread.dumpStack();
			if(!shutdown)
			{		
				// Indicate that thread should continue to run (if running).
				wanttorun	= true;
				
				if(!running)
				{
					running	= true;
					// Invoke the code of the executor object using the thread pool,
					// which allows thread to be shared, when code is idle.
					execute	= true;
				}
			}
		}

		if(execute)
			threadpool.execute(this);
	}

	/**
	 *  Shutdown the executor.
	 */
	public IFuture	shutdown()
	{
		Future	ret	= new Future();
		
		boolean directnotify = false;
		synchronized(this)
		{
			shutdown = true;
			if(!shutdowned)
			{
				shutdownfutures.add(ret);
			}
			else
			{
				directnotify = true;
			}
		}
		
		if(directnotify)
			ret.setResult(null);
		
		return ret;
	}
	
	/**
	 *  Set the executable.
	 *  @param executable The executable.
	 */
	public void setExecutable(IExecutable executable)
	{
//		if(this.executable!=null)
//			throw new RuntimeException("Setting executable allowed only once.");
		this.executable = executable;
	}
	
	/**
	 *  The code to be run.
	 *  @return True, when execution should continue.
	 */
	protected boolean code()
	{
		return executable.execute();
	}

	/**
	 *  Check if the executor is running.
	 *  Should only be called, when access to executor is
	 *  correctly synchronized, otherwise inconsistent values might be returned.
	 */
	protected boolean isRunning()
	{
		return running;
	}
	
	/**
	 *  Return true, if already shutowned.
	 *  @return True, if shutdowned.
	 * /
	protected boolean isShutdowned()
	{
		return shutdowned;
	}*/

	/**
	 *  Create a string representation of this executor.
	 * /
	public String	toString()
	{
		return "Executor("+(executable!=null?executable.toString():super.toString())+")";
	}*/
}