package jadex.tools.convcenter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import jadex.runtime.*;
import jadex.adapter.fipa.*;

/**
 *  The gui for sending and viewing messages.
 */
public class FipaMessageGui extends JFrame
{
	//-------- attributes --------

	/** The conversation panel. */
	protected FipaConversationPanel	convpanel;
	
	//-------- constructors --------

	/**
	 *  Open the GUI.
	 */
	public FipaMessageGui(final IExternalAccess agent)
	{
		this.setTitle("FipaMessageDialog");

		// Init on awt thread.
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				convpanel	= new FipaConversationPanel(agent, (AgentIdentifier)agent.getBeliefbase().getBelief("receiver").getFact());
				getContentPane().add(BorderLayout.CENTER, convpanel);
				pack();
				setVisible(true);
			}
		});

		// Kill agent on exit.
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				agent.killAgent();
			}
		});
	}

	//-------- constructors --------
	
	/**
	 *  Get the conversation panel.
	 */
	public FipaConversationPanel	getConversationPanel()
	{
		return convpanel;
	}

	//--------- static part ---------

	/**
	 *  Main method for testing gui layout.
	 */
	public static void main(String[] args)
	{
		new FipaMessageGui(null);
	}
}
