package jadex.runtime.impl;

import jadex.model.IMEvent;
import jadex.model.IMConfigParameterElement;
import jadex.runtime.IEvent;
import jadex.util.SReflect;

import java.util.Map;

/**
 *  A runtime event element.
 */
public abstract class REvent extends RParameterElement implements IREvent, Cloneable
{
	//-------- constants --------

	/** The event class (message, goal, internal). */
	public static final String CLASS	= "eventclass";

	/** The message event class. */
	public static final String CLASS_MESSAGE	= "eventclass_message";
	
	/** The goal event class. */
	public static final String CLASS_GOAL	= "eventclass_goal";
	
	/** The internal event class. */
	public static final String CLASS_INTERNAL	= "eventclass_internal";

	//-------- attributes --------

	/** The applicable candidate list. */
	private ApplicableCandidateList apl;

	//-------- constructors --------

	/**
	 *  Create a new event.
	 */
	protected REvent(String name, IMEvent event, IMConfigParameterElement state, RElement owner,
		RReferenceableElement creator, Map exparams)
	{
		super(name, event, state, owner, creator, exparams);
		setParameterProtectionMode(ACCESS_PROTECTION_INIT);
	}

	//-------- BDI event properties --------

	/**
	 *  Is it a post-to-all event.
	 *  The default for internal events is true.
	 */
	public boolean isPostToAll()
	{
		return ((IMEvent)getModelElement()).isPostToAll();
	}

	/**
	 *  Get the random selection flag.
	 *  @return True, when applicable
	 *  selection is random style.
	 */
	public boolean	isRandomSelection()
	{
		return ((IMEvent)getModelElement()).isRandomSelection();
	}

	//-------- methods ---------

	/**
	 *  Get the event class (message, goal, internal).
	 *  @return The event class.
	 */
	public String getEventclass()
	{
		String ret = null;
		 // Could be beautified ;-)
		if(this instanceof RGoalEvent)
			ret = CLASS_GOAL;
		else if(this instanceof RMessageEvent)
			ret = CLASS_MESSAGE;
		else
			ret = CLASS_INTERNAL;
		return ret;
	}

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public String toString()
	{
		StringBuffer	buf	= new StringBuffer();
		buf.append(SReflect.getInnerClassName(this.getClass()));
		buf.append("(");
		buf.append("posttoall:" );
		buf.append(isPostToAll());
		buf.append(", ");
		buf.append(")");
		return buf.toString();
	}

	/**
	 *  Generate a property representation for encoding this element
	 *  (eg to SL).
	 *  Method will be overridden by subclasses. When the method
	 *  is invoked it newly fetches several proporties.
	 *  @return A properties object representing this element.
	 */
	public Map getEncodableRepresentation()
	{
		Map rep = super.getEncodableRepresentation();
		rep.put(CLASS, getEventclass());
		return rep;
	}

	/**
	 *  Called when the event is dispatched.
	 */
	public void dispatched()
	{
		setParameterProtectionMode(ACCESS_PROTECTION_PROCESSING);
	}

	/**
	 *  Get (or create) the apl for the event.
	 *  @return The apl.
	 */
	public ApplicableCandidateList getApplicableCandidatesList()
	{
		if(apl==null)
			apl = new ApplicableCandidateList(this);
		return apl;
	}

	//-------- cloneable --------

	/**
	 *  Clone this event.
	 * /
	public Object	clone()
	{
		try
		{
			REvent	clone	= (REvent)super.clone();
			//clone.props	= (IndexMap)props.clone(); //todo
			return clone;
		}
		catch(CloneNotSupportedException e)
		{
			// Shouldn't happen.
			throw new RuntimeException("Cloneable not cloneable?");
		}
	}*/
}

