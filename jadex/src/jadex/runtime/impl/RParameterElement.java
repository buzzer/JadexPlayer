package jadex.runtime.impl;

import java.util.*;
import jadex.model.*;
import jadex.util.collection.*;
import jadex.runtime.*;

/**
 *  The abstract base class for parametrizable elements.
 */
public abstract class RParameterElement extends RReferenceableElement  implements IRParameterElement
{
	//-------- constants --------

//	public static final String ACCESS_PROTECTION_NONE = "protection_none";
	public static final String ACCESS_PROTECTION_INIT = "protection_init";
	public static final String ACCESS_PROTECTION_PROCESSING = "protection_processing";

	//-------- attributes --------

	/** The parameters. */
	protected Map parameters;

	/** The parameter sets. */
	protected Map parametersets;

	/** The protection mode for the parameters. */
	protected String protectionmode;

	//-------- constructors --------

	/**
	 *  Create a parametrized element.
	 * @param name The name.
	 * @param modelelement The model element.
	 * @param config The configuration.
	 * @param owner The owner.
	 */
	protected RParameterElement(String name, IMParameterElement modelelement, IMConfigParameterElement config,
		RElement owner, RReferenceableElement creator, Map exparams)
 	{
		super(name, modelelement, config, owner, creator, exparams);

		setParameterProtectionMode(ACCESS_PROTECTION_INIT);
		createParameters();
	}

	/**
	 *  Initialize the element.
	 */
	public void	init()
	{
		super.init();

		if(parameters!=null)
		{
			for(Iterator it = parameters.values().iterator(); it.hasNext(); )
			{
				((RParameter)it.next()).initStructure();
			}
		}
		if(parametersets!=null)
		{
			for(Iterator it = parametersets.values().iterator(); it.hasNext(); )
			{
				((RParameterSet)it.next()).initStructure();
			}
		}
	}

	/**
	 *  Perform any necessary clean up, when this element is
	 *  no longer used.
	 *  Subclasses may override this method to do their
	 *  cleanup operations.
	 */
	public void	cleanup()
	{
		if(cleanedup)
			return;

		super.cleanup();

		setParameterProtectionMode(ACCESS_PROTECTION_INIT);
		// Cleanup parameters (remove bean and change listeners).
		if(parameters!=null)
		{
			for(Iterator it = parameters.values().iterator(); it.hasNext(); )
			{
				((IRParameter)it.next()).cleanup();
			}
		}
		if(parametersets!=null)
		{
			for(Iterator it = parametersets.values().iterator(); it.hasNext(); )
			{
				((IRParameterSet)it.next()).cleanup();
			}
		}
	}
	
	//-------- parameter handling --------

	/**
	 *  Get the parameter protection mode.
	 *  @return The parameter protection mode.
	 */
	public String getParameterProtectionMode()
	{
		return protectionmode;
	}

	/**
	 *  Set the parameter protection mode.
	 *  @param protectionmode The protection mode.
	 */
	protected void setParameterProtectionMode(String protectionmode)
	{
		//System.out.println("Set protectionmode: "+getName()+" - "+protectionmode);
		this.protectionmode = protectionmode;
	}

	/**
	 *  Generate a change event for this element.
	 *  @param event The event.
	 */
	public void throwSystemEvent(SystemEvent event)
	{
		// Only throw events, when element is active.
		if(protectionmode.equals(ACCESS_PROTECTION_PROCESSING))
			super.throwSystemEvent(event);
	}

	/**
	 *  Get all parameters.
	 *  @return All parameters.
	 */
	public IRParameter[]	getParameters()
	{
		if(parameters==null)
			return new IRParameter[0];	// Hack???
		return (IRParameter[])parameters.values().toArray(new IRParameter[parameters.size()]);
	}

	/**
	 *  Get all parameter sets.
	 *  @return All parameter sets.
	 */
	public IRParameterSet[]	getParameterSets()
	{
		if(parametersets==null)
			return new IRParameterSet[0];	// Hack???
		return (IRParameterSet[])parametersets.values().toArray(new IRParameterSet[parametersets.size()]);
	}

	/**
	 *  Get the parameter element.
	 *  @param name The name.
	 *  @return The param.
	 */
	public IRParameter getParameter(String name)
	{
		IMParameter mparam;
		if((parameters==null || !parameters.containsKey(name)) && (mparam=((IMParameterElement)getModelElement()).getParameter(name))!=null)
		{
			if(parameters==null)
				this.parameters = SCollection.createHashMap();
			// Create parameters needed by abstract goals.
			// (getParamater called before createParameters :-()
			IMConfigParameterElement state = (IMConfigParameterElement)getConfiguration();
			RParameter param = new RParameter(mparam, state!=null ? state.getParameter(mparam) : null, this);
			parameters.put(mparam.getName(), param);
			return param;
		}
		else if(parameters!=null && parameters.containsKey(name))
		{
			return (IRParameter)parameters.get(name);
		}
		else
		{
			throw new RuntimeException("No such parameter: "+name+" in "+this);
		}
	}

	/**
	 *  Get the parameter set element.
 	 *  @param name The name.
	 *  @return The param set.
	 */
	public IRParameterSet getParameterSet(String name)
	{
		IMParameterSet mparamset;
		if((parametersets==null || !parametersets.containsKey(name)) && (mparamset=((IMParameterElement)getModelElement()).getParameterSet(name))!=null)
		{
			if(parametersets==null)
				this.parametersets = SCollection.createHashMap();
			// Create parametersets needed by abstract goals.
			// (getParamaterSet called before createParameters :-()
			IMConfigParameterElement state = (IMConfigParameterElement)getConfiguration();
			RParameterSet paramset = new RParameterSet(mparamset, state!=null ? state.getParameterSet(mparamset) : null, this);
			parametersets.put(mparamset.getName(), paramset);
			return paramset;
		}
		else if(parametersets!=null && parametersets.containsKey(name))
		{
			return (IRParameterSet)parametersets.get(name);
		}
		else
		{
			throw new RuntimeException("No such parameter set: "+name+" in "+this);
		}
	}

	/**
	 *  Has the element a parameter element.
	 *  @param name The name.
	 *  @return True, if it has the parameter.
	 */
	public boolean hasParameter(String name)
	{
		return ((IMParameterElement)getModelElement()).getParameter(name)!=null;
	}

	/**
	 *  Has the element a parameter set element.
	 *  @param name The name.
	 *  @return True, if it has the parameter set.
	 */
	public boolean hasParameterSet(String name)
	{
		return ((IMParameterElement)getModelElement()).getParameterSet(name)!=null;
	}

	/**
	 *  Create the declared parameters.
	 */
	protected void createParameters()
	{
		IMParameterElement modelelement = (IMParameterElement)getModelElement();
		IMConfigParameterElement state = (IMConfigParameterElement)getConfiguration();

		IMParameter[] params = modelelement.getParameters();
		if(params.length>0 && parameters==null)
		{
			this.parameters = SCollection.createHashMap();
		}
		for(int i=0; i<params.length; i++)
		{
			// Hack!!! parameters may be created already due to abstract goals.
			if(!parameters.containsKey(params[i].getName()))
			{
				IRParameter param;
				if(params[i] instanceof IMPlanParameter && params[i] instanceof IMParameter)
					param = new RPlanParameter(params[i], state!=null ? state.getParameter(params[i]) : null, this);
				else
					param = new RParameter(params[i], state!=null ? state.getParameter(params[i]) : null, this);
				parameters.put(params[i].getName(), param);
			}
		}

		IMParameterSet[] paramsets = modelelement.getParameterSets();
		if(paramsets.length>0 && parametersets==null)
		{
			this.parametersets = SCollection.createHashMap();
		}
		for(int i=0; i<paramsets.length; i++)
		{
			// Hack!!! parameter sets may be created already due to abstract goals.
			if(!parametersets.containsKey(paramsets[i].getName()))
			{
				IRParameterSet paramset;
				if(paramsets[i] instanceof IMPlanParameterSet)
					paramset = new RPlanParameterSet(paramsets[i], state!=null ? state.getParameterSet(paramsets[i]) : null, this);
				else
					paramset = new RParameterSet(paramsets[i], state!=null ? state.getParameterSet(paramsets[i]) : null, this);

				//RParameterSet paramset = new RParameterSet(paramsets[i], state!=null ? state.getParameterSet(paramsets[i]) : null, this);
				parametersets.put(paramsets[i].getName(), paramset);
			}
		}
	}

	//-------- Hack!!! needed because of bean access --------

	/**
	 *  Get a value corresponding to a belief.
	 *  @param name The name identifiying the belief.
	 *  @return The value.
	 */
	public Object getParameterValue(String name)
	{
    	return getParameter(name).getValue();
	}

	 /**
	 *  Get all values corresponding to one beliefset.
	 *  @param name The name identifiying the beliefset.
	 *  @return The values.
	 */
	public Object[] getParameterSetValues(String name)
	{
		return getParameterSet(name).getValues();
	}

	//-------- methods --------

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

 		if(parameters!=null)
		{
			RElement[] params = (RElement[])parameters.values().toArray(new RElement[parameters.size()]);
			for(int i=0; i<params.length; i++)
			{
				encparams.put(params[i].getName(), params[i].getEncodableRepresentation());
			}
		}

		if(parametersets!=null)
		{
			RElement[] paramsets = (RElement[])parametersets.values().toArray(new RElement[parametersets.size()]);
			for(int i=0; i<paramsets.length; i++)
			{
				encparams.put(paramsets[i].getName(), paramsets[i].getEncodableRepresentation());
			}
		}

		rep.put("parameters", encparams);

		return rep;
	}
}

