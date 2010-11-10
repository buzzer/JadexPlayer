package jadex.tools.introspector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import jadex.adapter.fipa.AgentIdentifier;
import jadex.runtime.*;
import jadex.util.SUtil;


/**
 *  The frame to show the introspector gui.
 */
public class IntrospectorFrame extends JFrame
{
	//-------- attributes --------

	/** The tool panel. */
	protected ToolPanel	toolpanel;
	
	//-------- constructors --------

	/**
	 *  Open the GUI.
	 */
	public IntrospectorFrame(final IExternalAccess agent)
	{
		this.setTitle("Introspector");

		// Init on awt thread.
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					AgentIdentifier	observed	= (AgentIdentifier)agent.getBeliefbase().getBelief("agent").getFact();
					toolpanel	= new ToolPanel(agent, observed, new boolean[]{false, false, false, false});
					IGoal	manage	= agent.getGoalbase().createGoal("manage_tool");
					manage.getParameter("tool").setValue(toolpanel);
					agent.dispatchTopLevelGoal(manage);
				}
				catch(GoalFailureException e)
				{
					String text = SUtil.wrapText("Manage tool goal failed: "+e.getMessage());
					JOptionPane.showMessageDialog(IntrospectorFrame.this, text,
						"Manager Tool Problem", JOptionPane.INFORMATION_MESSAGE);
				}
				getContentPane().add(BorderLayout.CENTER, toolpanel);
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

		// Close window on agent death.
		agent.addAgentListener(new IAgentListener()
		{
			public void agentTerminating(AgentEvent e)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						IntrospectorFrame.this.dispose();
					}
				});
			}
		}, false);	
	}

	//-------- constructors --------
	
	/**
	 *  Get the tool panel.
	 */
	public ToolPanel	getToolPanel()
	{
		return toolpanel;
	}
}
