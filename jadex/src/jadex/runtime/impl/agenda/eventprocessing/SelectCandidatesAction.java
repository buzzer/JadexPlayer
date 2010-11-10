package jadex.runtime.impl.agenda.eventprocessing;

import java.util.*;
import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.*;

/**
 *  The action for selecting candidates for an event.
 */
public class SelectCandidatesAction extends ProcessEventAction
{
	//-------- attributes --------

	/** The applicable candidates. */
	protected List applicables;

	//-------- constructors --------

	/**
	 *  Create a new select candidates action.
	 */
	public SelectCandidatesAction(IAgendaActionPrecondition precond, RBDIAgent agent, IREvent event, List applicables)
	{
		super(precond, agent, event);
		this.applicables = applicables;
	}

	//-------- methods --------

	/**
	 *  The action.
	 */
	public void execute()
	{
		boolean ml = false;
		if(applicables.size()>1)
		{
			ml = initiateMetaLevelReasoning(event, applicables);
		}
		
		if(!ml)
		{
			// Select a candidate (without meta-level reasoning)
			List candidates = selectCandidates(event, applicables);
			agent.getInterpreter().addAgendaEntry(new ScheduleCandidatesAction(getPrecondition(), agent, event, candidates), this);
		}
	}

}

