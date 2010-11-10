package jadex.tools.common.plugin;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.runtime.IExternalAccess;

import java.util.Map;

import javax.swing.JComponent;

/**
 *  Control center interface providing useful methods
 *  that may be called from plugins. 
 */
public interface IControlCenter
{
	/**
	 *  Get the external access interface.
	 *  @return the external agent access.
	 */
	public IExternalAccess getAgent();

	/**
	 *  Listen for changes to the list of known agents.
	 */
	public void addAgentListListener(IAgentListListener listener);

	/**
	 *  Listen for incoming messages.
	 */
	public void addMessageListener(IMessageListener listener);

	/**
	 *  Create a new agent on the platform.
	 *  Any errors will be displayed in a dialog to the user.
	 */
	public void createAgent(String type, String name, String configname, Map arguments);

	/**
	 *  Kill an agent on the platform.
	 *  Any errors will be displayed in a dialog to the user.
	 */
	public void killAgent(AgentIdentifier name);
	
	/**
	 *  Suspend an agent on the platform.
	 *  Any errors will be displayed in a dialog to the user.
	 */
	public void suspendAgent(AgentIdentifier name);
	
	/**
	 *  Resume an agent on the platform.
	 *  Any errors will be displayed in a dialog to the user.
	 */
	public void resumeAgent(AgentIdentifier name);
	
	/**
	 *  Set a text to be displayed in the status bar.
	 *  The text will be removed automatically after
	 *  some delay (or replaced by some other text).
	 */
	public void setStatusText(String text);
	
	/**
	 *  Add a component to the status bar.
	 *  @param id	An id for later reference.
	 *  @param comp	An id for later reference.
	 */
	public void	addStatusComponent(Object id, JComponent comp);

	/**
	 *  Remove a previously added component from the status bar.
	 *  @param id	The id used for adding the component.
	 */
	public void	removeStatusComponent(Object id);
	
	/**
	 *  Show the console.
	 *  @param show True, if console should be shown.
	 */
	public void showConsole(boolean show);
	
	/**
	 *  Test if console is shown.
	 *  @return True, if shown.
	 */
	public boolean isConsoleShown();
	
	/**
	 *  Set the console height.
	 *  @param height The console height.
	 * /
	public void setConsoleHeight(int height);*/
	
	/**
	 *  Get the console height.
	 *  @return The console height.
	 * /
	public int getConsoleHeight();*/
	
	/**
	 *  Set the console enable state.
	 *  @param enabled The enabled state.
	 */
	public void setConsoleEnabled(boolean enabled);
	
	/**
	 *  Test if the console is enabled.
	 *  @return True, if enabled.
	 */
	public boolean isConsoleEnabled();
}