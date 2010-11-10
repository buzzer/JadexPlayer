package jadex.runtime.impl.agenda.elementupdate;

import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.*;
import jadex.runtime.SystemEvent;
import jadex.util.SReflect;

/**
 *  Update the values in time intervals
 *  as specified by the update rate.
 */
public class UpdateSetAction extends AbstractElementAgendaAction implements java.io.Serializable
{
	//-------- attributes --------

	/** The element set. */
	protected RTypedElementSet elementset;

	//-------- constructors --------

	/**
	 *  Create a new action.
	 */
	public UpdateSetAction(RTypedElementSet elementset, IAgendaActionPrecondition precond)
	{
		super(elementset, precond);
		this.elementset = elementset;
	}

	//-------- methods --------

	/**
	 *  Execute the condition consequences.
	 */ 
	public void	execute()
	{
		// Check if element is still alive.
		if(!elementset.isCleanedup())
		{
			// Insert new time entry.
			elementset.getScope().getAgent().addTimetableEntry(
				new TimetableData(elementset.getUpdateRate(), this));

			if(elementset.internalGetInivals() != null)
			{
				elementset.internalGetInivals().refresh();
			}
			elementset.throwSystemEvent(elementset.createSystemEvent(SystemEvent.ESVALUES_CHANGED, null, -1));
		}
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
		return SReflect.getInnerClassName(this.getClass())+" (element="+elementset.getName()+")";
	}
}
