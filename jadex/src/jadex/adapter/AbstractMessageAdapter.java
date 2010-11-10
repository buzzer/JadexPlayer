package jadex.adapter;

import jadex.model.*;
import jadex.runtime.impl.*;
import jadex.util.SReflect;
import jadex.util.SUtil;
import jadex.util.collection.SCollection;
import jadex.util.collection.NestedMap;

import java.io.Serializable;
import java.util.*;

/**
 *  Generic implementation of the message adapter functionality.
 *  Only the getValue() method has to be implemented.
 */
public abstract class AbstractMessageAdapter implements IMessageAdapter, Serializable
{
	//-------- attributes --------

	/** The agent. */
	protected IJadexAgent	agent;

	/** The platform specific message object. */
	private Object	message_object;

	/** The in-reply message event. */
	protected IRMessageEvent inreply;

	/** Was the inreply already searched? */
	protected boolean searched_inreply;

	//-------- constructors --------

	/**
	 *  Create a message adapter for the given message object.
	 */
	public AbstractMessageAdapter(IJadexAgent agent, Object message)
	{
		this.agent = agent;
		this.message_object = message;
	}

	//-------- IMessageAdapter interface --------
	
//	/**
//	 *  Get the message to which this message is a reply.
//	 *  @return The message to which this message is a reply.
//	 */
//	public IRMessageEvent getInReplyMessage()
//	{
//		if(!searched_inreply)
//		{
//			this.inreply = retrieveInReplyMessageEvent();
//			searched_inreply = true;
//		}
//		return inreply;
//	}

	/**
	 *  Match a message with a message event.
	 *  @param msgevent The message event.
	 *  @return True, if message matches the message event.
	 */
	public boolean match(IMMessageEvent msgevent)
	{
		boolean	match	= true;

		RCapability scope = agent.lookupCapability(msgevent.getScope());

		// Match against parameters specified in the event type.
		IMParameter[]	params	= msgevent.getParameters();
		for(int i=0; match && i<params.length; i++)
		{
			if(params[i].getDirection().equals(IMParameter.DIRECTION_FIXED) && params[i].getDefaultValue()!=null)
			{
				//Object	pvalue	= RExpression.evaluateExpression(params[i].getDefaultValue(), scope.getExpressionParameters());
				Object	pvalue	= RExpression.evaluateExpression(params[i].getDefaultValue(), scope.getExpressionParameters());
				Object	mvalue	= getValue(params[i].getName(), scope);
				match	= pvalue==null && mvalue==null || pvalue!=null && mvalue!=null && pvalue.equals(mvalue);

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
				Iterator	it	= SReflect.getIterator(mvalue);
				while(it.hasNext())
					mvals.add(it.next());
				// Match each required value of the list.
				match = mvals.containsAll(vals);
				//System.out.println("matched "+msgevent.getName()+"."+params[i].getName()+": "+pvalue+", "+mvalue+", "+match);
			}
		}

		// Match against match expression.
		IMExpression matchexp = msgevent.getMatchExpression();
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
				exparams.put("$messagemap", exparams.getLocalMap());
				match = ((Boolean)RExpression.evaluateExpression(matchexp, exparams)).booleanValue();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				match = false;
			}
		}

		return match;
	}

	/**
	 *  Get the platform message.
	 *  @return The platform specific message.
	 */
	public Object getMessage()
	{
		return message_object;
	}

	//-------- template methods --------

	/**
	 *  Get the value for a parameter, or the values
	 *  for a parameter set from the native message.
	 *  Parameter set values can be provided as array, collection,
	 *  iterator or enumeration.
	 */
	public abstract Object	getValue(String name, RCapability scope);

	/**
	 *  Fill parameters of a message event from a native one.
	 *  Uses the abstract method getParameterOrSetValue
	 *  to retrieve the values from the native message.
	 */
	public static void prepareReceiving(IRMessageEvent msgevent, IMessageAdapter msg)
	{
		// Make native message object accessible for plans.
		msgevent.setMessage(msg.getMessage());

		msgevent.setId(msg.getId());
		
		// Fill all parameter values from the map (if available).
		IRParameter[]	params	= msgevent.getParameters();
		for(int i=0; i<params.length; i++)
		{
			IMParameter	model;
			if(params[i].getModelElement() instanceof IMParameterReference)
			{
				IMParameterReference pref = (IMParameterReference)params[i].getModelElement();
				model = (IMParameter)pref.getOriginalElement();
			}
			else
			{
				model	= (IMParameter)params[i].getModelElement();
			}

			if(model.getDirection().equals(IMParameter.DIRECTION_IN)
				|| model.getDirection().equals(IMParameter.DIRECTION_INOUT))
			{
				Object	mvalue	= msg.getValue(params[i].getName(), msgevent.getScope());
				// Todo: provide handling of null values!?
				if(mvalue!=null)
					params[i].setValue(mvalue);
			}
		}

		// Fill all parameter set values from the map (if available).
		IRParameterSet[]	paramsets	= msgevent.getParameterSets();
		for(int i=0; i<paramsets.length; i++)
		{
			IMParameterSet	model;
			if(paramsets[i].getModelElement() instanceof IMParameterSetReference)
			{
				IMParameterSetReference psetref = (IMParameterSetReference)paramsets[i].getModelElement();
				model = (IMParameterSet)psetref.getOriginalElement();
			}
			else
			{
				model	= (IMParameterSet)paramsets[i].getModelElement();
			}

			if(model.getDirection().equals(IMParameter.DIRECTION_IN)
				|| model.getDirection().equals(IMParameter.DIRECTION_INOUT))
			{
				Object	mvalue	= msg.getValue(paramsets[i].getName(), msgevent.getScope());
				// Todo: provide handling of null values!?
				if(mvalue!=null)
				{
					Iterator	it	= SReflect.getIterator(mvalue);
					while(it.hasNext())
						paramsets[i].addValue(it.next());
				}
			}
		}
	}
}
