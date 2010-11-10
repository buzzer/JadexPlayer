package jadex.model.jibximpl;

import java.util.*;
import jadex.model.*;
import jadex.util.collection.SCollection;

/**
 *  Configuration for the eventbase.
 */
public class MConfigEventbase extends MConfigBase implements IMConfigEventbase
{
	//-------- xml attributes --------

	/** The initial internal events. */
	protected ArrayList initialinternalevents;

	/** The initial message events. */
	protected ArrayList initialmessageevents;

	/** The initial internal events. */
	protected ArrayList endinternalevents;

	/** The initial message events. */
	protected ArrayList endmessageevents;

	//-------- constructors --------

	/**
	 *  Method called on first setup run.
	 *  Overwritten in elements that have
	 *  child elements.
	 *  @return The list of direct child elements.
	 */
	public List	getChildren()
	{
		List ret = super.getChildren();
		if(initialinternalevents!=null)
			ret.addAll(initialinternalevents);
		if(initialmessageevents!=null)
			ret.addAll(initialmessageevents);
		if(endinternalevents!=null)
			ret.addAll(endinternalevents);
		if(endmessageevents!=null)
			ret.addAll(endmessageevents);
		return ret;
	}

	//-------- initial internal events --------

	/**
	 *  Get all known initial internal events.
	 *  @return The initial internal events.
	 */
	public IMConfigInternalEvent[] getInitialInternalEvents()
	{
		if(initialinternalevents==null)
			return new IMConfigInternalEvent[0];
		return (IMConfigInternalEvent[])initialinternalevents
			.toArray(new IMConfigInternalEvent[initialinternalevents.size()]);
	}

	/**
	 *  Create an initial internal event.
	 *  @param ref	The name of the referenced event.
	 *  @return The initial internal event.
	 */
	public IMConfigInternalEvent	createInitialInternalEvent(String ref)
	{
		if(initialinternalevents==null)
			initialinternalevents = SCollection.createArrayList();

		MConfigInternalEvent ret = new MConfigInternalEvent();
		ret.setReference(ref);
		ret.setOwner(this);
		ret.init();
		initialinternalevents.add(ret);
		return ret;
	}

	/**
	 *  Delete an initial internal event.
	 *  @param event	The initial internal event.
	 */
	public void	deleteInitialInternalEvent(IMConfigInternalEvent event)
	{
		if(!initialinternalevents.remove(event))
			throw new RuntimeException("Initial internal event not found: "+event);
	}


	//-------- initial message events --------

	/**
	 *  Get all defined initial MessageEvents.
	 *  @return The initial MessageEvents.
	 */
	public IMConfigMessageEvent[] getInitialMessageEvents()
	{
		if(initialmessageevents==null)
			return new IMConfigMessageEvent[0];
		return (IMConfigMessageEvent[])initialmessageevents
			.toArray(new IMConfigMessageEvent[initialmessageevents.size()]);
	}

	/**
	 *  Create an initial message event.
	 *  @param ref	The name of the referenced message event.
	 *  @return The initial message event.
	 */
	public IMConfigMessageEvent	createInitialMessageEvent(String ref)
	{
		if(initialmessageevents==null)
			initialmessageevents = SCollection.createArrayList();

		MConfigMessageEvent ret = new MConfigMessageEvent();
		ret.setReference(ref);
		ret.setOwner(this);
		ret.init();
		initialmessageevents.add(ret);
		return ret;
	}

	/**
	 *  Delete an initial message event.
	 *  @param event	The initial message event.
	 */
	public void	deleteInitialMessageEvent(IMConfigMessageEvent event)
	{
		if(!initialmessageevents.remove(event))
			throw new RuntimeException("Initial internal event not found: "+event);
	}

	//-------- end internal events --------

	/**
	 *  Get all known end internal events.
	 *  @return The end internal events.
	 */
	public IMConfigInternalEvent[] getEndInternalEvents()
	{
		if(endinternalevents==null)
			return new IMConfigInternalEvent[0];
		return (IMConfigInternalEvent[])endinternalevents
			.toArray(new IMConfigInternalEvent[endinternalevents.size()]);
	}

	/**
	 *  Create an end internal event.
	 *  @param ref	The name of the referenced event.
	 *  @return The end internal event.
	 */
	public IMConfigInternalEvent	createEndInternalEvent(String ref)
	{
		if(endinternalevents==null)
			endinternalevents = SCollection.createArrayList();

		MConfigInternalEvent ret = new MConfigInternalEvent();
		ret.setReference(ref);
		ret.setOwner(this);
		ret.init();
		endinternalevents.add(ret);
		return ret;
	}

	/**
	 *  Delete an end internal event.
	 *  @param event	The end internal event.
	 */
	public void	deleteEndInternalEvent(IMConfigInternalEvent event)
	{
		if(!endinternalevents.remove(event))
			throw new RuntimeException("End internal event not found: "+event);
	}


	//-------- end message events --------

	/**
	 *  Get all defined end MessageEvents.
	 *  @return The end MessageEvents.
	 */
	public IMConfigMessageEvent[] getEndMessageEvents()
	{
		if(endmessageevents==null)
			return new IMConfigMessageEvent[0];
		return (IMConfigMessageEvent[])endmessageevents
			.toArray(new IMConfigMessageEvent[endmessageevents.size()]);
	}

	/**
	 *  Create an end message event.
	 *  @param ref	The name of the referenced message event.
	 *  @return The end message event.
	 */
	public IMConfigMessageEvent	createEndMessageEvent(String ref)
	{
		if(endmessageevents==null)
			endmessageevents = SCollection.createArrayList();

		MConfigMessageEvent ret = new MConfigMessageEvent();
		ret.setReference(ref);
		ret.setOwner(this);
		ret.init();
		endmessageevents.add(ret);
		return ret;
	}

	/**
	 *  Delete an end message event.
	 *  @param event	The end message event.
	 */
	public void	deleteEndMessageEvent(IMConfigMessageEvent event)
	{
		if(!endmessageevents.remove(event))
			throw new RuntimeException("End internal event not found: "+event);
	}

	/**
	 *  Resolve the reference to the original element.
	 *  @return The original element.
	 */
	protected IMElement findOriginalElement()
	{
		return getScope().getEventbase();
	}

	//-------- jibx related --------

	/**
	 *  Add a initialinternalevent.
	 *  @param initialinternalevent The initialinternalevent.
	 */
	public void addInitialInternalEvent(MConfigInternalEvent initialinternalevent)
	{
		if(initialinternalevents==null)
			initialinternalevents = SCollection.createArrayList();
		initialinternalevents.add(initialinternalevent);
	}

	/**
	 *  Get an iterator for all initialinternalevents.
	 *  @return The iterator.
	 */
	public Iterator iterInitialInternalEvents()
	{
		return initialinternalevents==null? Collections.EMPTY_LIST.iterator(): initialinternalevents.iterator();
	}

	/**
	 *  Add a initialmessageevent.
	 *  @param initialmessageevent The initialmessageevent.
	 */
	public void addInitialMessageEvent(MConfigMessageEvent initialmessageevent)
	{
		if(initialmessageevents==null)
			initialmessageevents = SCollection.createArrayList();
		initialmessageevents.add(initialmessageevent);
	}

	/**
	 *  Get an iterator for all initialmessageevents.
	 *  @return The iterator.
	 */
	public Iterator iterInitialMessageEvents()
	{
		return initialmessageevents==null? Collections.EMPTY_LIST.iterator(): initialmessageevents.iterator();
	}

	/**
	 *  Add a endinternalevent.
	 *  @param endinternalevent The endinternalevent.
	 */
	public void addEndInternalEvent(MConfigInternalEvent endinternalevent)
	{
		if(endinternalevents==null)
			endinternalevents = SCollection.createArrayList();
		endinternalevents.add(endinternalevent);
	}

	/**
	 *  Get an iterator for all endinternalevents.
	 *  @return The iterator.
	 */
	public Iterator iterEndInternalEvents()
	{
		return endinternalevents==null? Collections.EMPTY_LIST.iterator(): endinternalevents.iterator();
	}

	/**
	 *  Add a endmessageevent.
	 *  @param endmessageevent The endmessageevent.
	 */
	public void addEndMessageEvent(MConfigMessageEvent endmessageevent)
	{
		if(endmessageevents==null)
			endmessageevents = SCollection.createArrayList();
		endmessageevents.add(endmessageevent);
	}

	/**
	 *  Get an iterator for all endmessageevents.
	 *  @return The iterator.
	 */
	public Iterator iterEndMessageEvents()
	{
		return endmessageevents==null? Collections.EMPTY_LIST.iterator(): endmessageevents.iterator();
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MConfigEventbase clone = (MConfigEventbase)cl;
		if(initialinternalevents!=null)
		{
			clone.initialinternalevents = SCollection.createArrayList();
			for(int i=0; i<initialinternalevents.size(); i++)
				clone.initialinternalevents.add(((MElement)initialinternalevents.get(i)).clone());
		}
		if(initialmessageevents!=null)
		{
			clone.initialmessageevents = SCollection.createArrayList();
			for(int i=0; i<initialmessageevents.size(); i++)
				clone.initialmessageevents.add(((MElement)initialmessageevents.get(i)).clone());
		}
		if(endinternalevents!=null)
		{
			clone.endinternalevents = SCollection.createArrayList();
			for(int i=0; i<endinternalevents.size(); i++)
				clone.endinternalevents.add(((MElement)endinternalevents.get(i)).clone());
		}
		if(endmessageevents!=null)
		{
			clone.endmessageevents = SCollection.createArrayList();
			for(int i=0; i<endmessageevents.size(); i++)
				clone.endmessageevents.add(((MElement)endmessageevents.get(i)).clone());
		}
	}
}
