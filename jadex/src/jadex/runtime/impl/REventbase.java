package jadex.runtime.impl;

import jadex.adapter.*;
import jadex.model.*;
import jadex.runtime.*;
import jadex.util.SReflect;
import jadex.util.SUtil;
import jadex.util.collection.SCollection;

import java.util.*;

/**
 *  The container for all events.
 *  (Currently does not store events?!)
 */
public class REventbase extends RBase
{
	//-------- constants --------

	/** The max number of outstanding messages. */
	public static final String STORED_MESSAGES_MAX = "storedmessages.size";

	//-------- attributes --------

	/** The incoming message events. */
	//protected Set in_mevents;

	/** The outgoing message events. */
	//protected Set out_mevents;

	//-------- conversation handling --------

	/** Send message tracking (reply_with->Entry). */
	//protected Set sent_mevents;
	protected List sent_mevents;

	/** The maximum number of outstanding messages. */
	protected long mevents_max;

	/** The maximum number of outstanding messages. */
	protected long max_mevents_delay;

	/** The binding helpers. */
	protected Map bindings;
	
	/** The message event store. */
	protected List mevent_store;

	//-------- constructors --------

	/**
	 *  Create a new planbase instance.
	 *  @param model	The event base model.
	 *  @param owner	The owner of the instance.
	 */
	protected REventbase(IMEventbase model, RElement owner)
	{
		super(null, model, owner);
//		this.sent_mevents = SCollection.createArrayList();
		this.sent_mevents = SCollection.createWeakList();
		this.bindings = SCollection.createHashMap();
		this.mevent_store = SCollection.createArrayList();
	}

	/**
	 *  Initialize the event base.
	 */
	protected void	init(int level)
	{

		// On constructor initialization, create creation conditions for the goal model elements.
		if(level==0)
		{
			IMMessageEvent[] mevents = ((IMEventbase)getModelElement()).getMessageEvents();
			for(int i=0; i<mevents.length; i++)
			{
				registerEvent(mevents[i]);
			}
			IMInternalEvent[] ievents = ((IMEventbase)getModelElement()).getInternalEvents();
			for(int i=0; i<ievents.length; i++)
			{
				registerEvent(ievents[i]);
			}
		}
		// On action init, create and dispatch initial events.
		else if(level==1)
		{
			Integer cmax = (Integer)getScope().getPropertybase().getProperty(STORED_MESSAGES_MAX);
			if(cmax==null)
				getScope().getLogger().warning("Could not read default conversation max amount in agent properties.");
			else
				mevents_max = cmax.intValue();

			// Create initial events.
			IMConfiguration is = getScope().getConfiguration();
			if(is!=null)
			{
				IMConfigEventbase	initialbase	= is.getEventbase();
				if(initialbase!=null)
				{
					instantiateConfigMessages(initialbase.getInitialMessageEvents());
					instantiateConfigInternalEvents(initialbase.getInitialInternalEvents());
				}
			}
		}
	}

	/**
	 *  Instantiate initial or end events.
	 */
	protected void instantiateConfigInternalEvents(IMConfigInternalEvent[] ies)
	{
		for(int i=0; i<ies.length; i++)
		{
			BindingHelper bh = (BindingHelper)bindings.get(ies[i].getOriginalElement());
			if(bh!=null)
			{
				if(ies[i].getName()!=null)
				{
					throw new RuntimeException("Config elements for elements with bindings cannot have a name: "+ies[i]);
				}
				IMConfigParameter[] inips =  ies[i].getParameters();
				String[] names = new String[inips.length];
				Object[] vals = new Object[inips.length];
				for(int j=0; j<inips.length; j++)
				{
					// todo: does not need to calculate value as set in parameter setInitialValue()
					Object value = getScope().getExpressionbase().evaluateInternalExpression(inips[j].getInitialValue(), this);
					//Object value = inips[j].getInitialValue().getValue(null);
					names[j] = inips[j].getOriginalElement().getName();
					vals[j] = new Object[]{getScope().getExpressionbase().evaluateInternalExpression(inips[j].getInitialValue(), this)};
				}

				List binds = bh.calculateBindings(null, names, vals);

				for(int j=0; j<binds.size(); j++)
				{
					dispatchInternalEvent(createInternalEvent((IMReferenceableElement)ies[i].getOriginalElement()
						, ies[i], null, (Map)binds.get(j)));
				}
			}
			else
			{
				dispatchInternalEvent(createInternalEvent((IMReferenceableElement)ies[i].getOriginalElement(), ies[i], null, null));
			}
		}
	}

	/**
	 *  Instantiate initial or end messages.
	 */
	protected void instantiateConfigMessages(IMConfigMessageEvent[] mes)
	{
		for(int i=0; i<mes.length; i++)
		{
			BindingHelper bh = (BindingHelper)bindings.get(mes[i].getOriginalElement());
			if(bh!=null)
			{
				if(mes[i].getName()!=null)
				{
					throw new RuntimeException("Config elements for elements with bindings cannot have a name: "+mes[i]);
				}
				IMConfigParameter[] inips =  mes[i].getParameters();
				String[] names = new String[inips.length];
				Object[] vals = new Object[inips.length];
				
				for(int j=0; j<inips.length; j++)
				{
					// todo: does not need to calculate value as set in parameter setInitialValue()
					names[j] = inips[j].getOriginalElement().getName();
					vals[j] = new Object []{getScope().getExpressionbase().evaluateInternalExpression(inips[j].getInitialValue(), this)};
					//Object value = inips[j].getInitialValue().getValue(null);
				}

				List binds = bh.calculateBindings(null, names, vals);

				for(int j=0; j<binds.size(); j++)
				{
					sendMessage(createMessageEvent((IMReferenceableElement)mes[i].getOriginalElement(), mes[i], null
						, false, (Map)binds.get(j)));
				}
			}
			else
			{
				IRMessageEvent me = createMessageEvent((IMReferenceableElement)mes[i].getOriginalElement(), mes[i], null, false, null);
				sendMessage(me);
				// Store initial messages with name for being able to get follow-up messages in same conversation
				if(mes[i].getName()!=null)
					mevent_store.add(me);
			}
		}
	}

	//-------- user methods --------

	/**
	 *  Dispatch an event.
	 *  @param event The event.
	 */
	public void dispatchInternalEvent(IRInternalEvent event)
	{
		getScope().dispatchEvent(event, null);
		// info event
    	getScope().throwSystemEvent(new SystemEvent(SystemEvent.INTERNAL_EVENT_OCCURRED, event, null));
	}

	/**
	 *  Create a new intenal event.
	 *  @return The new intenal event.
	 */
	public IRInternalEvent createInternalEvent(String type)
	{
		IMReferenceableElement mevent = ((IMEventbase)getModelElement()).getInternalEvent(type);
		if(mevent==null)
			mevent = ((IMEventbase)getModelElement()).getInternalEventReference(type);
		if(mevent==null)
			throw new RuntimeException("Internal event type not found: "+type);
		return createInternalEvent(mevent, null, null, null);
	}

	/**
	 *  Create a legacy internal event (not explicitly defined in ADF).
	 *  @return The new internal event.
	 */
	public IRInternalEvent createInternalEvent(String type, Object content)
	{
		IMInternalEvent mevent = ((IMEventbase)getModelElement()).getInternalEvent(IMEventbase.LEGACY_INTERNAL_EVENT);
		IRInternalEvent event = createInternalEvent(mevent, null, null, null);
		event.getParameter(IMEventbase.LEGACY_TYPE).setValue(type);
		event.getParameter(IMEventbase.LEGACY_CONTENT).setValue(content);
		return event;
	}

	//-------- message methods --------

	/**
	 *  Send a message after some delay.
	 *  @param me	The message event.
	 *  @return The filter to wait for an answer.
	 */
	public IFilter	sendMessage(IRMessageEvent me)
	{
		// Check if it is allowed to send the message according to model spec.
		String dir = ((IMMessageEvent)me.getOriginalElement().getModelElement()).getDirection();
		if(IMMessageEvent.DIRECTION_RECEIVE.equals(dir))
			throw new RuntimeException("Cannot send message that is declared to be received only: "+me);

		//if(me.isIncoming())
		//	throw new RuntimeException("Cannot send incoming message: "+me);

		// Save sent message.
		registerMessageEvent(me);

		//me.setID("#"+message_no+++"@"+getScope().getAgentName());
		
		getScope().throwSystemEvent(new SystemEvent(SystemEvent.MESSAGE_SENT, me, null));

		// Hack!!! Only send original messages
		IRMessageEvent	ome	= (IRMessageEvent)me.getOriginalElement();
		// First try to send over platform.
		IMessageEventTransport[] ptrans = getScope().getAgentAdapter().getMessageEventTransports();
		boolean sent = false;
		for(int i=0; i<ptrans.length && !sent; i++)
			sent = ptrans[i].sendMessage(ome);
		/*if(!sent)
		{
			// todo: send over Jadex internal transports
		}*/
		if(!sent)
			throw new RuntimeException("No message event transport could send: "+me);

		return new MessageEventFilter(null, me.getName());
	}

	/**
	 *  Create a new message event.
	 */
	public IRMessageEvent createMessageEvent(String type)
	{
		IMReferenceableElement mevent = ((IMEventbase)getModelElement()).getMessageEvent(type);
		if(mevent==null)
			mevent = ((IMEventbase)getModelElement()).getMessageEventReference(type);
		if(mevent==null)
			throw new RuntimeException("Message event type not found: "+type);
		return createMessageEvent(mevent, null, null, false, null);
	}
	
	/**
	 *  Remove a stored message event.
	 *  @param name The instance name to remove the message event.
	 *  // todo: make accessible from wrapper layers!
	 */
	public void removeMessageEvent(String name)
	{
		for(int i=0; i<mevent_store.size(); i++)
		{
			IRMessageEvent me = (IRMessageEvent)mevent_store.get(i);
			if(name.equals(me.getName()))
			{
				mevent_store.remove(me);
				break;
			}
		}
	}

	//-------- internal methods --------

	/**
	 *  Factory method for internal event creation.
	 *  @param model The event model.
	 *  @return The new event.
	 */
	protected IRInternalEvent	createInternalEvent(IMReferenceableElement model,
		IMConfigInternalEvent config, RReferenceableElement creator, Map exparams)
	{
		assert model!=null;
		assert model.getScope()==getScope().getModelElement()
			: "Creation of elements only allowed in definition scope!";
		assert creator==null || creator instanceof IRInternalEvent : creator;

		IRInternalEvent ret = null;

		if(model instanceof IMInternalEvent)
		{
			ret = new RInternalEvent(config!=null ? config.getName() : null, (IMInternalEvent)model, config, this, creator, exparams);
		}
		else if(model instanceof IMInternalEventReference)
		{
			RInternalEventReference rier = new RInternalEventReference(config!=null ? config.getName() : null, (IMEventReference)model, config, this, creator);
			rier.init();
			ret	= rier;
		}
		else
		{
			throw new RuntimeException("Element not an internal event: "+model);
		}

		// Todo: store events in base???
		return ret;
	}

	/**
	 *  Internal factory method for internal event creation.
	 *  @param model The event model.
	 *  @return The new event.
	 */
	public RInternalEvent	createInternalEvent(IMInternalEvent model)
	{
		assert model.getScope()==getScope().getModelElement()
			: "Creation of elements only allowed in definition scope!";

		RInternalEvent	ret	= new RInternalEvent(null, model, null, this, null, null);

		// No references are created for internal events.
		// This means they only will occur in the creation scope.
		// todo: Is this sufficient? No, not for conditions. --> see hack method below :-(

		return ret;
	}

	/**
	 *  Factory method for outgoing message event creation.
	 *  @param model The event model.
	 *  @return The new event.
	 */
	protected IRMessageEvent	createMessageEvent(IMReferenceableElement model,
		IMConfigMessageEvent config, RReferenceableElement creator, boolean incoming, Map exparams)
	{
		assert model.getScope()==getScope().getModelElement()
			: "Creation of elements only allowed in definition scope! "
			+model.getScope().getName()+" "+getScope().getModelElement().getName();
		assert creator==null || creator instanceof IRMessageEvent : creator;

		IRMessageEvent ret = null;

		if(model instanceof IMMessageEvent)
		{
			ret = new RMessageEvent(config!=null? config.getName(): null, (IMMessageEvent)model, config, this, creator, incoming, exparams);
		}
		else if(model instanceof IMMessageEventReference)
		{
			RMessageEventReference rmer = new RMessageEventReference(config!=null? config.getName()
				: null, (IMEventReference)model, config, this, creator);
			rmer.init();
			ret	= rmer;
		}
		else
		{
			throw new RuntimeException("Element not an internal event: "+model);
		}

		// Todo: store events in base???
		return ret;
	}

	/**
	 *  Factory method for incoming message event creation.
	 *  @param model The event model.
	 *  @param msgad	The message object.
	 *  @param original	The original (in reply) message.
	 */
	public void	dispatchIncomingMessageEvent(IMReferenceableElement model, IMessageAdapter msgad, IRMessageEvent original)
	{
		assert model.getScope()==getScope().getModelElement()
			: "Creation of elements only allowed in definition scope!";

		IRMessageEvent msg = createMessageEvent(model, null, null, true, null);
		if(original!=null)
			msg.setInReplyMessageEvent(original);
		AbstractMessageAdapter.prepareReceiving(msg, msgad);
		//msgad.prepareReceiving(msg);

		RCapability	scope	= getScope();
		scope.dispatchEvent(msg, null);

		scope.throwSystemEvent(new SystemEvent(SystemEvent.MESSAGE_RECEIVED, msg, null));
	}

	/**
	 *  Internal factory method for internal event creation.
	 *  @param model The event model.
	 *  @return The new event.
	 */
	public RInternalEvent	createInternalConditionTriggeredEvent(IMInternalEvent model, RCondition cond)
	{
		assert model.getScope()==getScope().getModelElement()
			: "Creation of elements only allowed in definition scope!";

		return (RInternalEvent)createElementStructure(cond, new ConditionEventCreator(model, cond));
	}

	/**
	 *  Internal factory method for goal event creation.
	 *  @param goal	The goal.
	 *  @return The new event.
	 */
	public RGoalEvent	createGoalEvent(RGoal goal, boolean info)
	{
		IMGoalEvent model	= ((IMGoal)goal.getModelElement()).getGoalEvent();
		assert model.getScope()==getScope().getModelElement()
			: "Creation of elements only allowed in definition scope!";

		return (RGoalEvent)createElementStructure(goal, new GoalEventCreator(model, goal, info));
	}

	/**
	 *  Create a reference to the given event.
	 *  @param reference	The reference model.
	 *  @param creator	The creator to be referenced.
	 */
	protected RMessageEventReference	createMessageEventReference(
			IMMessageEventReference reference, RReferenceableElement creator)
	{
		assert reference.getScope()==getScope().getModelElement()
			: "Creation of elements only allowed in definition scope!";
		assert creator==null || creator instanceof IRMessageEvent : creator;

		RMessageEventReference	eventref	= new RMessageEventReference(null, reference, null, this, creator);
//		// Hack!!! Otherwise original event does not know reference.
//		eventref.resolveReference(creator);
//		eventref.init();

		return eventref;
	}

	/**
	 *  Create a reference to the given event.
	 *  @param reference	The reference model.
	 *  @param creator	The creator to be referenced.
	 */
	protected RInternalEventReference	createInternalEventReference(
			IMInternalEventReference reference, RReferenceableElement creator)
	{
		assert reference.getScope()==getScope().getModelElement()
			: "Creation of elements only allowed in definition scope!";
		assert creator==null || creator instanceof IRInternalEvent : creator;

		RInternalEventReference	eventref	= new RInternalEventReference(null, reference, null, this, creator);
//		// Hack!!! Otherwise original event does not know reference.
//		eventref.resolveReference(creator);
//		eventref.init();

		return eventref;
	}

	/**
	 *  Create a reference to the given event.
	 *  @param reference	The reference model.
	 *  @param creator	The creator to be referenced.
	 */
	protected RGoalEventReference	createGoalEventReference(
			IMGoalEventReference reference, RReferenceableElement creator, RGoalReference goalref)
	{
		assert reference!=null;
		assert goalref!=null;
		assert reference.getScope()==getScope().getModelElement()
			: "Creation of elements only allowed in definition scope!";
		assert creator==null || creator instanceof IRGoalEvent : creator;

		RGoalEventReference	eventref	= new RGoalEventReference(null, reference, this, creator, goalref);
//		// Hack!!! Otherwise original event does not know reference.
//		eventref.resolveReference(creator);
//		eventref.init();

		return eventref;
	}

	/**
	 *  Register a new event model.
	 *  @param mevent The event model.
	 */
	public void registerEvent(IMEvent mevent)
	{
		// Create bindings.
		BindingHelper	binding = null;
		if(mevent.getBindingParameters().length>0)
		{
			binding	= new BindingHelper(mevent, this, false);
			bindings.put(mevent, binding);
		}
	}

	/**
	 *  Deregister an event model.
	 *  @param mevent The event model.
	 */
	public void deregisterEvent(IMEvent mevent)
	{
		BindingHelper bh = (BindingHelper)bindings.remove(mevent);
		if(bh!=null)
			bh.cleanup();
	}

	/**
	 *  Register a new event reference model.
	 *  @param meventref The event reference model.
	 */
	public void registerEventReference(IMEventReference meventref)
	{
		// todo: NOP?
	}


	/**
	 *  Deregister an event reference model.
	 *  @param meventref The event reference model.
	 */
	public void deregisterEventReference(IMEventReference meventref)
	{
		// todo: NOP?
	}

	//-------- RBase abstract methods --------

	/**
	 *  Get the runtime element for a model element.
	 *  Depending on the type it might have to be created (e.g. a goal)
	 *  or might be already there (e.g. belief).
	 *  @param melement	The model of the element to be retrieved.
	 *  @param creator	The creator of the element (e.g. a reference).
	 */
	protected RReferenceableElement	getElementInstance(
			IMReferenceableElement melement, RReferenceableElement creator)
	{
		RCapability	scope	= getScope().getAgent()
			.lookupCapability(melement.getScope());

		//System.out.println("looking up: "+scope);
// Goal events are never created by users.
//		if(melement instanceof IMGoalEvent || melement instanceof IMGoalEventReference)
//		{
//			return (RReferenceableElement)scope.getEventbase().createGoalEvent(melement);
//		}

		if(melement instanceof IMMessageEvent || melement instanceof IMMessageEventReference)
		{
			return (RReferenceableElement)scope.getEventbase().createMessageEvent(melement, null, creator, false, null);
		}
		else if(melement instanceof IMInternalEvent || melement instanceof IMInternalEventReference)
		{
			return (RReferenceableElement)scope.getEventbase().createInternalEvent(melement, null, creator, null);
		}
		else
			throw new RuntimeException("Cannot create element (neither internal nor message event): "+melement);
	}

	/**
	 *  Get the corresponding base of a given scope.
	 *  Depending on the type of this base (e.g. beliefbase),
	 *  the corresponding base of the given scope is returned.
	 *  @param scope	The scope.
	 *  @return	The corresponding base.
	 */
	protected RBase getCorrespondingBase(RCapability scope)
	{
		return scope.getEventbase();
	}

	/**
	 *  Internal factory method for creation of structured elements
	 *  (elements with references).
	 *  @param orig_structure The element with a structure that will be copied for the new element.
	 *  @param creator The creator element capable of creating elements.
	 *  @return The new element structure.
	 */
	public RElement	createElementStructure(IRReferenceableElement orig_structure, IElementCreator creator)
	{
		// Must be the original element.
		assert !(orig_structure instanceof RElementReference);
		
		RReferenceableElement ret = creator.create();

		if(ret!=null)
		{
			List structure_elems = SCollection.createArrayList();
			List created_elems	= SCollection.createArrayList();

			structure_elems.add(orig_structure);
			created_elems.add(ret);

			while(structure_elems.size()>0)
			{
				RReferenceableElement structure_elem = (RReferenceableElement)structure_elems.remove(0);
				RReferenceableElement new_elem = (RReferenceableElement)created_elems.remove(0);
				List	refs	= structure_elem.getReferences();
				for(int i=0; i<refs.size(); i++)
				{
					RReferenceableElement structure_ref = (RReferenceableElement)refs.get(i);
					RElementReference new_ref = creator.createReference(structure_ref, new_elem);
					if(new_ref!=null)
					{
						structure_elems.add(structure_ref);
						created_elems.add(new_ref);
					}
				}
			}
		}

		assert ret!=null: "Created structure nulls: "+creator;

		ret.initStructure();
		return ret;
	}

	//-------- conversation handling --------

	/**
	 *  Register a conversation or reply-with to be able
	 *  to send back answers to the source capability.
	 *  @param msgevent The message event.
	 *  //@param replywith The reply-with tag.
	 *  todo: indexing for msgevents for speed.
	 */
	protected void registerMessageEvent(IRMessageEvent msgevent)
	{
		if(mevents_max!=0 && sent_mevents.size()>mevents_max)
		{
			getScope().getLogger().severe("Agent does not save conversation due " +
				"to too many outstanding messages. Increase buffer in runtime.xml - storedmessages.size");
		}
		else
		{
			sent_mevents.add(msgevent);
			//System.out.println("+++"+getScope().getAgent().getName()+" has open conversations: "+sent_mevents.size()+" "+sent_mevents);
		}
	}

	/**
	 *  Find a message event that the given native message is a reply to.
	 *  @param message	The (native) message.
	 */
	public IRMessageEvent	getInReplyMessageEvent(IMessageAdapter message)
	{
		//System.out.println("+++"+getScope().getAgent().getName()+" has open conversations: "+sent_mevents.size()+" "+sent_mevents);
		
		IRMessageEvent	ret	= null;
		// Prefer the newest messages for finding replies.
		// todo: conversations should be better supported
		IRMessageEvent[] smes = (IRMessageEvent[])sent_mevents.toArray(new IRMessageEvent[0]);
		for(int i=smes.length-1; ret==null && i>-1; i--)
		{
			boolean	match	= true;	// Does the message match all convid parameters?
			boolean	matched	= false;	// Does the message match at least one (non-null) convid parameter?
			MessageType	type	= ((IMMessageEvent)smes[i].getOriginalElement().getModelElement()).getMessageType();

			MessageType.ParameterSpecification[]	params	= type.getParameters();
			for(int j=0; match && j<params.length; j++)
			{
				if(params[j].isConversationIdentifier())
				{
					Object	sourceval	= smes[i].getParameter(params[j].getSource()).getValue();
					Object	destval	= message.getValue(params[j].getName(), getScope());
					match	= SUtil.equals(sourceval, destval);
					matched	= matched || sourceval!=null;
				}
			}

			MessageType.ParameterSpecification[]	paramsets	= type.getParameterSets();
			for(int j=0; match && j<paramsets.length; j++)
			{
				if(paramsets[j].isConversationIdentifier())
				{
					Object	sourceval	= smes[i].getParameter(paramsets[j].getSource()).getValue();
					Iterator	it2	= SReflect.getIterator(message.getValue(paramsets[j].getName(), getScope()));
					while(it2.hasNext())
					{
						Object	destval	= it2.next();
						match	= SUtil.equals(sourceval, destval);
						matched	= matched || sourceval!=null;
					}
				}
			}
			
			if(matched && match)
			{
				ret	= smes[i];
			}
		}
		
		return ret;
	}

	/**
	 *  Activate the end state of the eventbase.
	 *  Ccreates the events specified in the end state.
	 */
	public void activateEndState()
	{
		// Instantiate events from end state.
		IMConfiguration config = getScope().getConfiguration();
		if(config!=null)
		{
			IMConfigEventbase	eventbase	= config.getEventbase();
			if(eventbase!=null)
			{
				instantiateConfigMessages(eventbase.getEndMessageEvents());
				instantiateConfigInternalEvents(eventbase.getEndInternalEvents());
			}
		}
	}

	//-------- static part --------

	/**
	 *  Get the original event of the given event.
	 *  Resolves all references (if any).
	 */
	protected static IREvent	getOriginalEvent(IREvent event)
	{
		// Resolve references.
		while(event instanceof REventReference)
			event	= (IREvent)((REventReference)event).getReferencedElement();

		return event;
	}

	//-------- inner classes --------

	/**
	 *  The creator for condition event structures.
	 */
	public class ConditionEventCreator implements IElementCreator
	{
		/** The model element. */
		protected IMInternalEvent model;

		/** The condition. */
		protected RCondition cond;

		/**
		 *  Create a new condition event creator.
		 *  @param model The model element of the event to create.
		 *  @param cond The condition.
		 */
		public ConditionEventCreator(IMInternalEvent model, RCondition cond)
		{
			this.model = model;
			this.cond = cond;
		}

		/**
		 *  Create a new original element.
		 *  @return The new original element.
		 */
		public RReferenceableElement create()
		{
			RInternalEvent ret = new RInternalEvent(null, model, null, REventbase.this, null, null);
			ret.getParameter(IMEventbase.CONDITION).setValue(cond);
			return ret;
		}

		/**
		 *  Create a new reference element.
		 *  @param ref The orig structure element (Only the scope!) is interesting.
		 *  @param creator The element to which the new one will be connected.
		 *  @return The new reference element.
		 */
		public RElementReference createReference(RReferenceableElement ref, RReferenceableElement creator)
		{
			IMInternalEventReference	mevent	= ((IMCapability)ref.getScope().getModelElement())
				.getEventbase().getInternalEventReference(IMEventbase.TYPE_CONDITION_TRIGGERED_REFERENCE);
			RInternalEventReference	eventref	= ref.getScope()
				.getEventbase().createInternalEventReference(mevent, creator);
			// HACK!
			eventref.setCondition((RConditionReference)ref);
			return eventref;
		}
	}

	/**
	 *  The creator for goal event structures.
	 */
	public class GoalEventCreator implements IElementCreator
	{
		/** The model element. */
		protected IMGoalEvent model;

		/** The goal. */
		protected IRGoal goal;

		/** The info flag. */
		protected boolean info;

		/**
		 *  Create a new goal event creator.
		 *  @param model The model element of the event to create.
		 *  @param goal The goal.
		 *  @param info Is it an info event.
		 */
		public GoalEventCreator(IMGoalEvent model, IRGoal goal, boolean info)
		{
			this.model = model;
			this.goal = goal;
			this.info = info;
		}

		/**
		 *  Create a new original element.
		 *  @return The new original element.
		 */
		public RReferenceableElement create()
		{
			return new RGoalEvent(model, REventbase.this, goal, info, null);
		}

		/**
		 *  Create a new reference element.
		 *  @param ref The orig structure element (Only the scope!) is interesting.
		 *  @param creator The element to which the new one will be connected.
		 *  @return The new reference element.
		 */
		public RElementReference createReference(RReferenceableElement ref, RReferenceableElement creator)
		{
			IMGoalEventReference	mevent	= ((IMCapability)ref.getScope().getModelElement())
				.getEventbase().getGoalEventReference(IMEventbase.STANDARD_GOAL_EVENT_REFERENCE);
			if(mevent==null)
			{
				mevent	= ((IMCapability)ref.getScope().getModelElement())
					.getEventbase().getGoalEventReference(IMEventbase.STANDARD_GOAL_EVENT_REFERENCE);
			}
			RGoalEventReference	eventref	= ref.getScope().getEventbase()
				.createGoalEventReference(mevent, creator, (RGoalReference)ref);
			return eventref;
		}
	}
}

/**
 *  The interface for all creators that are passed as visitor
 *  to the createElementStructure() method.
 */
interface IElementCreator
{
	/**
	 *  Create a new original element.
	 *  @return The new original element.
	 */
	public RReferenceableElement create();

	/**
	 *  Create a new reference element.
	 *  @param ref The orig structure element (Only the scope!) is interesting.
	 *  @param creator The element to which the new one will be connected.
	 *  @return The new reference element.
	 */
	public RElementReference createReference(RReferenceableElement ref, RReferenceableElement creator);
}
