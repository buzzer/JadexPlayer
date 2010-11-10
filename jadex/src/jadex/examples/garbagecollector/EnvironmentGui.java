package jadex.examples.garbagecollector;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.runtime.*;
import jadex.util.SGUI;

/**
 *  The gui plan.
 */
public class EnvironmentGui	extends JFrame
{
	//-------- constructors --------

	/**
	 *  Create a new gui.
	 */
	public EnvironmentGui(final IExternalAccess agent)
	{
		super("Garbage Collector Environment");

		MapPanel	map = new MapPanel((Environment)agent.getBeliefbase().getBelief("env").getFact());
		getContentPane().add("Center", map);

		setSize(400, 400);
		setLocation(SGUI.calculateMiddlePosition(this));
		setVisible(true);
		
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				// On exit kill all known agents.
				Environment	env	= (Environment)agent.getBeliefbase().getBelief("env").getFact();
				WorldObject[]	wobs	= env.getWorldObjects();
				for(int i=0; i<wobs.length; i++)
				{
					if(wobs[i].getType().equals(Environment.BURNER)
						|| wobs[i].getType().equals(Environment.COLLECTOR))
					{
						try
						{
							IGoal	kill	= agent.createGoal("ams_destroy_agent");
							kill.getParameter("agentidentifier").setValue(new AgentIdentifier(wobs[i].getName(), true));
							agent.dispatchTopLevelGoalAndWait(kill);
						}
						catch(GoalFailureException gfe) {}
					}
				}
				
				// Finally shutdown environment agent.
				agent.killAgent();
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
						EnvironmentGui.this.dispose();
					}
				});
			}
		}, false);		
	}
}

