package jadex.runtime.impl.agenda.agent;

import jadex.adapter.IMessageAdapter;
import jadex.model.*;
import jadex.runtime.impl.*;
import jadex.runtime.impl.agenda.AbstractElementAgendaAction;
import jadex.runtime.impl.agenda.IAgendaActionPrecondition;
import jadex.util.SReflect;
import jadex.util.collection.SCollection;

import java.io.*;
import java.util.List;

/**
 *  Agenda action to handle a received message.
 *  Added from @link #messageArrived(IMessageAdapter).
 */
public class MessageArrivalAction	extends AbstractElementAgendaAction implements Serializable
{
	//-------- attributes --------

	/** The agent. */
	protected RBDIAgent agent;

	/** The message adapter. */
	protected IMessageAdapter	message;

	//-------- constructors --------

	/**
	 *  Create a new action.
	 *  @param agent The agent.
	 *  @param precond The precondition.
	 *  @param message The message.
	 */
	public MessageArrivalAction(RBDIAgent agent, IAgendaActionPrecondition precond, IMessageAdapter message)
	{
		super(agent, precond);
		this.agent = agent;
		this.message	= message;
	}

	//-------- methods --------

	/**
	 *  Initialize the agent.
	 */
	public void execute()
	{
		//System.out.println("MessageArrivalAction: "+agent.getAgentName()+" "+message);
		
		// Find the event to which the message is a reply (if any).
		IRMessageEvent original = null;
		List	capas = agent.getAllCapabilities();
		for(int i=0; i<capas.size(); i++)
		{
			IRMessageEvent	rep	= ((RCapability)capas.get(i)).getEventbase().getInReplyMessageEvent(message);
			if(rep!=null && original!=null)
			{
				agent.getLogger().severe("Cannot match reply message (multiple capabilities "+rep.getScope().getName()+", "+original.getScope().getName()+") for: "+message);
				return;	// Hack!!! Ignore message?
			}
			else if(rep!=null)
			{
				original	= rep;
				// Todo: break if production mode.
			}
		}

		// Find all matching event models for received message.
		List	events	= SCollection.createArrayList();
		List	matched	= SCollection.createArrayList();
		int	degree	= 0;

		// For messages without conversation all capabilities are considered.
		if(original==null)
		{
			// Search through event bases to find matching events.
			// Only original message events are considered to respect encapsualtion of a capability.
			//Object	content	= extractMessageContent(msg);
			for(int i=0; i<capas.size(); i++)
			{
				RCapability capa = (RCapability)capas.get(i);
				IMEventbase eb = (IMEventbase)capa.getEventbase().getModelElement();
				degree = matchMessageEvents(message, eb.getMessageEvents(), matched, events, degree);
			}
		}

		// For messages of ongoing conversations only the source capability is considered.
		else
		{
			//System.out.println("Found reply :-) "+original);
			RCapability capa = original.getScope();
			IMEventbase eb = (IMEventbase)capa.getEventbase().getModelElement();

			degree = matchMessageEvents(message, eb.getMessageEvents(), matched, events, degree);
			degree = matchMessageEventReferences(message, eb.getMessageEventReferences(), matched, events, degree);
		}

		if(events.size()==0)
		{
			agent.getLogger().warning(agent.getAgentName()+" cannot process message, no message event matches: "+message.getMessage());
		}
		else
		{
			if(events.size()>1)
			{
				// Multiple matches of highest degree.
				agent.getLogger().severe(agent.getAgentName()+" cannot decide which event matches message, " +
					"using first: "+message.getMessage()+", "+events);
			}
			else if(matched.size()>1)
			{
				// Multiple matches but different degrees.
				agent.getLogger().info(agent.getAgentName()+" multiple events matching message, using " +
					"message event with highest specialization degree: "+message+" ("+degree+"), "+events.get(0)+", "+matched);
			}

			IMReferenceableElement	mevent	= (IMReferenceableElement)events.get(0);
			RCapability	scope	= agent.lookupCapability(mevent.getScope());
			scope.getEventbase().dispatchIncomingMessageEvent(mevent, message, original);
		}
	}

	/**
	 *  Match message events with a message adapter.
	 */
	protected int matchMessageEvents(IMessageAdapter message, IMMessageEvent[] mevents, List matched, List events, int degree)
	{
		for(int i=0; i<mevents.length; i++)
		{
			Object dir = mevents[i].getDirection();

			try
			{
				if((dir.equals(IMMessageEvent.DIRECTION_RECEIVE)
					|| dir.equals(IMMessageEvent.DIRECTION_SEND_RECEIVE))
					&& message.match(mevents[i]))
				{
					matched.add(mevents[i]);
					if(mevents[i].getSpecializationDegree()>degree)
					{
						degree	= mevents[i].getSpecializationDegree();
						events.clear();
						events.add(mevents[i]);
					}
					else if(mevents[i].getSpecializationDegree()==degree)
					{
						events.add(mevents[i]);
					}
				}
			}
			catch(RuntimeException e)
			{
				StringWriter	sw	= new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				agent.getLogger().severe(sw.toString());
			}
		}
		return degree;
	}

	/**
	 *  Match message event references with a message adapter.
	 */
	protected int matchMessageEventReferences(IMessageAdapter message, IMMessageEventReference[] meventrefs,
		List matched, List events, int degree)
	{
		for(int i=0; i<meventrefs.length; i++)
		{
			IMMessageEvent morig = (IMMessageEvent)meventrefs[i].getOriginalElement();
			Object dir = morig.getDirection();

			try
			{
				if((dir.equals(IMMessageEvent.DIRECTION_RECEIVE)
					|| dir.equals(IMMessageEvent.DIRECTION_SEND_RECEIVE))
					&& message.match(morig))
				{
					matched.add(meventrefs[i]);
					if(morig.getSpecializationDegree()>degree)
					{
						degree	= morig.getSpecializationDegree();
						events.clear();
						events.add(meventrefs[i]);
					}
					else if(morig.getSpecializationDegree()==degree)
					{
						events.add(meventrefs[i]);
					}
				}
			}
			catch(RuntimeException e)
			{
				StringWriter	sw	= new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				agent.getLogger().severe(sw.toString());
			}
		}
		return degree;
	}

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public  String toString()
	{
		return SReflect.getInnerClassName(this.getClass())+" (agent="+agent.getName()+")";
	}
}
