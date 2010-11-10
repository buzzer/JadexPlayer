package jadex.examples.blackjack.manager;

import javax.swing.*;
import jadex.runtime.*;

import java.awt.EventQueue;

/**
 *  Update the Gui when manager informs about changes.
 */
public class ManagerGuiUpdatePlan extends Plan
{
	/**
	 *  Execute a plan.
	 */
	public void body()
	{		
		IMessageEvent msg = (IMessageEvent)getInitialEvent();
		String	content	= (String)msg.getContent();
		final String playerPlaying = content.substring(content.indexOf(':')+1, content.length());
		final ManagerFrame	gui	= (ManagerFrame)getBeliefbase().getBelief("gui").getFact();
		getLogger().info("received playerPlaying-Message " + playerPlaying);

		// AWTThread.
		EventQueue.invokeLater(new Runnable()
		{
			public void	run()
			{
				gui.setPlayerPlaying(playerPlaying);
			}
		});

		waitFor(IFilter.NEVER);
	}

	/**
	 *  On abort close the gui.
	 */
	public void	aborted()
	{
		// Use invoke later to avoid deadlocks,
		// when killAgent was issued by AWT thread.
		final JFrame	gui	= (JFrame)getBeliefbase().getBelief("GUI").getFact();
		if(gui!=null)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					gui.dispose();
				}
			});
		}
	}
}
