package jadex.runtime.impl;

import jadex.model.*;
import jadex.util.SUtil;

/**
 *  A plan parameter set supports a mapping to another IRParameterSet.
 */
public class RPlanParameterSet extends RParameterSet
{
	//-------- attributes --------

	/** The mapping element. */
	protected IRParameterSet connector;

	//-------- constructor --------

	/**
	 *  Create a new element.
	 *  To evaluate the initial values, init() has to be called.
	 *  @param parameter The model element.
	 *  @param owner The owner.
	 */
	protected RPlanParameterSet(IMParameterSet parameter, IMConfigParameterSet state, RElement owner)
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
			rps = ((IMPlanParameterSet)getModelElement()).getInternalEventMappings();
		else if(con instanceof IRMessageEvent)
			rps = ((IMPlanParameterSet)getModelElement()).getMessageEventMappings();
		else //if(con instanceof RProcessGoal)
			rps = ((IMPlanParameterSet)getModelElement()).getGoalMappings();

		for(int i=0; i<rps.length && connector==null; i++)
		{
			String type = rps[i].substring(0, rps[i].indexOf("."));
			String pname = rps[i].substring(rps[i].indexOf(".")+1);
			//System.out.println(type+" "+pname);
			if(con.getType().equals(type))
				connector = con.getParameterSet(pname);
		}

		if(connector==null)
			throw new RuntimeException("No connectable parameter found in: "+con+" "+SUtil.arrayToString(rps));
		//else
		//	System.out.println("Connected with: "+connector);
	}

	/**
	 *  Test, if the plan parameter is connectable to another parameter.
	 *  @return True, if connectable.
	 */
	protected boolean isConnectable()
	{
		return ((IMPlanParameterSet)getModelElement()).getInternalEventMappings().length
			+ ((IMPlanParameterSet)getModelElement()).getMessageEventMappings().length
			+ ((IMPlanParameterSet)getModelElement()).getGoalMappings().length >0;
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
	 *  Set the initial values.
	 *  May be default values (from model),
	 *  or initiual values provided on creation.
	 */
	public void	setInitialValues()
	{
		if(connector!=null)
			connector.setInitialValues();
		else
			super.setInitialValues();
	}

	//-------- methods --------

	/**
	 *  Add a value to the typed element.
	 *  @param value The new value.
	 */
	public void addValue(Object value)
	{
		assert isInValidConnectionState();

		if(connector!=null)
			connector.addValue(value);
		else
			super.addValue(value);
	}

	/**
	 *  Remove a value from a typed element
	 *  @param value The new value.
	 */
	public void removeValue(Object value)
	{
		assert isInValidConnectionState();

		if(connector!=null)
			connector.removeValue(value);
		else
			super.removeValue(value);
	}

	/**
	 *  Set all values for a parameter set.
	 */
	public void addValues(Object[] values)
	{
		assert isInValidConnectionState();

		if(connector!=null)
			connector.addValues(values);
		else
			super.addValues(values);
	}

	/**
	 *  Remove all values from a typed element.
	 */
	public void removeValues()
	{
		assert isInValidConnectionState();

		if(connector!=null)
			connector.removeValues();
		else
			super.removeValues();
	}

	/**
	 *  Update or add a value. When the value is already
	 *  contained it will be updated to the new value.
	 *  Otherwise the value will be added.
	 *  @param value The new or changed value
	 * /
	public void updateOrAddValue(Object value)
	{
		//checkWriteAccess();
		super.updateOrAddValue(value);
	}*/

	/**
	 *  Update a value to a new value. Searches the old
	 *  value with equals, removes it and stores the new fact.
	 *  @param newvalue The newvalue
	 */
	public void updateValue(Object newvalue)
	{
		assert isInValidConnectionState();

		if(connector!=null)
			connector.updateValue(newvalue);
		else
			super.updateValue(newvalue);
	}

	/**
	 *  Replace a value with another one.
	 *  @param oldval The old value.
	 *  @param newval The new value.
	 * /
	public void replaceValue(Object oldval, Object newval)
	{
		//checkWriteAccess();
		super.replaceValue(oldval, newval);
	}*/

	/**
	 *  Get a value equal to the given object.
	 *  @param oldval The old value.
	 */
	public Object	getValue(Object oldval)
	{
		assert isInValidConnectionState();

		Object ret;
		if(connector!=null)
			ret = connector.getValue(oldval);
		else
			ret = super.getValue(oldval);
		return ret;
	}

	/**
	 *  Test if a value is contained in a typed element.
	 *  @param value The value to test.
	 *  @return True, if value is contained.
	 */
	public boolean containsValue(Object value)
	{
		assert isInValidConnectionState();

		boolean ret;
		if(connector!=null)
			ret = connector.containsValue(value);
		else
			ret = super.containsValue(value);
		return ret;
	}

	/**
	 *  Get the values of a typed element.
	 *  @return The values.
	 */
	public Object[]	getValues()
	{
		assert isInValidConnectionState();

		Object[] ret;
		if(connector!=null)
			ret = connector.getValues();
		else
			ret = super.getValues();
		return ret;
	}
	
	/**
	 *  Get the number of values currently
	 *  contained in this set.
	 *  @return The values count.
	 */
	public int size()
	{
		int ret;
		if(connector!=null)
			ret = connector.size();
		else
			ret = super.size();
		return ret;
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
}

