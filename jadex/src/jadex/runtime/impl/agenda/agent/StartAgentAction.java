package jadex.runtime.impl.agenda.agent;

import java.io.Serializable;
import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.RBDIAgent;
import jadex.util.SReflect;

/**
 *  Agenda action to initialize and start the agent.
 *  Added automatically when the agent is born.
 */
public class StartAgentAction	extends AbstractElementAgendaAction implements Serializable
{
	//-------- attributes --------

	/** The agent. */
	protected RBDIAgent agent;

	//-------- constructors --------

	/**
	 *  Create a new action.
	 */
	public StartAgentAction(RBDIAgent agent, IAgendaActionPrecondition precond)
	{
		super(agent, precond);
		this.agent = agent;
	}

	//-------- methods --------

	/**
	 *  Initialize the agent.
	 */
	public void execute()
	{
		agent.startAtomic();
		agent.setLifecycleState(RBDIAgent.LIFECYCLESTATE_ALIVE);
		// Inits to be performed in action.
		for(int i=0; i<RBDIAgent.INIT_LEVELS; i++)
			agent.init(i);
		agent.endAtomic();
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
