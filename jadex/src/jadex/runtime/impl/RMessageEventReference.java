package jadex.runtime.impl;

import jadex.model.*;
import jadex.runtime.*;
import jadex.util.SUtil;

/**
 *  The reference to an event.
 */
public class RMessageEventReference extends REventReference implements IRMessageEvent
{
	//-------- constructor --------

	/**
	 *  Create a new event.
	 *  @param name The name.
	 *  @param event The event model element.
	 *  @param state The state.
	 *  @param owner The owner.
	 */
	protected RMessageEventReference(String name, IMEventReference event,
		IMConfigMessageEvent state, RElement owner, RReferenceableElement creator)
	{
		super(name, event, state, owner, creator);
	}

	//-------- methods --------

	/**
	 *  Get the platform message.
	 */
	public Object getMessage()
	{
		return ((IRMessageEvent)getReferencedElement()).getMessage();
	}

	/**
	 *  Set the (platform specific) message object.
	 *  @param message	The message object.
	 */
	public void setMessage(Object message)
	{
		((IRMessageEvent)getReferencedElement()).setMessage(message);
	}

	/**
	 *  Get the message direction.
	 *  @return True, if message is incoming.
	 * /
	public boolean isIncoming()
	{
		return ((IRMessageEvent)getReferencedElement()).isIncoming();
	}*/

	/**
	 *  Get the content.
	 *  Allowed content objects depend on the platform.
	 *  @return The content.
	 */
	public Object getContent()
	{
		return ((IRMessageEvent)getReferencedElement()).getContent();
	}

	/**
	 *  Set the content.
	 *  Allowed content objects depend on the platform.
	 *  @param content The content.
	 */
	public void setContent(Object content)
	{
		((IRMessageEvent)getReferencedElement()).setContent(content);
	}

	/**
	 *  Create a reply to this message event.
	 *  @param msgeventtype	The message event type.
	 *  @return The reply event.
	 */
	public IRMessageEvent	createReply(String msgeventtype)
	{
		// todo: this code is exactly (99%) the same as in RMessageEvent, should be unified somehow

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
		MessageType	thistype	= ((IMMessageEvent)this.getOriginalElement().getModelElement()).getMessageType();
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

	/**
	 *  Test, if this message is a reply to another message.
	 *  @return True, if it is a reply.
	 */
	public boolean isReply()
	{
		return ((IRMessageEvent)getReferencedElement()).isReply();
	}

	/**
	 *  Get the original message to reply to.
	 */
	// Hack!!! may be wrong scope...
	public IRMessageEvent getInReplyMessageEvent()
	{
		return ((IRMessageEvent)getReferencedElement()).getInReplyMessageEvent();
	}

	/**
	 *  set the original message (if this is a reply).
	 */
	public void setInReplyMessageEvent(IRMessageEvent event)
	{
		// Hack!!! Sets element with wrong scope.
		((IRMessageEvent)getReferencedElement()).setInReplyMessageEvent(event);
	}

//	/**
//	 *  Get the general message type (fipa, custom, etc.).
//	 *  @return The message type.
//	 */
//	public String getGeneralType()
//	{
//		return ((IRMessageEvent)getReferencedElement()).getGeneralType();
//	}

	/**
	 *  Get a filter for matching the reply of a message.
	 *  // todo: is that right?
	 */
	public IFilter getFilter()
	{
		// Probably needs name of original event??? 
		return new MessageEventFilter(null, getName());
	}

	/**
	 *  Get the unique message id (if any).
	 */
	public String getId()
	{
		return ((IRMessageEvent)getReferencedElement()).getId();
	}

	/**
	 *  Set the unique message id.  
	 */
	public void setId(String id)
	{
		((IRMessageEvent)getReferencedElement()).setId(id);
	}

	/**
	 *  Set the timeout associated with this message event
	 *  (i.e. the time to wait for an answer).
	 * /
	public void	setTimeout(long timeout)
	{
		((IRMessageEvent)getReferencedElement()).setTimeout(timeout);
	}*/

	/**
	 *  Get the timeout associated with this message event
	 *  (i.e. the time to wait for an answer).
	 * /
	public long	getTimeout()
	{
		return ((IRMessageEvent)getReferencedElement()).getTimeout();
	}*/
}

