package jadex.adapter.standalone;

import jadex.adapter.IMessageEventTransport;
import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.fipa.SFipa;
import jadex.model.IMMessageEvent;
import jadex.model.MessageType;
import jadex.runtime.IContentCodec;
import jadex.runtime.ContentException;
import jadex.runtime.MessageFailureException;
import jadex.runtime.impl.IRMessageEvent;
import jadex.util.collection.SCollection;

import java.util.Map;
import java.util.Properties;


/**
 *
 */
public class StandaloneMessageEventTransport implements IMessageEventTransport
{
	//-------- attributes --------

	/** The standalone agent adapter. */
	protected  StandaloneAgentAdapter agent;

	//-------- constructors --------

	/**
	 *  Create a new standalone local message event transport.
	 *  @param agent The standalone agent.
	 */
	public StandaloneMessageEventTransport(StandaloneAgentAdapter agent)
	{
		this.agent = agent;
	}

	//-------- methods --------

	/**
	 *  Send a message event.
	 *  @param msgevent The message event.
	 */
	public boolean sendMessage(IRMessageEvent msgevent)
	{
		MessageType	type	= ((IMMessageEvent)msgevent.getModelElement()).getMessageType();
		MessageType.ParameterSpecification[]	params	= type.getParameters();
		MessageType.ParameterSpecification[]	paramsets	= type.getParameterSets();

		// Prepare msg as map and envelope
		Map message = SCollection.createHashMap();		
		
		// Copy all parameter values to the map.		
		for(int i=0; i<params.length; i++)
		{
			if(!params[i].isDerived())
				message.put(params[i].getName(), msgevent.getParameter(params[i].getName()).getValue());
		}

		// Copy all parameter set values to the map.
		for(int i=0; i<paramsets.length; i++)
		{
			if(!paramsets[i].isDerived())
				message.put(paramsets[i].getName(), msgevent.getParameterSet(paramsets[i].getName()).getValues());
		}

		// Handle FIPA messages.
		if(SFipa.MESSAGE_TYPE_NAME_FIPA.equals(type.getName()))
		{
			// Check if message receivers are ok
			AgentIdentifier[] recs = (AgentIdentifier[])msgevent.getParameterSet(SFipa.RECEIVERS).getValues();
			if(recs.length==0)
				throw new MessageFailureException(msgevent, "No receivers specified");
			for(int i=0; i<recs.length; i++)
			{
				if(recs[i]==null)
					throw new MessageFailureException(msgevent, "A receiver nulls");
			}
			
			message.put(SFipa.X_MESSAGE_ID, msgevent.getId());
			
			if(msgevent.getContent()!=null)
			{
				Properties props = new Properties();
				String lang = (String)msgevent.getParameter(SFipa.LANGUAGE).getValue();
				String onto = (String)msgevent.getParameter(SFipa.ONTOLOGY).getValue();
				if(lang!=null)
					props.put(SFipa.LANGUAGE, lang);
				if(onto!=null)
					props.put(SFipa.ONTOLOGY, onto);

				IContentCodec codec = msgevent.getScope().getContentCodec(props);

				// todo: require lang+onto==null?
				if(codec==null && !(msgevent.getContent() instanceof String))
					throw new ContentException("No content codec found for: "+props);

				if(codec!=null)
					message.put(SFipa.CONTENT, codec.encode(msgevent.getContent()));
				else
					message.put(SFipa.CONTENT, msgevent.getContent());
			}
		}

		// Add sender if empty.
		if(message.get(SFipa.SENDER)==null)
			message.put(SFipa.SENDER, agent.getAgentIdentifier());
		
		IMessageEnvelope msgenv = new MessageEnvelope(message, (AgentIdentifier[])message.get(SFipa.RECEIVERS), type.getName());
		
		agent.getPlatform().getMessageService().sendMessage(msgenv);
		return true;
	}

}
