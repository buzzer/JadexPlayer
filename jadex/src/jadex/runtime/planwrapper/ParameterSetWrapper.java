package jadex.runtime.planwrapper;

import jadex.runtime.*;
import jadex.runtime.impl.*;
import jadex.model.*;

/**
 *  The parameter set wrapper.
 */
public class ParameterSetWrapper extends ElementWrapper implements IParameterSet
{
	//-------- attributes --------

	/** The original parameter set. */
	protected IRParameterSet parameterset;

	//-------- constructors --------

	/**
	 *  Create a new parameter set wrapper.
	 */
	protected ParameterSetWrapper(IRParameterSet parameterset)
	{
		super(parameterset);
		this.parameterset = parameterset;
	}

	/**
	 *  Add a fact to a parameter.
	 *  @param fact The new fact.
	 */
	public void addValue(Object fact)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try
		{
			parameterset.checkWriteAccess();
			parameterset.addValue(fact);
		}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Remove a fact to a parameter.
	 *  @param fact The new fact.
	 */
	public void removeValue(Object fact)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try
		{
			parameterset.checkWriteAccess();
			parameterset.removeValue(fact);
		}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Set all values for a parameter set.
	 */
	public void addValues(Object[] values)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try
		{
			parameterset.checkWriteAccess();
			parameterset.addValues(values);
		}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Remove all facts from a parameter.
	 */
	public void removeValues()
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try
		{
			parameterset.checkWriteAccess();
			parameterset.removeValues();
		}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Get a value equal to the given object.
	 *  @param oldval The old value.
	 */
	public Object	getValue(Object oldval)
	{
		checkThreadAccess();
		parameterset.checkReadAccess();
		return parameterset.getValue(oldval);
	}

	/**
	 *  Test if a fact is contained in a parameter.
	 *  @param fact The fact to test.
	 *  @return True, if fact is contained.
	 */
	public boolean containsValue(Object fact)
	{
		checkThreadAccess();
		parameterset.checkReadAccess();
		return parameterset.containsValue(fact);
	}

	/**
	 *  Get the facts of a parameterset.
	 *  @return The facts.
	 */
	public Object[]	getValues()
	{
		checkThreadAccess();
		parameterset.checkReadAccess();
		return parameterset.getValues();
	}

	/**
	 *  Get the number of values currently
	 *  contained in this set.
	 *  @return The values count.
	 */
	public int size()
	{
		checkThreadAccess();
		parameterset.checkReadAccess();
		return parameterset.size();
	}

	/**
	 *  Get the value class.
	 *  @return The value class.
	 */
	public Class	getClazz()
	{
		checkThreadAccess();
		parameterset.checkReadAccess();
		return parameterset.getClazz();
	}

	/**
	 *  Update or add a fact. When the fact is already
	 *  contained it will be updated to the new fact.
	 *  Otherwise the value will be added.
	 *  @param fact The new or changed fact.
	 * /
	public void updateOrAddValue(Object fact)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try
		{
			parameterset.checkWriteAccess();
			parameterset.updateOrAddValue(fact);
		}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}*/

	/**
	 *  Update a fact to a new fact. Searches the old
	 *  value with equals, removes it and stores the new fact.
	 *  @param newfact The new fact.
	 * /
	public void updateValue(Object newfact)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try
		{
			parameterset.checkWriteAccess();
			parameterset.updateValue(newfact);
		}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}*/

	/**
	 *  Replace a fact with another one.
	 *  @param oldfact The old fact.
	 *  @param newfact The new fact.
	 * /
	public void replaceValue(Object oldfact, Object newfact)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try
		{
			parameterset.checkWriteAccess();
			parameterset.replaceValue(oldfact, newfact);
		}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}*/
}
