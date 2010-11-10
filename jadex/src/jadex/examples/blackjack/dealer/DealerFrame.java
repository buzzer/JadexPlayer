package jadex.examples.blackjack.dealer;

import jadex.examples.blackjack.*;
import jadex.examples.blackjack.gui.*;
import jadex.runtime.AgentEvent;
import jadex.runtime.IAgentListener;
import jadex.runtime.IExternalAccess;
import jadex.util.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.*;

/**
 * This GUI gives an overview of the ongoings during the game.
 * It shows different panels representing the 'internal states'
 * (the cards, accounts and bets) of the dealer- and all the
 * player-agents as well as one special panel containing a
 * progressBar and a few buttons (the 'OptionPanel').
 * But this class is not just a viewing-component, but also an
 * agent-plan and therefor it contains some jadex-specific code,
 * which is perhaps worth looking at.
 */
public class DealerFrame extends GameStateFrame
{
	//-------- attributes --------

	/** The agent access object. */
	protected IExternalAccess	agent;
	
	/** child windows (e.g. statistics). */
	protected Set	children;

	//-------- constructors --------
	
	/**
	 * Creates a new instance of the dealer frame.
	 * Here, the GUI is build up for the first time, all
	 * panels are instantiated and shown on the screen.
	 */
	public DealerFrame(final Dealer me, final IExternalAccess agent)
	{
		super(null, null);
		this.agent	= agent;
		this.children	= new HashSet();

		// Show Jadex version information.
		String	title	= "Blackjack Dealer";
			//+ Configuration.getConfiguration().getReleaseNumber()
			//+ " (" + Configuration.getConfiguration().getReleaseDate() + ")";

		//Create the 'Main'-Window and the contentPane
		setTitle(title);
		//setResizable(false);
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				DealerFrame.this.agent.killAgent();
			}
		});
		agent.addAgentListener(new IAgentListener()
		{
			public void agentTerminating(AgentEvent ae)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						DealerFrame.this.dispose();
					}
				});
			}
		}, false);

		// set the icon to be displayed for the frame
		setIconImage(GUIImageLoader.getImage("heart_small_d").getImage());

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				DealerFrame.this.setGameState((GameState)agent.getBeliefbase().getBelief("gamestate").getFact());
			}
		});
		setControlPanel(new DealerOptionPanel(agent, DealerFrame.this));

		/*getContentPane().setLayout(new GridLayout(0, 2));

		// initialise the JPanels for the Dealer and the progressBar
		PlayerPanel	dealerPanel = new PlayerPanel(me);

		// add these JPanels as the first-row to the contentPane
		getContentPane().add(dealerPanel);
		getContentPane().add(new DealerOptionPanel(agent, DealerFrame.this));*/

		// display the gui on the screen
		pack();
		setLocation(SGUI.calculateMiddlePosition(DealerFrame.this));
		setVisible(true);
	}

	/**
	 *  Add a child window to be disposed on exit.
	 */
	public void	addChildWindow(Window child)
	{
		children.add(child);
	}

	/**
	 *  Remove a child window to be disposed on exit.
	 */
	public void	removeChildWindow(Window child)
	{
		children.remove(child);
	}

	/**
	 *  Dispose this window and child windows.
	 */
	public void	dispose()
	{
		super.dispose();

		for(Iterator i=children.iterator(); i.hasNext(); )
			((Window)i.next()).dispose();
	}
}
