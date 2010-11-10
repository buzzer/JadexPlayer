package jadex.runtime.impl;

import jadex.runtime.impl.agenda.IAgendaAction;

/**
 *  Timetable data container.
 */
public class TimetableData	implements Comparable, java.io.Serializable
{
	//-------- attributes --------

	/** The entry time. */
	private long	entrytime;

	/** The duration. */
	private long	duration;

	/** The wakeup timepoint. */
	private long	wakeup;

	/** The agenda entry. */
	private IAgendaAction agendaaction;

	//-------- constructor --------

	/**
	 *  Create a new TimtableData object.
	 *  @param duration The duration to wait.
	 *  @param agendaaction The agenda entry.
	 */
	public TimetableData(long duration, IAgendaAction agendaaction)
	{
		this.entrytime	= System.currentTimeMillis();
		this.duration	= duration;
		this.wakeup	= entrytime+duration;
		this.agendaaction = agendaaction;
	}

	//-------- methods --------

	/**
	 *  Get the entry time.
	 *  @return The entry time.
	 */
	public long getEntrytime()
	{
		return entrytime;
	}

	/**
	 *  Get the duration.
	 *  @return The duration.
	 */
	public long getDuration()
	{
		return duration;
	}

	/**
	 *  Get the wake up time.
	 *  @return The wake up time.
	 */
	public long getWakeup()
	{
		return wakeup;
	}

	/**
	 *  Get the agenda action.
	 *  @return The agenda action.
	 */
	public IAgendaAction getAgendaAction()
	{
		return agendaaction;
	}

	/**
	 *  Compare this object to another.
	 *  This method is rather complex, because
	 *  the TreeSet IGNORES compareTo()=0
	 *  objects when inserting. Therefore objects
	 *  with the same wakeup time must be distinguished
	 *  by the hashcode to have consistent planinstance.
	 *  @param o The object to compare.
	 */
	public int compareTo(Object o)
	{
		TimetableData tt	= (TimetableData)o;
				
		int	ret;
		long diff = this.wakeup-tt.wakeup;
		if(diff==0 && this!=o)
		{
			if(this.hashCode()>o.hashCode())
			{
				ret = 1;
			}
			else
			{
				ret = -1;
			}
		}
		else if(diff>0)
		{
			ret	= 1;
		}
		else
		{
			ret	= -1;
		}
		return ret;
	}

	/**
	 *  Get the string representation.
	 */
	public String toString()
	{
		return "Due in: "+(wakeup-System.currentTimeMillis())+" "+agendaaction;
	}
}

