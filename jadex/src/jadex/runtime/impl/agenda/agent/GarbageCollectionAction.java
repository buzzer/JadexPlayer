package jadex.runtime.impl.agenda.agent;

import java.io.Serializable;

import jadex.runtime.impl.RBDIAgent;
import jadex.runtime.impl.agenda.IAgendaAction;

/**
 *  Agenda action to cleanup garbage collected elements
 *  (e.g. unreferenced conditions).
 *  Necessary, because garbage collected elements
 *  should not be cleaned up on Java's finalizer thread.
 */
public class GarbageCollectionAction implements IAgendaAction, Serializable
{
	//-------- attributes --------
	
	/** The agent. */
	protected RBDIAgent	agent;
	
	//-------- constructors --------
	
	/**
	 *  Create garbage collection action.
	 */
	public GarbageCollectionAction(RBDIAgent agent)
	{
		this.agent	= agent;
	}
	
	//-------- IAgendaAction interface --------

	/**
	 *  Called to execute the action.
	 */
	public void execute()
	{
		agent.performGarbageCollection();
	}

	/**
	 *  Called before execute to check if the action is still valid.
	 */
	public boolean isValid()
	{
		// Valid as long as the agent is running.
		return true;
	}
}
