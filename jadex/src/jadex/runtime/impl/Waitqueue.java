package jadex.runtime.impl;

import java.util.*;
import jadex.runtime.*;
import jadex.util.collection.SCollection;

/**
 *  A plan' waitqueue.
 */
public class Waitqueue extends WaitAbstraction
{
	//-------- attributes --------

	/** The plan. */
	protected RPlan plan;

	/** The list of collected events. */
	protected List events;

	//-------- constructors --------

	/**
	 *  Create a new Waitqueue.
	 */
	public Waitqueue(RPlan plan)
	{
		super(plan.getScope());
		this.plan = plan;
		this.events = SCollection.createArrayList();
	}

	//-------- methods --------

	/**
	 *  Add an event.
	 *  @param event The event.
	 */
	public void addEvent(IREvent event)
	{
		//System.out.println("Event added to waitqueue: "+event);
		this.events.add(event);
	}

	/**
	 *  Remove a stored event.
	 */
	public void removeEvent(IREvent event)
	{
		if(!events.remove(event))
			throw new RuntimeException("Waitqueue entry could not be deleted: "+event);
	}

	/**
	 *  Get the next event and remove it.
	 *  @return event The next event.
	 */
	public IREvent getNextEvent()
	{
		IREvent ret = null;
		if(events.size()>0)
		{
			ret= (IREvent)events.get(0);
			events.remove(0);
		}
		return ret;
	}

	/**
	 *  Get all waitqueue events.
	 *  @return All events.
	 */
	public IREvent[] getEvents()
	{
		return (IREvent[])events.toArray(new IREvent[events.size()]);
	}

	/**
	 *  Get all waitqueue events that match the filter.
	 *  Removes the matching events from the waitqueue.
	 *  @param filter The filter.
	 *  @return The suitable events.
	 */
	public IREvent[] getEvents(IFilter filter)
	{
		List tmp = SCollection.createArrayList();
		for(int i=0; i<events.size(); i++)
		{
			if(getCapability().getAgent().applyFilter(filter, events.get(i)))
				tmp.add(events.get(i));
		}
		return (IREvent[])tmp.toArray(new IREvent[tmp.size()]);
	}

	/**
	 *  Get the number of events in the waitqueue.
	 */
	public int	size()
	{
		return events.size();
	}

	/**
	 *  Test if the waitqueue is empty.
	 */
	public boolean	isEmpty()
	{
		return events.isEmpty();
	}
}
