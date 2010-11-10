package jadex.runtime.externalaccesswrapper;

import jadex.runtime.*;
import jadex.runtime.impl.*;
import jadex.model.IMTypedElement;

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
		super(parameter.getScope().getAgent(), parameter);
		this.parameter = parameter;
	}

	//-------- methods --------

	/**
	 *  Set a value of a parameter.
	 *  @param value The new value.
	 */
	public void setValue(final Object value)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				parameter.checkWriteAccess();
				parameter.setValue(value);
			}
		};
	}

	/**
	 *  Get the value of a parameter.
	 *  @return The value.
	 */
	public Object	getValue()
	{
		//checkThreadAccess();
		AgentInvocation exe = new AgentInvocation()
		{
			public void run()
			{
				parameter.checkReadAccess();
				object = parameter.getValue();
			}
		};
		return exe.object;
	}

	/**
	 *  Get the value class.
	 *  @return The value class.
	 */
	public Class	getClazz()
	{
		//checkThreadAccess();
		AgentInvocation exe = new AgentInvocation()
		{
			public void run()
			{
				parameter.checkReadAccess();
				clazz = ((IMTypedElement)parameter.getModelElement()).getClazz();
			}
		};
		return exe.clazz;
	}
}