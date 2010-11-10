package jadex.runtime.impl.agenda.agent;

import java.io.*;
import java.util.logging.Level;
import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.RBDIAgent;
import jadex.util.SReflect;

/**
 *  An action for some arbitrary code to be executed on the agent's thread.
 */
public class InvokeLaterAction	extends AbstractElementAgendaAction implements Serializable
{
	//-------- attributes --------

	/** The agent. */
	protected RBDIAgent agent;

	/** The code to be executed. */
	protected Runnable	code;

	//-------- constructors --------

	/**
	 *  Create an invoke later action.
	 */
	public InvokeLaterAction(RBDIAgent agent, IAgendaActionPrecondition precond, Runnable code)
	{
		super(agent, precond);
		this.agent = agent;
		this.code	= code;
	}

	//-------- IAgendaAction interface --------

	/**
	 *  Execute the action.
	 */
	public void execute()
	{
		try
		{
			code.run();
		}
		catch(RuntimeException ex)
		{
			// Log exception.
			StringWriter	sw	= new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			agent.getLogger().log(Level.SEVERE, agent.getName()+
				": Exception while executing: "+code+"\n"+sw);
		}
	}

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public  String toString()
	{
		return SReflect.getInnerClassName(this.getClass());
	}
}
