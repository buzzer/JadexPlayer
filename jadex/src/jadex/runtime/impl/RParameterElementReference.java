package jadex.runtime.impl;

import java.util.*;
import jadex.model.*;
import jadex.util.SReflect;
import jadex.util.collection.*;
import jadex.runtime.*;

/**
 *  The parameter element reference.
 */
public class RParameterElementReference extends RElementReference implements IRParameterElement
{
	//-------- attributes --------

	/** The parameter references. */
	protected Map parameterrefs;

	/** The parameter set references. */
	protected Map parametersetrefs;

	//-------- constructors --------

	/**
	 *  Create a parametrized element.
	 *  @param name The name.
	 *  @param modelelement The model element.
	 *  @param state The state.
	 *  @param owner The owner.
	 */
	protected RParameterElementReference(String name, IMParameterElementReference modelelement,
		IMConfigParameterElement state, RElement owner, RReferenceableElement creator)
 	{
		super(name, modelelement, state, owner, creator);

		//this.parameterrefs = SCollection.createHashMap();
		//this.parametersetrefs = SCollection.createHashMap();
		createParameters();
	}

	/**
	 *  Initialize the element.
	 */
	protected void	init()
	{
		super.init();

		IRParameter[]	params	= getParameters();
		for(int i=0; i<params.length; i++)
		{
			((RParameterReference)params[i]).init();
		}
		IRParameterSet[]	paramsets	= getParameterSets();
		for(int i=0; i<paramsets.length; i++)
		{
			((RParameterSetReference)paramsets[i]).init();
		}
	}

	/**
	 *  Create the declared parameters.
	 */
	protected void createParameters()
	{
		IMParameterElementReference modelelement = (IMParameterElementReference)getModelElement();
		IMConfigParameterElement state = (IMConfigParameterElement)getConfiguration();

		IMParameterReference[] params = modelelement.getParameterReferences();
		if(parameterrefs==null)
			parameterrefs = SCollection.createHashMap();
		for(int i=0; i<params.length; i++)
		{
			RParameterReference param = new RParameterReference(params[i], state!=null ? state.getParameter(params[i]) : null, this);
			parameterrefs.put(params[i].getName(), param);
		}

		IMParameterSetReference[] paramsets = modelelement.getParameterSetReferences();
		if(parametersetrefs==null)
			parametersetrefs = SCollection.createHashMap();
		for(int i=0; i<paramsets.length; i++)
		{
			RParameterSetReference param = new RParameterSetReference(paramsets[i], state!=null ? state.getParameterSet(paramsets[i]) : null, this);
			parametersetrefs.put(paramsets[i].getName(), param);
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
		// Cleanup parameters (remove bean and change listeners).
		IRParameter[]	params	= getParameters();
		for(int i=0; i<params.length; i++)
		{
			params[i].cleanup();
		}
		IRParameterSet[]	paramsets	= getParameterSets();
		for(int i=0; i<paramsets.length; i++)
		{
			paramsets[i].cleanup();
		}
	}

	//-------- parameter handling --------

	/**
	 *  Get the parameter protection mode.
	 *  @return The parameter protection mode.
	 */
	public String getParameterProtectionMode()
	{
		return ((IRParameterElement)getReferencedElement()).getParameterProtectionMode();
	}

	/* *
	 *  Set the parameter protection mode.
	 *  @param protectionmode The protection mode.
	 * /
	public void setParameterProtectionMode(String protectionmode)
	{
		 ((IRParameterElement)getReferencedElement()).setParameterProtectionMode(protectionmode);
	}*/

	/**
	 *  Get all parameters.
	 *  @return All parameters.
	 */
	public IRParameter[]	getParameters()
	{
		if(parameterrefs==null)
			return new IRParameter[0];
		return (IRParameter[])parameterrefs.values().toArray(new IRParameter[parameterrefs.size()]);
	}

	/**
	 *  Get all parameter sets.
	 *  @return All parameter sets.
	 */
	public IRParameterSet[]	getParameterSets()
	{
		if(parametersetrefs==null)
			return new IRParameterSet[0];
		return (IRParameterSet[])parametersetrefs.values().toArray(new IRParameterSet[parametersetrefs.size()]);
	}

	/**
	 *  Get the parameter element.
	 *  @param name The name.
	 *  @return The param.
	 */
	public IRParameter getParameter(String name)
	{
		if(parameterrefs!=null && parameterrefs.containsKey(name))
		{
			return (IRParameter)parameterrefs.get(name);
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
		if(parametersetrefs!=null && parametersetrefs.containsKey(name))
		{
			return (IRParameterSet)parametersetrefs.get(name);
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
		return parameterrefs!=null && parameterrefs.containsKey(name);
	}

	/**
	 *  Has the element a parameter set element.
	 *  @param name The name.
	 *  @return True, if it has the parameter set.
	 */
	public boolean hasParameterSet(String name)
	{
		return parametersetrefs!=null && parametersetrefs.containsKey(name);
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
	 *  Get the parameter reference corresponding to the given original parameter. 
	 */
	// Hack!!! Designed to work transitively across many capabilities.
	protected  RParameterReference	getParameterReference(RParameter parameter)
	{
		RParameterReference	ret	= null;
		IRParameter[] params = getParameters();
		for(int i=0; i<params.length && ret==null; i++)
		{
			RParameterReference	test	= (RParameterReference)params[i];
			if(test.getOriginalElement()==parameter)
				ret	= test;
		}
		return ret;
	}

	/**
	 *  Get the parameter set reference corresponding to the given original parameter set.
	 */
	// Hack!!! Designed to work transitively across many capabilities.
	protected  RParameterSetReference	getParameterSetReference(RParameterSet parameterset)
	{
		RParameterSetReference	ret	= null;
		IRParameterSet[] paramsets = getParameterSets();
		for(int i=0; i<paramsets.length && ret==null; i++)
		{
			RParameterSetReference	test	= (RParameterSetReference)paramsets[i];
			if(test.getOriginalElement()==parameterset)
				ret	= test;
		}
		return ret;
	}

	/**
	 *  Generate a change event for this element.
	 *  @param event The event.
	 */
	public void throwSystemEvent(SystemEvent event)
	{
		// Only throw events, when element is active.
		if(getParameterProtectionMode().equals(RParameterElement.ACCESS_PROTECTION_PROCESSING))
			super.throwSystemEvent(event);
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

		IRParameter[] params = getParameters();
		for(int i=0; i<params.length; i++)
		{
			encparams.put(params[i].getName(), ((RElement)params[i]).getEncodableRepresentation());
		}
		IRParameterSet[] paramsets = getParameterSets();
		for(int i=0; i<paramsets.length; i++)
		{
			encparams.put(paramsets[i].getName(), ((RElement)paramsets[i]).getEncodableRepresentation());
		}

		rep.put("parameters", encparams);

		return rep;
	}
}

