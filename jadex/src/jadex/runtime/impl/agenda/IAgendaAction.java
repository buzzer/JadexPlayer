package jadex.runtime.impl.agenda;

/**
 *  This interface can be used for commands.
 */
public interface IAgendaAction
{
	/**
	 *  Execute the command.
	 */
	public void execute();

	/**
	 *  Test if precondition is valid.
	 *  @return True, if valid.
	 */
	public boolean isValid();
}
