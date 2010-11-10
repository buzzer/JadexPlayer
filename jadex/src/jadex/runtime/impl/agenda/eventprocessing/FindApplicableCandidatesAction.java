package jadex.runtime.impl.agenda.eventprocessing;

import jadex.runtime.impl.IREvent;
import jadex.runtime.impl.RBDIAgent;
import jadex.runtime.impl.agenda.IAgendaActionPrecondition;

import java.util.List;

/**
 *  The action for finding applicable candidates for an event.
 */
public class FindApplicableCandidatesAction extends ProcessEventAction
{
	//-------- constructors --------

	/**
	 *  Create a new select candidates action.
	 */
	public FindApplicableCandidatesAction(IAgendaActionPrecondition precond, RBDIAgent agent, IREvent event)
	{
		super(precond, agent, event);
	}

	//-------- methods --------

	/**
	 *  The action.
	 */
	public void execute()
	{
		// Perform the APL generation.
		//List applicables = findApplicableCandidates(event);
		List applicables = event.getApplicableCandidatesList().getCandidates();
		if(applicables.size()>0)
		{
			agent.getInterpreter().addAgendaEntry(new SelectCandidatesAction(getPrecondition(), agent, event, applicables), this);
		}
		else
		{
			eventNotHandled(event);		
		}
	}
}

