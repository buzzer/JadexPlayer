package jadex.runtime.externalaccesswrapper;

import jadex.runtime.*;
import jadex.runtime.impl.*;
import jadex.model.IMTypedElementSet;

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
		super(parameterset.getScope().getAgent(), parameterset);
		this.parameterset = parameterset;
	}

	//-------- methods --------

	/**
	 *  Add a fact to a parameter.
	 *  @param fact The new fact.
	 */
	public void addValue(final Object fact)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				parameterset.checkWriteAccess();
				parameterset.addValue(fact);
			}
		};
	}

	/**
	 *  Remove a fact to a parameter.
	 *  @param fact The new fact.
	 */
	public void removeValue(final Object fact)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				parameterset.checkWriteAccess();
				parameterset.removeValue(fact);
			}
		};
	}

	/**
	 *  Set all values for a parameter set.
	 */
	public void addValues(final Object[] values)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				parameterset.checkWriteAccess();
				parameterset.addValues(values);
			}
		};
	}

	/**
	 *  Remove all facts from a parameter.
	 */
	public void removeValues()
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				parameterset.checkWriteAccess();
				parameterset.removeValues();
			}
		};
	}

	/**
	 *  Get a value equal to the given object.
	 *  @param oldval The old value.
	 */
	public Object	getValue(final Object oldval)
	{
		//checkThreadAccess();
		AgentInvocation exe = new AgentInvocation()
		{
			public void run()
			{
				parameterset.checkReadAccess();
				object = parameterset.getValue(oldval);
			}
		};
		return exe.object;
	}

	/**
	 *  Test if a fact is contained in a parameter.
	 *  @param fact The fact to test.
	 *  @return True, if fact is contained.
	 */
	public boolean containsValue(final Object fact)
	{
		//checkThreadAccess();
		AgentInvocation exe = new AgentInvocation()
		{
			public void run()
			{
				parameterset.checkReadAccess();
				bool = parameterset.containsValue(fact);
			}
		};
		return exe.bool;
	}

	/**
	 *  Get the facts of a parameterset.
	 *  @return The facts.
	 */
	public Object[]	getValues()
	{
		//checkThreadAccess();
		AgentInvocation exe = new AgentInvocation()
		{
			public void run()
			{
				parameterset.checkReadAccess();
				oarray = parameterset.getValues();
			}
		};
		return exe.oarray;
	}

	/**
	 *  Get the number of values currently
	 *  contained in this set.
	 *  @return The values count.
	 */
	public int size()
	{
		//checkThreadAccess();
		AgentInvocation exe = new AgentInvocation()
		{
			public void run()
			{
				parameterset.checkReadAccess();
				integer = parameterset.size();
			}
		};
		return exe.integer;
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
				parameterset.checkReadAccess();
				clazz = ((IMTypedElementSet)parameterset.getModelElement()).getClazz();
			}
		};
		return exe.clazz;
	}
	
	/**
	 *  Update or add a fact. When the fact is already
	 *  contained it will be updated to the new fact.
	 *  Otherwise the value will be added.
	 *  @param fact The new or changed fact.
	 * /
	public void updateOrAddValue(final Object fact)
	{
		new AgentInvocation()
		{
			public void run()
			{
				parameterset.checkWriteAccess();
				parameterset.updateOrAddValue(fact);
			}
		};
	}*/

	/**
	 *  Update a fact to a new fact. Searches the old
	 *  value with equals, removes it and stores the new fact.
	 *  @param newfact The new fact.
	 * /
	public void updateValue(final Object newfact)
	{
		new AgentInvocation()
		{
			public void run()
			{
				parameterset.checkWriteAccess();
				parameterset.updateValue(newfact);
			}
		};
	}*/

	/**
	 *  Replace a fact with another one.
	 *  @param oldfact The old fact.
	 *  @param newfact The new fact.
	 * /
	public void replaceValue(final Object oldfact, final Object newfact)
	{
		new AgentInvocation()
		{
			public void run()
			{
				parameterset.checkWriteAccess();
				parameterset.replaceValue(oldfact, newfact);
			}
		};
	}*/
}
