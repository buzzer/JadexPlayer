package jadex.model.jibximpl;

import java.util.*;
import jadex.model.*;
import jadex.util.*;
import jadex.util.collection.SCollection;

/**
 *  The event container.
 */
public class MEventbase extends MBase implements IMEventbase
{
	//-------- xml attributes --------

	/** The internal events. */
	protected ArrayList internalevents;

	/** The message events. */
	protected ArrayList messageevents;

	/** The goal events. */
	protected ArrayList goalevents;

	/** The internal event references. */
	protected ArrayList internaleventrefs;

	/** The message event references. */
	protected ArrayList messageeventrefs;

	/** The goal event references. */
	protected ArrayList goaleventrefs;

	//-------- constructors --------

	/**
	 *  The setup is called by a configuration code object.
	 */
	protected void init()
	{
		super.init();

		// create standard events.

		createGoalEvent(STANDARD_GOAL_EVENT, IMReferenceableElement.EXPORTED_FALSE);

		IMInternalEvent stdintevent = createInternalEvent(LEGACY_INTERNAL_EVENT, IMReferenceableElement.EXPORTED_FALSE);
		stdintevent.createParameter(IMEventbase.LEGACY_TYPE, String.class, IMParameter.DIRECTION_IN, 0, null, null);
		stdintevent.createParameter(IMEventbase.LEGACY_CONTENT, Object.class, IMParameter.DIRECTION_IN, 0, null, null);

		IMInternalEvent timeout = createInternalEvent(TYPE_TIMEOUT, IMReferenceableElement.EXPORTED_FALSE);
		timeout.createParameter("exception", Exception.class, IMParameter.DIRECTION_IN, 0, null, null);

		// Hack!!! Cannot use RCondition.class
		IMInternalEvent condtrig = createInternalEvent(TYPE_CONDITION_TRIGGERED, IMReferenceableElement.EXPORTED_FALSE);
		MParameter	param = (MParameter)condtrig.createParameter(IMEventbase.CONDITION, null, IMParameter.DIRECTION_IN, 0, null, null);
		//param.setClassname("jadex.runtime.impl.RCondition");
		param.setClassname("jadex.runtime.impl.IInterpreterCondition");
		condtrig.createParameter(IMEventbase.CAUSE, Object.class, IMParameter.DIRECTION_IN, 0, null, null);

		IMInternalEvent exeplan = createInternalEvent(TYPE_EXECUTEPLAN, IMReferenceableElement.EXPORTED_FALSE);
		// Hack!!! Cannot use PlanInfo.class
		param	= (MParameter)exeplan.createParameter("candidate", null, IMParameter.DIRECTION_IN, 0, null, null);
		param.setClassname("jadex.runtime.impl.PlanInfo");

		// create references
		// todo: same name???
		IMGoalEventReference stdgoaleventref = createGoalEventReference(STANDARD_GOAL_EVENT_REFERENCE, IMReferenceableElement.EXPORTED_FALSE, null, false);
		// stdgoaleventref.setAbstract(true); todo

		createInternalEventReference(TYPE_CONDITION_TRIGGERED_REFERENCE, IMReferenceableElement.EXPORTED_FALSE, null, false);

//		createInternalEventReference(TYPE_TIMEOUT_REFERENCE, false, null, false);

//		createInternalEventReference(TYPE_EXECUTEPLAN_REFERENCE, false, null, false);
	}

	//-------- xml methods --------

	/**
	 *  Geneal add method for unmarshalling.
	 *  Necessary to support unordered collections :-(
	 *  @param elem The element to add.
	 */
	public void addElement(IMReferenceableElement elem)
	{
		assert elem instanceof IMInternalEvent || elem instanceof IMInternalEventReference
			|| elem instanceof IMMessageEvent || elem instanceof IMMessageEventReference
			|| elem instanceof IMGoalEvent || elem instanceof IMGoalEventReference;

		if(elem instanceof IMInternalEvent)
		{
			if(internalevents==null)
				internalevents = SCollection.createArrayList();
			internalevents.add(elem);
		}
		else if(elem instanceof IMInternalEventReference)
		{
			if(internaleventrefs==null)
				internaleventrefs = SCollection.createArrayList();
			internaleventrefs.add(elem);
		}
		else if(elem instanceof IMMessageEvent)
		{
			if(messageevents==null)
				messageevents = SCollection.createArrayList();
			messageevents.add(elem);
		}
		else if(elem instanceof IMMessageEventReference)
		{
			if(messageeventrefs==null)
				messageeventrefs = SCollection.createArrayList();
			messageeventrefs.add(elem);
		}
		else if(elem instanceof IMGoalEvent)
		{
			if(goalevents==null)
				goalevents = SCollection.createArrayList();
			goalevents.add(elem);
		}
		else //if(elem instanceof IMGoalEventReference)
		{
			if(goaleventrefs==null)
				goaleventrefs = SCollection.createArrayList();
			goaleventrefs.add(elem);
		}
	}

	/**
	 *  Geneal add method for marshalling.
	 *  @return Iterator with all elements.
	 */
	public Iterator iterElements()
	{
		return SReflect.getIterator(getReferenceableElements());
	}

	//-------- mbase method --------

	/**
	 *  Get the element name.
	 *  @return The element name.
	 */
	public String	getName()
	{
		return "eventbase";
	}

	/**
	 *  Get the corresponding base of a given scope.
	 *  Depending on the type of this base (e.g. beliefbase),
	 *  the corresponding base of the given scope is returned.
	 *  @param scope	The scope.
	 *  @return	The corresponding base.
	 */
	public IMBase	getCorrespondingBase(IMCapability scope)
	{
		return scope.getEventbase();
	}

	/**
	 *  Get the elements contained in the base.
	 *  @return The elements.
	 */
	public IMReferenceableElement[]	getReferenceableElements()
	{
		Object	ret	= new IMReferenceableElement[0];
		ret	= SUtil.joinArrays(ret, getInternalEvents());
		ret	= SUtil.joinArrays(ret, getInternalEventReferences());
		ret	= SUtil.joinArrays(ret, getMessageEvents());
		ret	= SUtil.joinArrays(ret, getMessageEventReferences());
		ret	= SUtil.joinArrays(ret, getGoalEvents());
		ret	= SUtil.joinArrays(ret, getGoalEventReferences());
		return (IMReferenceableElement[])ret;
	}

	/**
	 *  Delete a referenceable element per name.
	 *  @param elem The element.
	 */
	public void deleteReferenceableElement(IMReferenceableElement elem)
	{
		assert elem!=null;

		if(elem instanceof IMInternalEvent)
			deleteInternalEvent((IMInternalEvent)elem);
		else if(elem instanceof IMInternalEventReference)
			deleteInternalEventReference((IMInternalEventReference)elem);
		else if(elem instanceof IMMessageEvent)
			deleteMessageEvent((IMMessageEvent)elem);
		else if(elem instanceof IMMessageEventReference)
			deleteMessageEventReference((IMMessageEventReference)elem);
		else
			throw new RuntimeException("Element not event: "+elem);
	}

	//-------- internal events --------

	/**
	 *  Get all known internal events.
	 *  @return The internal events.
	 */
	public IMInternalEvent[] getInternalEvents()
	{
		if(internalevents==null)
			return new IMInternalEvent[0];
		return (IMInternalEvent[])internalevents.toArray(new IMInternalEvent[internalevents.size()]);
	}

	/**
	 *  Get an internal event by name.
	 *  @param name The internal event name.
	 *  @return The internal event with that name (if any).
	 */
	public IMInternalEvent getInternalEvent(String name)
	{
		assert name!=null;

		IMInternalEvent ret = null;
		for(int i=0; internalevents!=null && i<internalevents.size() && ret==null; i++)
		{
			IMInternalEvent test = (IMInternalEvent)internalevents.get(i);
			if(test.getName().equals(name))
				ret = test;
		}
		return ret;
	}

	/**
	 *  Create an internal event.
	 *  @param name	The name of the event.
	 *  @param exported	Flag indicating if this event may be referenced from outside capabilities.
	 *  @return The internal event.
	 */
	public IMInternalEvent	createInternalEvent(String name, String exported)
	{
		if(internalevents==null)
			internalevents = SCollection.createArrayList();

		MInternalEvent ret = new MInternalEvent();
		ret.setName(name);
		ret.setExported(exported);
		ret.setOwner(this);
		ret.init();
		internalevents.add(ret);
		return ret;
	}

	/**
	 *  Delete an internal event.
	 *  @param event	The internal event.
	 */
	public void	deleteInternalEvent(IMInternalEvent event)
	{
		if(!internalevents.remove(event))
			throw new RuntimeException("Event could not be found: "+event);
	}

	//-------- internal event references --------

	/**
	 *  Get all known internal event references.
	 *  @return The internal event references.
	 */
	public IMInternalEventReference[] getInternalEventReferences()
	{
		if(internaleventrefs==null)
			internaleventrefs = SCollection.createArrayList();
		return (IMInternalEventReference[])internaleventrefs
			.toArray(new IMInternalEventReference[internaleventrefs.size()]);
	}

	/**
	 *  Get an event by name.
	 *  @param name The internal event reference name.
	 *  @return The internal event reference with that name (if any).
	 */
	public IMInternalEventReference getInternalEventReference(String name)
	{
		assert name!=null;

		IMInternalEventReference ret = null;
		for(int i=0; internaleventrefs!=null && i<internaleventrefs.size() && ret==null; i++)
		{
			IMInternalEventReference test = (IMInternalEventReference)internaleventrefs.get(i);
			if(test.getName().equals(name))
				ret = test;
		}
		return ret;
	}

	/**
	 *  Create an internal event reference.
	 *  @param name	The name of the event reference.
	 *  @param exported	Flag indicating if this event reference may be referenced from outside capabilities.
	 *  @param ref	The referenced event (or null for abstract).
	 *  @param req Is a reference required (only for abstract).
	 *  @return The internal event reference.
	 */
	public IMInternalEventReference	createInternalEventReference(String name, String exported, String ref, boolean req)
	{
		if(internaleventrefs==null)
			internaleventrefs = SCollection.createArrayList();

		MInternalEventReference ret = new MInternalEventReference();
		ret.setName(name);
		ret.setExported(exported);
		if(ref==null)
		{
			ret.setAbstract(true);
			ret.setRequired(req);
		}
		else
		{
			ret.setReference(ref);
		}
		ret.setOwner(this);
		ret.init();
		internaleventrefs.add(ret);
		return ret;
	}

	/**
	 *  Delete an internal event reference.
	 *  @param event	The internal event reference.
	 */
	public void	deleteInternalEventReference(IMInternalEventReference event)
	{
		if(!internaleventrefs.remove(event))
			throw new RuntimeException("Event could not be found: "+event);
	}

	//-------- message events --------

	/**
	 *  Get all known message event.
	 *  @return The message events.
	 */
	public IMMessageEvent[] getMessageEvents()
	{
		if(messageevents==null)
			return new IMMessageEvent[0];
		return (IMMessageEvent[])messageevents.toArray(new IMMessageEvent[messageevents.size()]);
	}

	/**
	 *  Get a message event by name.
	 *  @param name The message event name.
	 *  @return The message event with that name (if any).
	 */
	public IMMessageEvent getMessageEvent(String name)
	{
		assert name!=null;

		IMMessageEvent ret = null;
		for(int i=0; messageevents!=null && i<messageevents.size() && ret==null; i++)
		{
			IMMessageEvent test = (IMMessageEvent)messageevents.get(i);
			if(test.getName().equals(name))
				ret = test;
		}
		return ret;
	}

	/**
	 *  Create an message event.
	 *  @param name	The name of the message event.
	 *  @param type	The type of the message event (e.g. "fipa").
	 *  @param direction	The direction of the message event (send/receive).
	 *  @param exported	Flag indicating if this event may be referenced from outside capabilities.
	 *  @return The message event.
	 */
	public IMMessageEvent	createMessageEvent(String name, String type, String direction, String exported)
	{
		if(messageevents==null)
			messageevents = SCollection.createArrayList();

		MMessageEvent ret = new MMessageEvent();
		ret.setName(name);
		ret.setType(type);
		ret.setDirection(direction);
		ret.setExported(exported);
		ret.setOwner(this);
		ret.init();
		messageevents.add(ret);
		return ret;
	}

	/**
	 *  Delete an message event.
	 *  @param event	The message event.
	 */
	public void	deleteMessageEvent(IMMessageEvent event)
	{
		if(!messageevents.remove(event))
			throw new RuntimeException("Event could not be found: "+event);
	}


	//-------- message event references --------

	/**
	 *  Get all known message event references.
	 *  @return The message event references.
	 */
	public IMMessageEventReference[] getMessageEventReferences()
	{
		if(messageeventrefs==null)
			return new IMMessageEventReference[0];
		return (IMMessageEventReference[])messageeventrefs
			.toArray(new IMMessageEventReference[messageeventrefs.size()]);
	}

	/**
	 *  Get a message event reference by name.
	 *  @param name The message event reference name.
	 *  @return The message event reference with that name (if any).
	 */
	public IMMessageEventReference getMessageEventReference(String name)
	{
		assert name!=null;

		IMMessageEventReference ret = null;
		for(int i=0; messageeventrefs!=null && i<messageeventrefs.size() && ret==null; i++)
		{
			IMMessageEventReference test = (IMMessageEventReference)messageeventrefs.get(i);
			if(test.getName().equals(name))
				ret = test;
		}
		return ret;
	}

	/**
	 *  Create an message event reference.
	 *  @param name	The name of the event reference.
	 *  @param exported	Flag indicating if this event reference may be referenced from outside capabilities.
	 *  @param ref	The referenced event (or null for abstract).
	 *  @param req Is a reference required (only for abstract).
	 *  @return The message event reference.
	 */
	public IMMessageEventReference	createMessageEventReference(String name, String exported, String ref, boolean req)
	{
		if(messageeventrefs==null)
			messageeventrefs = SCollection.createArrayList();

		MMessageEventReference ret = new MMessageEventReference();
		ret.setName(name);
		ret.setExported(exported);
		if(ref==null)
		{
			ret.setAbstract(true);
			ret.setRequired(req);
		}
		else
		{
			ret.setReference(ref);
		}
		ret.setOwner(this);
		ret.init();
		messageeventrefs.add(ret);
		return ret;
	}

	/**
	 *  Delete an message event reference.
	 *  @param event	The message event reference.
	 */
	public void	deleteMessageEventReference(IMMessageEventReference event)
	{
		if(!messageeventrefs.remove(event))
			throw new RuntimeException("Event could not be found: "+event);
	}


	//-------- goal events --------

	/**
	 *  Get all known goal events.
	 *  @return The goal events.
	 */
	public IMGoalEvent[] getGoalEvents()
	{
		if(goalevents==null)
			return new IMGoalEvent[0];
		return (IMGoalEvent[])goalevents.toArray(new IMGoalEvent[goalevents.size()]);
	}

	/**
	 *  Create a goal event.
	 *  @param name	The name of the message event.
	 *  @param exported	Flag indicating if this event may be referenced from outside capabilities.
	 *  @return The goal event.
	 */
	public IMGoalEvent	createGoalEvent(String name, String exported)
	{
		if(goalevents==null)
			goalevents = SCollection.createArrayList();

		MGoalEvent ret = new MGoalEvent();
		ret.setName(name);
		ret.setExported(exported);
		ret.setOwner(this);
		ret.init();
		goalevents.add(ret);
		return ret;
	}

	/**
	 *  Get an event by name.
	 *  @param name The goal event name.
	 *  @return The goal event with that name (if any).
	 */
	public IMGoalEvent getGoalEvent(String name)
	{
		assert name!=null;

		IMGoalEvent ret = null;
		for(int i=0; goalevents!=null && i<goalevents.size() && ret==null; i++)
		{
			IMGoalEvent test = (IMGoalEvent)goalevents.get(i);
			if(test.getName().equals(name))
				ret = test;
		}
		return ret;
	}


	//-------- goal event references --------

	/**
	 *  Get all known goal event references.
	 *  @return The goal event references.
	 */
	public IMGoalEventReference[] getGoalEventReferences()
	{
		if(goaleventrefs==null)
			return new IMGoalEventReference[0];
		return (IMGoalEventReference[])goaleventrefs
			.toArray(new IMGoalEventReference[goaleventrefs.size()]);
	}

	/**
	 *  Create a goal event.
	 *  @param name	The name of the message event.
	 *  @param exported	Flag indicating if this event may be referenced from outside capabilities.
	 *  @return The goal event.
	 */
	public IMGoalEventReference	createGoalEventReference(String name, String exported, String ref, boolean req)
	{
		if(goaleventrefs==null)
			goaleventrefs = SCollection.createArrayList();

		MGoalEventReference ret = new MGoalEventReference();
		ret.setName(name);
		ret.setExported(exported);
		if(ref==null)
		{
			ret.setAbstract(true);
			ret.setRequired(req);
		}
		else
		{
			ret.setReference(ref);
		}
		ret.setOwner(this);
		ret.init();
		goaleventrefs.add(ret);
		return ret;
	}

	/**
	 *  Get a goal event reference by name.
	 *  @param name The goal event reference name.
	 *  @return The goal event reference with that name (if any).
	 */
	public IMGoalEventReference getGoalEventReference(String name)
	{
		assert name!=null;

		IMGoalEventReference ret = null;
		for(int i=0; goaleventrefs!=null && i<goaleventrefs.size() && ret==null; i++)
		{
			IMGoalEventReference test = (IMGoalEventReference)goaleventrefs.get(i);
			if(test.getName().equals(name))
				ret = test;
		}
		return ret;
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MEventbase clone = (MEventbase)cl;
		if(internalevents!=null)
		{
			clone.internalevents = SCollection.createArrayList();
			for(int i=0; i<internalevents.size(); i++)
				clone.internalevents.add(((MElement)internalevents.get(i)).clone());
		}
		if(internaleventrefs!=null)
		{
			clone.internaleventrefs = SCollection.createArrayList();
			for(int i=0; i<internaleventrefs.size(); i++)
				clone.internaleventrefs.add(((MElement)internaleventrefs.get(i)).clone());
		}
		if(messageevents!=null)
		{
			clone.messageevents = SCollection.createArrayList();
			for(int i=0; i<messageevents.size(); i++)
				clone.messageevents.add(((MElement)messageevents.get(i)).clone());
		}
		if(messageeventrefs!=null)
		{
			clone.messageeventrefs = SCollection.createArrayList();
			for(int i=0; i<messageeventrefs.size(); i++)
				clone.messageeventrefs.add(((MElement)messageeventrefs.get(i)).clone());
		}
		if(goalevents!=null)
		{
			clone.goalevents = SCollection.createArrayList();
			for(int i=0; i<goalevents.size(); i++)
				clone.goalevents.add(((MElement)goalevents.get(i)).clone());
		}
		if(goaleventrefs!=null)
		{
			clone.goaleventrefs = SCollection.createArrayList();
			for(int i=0; i<goaleventrefs.size(); i++)
				clone.goaleventrefs.add(((MElement)goaleventrefs.get(i)).clone());
		}
	}
}
