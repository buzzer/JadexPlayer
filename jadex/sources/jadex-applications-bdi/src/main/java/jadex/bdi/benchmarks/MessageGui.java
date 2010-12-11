package jadex.bdi.benchmarks;

import jadex.bdi.runtime.AgentEvent;
import jadex.bdi.runtime.IAgentListener;
import jadex.bdi.runtime.IBDIExternalAccess;
import jadex.bdi.runtime.IBeliefListener;
import jadex.bdi.runtime.IEABelief;
import jadex.commons.SGUI;
import jadex.commons.concurrent.IResultListener;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *  Gui for displaying messages.
 */
public class MessageGui extends JFrame
{
	/**
	 *  Create a new message gui.
	 */
	public MessageGui(IBDIExternalAccess agent)
	{
		final JLabel sent = new JLabel("Sent: [0]");
		final JLabel rec = new JLabel("Received: [0]");
		
		
		agent.getBeliefbase().getBelief("sent").addResultListener(new IResultListener() 
		{
			public void resultAvailable(Object source, Object result) 
			{
				((IEABelief)result).addBeliefListener(new IBeliefListener()
				{
					public void beliefChanged(AgentEvent ae)
					{
						sent.setText("Sent: ["+ae.getValue()+"]");
					}
				});
			}
			
			public void exceptionOccurred(Object source, Exception exception) 
			{
			}
		});
		
		agent.getBeliefbase().getBelief("received").addResultListener(new IResultListener() 
		{
			public void resultAvailable(Object source, Object result) 
			{
				((IEABelief)result).addBeliefListener(new IBeliefListener()
				{
					public void beliefChanged(AgentEvent ae)
					{
						rec.setText("Received: ["+ae.getValue()+"]");
					}
				});
			}
			
			public void exceptionOccurred(Object source, Exception exception) 
			{
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
						MessageGui.this.dispose();	
					}
				});
			}
			
			public void agentTerminated(AgentEvent ae)
			{
			}
		});
		
		JPanel infos = new JPanel(new GridLayout(2,1));
		infos.add(sent);
		infos.add(rec);
		getContentPane().add(infos);
		pack();
		setLocation(SGUI.calculateMiddlePosition(this));
		setVisible(true);
	}
}