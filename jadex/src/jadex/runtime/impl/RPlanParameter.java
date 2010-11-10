package jadex.runtime.impl;

import jadex.model.IMConfigParameter;
import jadex.model.IMParameter;
import jadex.model.IMPlanParameter;
import jadex.util.SUtil;

/**
 *  A plan parameter supports a mapping to another IRParameter.
 *  todo: implement cache, remove ProcessGoal cache, remove ProcessGoal?
 */
public class RPlanParameter extends RParameter
{
	//-------- attributes --------

	/** The mapping element. */
	protected IRParameter connector;

	//-------- constructor --------

	/**
	 *  Create a new element.
	 *  To evaluate the initial values, init() has to be called.
	 *  @param parameter The model element.
	 *  @param owner The owner.
	 */
	protected RPlanParameter(IMParameter parameter, IMConfigParameter state, RElement owner)
	{
		super(parameter, state, owner);
	}

	/**
	 *  Connect to a parameter element.
	 */
	protected void connect(IRParameterElement con)
	{
		assert con!=null;
		assert isConnectable();
		assert con instanceof IRInternalEvent || con instanceof IRMessageEvent || con instanceof RProcessGoal;

		String[] rps;
		if(con instanceof IRInternalEvent)
			rps = ((IMPlanParameter)getModelElement()).getInternalEventMappings();
		else if(con instanceof IRMessageEvent)
			rps = ((IMPlanParameter)getModelElement()).getMessageEventMappings();
		else //if(con instanceof RProcessGoal)
			rps = ((IMPlanParameter)getModelElement()).getGoalMappings();

		for(int i=0; i<rps.length && connector==null; i++)
		{
			String type = rps[i].substring(0, rps[i].indexOf("."));
			String pname = rps[i].substring(rps[i].indexOf(".")+1);
			//System.out.println(type+" "+pname);
			if(con.getType().equals(type)) //&& con.hasParameter(pname))
				connector = con.getParameter(pname);
		}

		if(connector==null)
		{
			for(int i=0; i<rps.length && connector==null; i++)
			{
				String type = rps[i].substring(0, rps[i].indexOf("."));
				String pname = rps[i].substring(rps[i].indexOf(".")+1);
				//System.out.println(type+" "+pname);
				//System.out.println(con.getType());
				if(con.getType().equals(type))
					connector = con.getParameter(pname);
			}
			throw new RuntimeException("No connectable parameter found in: "+getOwner().getName()
				+" "+con+" "+SUtil.arrayToString(rps));
		}
		//else
		//	System.out.println("Connected with: "+connector);
	}

	/**
	 *  Test, if the plan parameter is connectable to another parameter.
	 *  @return True, if connectable.
	 */
	protected boolean isConnectable()
	{
		return ((IMPlanParameter)getModelElement()).getInternalEventMappings().length
			+ ((IMPlanParameter)getModelElement()).getMessageEventMappings().length
			+ ((IMPlanParameter)getModelElement()).getGoalMappings().length >0;
	}

	/**
	 *  Test, if the parameter's state is ok.
	 *  @return True, if ok.
	 */
	protected boolean isInValidConnectionState()
	{
		return isConnectable() && connector!=null || connector==null;
	}

	//-------- initialization methods --------

	/**
	 *  Set the initial value.
	 *  May be a default value (from model),
	 *  or an initiual value provided on creation.
	 */
	public void	setInitialValue()
	{
		Object ret;
		if(connector!=null)
			connector.setInitialValue();
		else
			super.setInitialValue();
	}

	//-------- methods --------

    // todo: implement cache for values

	/**
	 *  Get the value of a typed element.
	 *  @return The value.
	 */
	public Object	getValue()
	{
		assert isInValidConnectionState();

		Object ret;
		if(connector!=null)
			ret = connector.getValue();
		else
			ret = super.getValue();
		return ret;
	}

	/**
	 *  Set a value of a typed element.
	 *  @param value The new value.
	 */
	public void setValue(Object value)
	{
		assert isInValidConnectionState();

		if(connector!=null)
			connector.setValue(value);
		else
			super.setValue(value);
	}

	//-------- protection mode --------

	/**
	 *  Get the protection mode of this parameter.
	 *  @return The protection mode.
	 * /
	public String getProtectionMode()
	{
		assert isInValidConnectionState();

		String ret;
		if(connector!=null)
			ret = connector.getProtectionMode();
		else
			ret = super.getProtectionMode();
		return ret;
	}*/

	/**
	 *  Check if this paramter can be accessed for read access.
	 */
	public void checkReadAccess()
	{
		assert isInValidConnectionState();

		if(connector!=null)
			connector.checkReadAccess();
		else
			super.checkReadAccess();
	}

	/**
	 *  Check if this paramter can be accessed for write access.
	 */
	public void checkWriteAccess()
	{
		assert isInValidConnectionState();

		if(connector!=null)
			connector.checkWriteAccess();
		else
			super.checkWriteAccess();
	}

	/**
	 *  Generate a change event for this element.
	 *  @param event The event.
	 * /
	public void throwSystemEvent(SystemEvent event)
	{
		assert isInValidConnectionState();

		if(connector!=null)
			connector.throwSystemEvent(event);
		else
			super.throwSystemEvent(event);
	}*/

}
