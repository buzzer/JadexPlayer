package jadex.tools.introspector;

import jadex.adapter.fipa.AMSAgentDescription;
import jadex.runtime.GoalFailureException;
import jadex.runtime.IGoal;
import jadex.tools.common.AgentTreeTable;
import jadex.tools.common.GuiProperties;
import jadex.tools.common.ObjectCardLayout;
import jadex.tools.common.jtreetable.DefaultTreeTableNode;
import jadex.tools.common.jtreetable.TreeTableNodeType;
import jadex.tools.common.plugin.IAgentListListener;
import jadex.tools.jcc.AbstractJCCPlugin;
import jadex.util.SGUI;
import jadex.util.SUtil;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Properties;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreePath;


/**
 *  Introspector plugin allows to inspect beliefs, goals and plans of an agent
 *  and to debug the steps in the agent's agenda.
 */
public class IntrospectorPlugin extends AbstractJCCPlugin implements IAgentListListener
{
	// -------- constants --------

	/**
	 * The image icons.
	 */
	protected static final UIDefaults	icons	= new UIDefaults(new Object[]{
		"introspector", SGUI.makeIcon(IntrospectorPlugin.class, "/jadex/tools/common/images/new_introspector.png"),
		"introspector_sel", SGUI.makeIcon(IntrospectorPlugin.class, "/jadex/tools/common/images/new_introspector_sel.png"),
		"introspect_agent", SGUI.makeIcon(IntrospectorPlugin.class, "/jadex/tools/common/images/new_introspector.png"),
		"close_introspector", SGUI.makeIcon(IntrospectorPlugin.class, "/jadex/tools/common/images/close_introspector.png"),
		"agent_introspected", SGUI.makeIcon(IntrospectorPlugin.class, "/jadex/tools/common/images/new_agent_introspected.png"),
		"introspector_empty", SGUI.makeIcon(IntrospectorPlugin.class, "/jadex/tools/common/images/introspector_empty.png"),
		"show_beliefbase", SGUI.makeIcon(IntrospectorPlugin.class, "/jadex/tools/common/images/bulb2.png"),
		"show_goalbase", SGUI.makeIcon(IntrospectorPlugin.class, "/jadex/tools/common/images/cloud2.png"),
		"show_planbase", SGUI.makeIcon(IntrospectorPlugin.class, "/jadex/tools/common/images/plan2.png"),
		"show_debugger", SGUI.makeIcon(IntrospectorPlugin.class, "/jadex/tools/common/images/bug_small.png"),
	});

	//-------- attributes --------

	/** The split panel. */
	protected JSplitPane	split;

	/** The agent tree table. */
	protected AgentTreeTable	agents;

	/** The detail panel. */
	protected JPanel	detail;

	/** The detail layout. */
	protected ObjectCardLayout	cards;
	
	/** The checkbox items for selecting default views. */
	protected JCheckBoxMenuItem[]	checkboxes;

	//-------- constructors --------
	
	/**
	 *  Create a new introspector plugin.
	 */
	public IntrospectorPlugin()
	{
		this.checkboxes	= new JCheckBoxMenuItem[]
		{
			new JCheckBoxMenuItem("Show Beliefbase", icons.getIcon("show_beliefbase")),
			new JCheckBoxMenuItem("Show Goalbase", icons.getIcon("show_goalbase")),
			new JCheckBoxMenuItem("Show Planbase", icons.getIcon("show_planbase")),
			new JCheckBoxMenuItem("Show Debugger", icons.getIcon("show_debugger"))
		};
	}
	
	//-------- IControlCenterPlugin interface --------
	
	/**
	 *  Get plugin properties to be saved in a project.
	 */
	public void getProperties(Properties props)
	{
		for(int i=0; i<checkboxes.length; i++)
		{
			props.setProperty(checkboxes[i].getText(), ""+checkboxes[i].isSelected());
		}
	}
	
	/**
	 *  Set plugin properties loaded from a project.
	 */
	public void setProperties(Properties props)
	{
		for(int i=0; i<checkboxes.length; i++)
		{
			String	selected	= props.getProperty(checkboxes[i].getText());
			checkboxes[i].setSelected(selected!=null ? new Boolean(selected).booleanValue() : false);
		}
	}
	
	/**
	 * @return "Introspector"
	 * @see jadex.tools.common.plugin.IControlCenterPlugin#getName()
	 */
	public String getName()
	{
		return "Introspector";
	}

	/**
	 * @return the icon of introspector
	 * @see jadex.tools.common.plugin.IControlCenterPlugin#getToolIcon()
	 */
	public Icon getToolIcon(boolean selected)
	{
		return selected? icons.getIcon("introspector_sel"): icons.getIcon("introspector");
	}

	/**
	 *  Create tool bar.
	 *  @return The tool bar.
	 */
	public JComponent[] createToolBar()
	{
		JButton b1 = new JButton(START_INTROSPECTOR);
		b1.setBorder(null);
		b1.setToolTipText(b1.getText());
		b1.setText(null);
		b1.setEnabled(true);

		JButton b2 = new JButton(STOP_INTROSPECTOR);
		b2.setBorder(null);
		b2.setToolTipText(b2.getText());
		b2.setText(null);
		b2.setEnabled(true);
		
		return new JComponent[]{b1, b2};
	}
	
	/**
	 *  Create menu bar.
	 *  @return The menu bar.
	 */
	public JMenu[] createMenuBar()
	{
		JMenu	menu	= new JMenu("Default Options");
		for(int i=0; i<checkboxes.length; i++)
			menu.add(checkboxes[i]);
		
		return new JMenu[]{menu};
	}
	
	/**
	 *  Create main panel.
	 *  @return The main panel.
	 */
	public JComponent createView()
	{
		this.split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		split.setOneTouchExpandable(true);

		agents = new AgentTreeTable();
		agents.setMinimumSize(new Dimension(0, 0));
		split.add(agents);
		agents.getTreetable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		agents.getTreetable().getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				JTree tree = agents.getTreetable().getTree();
				if(!e.getValueIsAdjusting() && !tree.isSelectionEmpty())
				{
					DefaultTreeTableNode node = (DefaultTreeTableNode)tree.getSelectionPath().getLastPathComponent();
					cards.show(node.getUserObject());
				}
			}
		});
		// Change agent node type to enable introspected icon for agents.
		agents.addNodeType(new TreeTableNodeType(AgentTreeTable.NODE_AGENT,
			new Icon[0], new String[]{"name", "address"}, new String[]{"Name", "Address"})
		{
			public Icon selectIcon(Object value)
			{
				Icon ret;
				AMSAgentDescription ad = (AMSAgentDescription)((DefaultTreeTableNode)value).getUserObject();
				if(cards.getComponent(ad)!=null)
				{
					ret = IntrospectorPlugin.icons.getIcon("agent_introspected");
				}
				else
				{
					ret = AgentTreeTable.icons.getIcon(AgentTreeTable.NODE_AGENT);
				}
				return ret;
			}
		});
		agents.getNodeType(AgentTreeTable.NODE_AGENT).addPopupAction(START_INTROSPECTOR);
		agents.getNodeType(AgentTreeTable.NODE_AGENT).addPopupAction(STOP_INTROSPECTOR);

		JLabel	emptylabel	= new JLabel("Select agents to activate the introspector",
			icons.getIcon("introspector_empty"), JLabel.CENTER);
		emptylabel.setVerticalAlignment(JLabel.CENTER);
		emptylabel.setHorizontalTextPosition(JLabel.CENTER);
		emptylabel.setVerticalTextPosition(JLabel.BOTTOM);
		emptylabel.setFont(emptylabel.getFont().deriveFont(emptylabel.getFont().getSize()*1.3f));

		cards = new ObjectCardLayout();
		detail = new JPanel(cards);
		detail.setMinimumSize(new Dimension(0, 0));
		detail.add(ObjectCardLayout.DEFAULT_COMPONENT, emptylabel);
		split.add(detail);
		//split.setResizeWeight(1.0);

		GuiProperties.setupHelp(split, "tools.introspector");

		agents.getTreetable().getSelectionModel().setSelectionInterval(0, 0);
		split.setDividerLocation(150);

		agents.getTreetable().addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if(e.getClickCount() == 2)
				{
					if(START_INTROSPECTOR.isEnabled())
						START_INTROSPECTOR.actionPerformed(null);
					else if(STOP_INTROSPECTOR.isEnabled())
						STOP_INTROSPECTOR.actionPerformed(null);
				}

			}
		});

		jcc.addAgentListListener(this);
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				agents.adjustColumnWidths();
			}
		});

		return split;
	}
	
	/**
	 * @param ad
	 */
	public void agentDied(final AMSAgentDescription ad)
	{
		// Update components on awt thread.
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				agents.removeAgent(ad);
				if(cards.isAvailable(ad))
				{
					detail.remove(cards.getComponent(ad));
				}
			}
		});
	}

	/**
	 * @param ad
	 */
	public void agentBorn(final AMSAgentDescription ad)
	{
		// Update components on awt thread.
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				// hack dont introspect the agent
				// if(!jcc.getAgent().getAgentIdentifier().equals(ad.getName()))
				// {
				agents.addAgent(ad);
				// }
			}
		});
	}

	/**
	 * @param ad
	 */
	public void agentChanged(final AMSAgentDescription ad)
	{
		// nop?
		// Update components on awt thread.
		/*
		 * SwingUtilities.invokeLater(new Runnable() { public void run() {
		 * agents.addAgent(ad); } });
		 */
	}

	final AbstractAction	START_INTROSPECTOR	= new AbstractAction("Introspect Agent", icons.getIcon("introspect_agent"))
	{
		public void actionPerformed(ActionEvent e)
		{
			if(!isEnabled())
				return;
			
			split.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			DefaultTreeTableNode node = (DefaultTreeTableNode)agents.getTreetable()
				.getTree().getSelectionPath().getLastPathComponent();
			AMSAgentDescription desc = (AMSAgentDescription)node.getUserObject();
			try
			{				
				boolean[]	active	= new boolean[checkboxes.length];
				for(int i=0; i<checkboxes.length; i++)
					active[i]	= checkboxes[i].isSelected();
				ToolPanel	intro = new ToolPanel(getJCC().getAgent(), desc.getName(), active);
				IGoal manage = getJCC().getAgent().getGoalbase().createGoal("manage_tool");
				manage.getParameter("tool").setValue(intro);
				getJCC().getAgent().dispatchTopLevelGoal(manage);
				GuiProperties.setupHelp(intro, "tools.introspector");
				detail.add(intro, node.getUserObject());
			}
			catch(GoalFailureException ex)
			{
				String text = SUtil.wrapText("Manage tool goal failed: " + ex.getMessage());
				JOptionPane.showMessageDialog(SGUI.getWindowParent(split), text, "Manager Tool Problem", JOptionPane.INFORMATION_MESSAGE);
			}
			agents.updateAgent(desc);
			split.setCursor(Cursor.getDefaultCursor());
		}

		public boolean isEnabled()
		{
			boolean	ret	= false;
			TreePath	path	= agents.getTreetable().getTree().getSelectionPath();
			if(path!=null)
			{
				DefaultTreeTableNode node = (DefaultTreeTableNode)path.getLastPathComponent();
				ret = node!=null && node.getUserObject() instanceof AMSAgentDescription
					&& cards.getComponent(node.getUserObject())==null;
			}
			return ret;
		}
	};

	final AbstractAction	STOP_INTROSPECTOR	= new AbstractAction("Close Introspector", icons.getIcon("close_introspector"))
	{
		public void actionPerformed(ActionEvent e)
		{
			if(!isEnabled())
				return;

			split.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			DefaultTreeTableNode node = (DefaultTreeTableNode)agents.getTreetable().getTree().getSelectionPath().getLastPathComponent();
			ToolPanel intro = (ToolPanel)cards.getComponent(node.getUserObject());
			
			// Try to find goal for panel.
			IGoal	goal	= null;
			IGoal[]	goals	= getJCC().getAgent().getGoalbase().getGoals("manage_tool");
			for(int i=0; goal==null && i<goals.length; i++)
			{
				if(intro.equals(goals[i].getParameter("tool").getValue()))
					goal	= goals[i];
			}
			goal.drop();
			detail.remove(intro);
			agents.updateAgent((AMSAgentDescription)node.getUserObject());
			split.setCursor(Cursor.getDefaultCursor());
		}

		public boolean isEnabled()
		{
			boolean	ret	= false;
			TreePath	path	= agents.getTreetable().getTree().getSelectionPath();
			if(path!=null)
			{
				DefaultTreeTableNode node = (DefaultTreeTableNode)path.getLastPathComponent();
				ret = node!=null && node.getUserObject() instanceof AMSAgentDescription
					&& cards.getComponent(node.getUserObject())!=null;
			}
			return ret;
		}
	};

	/**
	 * @return the help id of the perspective
	 * @see jadex.tools.jcc.AbstractJCCPlugin#getHelpID()
	 */
	public String getHelpID()
	{
		return "tools.introspector";
	}
}
