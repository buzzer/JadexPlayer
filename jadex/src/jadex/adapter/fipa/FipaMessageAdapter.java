package jadex.adapter.fipa;

import jadex.adapter.AbstractMessageAdapter;
import jadex.adapter.IJadexAgent;
import jadex.model.*;
import jadex.runtime.ContentException;
import jadex.runtime.IContentCodec;
import jadex.runtime.impl.RCapability;
import jadex.runtime.impl.RExpression;
import jadex.util.SReflect;
import jadex.util.SUtil;
import jadex.util.collection.SCollection;
import jadex.util.collection.NestedMap;

import java.util.*;

/**
 *  Generic implementation of the message adapter functionality,
 *  supporting FIPA messages.
 *  Only the getParameterOrSetValue() has to be implemented.
 */
public abstract class FipaMessageAdapter extends AbstractMessageAdapter
{
	//-------- attributes --------

	/** The decoded content (cached for speed). */
	// Hack!!! implement this generically?
	protected Object	content;

	//-------- constructors --------

	/**
	 *  Create a message adapter for the given message object.
	 */
	public FipaMessageAdapter(IJadexAgent agent, Object message)
	{
		super(agent, message);
	}

	//-------- IMessageAdapter interface --------

	/**
	 *  Match a message with a message event.
	 *  @param msgevent The message event.
	 *  @return True, if message matches the message event.
	 */
	public boolean match(IMMessageEvent msgevent)
	{
		boolean	match	= SFipa.MESSAGE_TYPE_NAME_FIPA.equals(msgevent.getMessageType().getName());

		RCapability scope = agent.lookupCapability(msgevent.getScope());

		// Match against parameters specified in the event type.
		IMParameter[]	params	= msgevent.getParameters();
		for(int i=0; match && i<params.length; i++)
		{
			if(params[i].getDirection().equals(IMParameter.DIRECTION_FIXED) && params[i].getDefaultValue()!=null)
			{
				Object	pvalue	= RExpression.evaluateExpression(params[i].getDefaultValue(), scope.getExpressionParameters());
				Object	mvalue;

				// Some FIPA parameters need special treatment.
				if(params[i].getName().equals(SFipa.CONTENT_START))
				{
					mvalue	= getValue(SFipa.CONTENT, scope);
					match	= pvalue==null && mvalue==null || pvalue instanceof String && mvalue instanceof String
						&& ((String)mvalue).startsWith((String)pvalue);
				}
				else if(params[i].getName().equals(SFipa.CONTENT_CLASS))
				{
					mvalue	= getValue(SFipa.CONTENT, scope);
					match	= pvalue==null || mvalue==null || SReflect.isSupertype((Class)pvalue, mvalue.getClass());
				}

				// As default, just compare values.
				else
				{
					mvalue	= getValue(params[i].getName(), scope);
					match	= SUtil.equals(pvalue, mvalue);
				}

				//System.out.println("matched "+msgevent.getName()+"."+params[i].getName()+": "+pvalue+", "+mvalue+", "+match);
			}
		}

		// Match against parameter sets specified in the event type.
		// todo: this implements a default strategy for param sets by checking if all values
		// todo: of the message event are also contained in the native message
		// todo: this allows further values being contained in the native message
		IMParameterSet[]	paramsets	= msgevent.getParameterSets();
		for(int i=0; match && i<paramsets.length; i++)
		{
			if(paramsets[i].getDirection().equals(IMParameterSet.DIRECTION_FIXED))
			{
				// Create and save the default values that must be contained in the native message to match.
				List vals = new ArrayList();
				if(paramsets[i].getDefaultValues().length>0)
				{
					IMExpression[] dvs = paramsets[i].getDefaultValues();
					for(int j=0; j<dvs.length; j++)
						vals.add(RExpression.evaluateExpression(dvs[i], null)); // Hack!
				}
				else if(paramsets[i].getDefaultValuesExpression()!=null)
				{
					Iterator it = SReflect.getIterator(RExpression.evaluateExpression(paramsets[i].getDefaultValuesExpression(), null)); // Hack!
					while(it.hasNext())
						vals.add(it.next());
				}

				// Create the message values and store them in a set for quick contains tests.
				Object mvalue = getValue(paramsets[i].getName(), scope);
				Set mvals = new HashSet();
				Iterator it = SReflect.getIterator(mvalue);
				while(it.hasNext())
					mvals.add(it.next());

				// Match each required value of the list.
				match = mvals.containsAll(vals);
				//System.out.println("matched "+msgevent.getName()+"."+params[i].getName()+": "+pvalue+", "+mvalue+", "+match);
			}
		}

		// Match against match expression.
		IMExpression matchexp = msgevent.getMatchExpression();
		//System.out.println("Matchexp: "+msgevent.getMatchExpression()+" "+msgevent.getName());
		if(match && matchexp!=null)
		{
			NestedMap exparams = SCollection.createNestedMap(scope.getExpressionParameters());
			for(int i=0; i<params.length; i++)
			{
				try
				{
					Object	mvalue	= getValue(params[i].getName(), scope);
					// Hack! converts "-" to "_" because variable names must not contain "-" in Java
					String paramname = "$"+ SUtil.replace(params[i].getName(), "-", "_");
					exparams.put(paramname, mvalue);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			for(int i=0; i<paramsets.length; i++)
			{
				try
				{
					Object mvalue = getValue(paramsets[i].getName(), scope);
					// Hack! converts "-" to "_" because variable names must not contain "-" in Java
					String paramsetname = "$"+SUtil.replace(paramsets[i].getName(), "-", "_");
					exparams.put(paramsetname, mvalue);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			try
			{
				//System.out.println(exparams);
				exparams.put("$messagemap", exparams.getLocalMap());
				match = ((Boolean)RExpression.evaluateExpression(matchexp, exparams)).booleanValue();
				//if(msgevent.getName().indexOf("cnp")!=-1)
				//	System.out.println(msgevent.getName()+" "+match);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				match = false;
			}
		}

		return match;
	}

	//-------- abstract methods from AbstractMessageAdapter --------

//	/**
//	 *  Get the in reply message event.
//	 *  @return The in-reply message event.
//	 */
//	protected IRMessageEvent	retrieveInReplyMessageEvent()
//	{
//		IRMessageEvent ret = null;
//		// find in-reply message
//		String inreplyto = (String)getParameterOrSetValue(SFipa.IN_REPLY_TO);
//		String convid = (String)getParameterOrSetValue(SFipa.CONVERSATION_ID);
//		if(inreplyto!=null)
//			ret = agent.getMatchingMessage(SFipa.REPLY_WITH, inreplyto);
//		else if(convid!=null)
//			ret = agent.getMatchingMessage(SFipa.CONVERSATION_ID, convid);
//		//if(inreply!=null)
//		//	System.out.println("Found matching in-reply message!");
//		return ret;
//	}

	//-------- template methods --------

	/**
	 *  Get the value for a parameter, or the values
	 *  for a parameter set from a native message.
	 *  Parameter set values can be provided as array, collection,
	 *  iterator or enumeration.
	 */
	public Object	getValue(String name, RCapability scope)
	{
		Object val;
		// Todo: implement generically for all message types.

		if(SFipa.CONTENT.equals(name))
		{
			if(content!=null)
			{
				val	= content;
			}
			else if((val=getRawValue(name)) != null)
			{
				String lang = (String)getValue(SFipa.LANGUAGE, scope);
				String onto = (String)getValue(SFipa.ONTOLOGY, scope);
				if(lang!=null || onto!=null)
				{
					Properties props = new Properties();
					if(lang!=null)
						props.setProperty(SFipa.LANGUAGE, lang);
					if(onto!=null)
						props.setProperty(SFipa.ONTOLOGY, onto);

					IContentCodec codec = scope.getContentCodec(props); // agent.get.. does not work :-(

					// todo: require lang+onto==null?
					if(codec==null && !(val instanceof String))
						throw new ContentException("No content codec found for: "+props);

					if(codec!=null)
						val = codec.decode((String)val);
					content	= val;
				}
			}
		}
		else
		{
			val = getRawValue(name);
		}

		return val;
	}

	/**
	 *  Get the value for a parameter, or the values
	 *  for a parameter set from a native message.
	 *  Parameter set values can be provided as array, collection,
	 *  iterator or enumeration.
	 *  Layered values (such as content) are not decoded in raw format.
	 */
	public abstract Object	getRawValue(String name);
}
