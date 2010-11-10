package jadex.runtime.externalaccesswrapper;

import jadex.model.ISystemEventTypes;
import jadex.runtime.*;

/**
 *  Synchronous listener for agent events, i.e. ProcessEventActions being executed.
 *  The filter can be used to match a specific event. Hence one can wait
 *  for the occurrence of some event, e.g. a goal info event of a goal.
 */
public class SynchronousSystemEventListener implements ISystemEventListener
{
	//-------- attributes --------
	
	/** The system event. */
	protected SystemEvent event;

	/** Flag indicating the agent has died. */
	protected boolean	died;

	/** Flag indicating the listener was notified. */
	protected boolean	notified;

	//-------- methods --------

	/**
	 *  Get the event.
	 *  @return The event.
	 */
	public SystemEvent getEvent()
	{
		return event;
	}

	/**
	 *  Check, if the agent has died while waiting for the event.
	 */
	public boolean	isAgentDied()
	{
		return died;
	}
	
	/**
	 *  Check if the listener was already notified.
	 */
	public boolean	isNotified()
	{
		return notified;
	}

	/**
	 *  Notify that system events occured.
	 *  @see jadex.runtime.SystemEvent
	 */
	public void systemEventsOccurred(SystemEvent[] events)
	{
		for(int i=0; i<events.length; i++)
		{
			if(events[i].getType().equals(ISystemEventTypes.AGENT_DIED))
			{
				// ...wake up the external thread.
				synchronized(this)
				{
					this.died	= true;
					this.notify();
					this.notified	= true;
				}
			}
			else
			{
				// ...wake up the external thread.
				synchronized(this)
				{
					this.event = events[i];
					this.notify();
					this.notified	= true;
					//System.out.println("event: "+this.event.getName());
				}
			}
		}
	}
}
