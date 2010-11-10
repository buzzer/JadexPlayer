package jadex.runtime.impl;

import jadex.model.*;
import jadex.runtime.*;
import jadex.util.SReflect;
import jadex.util.SUtil;

import java.util.Map;


/**
 *  A message event represents an inter-agent message.
 */
public class RMessageEvent extends REvent implements IRMessageEvent
{
	//--------  constants --------

	/** Static message no. */
	private  static int	message_no	= 0;

	//-------- attributes --------

	/** The native message object (platform dependent). */
	protected Object	message;

	/** The message direction. */
	//protected boolean	incoming;

	/** The original message (if this is a reply). */
	protected IRMessageEvent originalmsg;

	/** The timeout associated to the message event (if any). */
	protected long	timeout;

	/** The unique message id. */
	private String	id;
	
	//-------- constructors --------

	/**
	 *  Create a new outgoing message event.
	 */
	protected RMessageEvent(String name, IMMessageEvent event, IMConfigMessageEvent state,
		RElement owner, RReferenceableElement creator, boolean incoming, Map exparams)
	{
		super(name, event, state, owner, creator, exparams);
		if(event.getDirection().equals(IMMessageEvent.DIRECTION_RECEIVE) && !incoming)
			throw new RuntimeException("Cannot create outgoing message. Is declared to be only received: "+event);
		else if(event.getDirection().equals(IMMessageEvent.DIRECTION_SEND) && incoming)
			throw new RuntimeException("Cannot create incoming message. Is declared to be only received: "+event);
		//this.incoming	= incoming;
		this.timeout	= -1;
		if(!incoming) // Generate id for new message events.
			this.id = "#"+message_no+++"@"+getScope().getAgentName();
	}

	//-------- methods --------

	/**
	 *  Get the platform specific message.
	 */
	public Object getMessage()
	{
		return message;
	}

	/**
	 *  Set the (platform specific) message object.
	 *  @param message	The message object.
	 */
	public void setMessage(Object message)
	{
		assert this.message==null : this;
		this.message	= message;
	}

	/**
	 *  Get the message direction.
	 *  @return True, if message is incoming.
	 * /
	public boolean isIncoming()
	{
		return this.incoming;
	}*/

	/**
	 *  Test, if this message is a reply to another message.
	 *  @return True, if it is a reply.
	 */
	public boolean isReply()
	{
		return originalmsg!=null;
	}

	/**
	 *  Get the original message to reply to.
	 */
	public IRMessageEvent getInReplyMessageEvent()
	{
		return originalmsg;
	}

	/**
	 *  Set the original message (if this is a reply).
	 */
	public void setInReplyMessageEvent(IRMessageEvent event)
	{
		this.originalmsg	= event;
	}

	/**
	 *  Get the content.
	 *  Allowed content objects depend on the platform.
	 *  @return The content.
	 */
	public Object getContent()
	{
		if(!hasParameter("content"))
			throw new RuntimeException("No content parameter defined in message: "+this.getType());
		IRParameter param = getParameter("content");
		Object content = param.getValue();
		if(content instanceof ContentException)
			throw (ContentException)content;
		return content;
	}

	/**
	 *  Set the content.
	 *  Allowed content objects depend on the platform.
	 *  @param content The content.
	 */
	public void setContent(Object content)
	{
		if(!hasParameter("content"))
		{
			hasParameter("content");
			throw new RuntimeException("No content parameter defined in message: "+this.getType());
		}
		IRParameter param = getParameter("content");
		param.setValue(content);
	}

	/**
	 *  Create a reply to this message event.
	 *  @param msgeventtype	The message event type.
	 *  @return The reply event.
	 */
	public IRMessageEvent	createReply(String msgeventtype)
	{
		// todo: this code is exactly (99%) the same as in RMessageEventReference, should be unified somehow

		IMEventbase ebase = (IMEventbase)getScope().getEventbase().getModelElement();
		IMMessageEvent	mevent = ebase.getMessageEvent(msgeventtype);
		IMMessageEventReference	meventref	= null;
		MessageType	eventtype	= null;
		if(mevent==null)
		{
			meventref = ebase.getMessageEventReference(msgeventtype);
			if(meventref!=null)
			{
				mevent = (IMMessageEvent)meventref.getOriginalElement();
			}
			else
			{
				throw new RuntimeException("Message event type not found in model: "+msgeventtype);
			}
		}
		eventtype	= mevent.getMessageType();

		// Check if events are of same type.
		MessageType	thistype	= ((IMMessageEvent)this.getModelElement()).getMessageType();
		if(!SUtil.equals(thistype, eventtype))
		{
			throw new RuntimeException("Cannot create reply of incompatible message type: "+this+", "
				+(meventref!=null ? meventref.toString() : mevent.toString()));
		}

		// Create reply event.
		IRMessageEvent	event	= getScope().getEventbase().createMessageEvent(
			meventref!=null? (IMReferenceableElement)meventref: (IMReferenceableElement)mevent, null, null, false, null);
		event.setInReplyMessageEvent(this);

		// Copy parameter(set) values as specified in event type.
		MessageType.ParameterSpecification[]	params	= eventtype.getParameters();
		MessageType.ParameterSpecification[]	paramsets	= eventtype.getParameterSets();
		for(int i=0; i<params.length; i++)
		{
			// Only copy when source is specified and the target is not fixed
			if(params[i].getSource()!=null
				&& !mevent.getParameter(params[i].getName()).getDirection().equals(IMParameter.DIRECTION_FIXED))
			{
				IRParameter	source	= this.getParameter(params[i].getSource());
				// todo: eventref could have renamed parameter, need to lookup actual name
				IRParameter	dest	= event.getParameter(params[i].getName());
				Object	sourceval	= source.getValue();
				if(IMParameter.DIRECTION_IN.equals(((IMParameter)dest.getOriginalElement().getModelElement()).getDirection())
					|| IMParameter.DIRECTION_INOUT.equals(((IMParameter)dest.getOriginalElement().getModelElement()).getDirection()))
				{
					dest.setValue(sourceval);
				}
				else
				{
					Object	destvalue	= dest.getValue();
					if(!SUtil.equals(sourceval, destvalue))
						getScope().getLogger().warning("Cannot overwrite reply value "+params[i].getName()+" on "+this);
				}
			}
		}
		for(int i=0; i<paramsets.length; i++)
		{
			// Only copy when source is specified and the target is not fixed
			if(paramsets[i].getSource()!=null
				&& !mevent.getParameterSet(paramsets[i].getName()).getDirection().equals(IMParameterSet.DIRECTION_FIXED))
			{
				// Todo: support source parameter set.
				IRParameter	source	= this.getParameter(paramsets[i].getSource());
				// todo: eventref could have renamed parameter, need to lookup actual name
				IRParameterSet	dest	= event.getParameterSet(paramsets[i].getName());
				Object	sourceval	= source.getValue();
				if(IMParameterSet.DIRECTION_IN.equals(((IMParameterSet)dest.getOriginalElement().getModelElement()).getDirection())
					|| IMParameterSet.DIRECTION_INOUT.equals(((IMParameterSet)dest.getOriginalElement().getModelElement()).getDirection()))
				{
					dest.removeValues();	// Todo: is that right?
					dest.addValue(sourceval);
				}
				else
				{
					Object[]	destvalues	= dest.getValues();
					if((sourceval!=null || destvalues.length>0) && (sourceval==null || destvalues.length>1 || !sourceval.equals(destvalues[0])))
						getScope().getLogger().warning("Cannot overwrite reply value "+params[i].getName()+" on "+this);
				}
			}
		}
		
		return event;
	}

	/**
	 *  Create a reply to this message event.
	 *  @param msgeventtype	The message event type.
	 *  @param content	The message content.
	 *  @return The reply event.
	 */
	public IRMessageEvent	createReply(String msgeventtype, Object content)
	{
		IRMessageEvent	event	= createReply(msgeventtype);
		event.setContent(content);
		return event;
	}

//	/**
//	 *  Get the general message type (fipa, custom, etc.).
//	 *  @return The message type.
//	 */
//	public String getGeneralType()
//	{
//		return ((IMMessageEvent)getModelElement()).getType();
//	}

	/**
	 *  Get a filter for matching the reply of a message.
	 */
	public IFilter getFilter()
	{
		return new MessageEventFilter(null, getName());
	}

	/**
	 *  Return a string representation of this element.
	 *  @return A string representation of this element.
	 */
	public String	toString()
	{
		try
		{
			return getType()+"("+getContent()+")";
		}
		catch(ContentException e)
		{
			return getType()+"(invalid content)";
		}
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
		StringBuffer msgtxt = new StringBuffer();
		/*if(incoming)
		{
			msgtxt.append("incoming: ");
			msgtxt.append(message);
		}
		else
		{
			msgtxt.append("outgoing: ");*/
			msgtxt.append(SUtil.arrayToString(this.getParameters()));
			msgtxt.append(" ");
			msgtxt.append(SUtil.arrayToString(this.getParameterSets()));
		//}
		//msgtxt.append("Content: "+content);
		rep.put("message", msgtxt);
		return rep;
	}

	/**
	 *  Get the id.
	 *  @return The id of this message.
	 */
	public String getId()
	{
		return id;
	}

	/**
	 *  Set the id.
	 *  @param id The id.
	 */
	public void setId(String id)
	{
		this.id = id;
	}
}


