package jadex.model.jibximpl;

import jadex.config.Configuration;
import jadex.model.*;
import jadex.model.MessageType.ParameterSpecification;
import jadex.util.SReflect;
import jadex.util.collection.SCollection;

import java.util.*;

/**
 *  The message event type.
 */
public class MMessageEvent extends MEvent implements IMMessageEvent
{
	//-------- xml-attributes --------

	/** The direction. */
	protected String direction	= DIRECTION_SEND_RECEIVE;

	/** The type. */
	protected String type;

	//-------- attributes --------

	/** The specialization degree. */
	protected int degree;

	/** The message type object. */
	protected MessageType	messagetype; // todo: clone?!

	/** The declared parameter. */
	protected List declaredparams;

	/** The declared parametersets. */
	protected List declaredparamsets;

	/** The match expression for sophisticated type specification. */
	protected MExpression match;

	//-------- constructors --------

	/**
	 *  Called when the element is setup.
	 */
	protected void init()
	{
		super.init();

		declaredparams = SCollection.createArrayList();
		declaredparamsets = SCollection.createArrayList();

		// Set default post to all to false.
		if(posttoall==null)
			setPostToAll(false);

		/*IMExpression exp = getPredefinedTemplate();
		if(exp!=null)
		{
			// todo: increase degree when message template is specified
			template = (MessageTemplate)exp.getTerm().getValue();
			degree++;
		}*/

		// Calculate specificity by summing up fixed parameters and parameter sets.
		IMParameter[] parameters = getParameters();
		for(int i=0; i<parameters.length; i++)
		{
			declaredparams.add(parameters[i]);
			if(parameters[i].getDirection().equals(IMParameter.DIRECTION_FIXED)
				&& parameters[i].getDefaultValue()!=null)
			{
				degree++;
			}
		}
		IMParameterSet[] parametersets = getParameterSets();
		for(int i=0; i<parametersets.length; i++)
		{
			declaredparamsets.add(parametersets[i]);
			if(parametersets[i].getDirection().equals(IMParameterSet.DIRECTION_FIXED)
				&& (parametersets[i].getDefaultValuesExpression()!=null || parametersets[i].getDefaultValues().length>0))
			{
				degree++;
			}
		}
		// Messages with match expression have higher degree.
		if(getMatchExpression()!=null)
			degree++;

		// Search the message type in the properties.
		if(getMessageType()!=null)
		{
			/*String	typename	= "messagetype."+getType();
			IMExpression	exp	= null;
			IMCapability	cap = getScope();
			while(exp==null && cap!=null)
			{
				//System.out.println("resolving "+capaname+" in "+cap);
				// todo: ???
				//if(((IJBindMCapability)cap).hasJBindPropertybase())	// Hack!!! Propertybase may not have been inited.
				//{
					exp	= cap.getPropertybase().getProperty(typename);
				//}
				cap	= (IMCapability)cap.getOwner();
			}
	
			if(exp!=null)
			{
				this.messagetype	= (MessageType)exp.getValue(null); // Hack!!! 
			}
			else
			{
				throw new RuntimeException("Messagetype could not be resolved: "+getType());
			}*/

			//setMessageType(Configuration.getConfiguration().getMessageType(getType()));

			// Initialize message event based on type.
//			if(messagetype!=null)
//			{
				MessageType.ParameterSpecification[]	params	= messagetype.getParameters();
				MessageType.ParameterSpecification[]	paramsets	= messagetype.getParameterSets();
				
				// Add missing parameters.
				for(int i=0; i<params.length; i++)
				{
					// Create, if parameter does not exist.
					if(getParameter(params[i].getName())==null)
					{
						// Don't add these type parameters to the declared ones.
						super.createParameter(params[i].getName(), params[i].getClazz(), IMParameter.DIRECTION_INOUT, 0, params[i].getDefaultValue(), null);
					}
				}
	
				// Add missing parameter sets.
				for(int i=0; i<paramsets.length; i++)
				{
					// Create, if parameter does not exist.
					if(getParameterSet(paramsets[i].getName())==null)
					{
						// Don't add these type parameter sets to the declared ones.
						super.createParameterSet(paramsets[i].getName(), paramsets[i].getClazz(), IMParameterSet.DIRECTION_INOUT, 0, paramsets[i].getDefaultValue(), null);
					}
				}
//			}
		}
	}

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);
		
		if(match!=null)
		{
			match.checkClass(Boolean.class, report);
		}

		// Search the message type in the properties.
		if(getType()==null)
		{
			report.addEntry(this, "No 'type' specified.");
		}
		else
		{
			// Check message event based on type.
			MessageType	messagetype	= Configuration.getConfiguration().getMessageType(getType());
			if(messagetype==null)
			{
				report.addEntry(this, "Unknown message type '"+getType()+"'.");
			}
			else
			{
				IMParameter[]	mparams	= getParameters();
				IMParameterSet[]	mparamsets	= getParameterSets();
				MessageType.ParameterSpecification[]	params	= messagetype.getParameters();
				MessageType.ParameterSpecification[]	paramsets	= messagetype.getParameterSets();
				
				// Store names of allowed parameter(set)s.
				Set	allowedparams	= new HashSet();
				Set	allowedparamsets	= new HashSet();
				
				// Check parameters.
				for(int i=0; i<params.length; i++)
				{
					allowedparams.add(params[i].getName());
					IMParameter	p	= getParameter(params[i].getName());
	
					// Add error, when type does not match.
					if(p!=null && p.getClazz()!=null && params[i].getClazz()!=null && !SReflect.isSupertype(params[i].getClazz(), p.getClazz()))
					{
						report.addEntry(p, "Unsupported parameter type '"+p.getClazz()+"', expected '"+params[i].getClazz().getName()+"'.");
					}
				}
	
				// Check parameter sets.
				for(int i=0; i<paramsets.length; i++)
				{
					allowedparamsets.add(paramsets[i].getName());
					IMParameterSet	p	= getParameterSet(paramsets[i].getName());
		
					// Add error, when type does not match.
					if(p!=null && p.getClazz()!=null && paramsets[i].getClazz()!=null && !SReflect.isSupertype(paramsets[i].getClazz(), p.getClazz()))
					{
						report.addEntry(p, "Unsupported parameter set type '"+p.getClazz()+"', expected '"+paramsets[i].getClazz()+"'.");
					}
				}
	
				// Check for unsupported parameter(set)s.
				for(int i=0; i<mparams.length; i++)
				{
					if(!allowedparams.contains(mparams[i].getName()))
					{
						report.addEntry(mparams[i], "Parameter '"+mparams[i].getName()+"' not supported for messages of type '"+getType()+"'.");
					}
				}
				for(int i=0; i<mparamsets.length; i++)
				{
					if(!allowedparamsets.contains(mparamsets[i].getName()))
					{
						report.addEntry(mparamsets[i], "Parameter set '"+mparamsets[i].getName()+"' not supported for messages of type '"+getType()+"'.");
					}
				}
			}
		}

		// Check for binding parameters in "receive" messages.
		if(DIRECTION_RECEIVE.equals(getDirection()) || DIRECTION_SEND_RECEIVE.equals(getDirection()))
		{
			IMParameter[]	mparams	= getParameters();
			for(int i=0; i<mparams.length; i++)
			{
				if(mparams[i].getBindingOptions()!=null)
				{
					report.addEntry(mparams[i], "Binding options not supported for parameters of messages with direction '"+DIRECTION_RECEIVE+"' or '"+DIRECTION_SEND_RECEIVE+"'.");
				}
			}
		}
	}

	/**
	 *  Method called on first setup run.
	 *  Overwritten in elements that have
	 *  child elements.
	 *  @return The list of direct child elements.
	 */
	public List	getChildren()
	{
		List ret = super.getChildren();
		if(match!=null)
			ret.add(match);
		return ret;
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MMessageEvent clone = (MMessageEvent)cl;
		if(declaredparams!=null)
		{
			clone.declaredparams = SCollection.createArrayList();
			for(int i=0; i<declaredparams.size(); i++)
				clone.declaredparams.add(((MElement)declaredparams.get(i)).clone());
		}
		if(declaredparamsets!=null)
		{
			clone.declaredparamsets = SCollection.createArrayList();
			for(int i=0; i<declaredparamsets.size(); i++)
				clone.declaredparamsets.add(((MElement)declaredparamsets.get(i)).clone());
		}
		if(match!=null)
			clone.match = (MExpression)match.clone();
	}
	
	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMMessageEventReference;
	}

	//-------- direction --------

	/**
	 *  Get the direction (send/receive).
	 *  @return The direction of the message.
	 */
	public String	getDirection()
	{
		return direction;
	}

	/**
	 *  Set the direction (send/receive).
	 *  @param direction	The direction of the message.
	 */
	public void	setDirection(String direction)
	{
		this.direction = direction;
	}

	//-------- type --------

	/**
	 *  Get the message type (e.g. "fipa").
	 *  @return The type of the message.
	 */
	public String	getType()
	{
		return this.type;
	}

	/**
	 *  Set the message type (e.g. "fipa").
	 *  @param type	The type of the message.
	 */
	public void	setType(String type)
	{
		this.type = type;
	}

	//-------- match expression --------

	/**
	 *  Get the match expression.
	 *  @return The match expression.
	 */
	public IMExpression	getMatchExpression()
	{
		return this.match;
	}

	/**
	 *  Create a new parameter.
	 *  @param match	The match expression.
	 *  @return	The newly created match expression.
	 */
	public IMExpression	createMatchExpression(String match)
	{
		MExpression ret = new MExpression();
		ret.setName(name);
		ret.setExpressionText(match);
		ret.setClazz(Boolean.class);
		ret.setEvaluationMode(IMExpression.MODE_DYNAMIC);
		ret.setExported(IMReferenceableElement.EXPORTED_FALSE);
		ret.setOwner(this);
		this.match = ret;
		return ret;
	}

	/**
	 *  Delete a parameter.
	 */
	public void	deleteMatchExpression()
	{
		match = null;
	}

	//-------- non-xml related methods --------

	/**
	 *  Get the specialization degree.
	 */
	public int getSpecializationDegree()
	{
		return degree;
	}

	/**
	 *  Get the message type object.
	 *  @return The type specification object of the message.
	 */
	public MessageType getMessageType()
	{
		if(messagetype==null)
			messagetype	= Configuration.getConfiguration().getMessageType(getType());
		if(messagetype==null)
			throw new RuntimeException("Message type nulls: "+getName());
		return messagetype;
	}

	/**
	 *  Set the message type.
	 *  @param messagetype The message type.
	 */
	public void setMessageType(MessageType messagetype)
	{
		assert messagetype!=null: "Message type nulls.";
		this.messagetype = messagetype;
	}

	/**
	 *  Get a parameter by name.
	 *  @param name The parameter name.
	 *  @return The parameter expression.
	 */
	public IMParameter	getParameter(String name)
	{
		// overridden to provide implicit parameters while checking.
		IMParameter ret = super.getParameter(name);
		if(ret==null && isChecking())
		{
			//MessageType	messagetype	= Configuration.getConfiguration().getMessageType(getType());
			if(getMessageType()!=null)
			{
				ParameterSpecification[]	params	= getMessageType().getParameters();
				for(int i=0; ret==null && i<params.length; i++)
				{
					// Hack!!! Provide dummy parameter for checking.
					if(params[i].getName().equals(name))
					{
						ret = new MParameter();
						ret.setName(params[i].getName());
						ret.setClazz(params[i].getClazz());
						((MParameter)ret).setOwner(this);	// better not do this?
					}
				}
			}
		}
		return ret;
	}

	/**
	 *  Get a parameter set by name.
	 *  @param name The parameter set name.
	 *  @return The parameter set expression.
	 */
	public IMParameterSet	getParameterSet(String name)
	{
		// overridden to provide implicit parameters while checking.
		IMParameterSet ret = super.getParameterSet(name);
		if(ret==null && isChecking())
		{
			//MessageType	messagetype	= Configuration.getConfiguration().getMessageType(getType());
			if(getMessageType()!=null)
			{
				ParameterSpecification[]	params	= getMessageType().getParameterSets();
				for(int i=0; ret==null && i<params.length; i++)
				{
					// Hack!!! Provide dummy parameter for checking.
					if(params[i].getName().equals(name))
					{
						ret = new MParameterSet();
						ret.setName(params[i].getName());
						ret.setClazz(params[i].getClazz());
						((MParameterSet)ret).setOwner(this);	// better not do this?
					}
				}
			}
		}
		return ret;
	}

	/**
	 *  Get the user defined parameters.
	 *  Does not return the additional type parameters.
	 *  @return All declared parameters.
	 */
	public IMParameter[] getDeclaredParameters()
	{
		return (IMParameter[])declaredparams.toArray(new IMParameter[declaredparams.size()]);
	}

	/**
	 *  Get the user defined parameter sets.
	 *  Does not return the additional type parameter sets.
	 *  @return All declared parameter sets.
	 */
	public IMParameterSet[] getDeclaredParameterSets()
	{
		return (IMParameterSet[])declaredparamsets.toArray(new IMParameterSet[declaredparamsets.size()]);
	}

	/**
	 *  Create a new parameter.
	 *  @param name	The name of the parameter.
	 *  @param clazz	The class for values.
	 *  @param updaterate	The updaterate (or -1 for none).
	 *  @param expression	The default value expression (if any).
	 *  @param mode	The evaluation mode.
	 *  @return	The newly created parameter.
	 */
	public IMParameter	createParameter(String name, Class clazz, String direction, long updaterate, String expression, String mode)
	{
		IMParameter ret = super.createParameter(name, clazz, direction, updaterate, expression, mode);
		declaredparams.add(ret);
		return ret;
	}

	/**
	 *  Delete a parameter.
	 *  @param parameter	The parameter to delete.
	 */
	public void	deleteParameter(IMParameter parameter)
	{
		super.deleteParameter(parameter);
		declaredparams.remove(parameter);
	}

	/**
	 *  Create a new parameter set.
	 *  @param name	The name of the  parameter set.
	 *  @param clazz	The class for values.
	 *  @param direction	The direction (in/out).
	 *  @param updaterate	The updaterate (or -1 for none).
	 *  @param expression	The default values expression (if any).
	 *  @param mode	The evaluation mode.
	 *  @return	The newly created parameter set.
	 */
	public IMParameterSet	createParameterSet(String name, Class clazz, String direction, long updaterate, String expression, String mode)
	{
		IMParameterSet ret = super.createParameterSet(name, clazz, direction, updaterate, expression, mode);
		declaredparamsets.add(ret);
		return ret;
	}

	/**
	 *  Delete a parameter set.
	 *  @param parameterset	The parameter set to delete.
	 */
	public void	deleteParameterSet(IMParameterSet parameterset)
	{
		super.deleteParameterSet(parameterset);
		declaredparamsets.remove(parameterset);
	}
}
