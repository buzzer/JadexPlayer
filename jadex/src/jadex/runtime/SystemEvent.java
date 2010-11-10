package jadex.runtime;

import java.util.Set;
import jadex.model.ISystemEventTypes;
import jadex.util.SReflect;
import jadex.util.collection.SCollection;

/**
 *  A system event indicates that something happend.
 */
// Hack!!! Should declare event types directly.
public class SystemEvent	implements ISystemEventTypes, java.io.Serializable 
{
	//-------- attributes --------

	/** The system event type. */
	protected String	type;

	/** Flag denoting a system change event, potentially relevant for conditions. */
	protected boolean	change;

	/** Flag indicating if this is a derived change, meaning that
	    the event was collected as effect of another change. */
	protected boolean derived;

	/** The event source. */
	protected Object	source;

	/** The value of the element (only for valued elements). */
	protected Object	value;

	/** The index where the change occurred (only for element sets). */
	protected int	index;
  
	/** Reference to the object that introduced this event to the system (if any). */
	protected String	cause;

	/** Test if it is the initing event. */
	protected boolean init;

	/** The hashcode (cached for speed). */
	protected int	hashcode;

	//-------- constructors --------

	/**
	 *  Bean constructor.
	 */
	public SystemEvent()
	{
		// Todo: what about hashcode for beans?
	}

	/**
	 *  Create a new change event.
	 *  @param type The system event type.
	 *  @param source The event source.
	 */
	public SystemEvent(String type, Object source)
	{
		this(type, source, null);
	}

	/**
	 *  Create a new change event.
	 *  @param type The system event type.
	 *  @param source The event source.
	 *  @param value The value of the element.
	 */
	public SystemEvent(String type, Object source, Object value)
	{
		this(type, source, value, -1);
	}

	/**
	 *  Create a new change event.
	 *  @param type The system event type.
	 *  @param source The event source.
	 *  @param value The value at the specified index.
	 *  @param index The index where the change occurred.
	 */
	public SystemEvent(String type, Object source, Object value, int index)
	{
		this.type = type;
		this.source = source;
		this.value = value;
		this.index = index;
		this.change	= isChangeRelevant(type);
	}

	//-------- methods --------

	/**
	 *  Get the type.
	 *  @return The type.
	 */
	public String getType()
	{
		return this.type;
	}

	/**
	 *  Set the type.
	 */
	public void	setType(String type)
	{
		this.type	= type;
		this.change	= isChangeRelevant(type);
	}

	/**
	 *  Does this event denote a system change,
	 *  potentially relevant for conditions?
	 *  @return True, if this event denotes a relevant system change.
	 */
	public boolean	isChangeRelevant()
	{
		return change;
	}
	
	/**
	 *  Get the event source.
	 */
	public Object getSource()
	{
		return this.source;
	}

	/**
	 *  Set the event source.
	 */
	public void setSource(Object source)
	{
		this.source	= source;
	}

	/**
	 *  Get the value of this event (if any).
	 */
	public Object getValue()
	{
		return value;
	}

	/**
	 *  Set the value of this event (if any).
	 */
	public void	setValue(Object value)
	{
		this.value	= value;
	}

	/**
	 *  Get the index of this event (if any).
	 */
	public int getIndex()
	{
		return index;
	}
  
	/**
	 *  Get the index of this event (if any).
	 */
	public void setIndex(int index)
	{
		this.index	= index;
	}

	/**
	 *  The cause of this event
	 *  @param cause The cause.
	 */
	public void setCause(String cause)
	{
		//  System.out.println("Stub: SystemEvent.addCause");
		this.cause = cause;
	}

	/**
	 *  Return the cause of this event
	 */
	public String getCause()
	{
		return cause;
	}

	/**
	 *  Test if the system event is derived.
	 *  @return True, if derived.
	 */
	public boolean isDerived()
	{
		return derived;
	}

	/**
	 *  Set the derived state.
	 *  @param derived The derived state.
	 */
	public void setDerived(boolean derived)
	{
		this.derived = derived;
	}

	/**
	 *  Test if init event.
	 *  @return True, if init event.
	 */
	public boolean isInit()
	{
		return init;
	}

	/**
	 *  Set init event state.
	 *  @param init The init event state.
	 */
	public void setInit(boolean init)
	{
		this.init = init;
	}

	/**
	 *  Check if this event type is a (sub) type
	 *  of the given event type.
	 *  @param type	The system event type that may be a subtype.
	 */
	public boolean	instanceOf(String type)
	{
		return ISystemEventTypes.Subtypes.getSubtypes(type).contains(this.type);
	}

	/**
	 *  Test if two system events are equal.
	 *  @return True, if equal.
	 */
	public boolean equals(Object o)
	{
		boolean ret = false;
		if(o instanceof SystemEvent)
		{
			SystemEvent se = (SystemEvent)o;
			if(type==se.getType() || (type!=null && type.equals(se.getType())))
				if(source==se.getSource() || (source!=null && source.equals(se.getSource())))
					if(value==se.getValue() || (value!=null && value.equals(se.getValue())))
						if(index==se.getIndex())
							ret = true;
		}
		return ret;
	}

	/**
	 *  Get the elements hashcode.
	 *  @return The hashcode for identification.
	 */
	public int hashCode()
	{
		if(hashcode==0)
		{
			// Cache hashcode for speed (hack?).
			this.hashcode = type.hashCode();
			if(source!=null)
				hashcode = hashcode ^ source.hashCode();
			if(index!=-1)
				hashcode = hashcode ^ index;
		}
		return hashcode;
	}

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public String toString()
	{
		StringBuffer	sb	= new StringBuffer();
		sb.append(SReflect.getInnerClassName(this.getClass()));
		sb.append("(type=");
		sb.append(type);
		sb.append(", source=");
		sb.append(source);
		if(index!=-1)
		{
			sb.append(", index=");
			sb.append(index);
			sb.append(", value=");
			sb.append(value);
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 *  Create a shallow copy of this system event.
	 */
	public Object	clone()
	{
		SystemEvent	clone	= new SystemEvent();
		clone.cause	= cause;
		clone.change	= change;
		clone.hashcode	= hashcode;
		clone.index	= index;
		clone.source	= source;
		clone.type	= type;
		clone.value	= value;
		return clone;
	}
	
	//-------- static part --------

	/** The lookup table for change relevant events. */
	protected static Set	changetypes;

	static
	{
		changetypes	= SCollection.createHashSet();

//		changetypes.add(TYPE_ANY);	// abstract
//		changetypes.add(EVENT_TYPE_BDI);	// abstract

//		changetypes.add(CAPABILITY_EVENT);	// abstract
//		changetypes.add(CAPABILITY_ADDED);	// info event
//		changetypes.add(CAPABILITY_REMOVED);	// info event

//		changetypes.add(BELIEF_EVENT);	// abstract
		changetypes.add(BELIEF_ADDED);
		changetypes.add(BELIEF_REMOVED);

//		changetypes.add(FACT_EVENT);	// abstract
//		changetypes.add(FACT_READ);
		changetypes.add(FACT_CHANGED);

//		changetypes.add(BSFACT_EVENT);	// abstract
//		changetypes.add(BSFACT_READ);	// info event
 		changetypes.add(BSFACT_ADDED);
		changetypes.add(BSFACT_REMOVED);
		changetypes.add(BSFACT_CHANGED);
		changetypes.add(BSFACTS_CHANGED);

		changetypes.add(VALUE_CHANGED);

//		changetypes.add(ESVALUE_EVENT);	// abstract
		changetypes.add(ESVALUE_ADDED);
		changetypes.add(ESVALUE_REMOVED);
		changetypes.add(ESVALUE_CHANGED);
		changetypes.add(ESVALUES_CHANGED);

//		changetypes.add(GOAL_EVENT);	// abstract
		changetypes.add(GOAL_ADDED);
		changetypes.add(GOAL_REMOVED);
		changetypes.add(GOAL_CHANGED);

		changetypes.add(BINDING_EVENT);

//		changetypes.add(PLAN_EVENT);	// abstract
		changetypes.add(PLAN_ADDED);
		changetypes.add(PLAN_REMOVED);
		changetypes.add(PLAN_CHANGED);

//		changetypes.add(EVENT_TYPE_STEPPABLE);	// abstract
//		changetypes.add(AGENDA_MODE_CHANGED);	// info event
//		changetypes.add(AGENDA_STEPS_CHANGED);	// info event
//		changetypes.add(AGENDA_STEP_DONE);	// info event (almost!!!)
//		changetypes.add(AGENDA_CHANGED);	// info event
	}
	
	/**
	 *  Check if an event type is a (sub) type
	 *  of any event of a given set of event types.
	 *  @param event	The system event type.
	 *  @param types	The system event types.
	 * /
	public static boolean	isSubtype(String event, String[] types)
	{
		boolean	found	= false;
		for(int i=0; !found && i<types.length; i++)
		{
			Collection	stypes	= ISystemEventTypes.Subtypes.getSubtypes(types[i]);
			found	= stypes.contains(event);
		}
		return found;
	}*/

	/**
	 *  Check, if an event type is change relevant. 
	 */
	//todo:
	// Hack!!! Should be in interface???
	protected static boolean	isChangeRelevant(String type)
	{
		return changetypes.contains(type);
	}	
}

