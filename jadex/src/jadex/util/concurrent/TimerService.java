package jadex.util.concurrent;

import jadex.util.collection.SCollection;

import java.util.Collections;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *  Active timing object for standalone platform.
 *  Calls timed object as requested.
 */
public class TimerService	extends Executor implements ITimerService
{
	//-------- attributes --------

	/** The timetable entries (mapped by timed object). */
	protected Map	entries;

	/** The timetable (entries ordered by timepoint). */
	protected SortedSet	timetable;

	//-------- constructors --------

	/**
	 *  Create a new timer.  
	 */
	public TimerService()
	{
		// Use two data structures to store timing entries:
		// Map for storing the (single) timing entry for each timed object.
		entries	= SCollection.createHashMap();
		// Sorted set for all entries ordered by due time.
		timetable	= Collections.synchronizedSortedSet(new TreeSet());
	}
	
	//-------- methods --------

	/**
	 *  Add a timing entry.
	 *  @param to	The timed object to notify.
	 *  @param time	The absolute wakeup time.
	 */
	public synchronized void	addEntry(ITimedObject to, long time)
	{
//		if(to==null)
//			throw new RuntimeException("to is null");
//		assert to!=null;

		// Remove old entry, if any.
		Entry	entry	= (Entry)entries.get(to);
		if(entry!=null)
		{
			// Update entry (remove and re-add, to keep list sorted).
			timetable.remove(entry);
			entry.time	= time;
			timetable.add(entry);
		}
		else
		{
			// Create and add new entry.
			entry	= new Entry(to, time);
			entries.put(to, entry);
			timetable.add(entry);
		}

		// Execute or notify timing thread.
		execute();
//		if(entry.toString().indexOf("Patient")!=-1)
//			System.out.println("adding: "+entry);
	}
	
	/**
	 *  Remove a timing entry.
	 *  @param to	The object to notify.
	 */
	public synchronized void	removeEntry(ITimedObject to)
	{
		// Remove old entry, if any.
		Entry	entry	= (Entry)entries.remove(to);
		if(entry!=null)
			timetable.remove(entry);

//		if(entry!=null && entry.toString().indexOf("Patient")!=-1)
//			System.out.println("removing: "+entry+", "+to);

		// No need to resume execution (timer checks entries whenever it wakes up). 
	}

	//-------- executor extensions --------
	
	/**
	 *  The code to run on the timer thread.
	 */
	public boolean	code()
	{
		Entry	next;
		long	diff;

		// Getting entry and waiting has to be synchronized
		// to avoid new (earlier) entries being added in between.
		synchronized(this)
		{
			// Exit thread when timetable is empty.
			if(timetable.isEmpty())
				return false;

			//System.out.println("timer: "+timetable);

			// Get next entry from timetable.
			next	= (Entry)timetable.first();
			diff	= 	next.time - System.currentTimeMillis();

			// Wait until next entry is due.
			if(diff>0)
			{
				try
				{
					//System.out.println("timer waiting for "+diff+" millis");
					this.wait(diff);
					//System.out.println("timer awake");
				}
				catch(InterruptedException e){}
			}
		}

		// Handle due entry (must not be synchronized to avoid
		// deadlock when timed object concurrently accesses timetable).
		// Problem: Timed object may concurrently remove/change its entry
		// (timed object is notified anyways, and too much notifications don't hurt?)
		if(diff<=0)
		{
			// It is important to remove the entry before calling the notifyDue() method,
			// as from this method a new timing entry might be added,
			// which will otherwise be removed aftwerwards.
			removeEntry(next.to);

			// Now notify the agent.
			if(next==null)
				throw new RuntimeException("next is null");
			else if(next.to==null)
				throw new RuntimeException("next.to is null");
			next.to.timeEventOccurred();
			//System.out.println("notified: "+next);
		}

		return !timetable.isEmpty();
	}

	/**
	 *  Make sure a thread is executing the code.
	 *  Notify thread to resume execution when entry is added
	 *  while thread is waiting.
	 */
	public synchronized void	execute()
	{
		super.execute();

		// Notify thread to possibly interrupt current waiting thread.
		this.notify();
	}

	/**
	 *  Shutdown the executor.
	 */
	public synchronized void	shutdown()
	{
		super.shutdown();

		// Notify thread.
		this.notify();
	}

	/**
	 *  Execute the code. 
	 * 
	// Hack!!! For debugging purposes do not free thread.
	public void run()
	{
		while(true)
		{
			while(code());

			try
			{
				Thread.currentThread().sleep(100);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}*/

	//-------- helper classes --------

	/**
	 *  Timetable data container.
	 */
	static class Entry	implements Comparable
	{
		//-------- attributes --------
	
		/** The timed object to notify. */
		protected ITimedObject	to;
	
		/** The requested wakeup time. */
		protected long	time;
	
		//-------- constructor --------
	
		/**
		 *  Create a new timetable entry.
		 *  @param agent	The agent to notify.
		 *  @param time	The absolute wakeup time.
		 */
		public Entry(ITimedObject to, long time)
		{
			this.to = to;
//			if(to==null)
//				throw new RuntimeException("to is null");
			this.time	= time;
		}
	
		//-------- methods --------
	
		/**
		 *  Compare this object to another.
		 *  This method is rather complex, because
		 *  the TreeSet IGNORES compareTo()==0
		 *  objects when inserting. Therefore objects
		 *  with the same wakeup time must be distinguished
		 *  by the hashcode to have a consistent timetable.
		 *  @param o The object to compare.
		 */
		public int compareTo(Object o)
		{
			Entry entry	= (Entry)o;
			int ret	= (int)(this.time - entry.time);
			if(ret==0 && entry!=this)
			{
				ret	= this.hashCode() - entry.hashCode();
			}
			return ret;
		}

		/**
		 *  Method implemented for easy debugging.
		 */
		public String	toString()
		{
			return "Timer.Entry("+to+", "+time+")";
		}
	}
}
