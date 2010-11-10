package jadex.runtime.impl.agenda;

import java.util.List;

/**
 *  The agenda of an agent, holding the actions the agent wants to perform.
 *  @see jadex.runtime.impl.agenda.IAgendaEntry
 */
public interface IAgenda
{
	/**
	 *  Check if there are unprocessed elements in the agenda.
	 * /
	public boolean	isEmpty();*/

	/**
	 *  Get the number of entries in the agenda.
	 * /
	public int	size();*/

	/**
	 *  Select and execute an agenda entry.
	 *  @return The executed agenda entry or null if no entry was executed.
	 */
	public IAgendaEntry	executeAction();

	/**
	 *  Add an agenda entry.
	 */
	public void	add(IAgendaAction action, Object cause);

	/**
	 *  Add an agenda entry from external thread.
	 *  This method must be threadsafe as the agenda
	 *  might currently be manipulated by the agent thread
	 *  and other external threads might add external actions
	 *  concurrently.
	 */
	public void addExternal(IAgendaAction action);

	/**
	 *  Get the agenda state. Changes whenever the
	 *  agenda changes. Can be used to determine changes.
	 *  @return The actual state.
	 */
	public int getState();

	/**
	 *  Hack!?
	 *  Get the actual state.
	 *  @param types The types.
	 *  @return The list of change events that describe the state.
	 * /
	public List	getState(String[] types);*/

	/**
	 *  Get unprocessed entries as list.
	 * /
	public List getUnprocessedEntries();*/

	/**
	 *  Get the current agenda entry.
	 *  @return The current agenda entry.
	 */
	public IAgendaEntry getCurrentEntry();

	/**
	 *  Get all (top-level) entries
	 *  @return The (top-level) entries.
	 */
	public List getEntries();
}
