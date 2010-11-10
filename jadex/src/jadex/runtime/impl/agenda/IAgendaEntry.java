package jadex.runtime.impl.agenda;


/**
 *
 */
public interface IAgendaEntry
{
	/**
	 *  Get the action.
	 *  @return The action.
	 */
	public IAgendaAction getAction();

	/**
	 *  Get the cause.
	 *  @return The cause.
	 */
	public Object getCause();

}