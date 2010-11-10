package jadex.model;

import java.io.Serializable;


/**
 *  Representation of characteristics of a certain type of messages
 *  (e.g. fipa messages).
 */
public abstract class MessageType	implements Serializable //, Cloneable // todo
{
	//-------- attributes --------

	/** The name of the message type. */
	protected String	name;

	/** The allowed parameters. */
	protected ParameterSpecification[]	params;

	/** The allowed parameter sets. */
	protected ParameterSpecification[]	paramsets;

	//-------- constructors --------

	/**
	 *  Create a new message type.
	 *  @param name	The name of the message type.
	 */
	public MessageType(String name, ParameterSpecification[] params, ParameterSpecification[] paramsets)
	{
		this.name	= name;
		this.params	= params;
		this.paramsets	= paramsets;
	}

	//-------- methods --------

	/**
	 *  The name of the message type.
	 */
	public String	getName()
	{
		return name;
	}

	/**
	 *  Get the default parameters.
	 */
	public ParameterSpecification[]	getParameters()
	{
		return params;
	}

	/**
	 *  Get the default parameter sets.
	 */
	public ParameterSpecification[]	getParameterSets()
	{
		return paramsets;
	}

	/**
	 *  Get all parameter names.
	 *  @return The parameter names.
	 */
	public String[] getParameterNames()
	{
		String[] names = new String[params.length];
		for(int i=0; i<names.length; i++)
		{
			names[i] = params[i].getName();
		}
		return names;
	}

	/**
	 *  Get all parameter set names.
	 *  @return The parameter set names.
	 */
	public String[] getParameterSetNames()
	{
		String[] names = new String[paramsets.length];
		for(int i=0; i<names.length; i++)
		{
			names[i] = paramsets[i].getName();
		}
		return names;
	}

	/**
	 *  Get the identifier for fetching the receivers.
	 *  @return The receiver identifier.
	 */
	public abstract String getReceiverIdentifier();

	/**
	 *  Get the identifier for fetching the sender.
	 *  @return The sender identifier.
	 */
	public abstract String getSenderIdentifier();

	/**
	 *  Test if two message types are equal (based on the name).
	 */
	public boolean	equals(Object o)
	{
		return o instanceof MessageType && name.equals(((MessageType)o).getName()); 
	}

	//-------- inner classes --------

	/**
	 *  A class representing a parameter or parameter set specification.
	 */
	public static class ParameterSpecification	implements Serializable
	{
		//-------- attributes --------

		/** The parameter(set) name. */
		protected String	name;

		/** The parameter(set) class. */
		protected Class	clazz;

		/** Default value expression of the parameter(set), if any. */
		protected String	defaultvalue;

		/** Source parameter when copying reply values (if any). */
		// Todo: support source parameter sets?
		protected String	source;

		/** True, if this parameter can be used to identify an ongoing conversation. */
		// Todo: support convid parameter sets?
		protected boolean	convid;

		/** A derived parameter is ignored, when sending/receiving messages and only used for matching incoming messages. */
		protected boolean	derived;

		/** A layered parameter needs a codec to be en/decoded. This meta info specifies which other parameters are needed
		 	to find the codec. todo */
		//protected String[] codecinfos;

		//-------- constructors --------
	
		/**
		 *  Create a parameter(set) specification.
		 */
		public ParameterSpecification(String name, Class clazz)
		{
			this(name, clazz, null, null, false);//, null);
		}

		/**
		 *  Create a parameter(set) specification.
		 */
		public ParameterSpecification(String name, Class clazz, boolean derived)
		{
			this(name, clazz, null, null, false);//, null);
			this.derived	= derived;
		}

		/**
		 *  Create a parameter(set) specification.
		 */
		public ParameterSpecification(String name, Class clazz, String defaultvalue, String source, boolean convid)//, String[] codecinfos)
		{
			// Conversation identifying parameters must have a source (to match against).
			assert !convid || source!=null;

			this.name	= name;
			this.clazz	= clazz;
			this.defaultvalue	= defaultvalue;
			this.source	= source;
			this.convid	= convid;
			//this.codecinfos = codecinfos==null? new String[0]: codecinfos;
		}

		//-------- methods --------

		/**
		 *  Get the name of the parameter(set).
		 */
		public String	getName()
		{
			return name;
		}

		/**
		 *  Get the clazz of the parameter(set).
		 */
		public Class	getClazz()
		{
			return clazz;
		}

		/**
		 *  Get the default value of the parameter(set).
		 */
		public String	getDefaultValue()
		{
			return defaultvalue;
		}

		/**
		 *  Get the source parameter for copying reply values (if any).
		 */
		// Todo: support source parameter sets?
		public String	getSource()
		{
			return source;
		}

		/**
		 *  True, if this parameter can be used to identify an ongoing conversation.
		 */
		// Todo: support convid parameter sets?
		public boolean	isConversationIdentifier()
		{
			return convid;
		}

		/**
		 *  A derived parameter is ignored, when sending/receiving messages and only used for matching incoming messages.
		 */
		public boolean	isDerived()
		{
			return derived;
		}

		/**
		 *  Get the codecinfos.
		 *  @return The codec infos.
		 * /
		public String[] getCodecInfos()
		{
			return codecinfos;
		}*/
	}
}
