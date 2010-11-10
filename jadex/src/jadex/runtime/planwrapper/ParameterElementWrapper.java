package jadex.runtime.planwrapper;

import jadex.runtime.impl.*;
import jadex.runtime.*;

/**
 *  Abstract wrapper for all kinds of parameter elements.
 */
public abstract class ParameterElementWrapper extends ElementWrapper
{
	//-------- attributes --------

	/** The parameter element. */
	protected IRParameterElement parelem;

	//-------- constructors --------

	/**
	 *  Create an abstract wrapper.
	 */
	public ParameterElementWrapper(IRParameterElement element)
	{
		super(element);
		this.parelem = element;
	}

	//-------- parameter handling --------

	/**
	 *  Get all parameters.
	 *  @return All parameters.
	 */
	public IParameter[]	getParameters()
	{
		checkThreadAccess();
		IRParameter[] params = parelem.getParameters();
		IParameter[] ret = new IParameter[params.length];
		for(int i=0; i<params.length; i++)
			ret[i] = new ParameterWrapper(params[i]);
		return ret;
	}

	/**
	 *  Get all parameter sets.
	 *  @return All parameter sets.
	 */
	public IParameterSet[]	getParameterSets()
	{
		checkThreadAccess();
		IRParameterSet[] paramsets = parelem.getParameterSets();
		IParameterSet[] ret = new IParameterSet[paramsets.length];
		for(int i=0; i<paramsets.length; i++)
			ret[i] = new ParameterSetWrapper(paramsets[i]);
		return ret;
	}

	/**
	 *  Get a parameter.
	 *  @param name The name.
	 *  @return The parameter.
	 */
	public IParameter getParameter(String name)
	{
		return new ParameterWrapper(parelem.getParameter(name));
	}

	/**
	 *  Get a parameter.
	 *  @param name The name.
	 *  @return The parameter set.
	 */
	public IParameterSet getParameterSet(String name)
	{
		return new ParameterSetWrapper(parelem.getParameterSet(name));
	}

	/**
	 *  Has the element a parameter element.
	 *  @param name The name.
	 *  @return True, if it has the parameter.
	 */
	public boolean hasParameter(String name)
	{
		checkThreadAccess();
		return parelem.hasParameter(name);
	}

	/**
	 *  Has the element a parameter set element.
	 *  @param name The name.
	 *  @return True, if it has the parameter set.
	 */
	public boolean hasParameterSet(String name)
	{
		checkThreadAccess();
		return parelem.hasParameterSet(name);
	}

}
