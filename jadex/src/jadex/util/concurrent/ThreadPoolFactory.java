package jadex.util.concurrent;

/**
 *  Factory class for obtaining a thread pool.
 */
public class ThreadPoolFactory
{
	//-------- constants --------
	
	/** The standard (1.4 compliant) thread pool implementation. */
	public static final String	THREADPOOL_STANDARD	= "jadex.util.concurrent.ThreadPool";

	/** The java 5.0 thread pool implementation. */
	public static final String	THREADPOOL_JAVA5	= "jadex.util.concurrent.java5.JavaThreadPool";

	//-------- attributes --------
	
	/** The thread pool instance. */
	protected static IThreadPool	instance;
	
	//-------- methods --------
	
	/**
	 *  Get the thread pool.
	 *  @return The thread pool.
	 */
	public static synchronized IThreadPool	getThreadPool()
	{
		if(instance==null)
		{
			try
			{
//				try
//				{
//					instance	= (IThreadPool)Class.forName(THREADPOOL_JAVA5).newInstance();
//				}
//				catch(Throwable e)
//				{
					instance	= (IThreadPool)Class.forName(THREADPOOL_STANDARD).newInstance();
//				}
			}
			catch(Exception e)
			{
				throw new RuntimeException("Could not create thread pool.");
			}
		}
		return instance;
	}
}
