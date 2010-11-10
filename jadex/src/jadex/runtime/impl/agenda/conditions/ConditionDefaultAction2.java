package jadex.runtime.impl.agenda.conditions;

import jadex.runtime.impl.agenda.AbstractAgendaAction;
import jadex.runtime.impl.*;
import jadex.model.*;

/**
 *  The default action for triggered condition.
 *  todo: should be unified with the ConditionDefaultAction!
 *  Is separate because it is for the InterpreterConditions.
 */
public class ConditionDefaultAction2 extends AbstractAgendaAction implements java.io.Serializable
{
	//-------- attributes --------

	/** The condition. */
	protected IInterpreterCondition condition;

	/** The scope. */
	protected RCapability scope;

	//-------- constructors --------

	/**
	 *  Create a new action.
	 */
	public ConditionDefaultAction2(IInterpreterCondition condition, RCapability scope)
	{
		super(null); // todo: ?
		this.condition = condition;
		this.scope = scope;
	}

	//-------- methods --------

	/**
	 *  Throw condition satisfied event.
	 */
	public void	execute()
	{
		// Must be called as agenda action.
		assert scope.getAgent().getInterpreter().getCurrentAgendaEntry()!=null;

		IMInternalEvent	mevent	= ((IMCapability)scope.getModelElement())
			.getEventbase().getInternalEvent(IMEventbase.TYPE_CONDITION_TRIGGERED);

		// The cause of a conditional triggered action is the corresponding system event
		Object cause = scope.getAgent().getInterpreter().getCurrentAgendaEntry().getCause();

		RInternalEvent event = scope.getEventbase().createInternalEvent(mevent);
		event.getParameter(IMEventbase.CONDITION).setValue(condition);
		if(cause!=null)
			event.getParameter(IMEventbase.CAUSE).setValue(cause);

		scope.dispatchEvent(event, getPrecondition());
	}
}
