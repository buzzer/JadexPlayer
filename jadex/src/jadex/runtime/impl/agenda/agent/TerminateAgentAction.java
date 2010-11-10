package jadex.runtime.impl.agenda.agent;

import jadex.runtime.SystemEvent;
import jadex.runtime.impl.RBDIAgent;
import jadex.runtime.impl.TimetableData;
import jadex.runtime.impl.agenda.AbstractAgendaAction;
import jadex.runtime.impl.agenda.IAgendaActionPrecondition;
import jadex.util.SReflect;

/**
 *  Agenda action to cleanup and remove the agent.
 */
public class TerminateAgentAction	extends AbstractAgendaAction
{
	//-------- attributes --------

	/** The agent. */
	protected RBDIAgent agent;

	//-------- constructors --------

	/**
	 *  Create a new action.
	 */
	public TerminateAgentAction(RBDIAgent agent)
	{
		super(null);
		this.agent = agent;
	}

	//-------- methods --------

	/**
	 *  Initialize the agent.
	 */
	public void execute()
	{
		// Change to terminating state and activate end state.
		agent.setLifecycleState(RBDIAgent.LIFECYCLESTATE_TERMINATING);
		agent.exitRunningState();
		agent.throwSystemEvent(new SystemEvent(SystemEvent.AGENT_TERMINATING, agent));
		agent.activateEndState();
		
		int	timeout	= 10000;
		try
		{
			Integer	itimeout	= (Integer)agent.getPropertybase().getProperty("termination.timeout");
			if(itimeout==null)
				itimeout	= (Integer)agent.getPropertybase().getProperty("standard.timeout");
			timeout	= itimeout.intValue();
		}
		catch(RuntimeException e)
		{
			agent.getLogger().warning("Agent cannot determine timeout: "+e);
		}
		
		// Todo: different timeouts for each capability.
		agent.addTimetableEntry(new TimetableData(timeout, new CleanupAgentAction(agent, new IAgendaActionPrecondition()
		{
			public boolean check()
			{
				// Agent should not be already terminated (if condition is triggered from different capabilities).
				return RBDIAgent.LIFECYCLESTATE_TERMINATING.equals(agent.getLifecycleState());
			}
		})));
	}
	
	/**
	 *  Check if the action can be executed.
	 */
	public boolean isValid()
	{
		return super.isValid() && RBDIAgent.LIFECYCLESTATE_ALIVE.equals(agent.getLifecycleState());
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
