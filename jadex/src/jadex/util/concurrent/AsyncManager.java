package jadex.util.concurrent;

import java.util.List;

import jadex.util.collection.SCollection;

/**
 *  The async manager helps decoupling calls that should be
 *  executed on a separate thread. In async mode the own manager
 *  thread executes these calls asynchronously. In sync mode
 *  calls are directly executed on the caller thread.
 *  
 *  The method to be called for sync/async execution is
 *  execute(ICommand com, Object args)  
 */
public class AsyncManager
{
	//-------- attributes --------
	
	/** Should be sent asynchronously (on a decoupled thread). */
	protected boolean async;
	
	/** The synchronized list of send requests. */
	protected List requests;
	
	/** The shutdown state. */
	protected boolean closed;
	
	/** The executor. */
	protected Executor executor;
	
	//-------- constructors --------

	/**
	 *  Create a new async manager.
	 *  @param async True, for asynchronous execute calls.
	 */
	public AsyncManager(boolean async)
	{
		setAsync(async);
	}
	
	//-------- methods --------
	
	/**
	 *  Delegation method that invokes the
	 *  command with the given args.
	 *  In async mode the call will be executed
	 *  on a the async manager thread.
	 */
	public void execute(ICommand com , Object args)
	{
		if(closed)
			throw new RuntimeException("AsyncManager already closed.");
		
		if(async)
		{
			synchronized(requests)
			{
				requests.add(new Object[]{com, args});
			}
			executor.execute();
		}
		else
		{
			com.execute(args);
		}
	}

	/**
	 *  Test in which mode the manager is running.
	 *  @return True, if in async mode.
	 */
	public boolean isAsync()
	{
		return async;
	}

	/**
	 *  Set the async mode.
	 *  @param async True, for async mode.
	 */
	public void setAsync(boolean async)
	{
		this.async = async;
		
		if(async)
		{
			requests = SCollection.createArrayList();
			executor = new Executor()
			{
				public boolean code()
				{
					synchronized(requests)
					{
						if(requests.size()>0)
						{
							Object[] req = (Object[])requests.remove(0);
							((ICommand)req[0]).execute(req[1]);
						}
						return requests.size()>0;
					}
				}
			};
		}
		else
		{
			requests = null;
			executor = null;
		}
	}
	
	/**
	 *  Shutdown the manager.
	 *  No further execute calls are
	 *  accepted via the manager. 
	 *  (Kills manager thread).
	 */
	public void shutdown()
	{
		closed = true;
		if(executor!=null)
			executor.shutdown();
		executor = null;
	}
}

