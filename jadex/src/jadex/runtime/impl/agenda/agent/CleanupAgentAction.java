package jadex.runtime.impl.agenda.agent;

import jadex.runtime.impl.RBDIAgent;
import jadex.runtime.impl.agenda.AbstractAgendaAction;
import jadex.runtime.impl.agenda.IAgendaActionPrecondition;
import jadex.util.SReflect;

import java.io.Serializable;

/**
 *  Agenda action to cleanup and remove the agent.
 */
public class CleanupAgentAction	extends AbstractAgendaAction implements Serializable
{
	//-------- attributes --------

	/** The agent. */
	protected RBDIAgent agent;

	//-------- constructors --------

	/**
	 *  Create a new action.
	 */
	public CleanupAgentAction(RBDIAgent agent, IAgendaActionPrecondition precond)
	{
		super(precond);
		this.agent = agent;
	}

	//-------- methods --------

	/**
	 *  Initialize the agent.
	 */
	public void execute()
	{
		agent.cleanup();
		agent.setLifecycleState(RBDIAgent.LIFECYCLESTATE_TERMINATED);
		agent.getAgentAdapter().cleanupAgent();
	}

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public  String toString()
	{
		return SReflect.getInnerClassName(this.getClass())+" (agent="+agent.getName()+")";
	}
}
