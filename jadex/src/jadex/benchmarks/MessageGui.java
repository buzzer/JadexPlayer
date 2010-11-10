package jadex.benchmarks;

import jadex.runtime.AgentEvent;
import jadex.runtime.IAgentListener;
import jadex.runtime.IBeliefListener;
import jadex.runtime.IExternalAccess;
import jadex.util.SGUI;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MessageGui extends JFrame
{
	/**
	 *  Create a new message gui.
	 */
	public MessageGui(IExternalAccess agent)
	{
		final JLabel sent = new JLabel("Sent: [0]");
		final JLabel rec = new JLabel("Received: [0]");
		
		agent.getBeliefbase().getBelief("sent").addBeliefListener(new IBeliefListener()
		{
			public void beliefChanged(AgentEvent ae)
			{
				sent.setText("Sent: ["+ae.getValue()+"]");
			}
		}, false);
		agent.getBeliefbase().getBelief("received").addBeliefListener(new IBeliefListener()
		{
			public void beliefChanged(AgentEvent ae)
			{
				rec.setText("Received: ["+ae.getValue()+"]");
			}
		}, false);
		agent.addAgentListener(new IAgentListener()
		{
			public void agentTerminating(AgentEvent ae)
			{
				MessageGui.this.dispose();
			}
		}, false);
		
		JPanel infos = new JPanel(new GridLayout(2,1));
		infos.add(sent);
		infos.add(rec);
		getContentPane().add(infos);
		pack();
		setLocation(SGUI.calculateMiddlePosition(this));
		setVisible(true);
	}
}
