package jadex.runtime.impl.agenda;

import jadex.runtime.impl.*;

/**
 *  Abstract action for (most) actions that belong to one RElement.
 *  Creates and uses the default precondition.
 */
public abstract class AbstractElementAgendaAction extends AbstractAgendaAction
{
	//-------- constructors --------

	/**
	 * Create a deliberation action.
	 */
	public AbstractElementAgendaAction(IRElement element, IAgendaActionPrecondition precondition)
	{
		super(new ComposedPrecondition(new DefaultPrecondition(element), precondition));
	}
}
