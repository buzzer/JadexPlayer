package jadex.runtime.impl.agenda.elementupdate;

import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.*;
import jadex.runtime.SystemEvent;
import jadex.util.SReflect;

/**
 *  The update action for refreshing the value.
 */
public class UpdateAction extends AbstractElementAgendaAction implements java.io.Serializable
{
	//-------- attributes --------

	private RTypedElement element;

	//-------- constructors --------

	/**
	 *  Create a new action.
	 */
	public UpdateAction(RTypedElement element, IAgendaActionPrecondition precond)
	{
		super(element, precond);
		this.element = element;
	}

	//-------- methods --------

	/**
	 *  Execute the condition consequences.
	 */ 
	public void	execute()
	{
		//System.out.println("UpdateAction start: "+RTypedElement.this.getName());
		// Check if element is still alive.
		if(!element.isCleanedup())
		{
			// Insert new time entry.
			element.getScope().getAgent().addTimetableEntry(
				new TimetableData(element.getUpdateRate(), this));

			if(element.internalGetValue() instanceof RExpression)
				((RExpression)element.internalGetValue()).refresh();

			element.throwSystemEvent(element.createSystemEvent(SystemEvent.VALUE_CHANGED));
		}
		//System.out.println("UpdateAction end: "+RTypedElement.this.getName());
	}

	/**
	* @return the "cause" of this action
	* @see jadex.runtime.impl.agenda.IAgendaAction#getCause()
	* /
	public Object getCause()
	{
		return getName();
	}*/

	/**
	  *  Get the string representation.
	 *  @return The string representation.
	 */
	public  String toString()
	{
		return SReflect.getInnerClassName(this.getClass())+" (element="+element.getName()+")";
	}
}
