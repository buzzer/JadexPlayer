package jadex.runtime.impl.agenda.agent;

import java.io.Serializable;
import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.*;
import jadex.util.SReflect;

/**
 *  Agenda action to check the time table.
 *  Added from @link #notifyDue().
 */
public class NotifyDueAction	extends AbstractElementAgendaAction implements Serializable
{
	//-------- attributes --------

	/** The agent. */
	protected RBDIAgent agent;

	//-------- constructors --------

	/**
	 *  Create a new action.
	 */
	public NotifyDueAction(RBDIAgent agent, IAgendaActionPrecondition precond)
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
		while(agent.getTimetable().size()>0)
		{
			TimetableData tt	= (TimetableData)agent.getTimetable().get(0);
			long diff	= tt.getWakeup() - System.currentTimeMillis();

			if(diff<=0)
			{
				// System.out.println("event dispatched");
				agent.getInterpreter().addAgendaEntry(tt.getAgendaAction(), null); // todo: cause time?!
				agent.getTimetable().remove(tt);
				//System.out.println("removed timetable entry: "+tt);
			}
			else
			{
				//System.out.println("notify in: "+diff);
				agent.getAgentAdapter().notifyIn(diff);
				break;
			}
		}
		//System.out.println("Timetable is: "+timetable);
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
