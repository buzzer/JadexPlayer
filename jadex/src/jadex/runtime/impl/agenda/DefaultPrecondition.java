package jadex.runtime.impl.agenda;

import jadex.runtime.impl.IRElement;
import jadex.util.SReflect;

/**
 *  The default precondition for elements.
 *  Returns true, if the element is not cleaned up.
 */
public class DefaultPrecondition implements IAgendaActionPrecondition, java.io.Serializable
{
	//-------- attributes --------

	/** The element. */
	protected IRElement element;

	//-------- constructors --------

	/**
	 *  Create a new precondition.
	 *  @param element The element.
	 */
	public DefaultPrecondition(IRElement element)
	{
		this.element = element;
	}

	//-------- methods --------

	/**
	 *  Perform the check.
	 *  @return True, if condition is valid.
	 */
	public boolean check()
	{
		// For deciding if the agenda action is still relevant.
		return !element.isCleanedup();
	}

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public String toString()
	{
		return SReflect.getInnerClassName(this.getClass())+"( valid="+check()+" )";
	}
}
