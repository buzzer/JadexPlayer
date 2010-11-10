package jadex.runtime.planwrapper;

import jadex.runtime.*;
import jadex.runtime.impl.*;

/**
 *  The parameterbase wrapper accessible from within plans.
 */
public class ParameterWrapper extends ElementWrapper implements IParameter
{
	//-------- attributes --------

	/** The original goal base. */
	protected IRParameter parameter;

	//-------- constructors --------

	/**
	 *  Create a new parameter wrapper.
	 */
	protected ParameterWrapper(IRParameter parameter)
	{
		super(parameter);
		this.parameter = parameter;
	}

	//-------- methods --------

	/**
	 *  Set a value of a parameter.
	 *  @param value The new value.
	 */
	public void setValue(Object value)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try
		{
			parameter.checkWriteAccess();
			parameter.setValue(value);
		}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Get the value of a parameter.
	 *  @return The value.
	 */
	public Object	getValue()
	{
		checkThreadAccess();
		parameter.checkReadAccess();
		return parameter.getValue();
	}

	/**
	 *  Get the value class.
	 *  @return The value class.
	 */
	public Class	getClazz()
	{
		checkThreadAccess();
		parameter.checkReadAccess();
		return parameter.getClazz();
	}
}