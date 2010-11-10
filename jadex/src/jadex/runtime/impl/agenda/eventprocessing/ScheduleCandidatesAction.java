package jadex.runtime.impl.agenda.eventprocessing;

import jadex.runtime.impl.IREvent;
import jadex.runtime.impl.RBDIAgent;
import jadex.runtime.impl.agenda.IAgendaActionPrecondition;

import java.util.List;

/**
 *  The action for scheduling selected candidates for an event.
 */
public class ScheduleCandidatesAction extends ProcessEventAction
{
	//-------- attributes --------

	/** The applicable candidates. */
	protected List candidates;

	//-------- constructors --------

	/**
	 *  Create a new schedule candidates action.
	 */
	public ScheduleCandidatesAction(IAgendaActionPrecondition precond, RBDIAgent agent, IREvent event, List candidates)
	{
		super(precond, agent, event);
		this.candidates = candidates;
	}

	//-------- methods --------

	/**
	 *  The action.
	 */
	public void execute()
	{
		// Schedule the selected candidate(s) by creating ExecutePlanActions.
		scheduleCandidates(event, candidates);
	}
}



