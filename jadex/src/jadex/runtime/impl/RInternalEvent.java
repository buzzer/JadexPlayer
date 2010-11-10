package jadex.runtime.impl;

import java.util.*;
import jadex.model.*;
import jadex.util.SReflect;
import jadex.util.collection.*;

/**
 *  An internal event occurs inside of an agent.
 */
public class RInternalEvent extends REvent implements IRInternalEvent
{
	//-------- constructors --------

	/**
	 *  Create a intenal event with additional custom values.
	 */
	protected RInternalEvent(String name, IMInternalEvent event, IMConfigInternalEvent state,
			RElement owner, RReferenceableElement creator, Map exparams)
 	{
		super(name, event, state, owner, creator, exparams);
	}

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public String toString()
	{
		StringBuffer	buf	= new StringBuffer();
		buf.append(SReflect.getInnerClassName(this.getClass()));
		buf.append("(type=");
		buf.append(getType());
		
		// Add hacktype (Hack???)
		if(parameters!=null && parameters.get(IMEventbase.LEGACY_TYPE)!=null)
		{
			try
			{
				String	hacktype	= (String)getParameter(IMEventbase.LEGACY_TYPE).getValue();
				buf.append(", hacktype=");
				buf.append(hacktype);
			}
			catch(Exception e){}
		}

		//		buf.append(", content=");
//		buf.append(getContent());
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
		//rep.put("content", content);

			// Encodable representation of parameters.
		Map	encparams = SCollection.createHashMap();
		encparams.put("isencodeablepresentation", "true");
		RElement[] params = (RElement[])parameters.values().toArray(new RElement[parameters.size()]);
		for(int i=0; i<params.length; i++)
		{
			encparams.put(params[i].getName(), params[i].getEncodableRepresentation());
		}
		rep.put("parameters", encparams);

		return rep;
	}
}

