package jadex.runtime.impl.agenda;

import java.io.Serializable;

/**
 *  Precondition for testing if an agenda action can be executed.
 */
public interface IAgendaActionPrecondition	extends Serializable
{
	/**
	 *  Test, if the precondition is valid.
	 *  @return True, if precondition is valid.
	 */
	public boolean check();
}
