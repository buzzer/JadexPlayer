package jadex.runtime.impl.agenda.conditions;

import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.*;
import jadex.model.*;

/**
 *  The default action for triggered condition.
 */
public class ConditionDefaultAction extends AbstractElementAgendaAction implements java.io.Serializable
{
	//-------- attributes --------

	/** The condition. */
	protected RCondition condition;

	//-------- constructors --------

	/**
	 *  Create a new action.
	 */
	public ConditionDefaultAction(RCondition condition)
	{
		super(condition, new ConditionDefaultPrecondition(condition));
		this.condition = condition;
	}

	//-------- methods --------

	/**
	 *  Throw condition satisfied event.
	 */
	public void	execute()
	{
		RCapability scope = condition.getScope();
		// Must be called as agenda action.
		assert scope.getAgent().getInterpreter().getCurrentAgendaEntry()!=null;

		IMInternalEvent	mevent	= ((IMCapability)scope.getModelElement())
			.getEventbase().getInternalEvent(IMEventbase.TYPE_CONDITION_TRIGGERED);

		// The cause of a conditional triggered action is the corresponding system event
		Object cause = scope.getAgent().getInterpreter().getCurrentAgendaEntry().getCause();

		RInternalEvent event = scope.getEventbase()
			.createInternalConditionTriggeredEvent(mevent, condition);  // todo: remove HACK!!!
		if(cause!=null)
			event.getParameter(IMEventbase.CAUSE).setValue(cause);

		scope.dispatchEvent(event, getPrecondition());
	}
}
