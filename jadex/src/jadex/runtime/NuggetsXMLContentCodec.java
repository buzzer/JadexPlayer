package jadex.runtime;

import nuggets.Nuggets;

import java.io.Serializable;
import java.util.Properties;

/**
 *  The XML codec based on the nuggest framework.
 */
public class NuggetsXMLContentCodec implements IContentCodec, Serializable
{
	//-------- constants --------
	
	/** The language identifier. */
	public static final String	NUGGETS_XML	= "nuggets-xml";
	
	/** The nuggets codec. */
	protected static Nuggets nuggets;

	/**
	 *  Test if the codec can be used with the provided meta information.
	 *  @param props The meta information.
	 *  @return True, if it can be used.
	 */
	public boolean match(Properties props)
	{
		return NUGGETS_XML.equals(props.getProperty("language"));	// Hack!!! avoid dependency to fipa
	}

	/**
	 *  Encode data with the codec.
	 *  @param val The value.
	 *  @return The encoded object.
	 */
	public String encode(Object val)
	{
		if(nuggets == null)
			nuggets = new Nuggets();
		return nuggets.toXML(val);
	}

	/**
	 *  Decode data with the codec.
	 *  @param val The string value.
	 *  @return The encoded object.
	 */
	public Object decode(String val)
	{
		if(nuggets == null)
			nuggets = new Nuggets();
		return nuggets.fromXML(val);
	}
}
