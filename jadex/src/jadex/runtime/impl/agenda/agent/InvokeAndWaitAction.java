package jadex.runtime.impl.agenda.agent;

import java.io.*;
import java.util.logging.Level;
import jadex.runtime.impl.agenda.IAgendaActionPrecondition;
import jadex.runtime.impl.RBDIAgent;
import jadex.util.SReflect;

/**
 *  An action for some arbitrary code to be executed on the agent's thread.
 */
public class InvokeAndWaitAction	extends InvokeLaterAction implements Serializable
{
	//-------- attributes --------

	/** The agent. */
	protected RBDIAgent agent;

	/** The monitor for waiting thread. */
	protected Object monitor;

	/** The exception while executing the code. */
	protected RuntimeException	ex;

	/** The finished flag. */
	protected boolean finished;

	//-------- constructors --------

	/**
	 *  Create an invoke later action.
	 */
	InvokeAndWaitAction(RBDIAgent agent, IAgendaActionPrecondition precond, Runnable code, Object monitor)
	{
		super(agent, precond, code);
		this.agent = agent;
		this.code	= code;
		this.monitor = monitor;
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
			if(monitor!=null)
			{
				// Store exception to be thrown on original thread.
				this.ex	= ex;
			}
			else
			{
				// Log exception.
				StringWriter	sw	= new StringWriter();
				ex.printStackTrace(new PrintWriter(sw));
				agent.getLogger().log(Level.SEVERE, agent.getName()+
					": Exception while executing: "+code+"\n"+sw);
			}
		}
		finally
		{
			// Notify monitor to continue execution.
			if(monitor!=null)
			{
				synchronized(monitor)
				{
					finished = true;
					monitor.notify();
				}
			}
		}
	}

	/**
	 *  Test if the action finished normally.
	 *  @return True if action was finished.
	 */
	public boolean isFinished()
	{
		return this.finished;
	}

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public  String toString()
	{
		return SReflect.getInnerClassName(this.getClass());
	}

	/**
	 *  Get the exception (if any).
	 */
	public RuntimeException	getException()
	{
		return ex;
	}
}
