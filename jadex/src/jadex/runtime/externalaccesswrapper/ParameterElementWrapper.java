package jadex.runtime.externalaccesswrapper;

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
		super(element.getScope().getAgent(), element);
		this.parelem = element;
	}

	//-------- parameter handling --------

	/**
	 *  Get all parameters.
	 *  @return All parameters.
	 */
	public IParameter[]	getParameters()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object =  parelem.getParameters();
			}
		};
		IRParameter[] params = (IRParameter[])exe.object;
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
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object =  parelem.getParameterSets();
			}
		};
		IRParameterSet[] paramsets = (IRParameterSet[])exe.object;
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
	public IParameter getParameter(final String name)
	{
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object =  parelem.getParameter(name);
			}
		};
		return new ParameterWrapper((IRParameter)exe.object);
	}

	/**
	 *  Get a parameter.
	 *  @param name The name.
	 *  @return The parameter set.
	 */
	public IParameterSet getParameterSet(final String name)
	{
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object =  parelem.getParameterSet(name);
			}
		};
		return new ParameterSetWrapper((IRParameterSet)exe.object);
	}

	/**
	 *  Has the element a parameter element.
	 *  @param name The name.
	 *  @return True, if it has the parameter.
	 */
	public boolean hasParameter(final String name)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				bool =  parelem.hasParameter(name);
			}
		};
		return exe.bool;
	}

	/**
	 *  Has the element a parameter set element.
	 *  @param name The name.
	 *  @return True, if it has the parameter set.
	 */
	public boolean hasParameterSet(final String name)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				bool =  parelem.hasParameterSet(name);
			}
		};
		return exe.bool;
	}

}
