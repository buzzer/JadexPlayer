package jadex.tools.tracer.ui;

import jadex.tools.ontology.Tracing;
import jadex.tools.tracer.nodes.TAgent;
import jadex.tools.tracer.TracerPlugin;
import jadex.util.SGUI;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;

/**
 *  A panel for displaying and editing filter settings for an agent node.
 *  The prototype agent node is used to show/edit default settings.
 */
public class AgentFilterPanel extends JPanel
{
	//-------- attributes --------

	/** The traced agent's node. */
	protected TAgent	agent;
	
	/** The trace belief reads checkbox. */
	protected JCheckBox	breads;

	/** The trace belief writes checkbox. */
	protected JCheckBox	bwrites;

	/** The trace goals checkbox. */
	protected JCheckBox	goals;

	/** The trace plans checkbox. */
	protected JCheckBox	plans;

	/** The trace messages checkbox. */
	protected JCheckBox	messages;

	/** The trace checkbox checkbox. */
	protected JCheckBox	events;

	/** The trace actions checkbox. */
	protected JCheckBox	actions;

	/** The nodes limit combo box. */
	protected JComboBox	nodelimit;

	//-------- constructors -------
	
	/**
	 *  Create new AgentFilterPanel for the given agent node.
	 */
	public AgentFilterPanel(final TAgent agent)
	{
		this.agent	= agent;
		
		Tracing	tracing	= agent.getTracing();
		breads = new JCheckBox("Trace Belief Reads", tracing.isBeliefReads());
		bwrites = new JCheckBox("Trace Belief Writes", tracing.isBeliefWrites());
		goals = new JCheckBox("Trace Goals", tracing.isGoals());
		plans = new JCheckBox("Trace Plans", tracing.isPlans());
		messages = new JCheckBox("Trace Messages", tracing.isMessages());
		events = new JCheckBox("Trace Internal Events", tracing.isEvents());
		actions = new JCheckBox("Trace Actions", tracing.isActions());
		breads.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				agent.getTracing().setBeliefReads(breads.isSelected());
			}
		});
		bwrites.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				agent.getTracing().setBeliefWrites(bwrites.isSelected());
			}
		});
		goals.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				agent.getTracing().setGoals(goals.isSelected());
			}
		});
		plans.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				agent.getTracing().setPlans(plans.isSelected());
			}
		});
		messages.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				agent.getTracing().setMessages(messages.isSelected());
			}
		});
		events.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				agent.getTracing().setEvents(events.isSelected());
			}
		});
		actions.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				agent.getTracing().setActions(actions.isSelected());
			}
		});
		
		nodelimit = new JComboBox(new String[]{"10", "100", "250", "unlimited"});
		nodelimit.setPreferredSize(new Dimension(new JTextField(6).getPreferredSize().width, nodelimit.getPreferredSize().height));
		nodelimit.setSelectedItem(agent.getEnforceNodeLimit() ? ""+agent.getNodesLimt() : "unlimited");
		nodelimit.setEditable(true);

		nodelimit.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				if(nodelimit.getSelectedItem().equals("unlimited"))
				{
					agent.setEnforceNodeLimit(false);
				}
				else
				{
					try
					{
						agent.setNodesLimit(Integer.parseInt((String)nodelimit.getSelectedItem()));
						agent.setEnforceNodeLimit(true);
					}
					catch(NumberFormatException ex)
					{
						nodelimit.setSelectedItem(agent.getEnforceNodeLimit() ? ""+agent.getNodesLimt() : "unlimited");
					}
				}
			}
		});
		
		this.setLayout(new GridBagLayout());
		
		int row	= 0;
		int	col	= 1;

		this.add(new JLabel(LookAndFeel.READ_ICON), new GridBagConstraints(0, row, 1, 1, 0,0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
		this.add(breads, new GridBagConstraints(col, row++, GridBagConstraints.REMAINDER,1, 0,0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
		this.add(new JLabel(LookAndFeel.WRITE_ICON), new GridBagConstraints(0, row, 1, 1, 0,0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
		this.add(bwrites, new GridBagConstraints(col, row++, GridBagConstraints.REMAINDER,1, 0,0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
		this.add(new JLabel(LookAndFeel.GOAL_ICON), new GridBagConstraints(0, row, 1, 1, 0,0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
		this.add(goals, new GridBagConstraints(col, row++, GridBagConstraints.REMAINDER,1, 0,0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
		this.add(new JLabel(LookAndFeel.PLAN_ICON), new GridBagConstraints(0, row, 1, 1, 0,0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
		this.add(plans, new GridBagConstraints(col, row++, GridBagConstraints.REMAINDER,1, 0,0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
		this.add(new JLabel(LookAndFeel.SEND_ICON), new GridBagConstraints(0, row, 1, 1, 0,0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
		this.add(messages, new GridBagConstraints(col, row++, GridBagConstraints.REMAINDER,1, 0,0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
		this.add(new JLabel(LookAndFeel.EVENT_ICON), new GridBagConstraints(0, row, 1, 1, 0,0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
		this.add(events, new GridBagConstraints(col, row++, GridBagConstraints.REMAINDER,1, 0,0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
		this.add(new JLabel(LookAndFeel.ACTION_ICON), new GridBagConstraints(0, row, 1, 1, 0,0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
		this.add(actions, new GridBagConstraints(col, row++, GridBagConstraints.REMAINDER,1, 0,0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
		this.add(new JLabel("Nodes Limit"), new GridBagConstraints(col, row, 1,1, 0,0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,4,0,2), 0,0));
		this.add(nodelimit, new GridBagConstraints(col+1, row++, 1,1, 1,0,
			GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0,4,0,0), 0,0));
	}
	
	//-------- methods --------
	
	/**
	 *  Reset the panel to the values from the model.
	 */
	public void	reset()
	{
		breads.setSelected(agent.getTracing().isBeliefReads());
		bwrites.setSelected(agent.getTracing().isBeliefWrites());
		goals.setSelected(agent.getTracing().isGoals());
		plans.setSelected(agent.getTracing().isPlans());
		messages.setSelected(agent.getTracing().isMessages());
		events.setSelected(agent.getTracing().isEvents());
		actions.setSelected(agent.getTracing().isActions());

		nodelimit.setSelectedItem(agent.getEnforceNodeLimit() ? ""+agent.getNodesLimt() : "unlimited");
	}
}
