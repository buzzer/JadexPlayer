package jadex.adapter.fipa;

import java.util.Date;
import jadex.model.MessageType;

/**
 *  The FIPA message type.
 */
public class FIPAMessageType extends MessageType
{
	//-------- constructors --------

	/**
	 *  Create a new fipa message type.
	 */
	public FIPAMessageType()
	{
		super(SFipa.MESSAGE_TYPE_NAME_FIPA, new MessageType.ParameterSpecification[]
		{
			// Std. parameters
			new MessageType.ParameterSpecification(SFipa.PERFORMATIVE, String.class),
			new MessageType.ParameterSpecification(SFipa.SENDER, AgentIdentifier.class),
			new MessageType.ParameterSpecification(SFipa.REPLY_TO, AgentIdentifier.class),
			new MessageType.ParameterSpecification(SFipa.CONTENT, Object.class),
			new MessageType.ParameterSpecification(SFipa.LANGUAGE, String.class, null, SFipa.LANGUAGE, false),
			new MessageType.ParameterSpecification(SFipa.ENCODING, String.class, null, SFipa.ENCODING, false),
			new MessageType.ParameterSpecification(SFipa.ONTOLOGY, String.class, null, SFipa.ONTOLOGY, false),
			new MessageType.ParameterSpecification(SFipa.PROTOCOL, String.class, null, SFipa.PROTOCOL, false),
			new MessageType.ParameterSpecification(SFipa.REPLY_WITH, String.class),
			new MessageType.ParameterSpecification(SFipa.IN_REPLY_TO, String.class, null, SFipa.REPLY_WITH, true),
			new MessageType.ParameterSpecification(SFipa.CONVERSATION_ID, String.class, null, SFipa.CONVERSATION_ID, true),
			new MessageType.ParameterSpecification(SFipa.REPLY_BY, Date.class),

			// Derived parameters.
			new MessageType.ParameterSpecification(SFipa.CONTENT_START, String.class, true),
			new MessageType.ParameterSpecification(SFipa.CONTENT_CLASS, Class.class, true),
			new MessageType.ParameterSpecification(SFipa.ACTION_CLASS, Class.class, true)
		},
		// Second parameter represents the parameter sets.
		new MessageType.ParameterSpecification[]
		{
			new MessageType.ParameterSpecification(SFipa.RECEIVERS, AgentIdentifier.class, null, SFipa.SENDER, false)
		});
	}

	//-------- methods --------

	/**
	 *  Get the identifier for fetching the receivers.
	 *  @return The receiver identifier.
	 */
	public String getReceiverIdentifier()
	{
		return SFipa.RECEIVERS;
	}

	/**
	 *  Get the identifier for fetching the sender.
	 *  @return The sender identifier.
	 */
	public String getSenderIdentifier()
	{
		return SFipa.SENDER;
	}
}
