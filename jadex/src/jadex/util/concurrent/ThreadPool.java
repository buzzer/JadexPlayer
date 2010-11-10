package jadex.util.concurrent;

import jadex.util.collection.ArrayBlockingQueue;
import jadex.util.collection.IBlockingQueue;
import jadex.util.DynamicURLClassLoader;
import jadex.util.SReflect;

import java.util.*;

/**
 *  A thread pool manages pool and saves resources
 *  and time by precreating and reusing pool.
 */
public class ThreadPool implements IThreadPool, Runnable
{
	//-------- constants --------
	
	/** Timeout in milliseconds before an idle thread is garbage collected. */
//	protected static final long	THREAD_TIMEOUT	= 60000;
	protected static final long	THREAD_TIMEOUT	= -1;
	
	/** The thread number. */
	protected static int threadcnt = 0;

	//-------- attributes --------

	/** The min number of pool. */
	protected int	min;

	/** The max number of pool. */
	protected int	max;

	/** The pool of service threads. */
	protected List	pool;

	/** The tasks to execute. */
	// Todo: fix blocking queue.
	protected IBlockingQueue	tasks;

	/** The running flag. */
	protected boolean	running;

	/** The capacity of idle threads, that can execute a task. */
	protected int	capacity;

	/** The task - thread mapping. */
	protected Map	threads;

	//-------- constructors --------

	/**
	 *  Default constructor, called by the factory.
	 */
	public ThreadPool()
	{
		// min=10, max=unlimited.
		this(10, -1);

		// Todo: enable better thread recycling.
//		this(1, 3);
	}
	
	/**
	 *  Create a new thread pool.
	 *  @param min The min (and initial) number of pool.
	 *  @param max The max number of pool (-1 for unlimited).
	 */
	private ThreadPool(int min, int max)
	{
		this.min = min;
		this.max = max;
		this.running = true;
		this.capacity	= 0;
		this.tasks	= new ArrayBlockingQueue();
		this.pool = new Vector();
		this.threads = new Hashtable();
        addThreads(min);
        
        // Observer for testing!
//		Thread observer = new Thread(this);
//		observer.start();
	}

	//-------- methods --------

	/**
	 *  Execute a task in its own thread.
	 *  @param task The task to execute.
	 */
	public synchronized void execute(Runnable task)
	{
		//System.out.println("Execute: "+task);
		if(!running)
			throw new RuntimeException("Thread pool not running: "+this);
		capacity--;
        if(capacity<0 && (max==-1 || pool.size()<max))
		{
			addThreads(1);
//			System.out.println("Added, now: "+pool.size());
		}
//        else
//        {
//        	System.out.println("reusing threads: "+pool.size());
//        }
		this.tasks.enqueue(task);
	}

	/**
	 *  Shutdown the task pool
	 */
	public void dispose()
	{
		this.running = false;
		this.tasks.setClosed(true);
		Thread.yield();
		for(int i=0; i<pool.size(); i++) // Hack!!! Kill all threads.
		{
			Thread t = (Thread)pool.get(i);
			t.stop();
		}
	}

	static int cnt = 0;
	/**
	 *  Main for testing.
	 *  @param args The arguments.
	 */
	public static void main(String[] args)
	{
		ThreadPool tp	= new ThreadPool(2, 5);
		for(int i=0; i<20; i++)
		{
			tp.execute(new Runnable()
			{
				int n = cnt++;
				public void run()
				{
					String t = Thread.currentThread().toString();
					System.out.println("a_"+this+" : "+t);
					try{Thread.sleep(1000);}
					catch(InterruptedException e){}
					System.out.println("b_"+this+" : "+t);
				}

				public String toString()
				{
					return "Task_"+n;
				}
			});

			//Thread.currentThread().yield();
		}
		/*try{Thread.currentThread().sleep(2000);}
		catch(Exception e){}
		tp.dispose();*/
	}

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(SReflect.getInnerClassName(getClass()));
		buf.append("(min=");
		buf.append(min);
		buf.append(" max=");
		buf.append(max);
		buf.append(", poolsize=");
		buf.append(pool.size());
		buf.append(", running=");
		buf.append(running);
		buf.append(")");
		return buf.toString();
	}

	//-------- helper methods --------

	/**
	 *  Create some pool.
	 *  @param num The number of pool.
	 */
	protected void addThreads(int num)
	{
		capacity += num;
		//System.out.println("Cap+1(add): "+capacity);
		for(int i=0; i<num; i++)
		{
			Thread thread = new ServiceThread();
			pool.add(thread);
			thread.start();
		}
	}

	/**
	 *  Get a thread for a task.
	 */
	public Thread getThread(Runnable task)
	{
		return (Thread)threads.get(task);
	}

	/**
	 *  The task for a given thread.
	 */
	public Runnable getTask(Thread thread)
	{
		Runnable	ret	= null;
		if(thread instanceof ServiceThread)
		{
			ret	= ((ServiceThread)thread).getTask();
		}
		return ret;
	}

	/**
	 *  The observer thread.
	 */
	public void run()
	{
		int	longrunning	= 10000;
		while(true)
		{
			try{Thread.sleep(longrunning);}
			catch(InterruptedException e){}
			ArrayList longrunners = new ArrayList();
			int	cnt	= 0;
			for(int i=0; i<pool.size(); i++)
			{
				ServiceThread st = (ServiceThread)pool.get(i);
				long time = st.getTaskExecutionTime();
				if(time>(int)(longrunning*0.9))
				{
					// Add output string directly, as printing causes context switches leading to outdated data.
					longrunners.add((++cnt)+". ("+time/1000+"s): "+st.getName());
				}
			}
			System.out.println("\n-----------------------------------");
			System.out.println("Threadpool longrunners: ");
			for(int i=0; i<longrunners.size(); i++)
			{
				System.out.println(longrunners.get(i));
			}
			System.out.println("-----------------------------------\n");
		}
	}

	//-------- inner classes --------

	/**
	 *  A service thread executes tasks.
	 */
	public class ServiceThread extends Thread
	{
		//-------- attributes --------

		/** The actual task. */
		protected Runnable task;

		/** The start time. */
		protected long start;

		//-------- constructors --------

		/**
		 *  Create a new thread.
		 */
		public ServiceThread()
		{
            super("ServiceThread_"+(++threadcnt));
            // Use Jadex class loader as default (hack???).
			// Hack!!! Might throw exception in applet / webstart.
            try
            {
            	this.setContextClassLoader(DynamicURLClassLoader.getInstance());
            }
            catch(SecurityException e)
            {
            	// No problem: in applets / webstart the dynamic class loader
            	// isn't used, so the standard one will do.
            }
		}

		//-------- methods --------

		/**
		 *  Dequeue an element from the queue
		 *  and execute it.
		 */
		public void run()
		{
//			for(int reuse=0; ; reuse++)
			while(running)
			{
//				System.out.println(this+" reused "+reuse+" times. "
//					+ "Pool size is "+pool.size()+"."
//					+ "Capacity is "+capacity+".");
				try
				{
					this.task = ((Runnable)tasks.dequeue(THREAD_TIMEOUT));
					threads.put(task, this);
					this.start = System.currentTimeMillis();
					this.setName(task.toString());

					try
					{
						this.task.run();
					}
					catch(ThreadDeath e){}
					capacity++;
					//{System.out.println("Thread terminated (interrupted): "+this);}
				}
				catch(IBlockingQueue.ClosedException e){}
				catch(IBlockingQueue.TimeoutException e)
				{
					if(capacity>min)
					{
						capacity--;
						System.out.println("Thread timeout (queue): "+this);
						break;
					}
				}
				finally
				{
					if(task!=null)
					{
						threads.remove(task);
						this.task = null;
					}
				}
			}
			pool.remove(this);
		}

		/**
		 *  Get the runnable (the task).
		 *  @return The runnable.
		 */
		public Runnable getTask()
		{
			return this.task;
		}

		/**
		 *  Get the task execution time.
		 */
		public long getTaskExecutionTime()
		{
			long ret = 0;
			if(task!=null)
				ret = System.currentTimeMillis()-start;
			return ret;
		}
	}
}