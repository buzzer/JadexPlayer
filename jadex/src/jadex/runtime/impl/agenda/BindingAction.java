package jadex.runtime.impl.agenda;

import java.io.*;
import java.util.Map;

/**
 *  The bidning action.
 */
public abstract class BindingAction extends AbstractAgendaAction implements Serializable, Cloneable
{
	//-------- attributes --------

	/** The binding. */
	protected Map	binding;

	//-------- constructors --------

	/**
	 *  Create a new action.
	 */
	public BindingAction(IAgendaActionPrecondition precond)
	{
		super(precond);
	}

	//-------- methods --------

	/**
	 *  Get the binding of the action.
	 *  @return The binding.
	 */
	public Map	getBinding()
	{
		return binding;
	}
	
	/**
	 *  Set the binding of the action.
	 *  @param binding The binding.
	 */
	public void setBinding(Map binding)
	{
		this.binding = binding;
	}

	//-------- helper methods --------

	/**
	 *  Create an instance of the action using the given binding.
	 *  Method used by condition to create clones for each binding.
	 *  Is called from doTrace() in condition.
	 */
	public jadex.runtime.impl.agenda.BindingAction	createBindingInstance(Map binding)
	{
		try
		{
			jadex.runtime.impl.agenda.BindingAction	clone	= (jadex.runtime.impl.agenda.BindingAction)clone();
			clone.binding	= binding;
			return clone;
		}
		catch(CloneNotSupportedException e)
		{
			// Should not happen...
			StringWriter	sw	= new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			throw new RuntimeException("BindingAction could not be cloned: "+sw);
		}
	}
}
