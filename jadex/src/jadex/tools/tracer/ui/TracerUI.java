package jadex.tools.tracer.ui;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import jadex.tools.common.GuiProperties;
import jadex.tools.ontology.Tracing;
import jadex.tools.tracer.TracerController;
import jadex.tools.tracer.nodes.*;


/**
 * <code>TracerUI</code>
 * @since Oct 19, 2004
 */
public final class TracerUI extends JSplitPane implements TreeSelectionListener
{
	/** <code>ctrl</code> the controller with all data for view models */
	final protected TracerController ctrl;

	// ------------------- ui -----------------------

	/** <code>table</code> the table of observations */
	final protected TraceTable table;

	/** <code>table_model</code> the model for the table */
	final protected TraceTableModel table_model;

	/** <code>tree</code> the tree of observations */
	final protected TraceTree tree;

	/** <code>root</code> the root node of this tree */
	protected final TNode root;

	/** <code>graph</code> TouchGraph panel */
	protected final GraphPanel graph;
	
	/** The panel of the default agent filter. */
	protected AgentFilterPanel	afp;

	// ---------------------------------------------------------------

	/** <code>selectedAgent</code> the agent selected in this gui */
	protected TAgent selectedAgent;

	private JCheckBoxMenuItem autoscroll;

	private JCheckBoxMenuItem ignoreAtFirst;

	/**
	 * Constructor: <code>TracerUI</code>.
	 * @param ctrl
	 */
	public TracerUI(final TracerController ctrl)
	{
		super(JSplitPane.HORIZONTAL_SPLIT);
		this.ctrl = ctrl;

		// set up ui
		root = new TTop();
		tree = new TraceTree(root, this);

		table_model = new TraceTableModel();
		table = new TraceTable(table_model, this);


		graph = new GraphPanel(this);
		GuiProperties.setupHelp(graph, "tracer.graph");

		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(TracerUI.this);

		GuiProperties.setupHelp(tree, "tracer.tree");

		setSize(800, 600);

		add(new JScrollPane(tree));

		tableSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		tableSplit.setOneTouchExpandable(true);
		JScrollPane sp = new JScrollPane(table);

		GuiProperties.setupHelp(sp, "tracer.table");
		
		JPanel	north	= new JPanel(new BorderLayout());
		north.add(sp, BorderLayout.CENTER);
		
		this.afp	= new AgentFilterPanel(ctrl.getPrototype());
		JButton	clear	= new JButton("Clear");
		clear.setToolTipText("Clear the list of traces");
		JButton apply	= new JButton("Apply");
		apply.setToolTipText("Apply current settings to all traced agents");
		
		clear.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ctrl.removeTraces(table_model.getAllTraces());
			}
		});
		
		apply.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				TAgent[]	agents	= ctrl.getAgents();
				for(int i=0; i<agents.length; i++)
				{
					// Apply prototype settings but keep ignored/observed state.
					boolean	ignored	= agents[i].isIgnored();
					agents[i].copyFilters(ctrl.getPrototype());
					agents[i].setIgnored(ignored);
					ctrl.enforceNodesLimit(agents[i]);
				}
			}
		});
		
		JPanel	settings	= new JPanel(new GridBagLayout());
		settings.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), " Tracing Settings "));
		settings.add(afp, new GridBagConstraints(0,0, GridBagConstraints.REMAINDER,1, 1,0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 0,0));
		settings.add(clear, new GridBagConstraints(0,1, 1,1, 1,1,
			GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, new Insets(2,4,2,2), 0,0));
		settings.add(apply, new GridBagConstraints(1,1, 1,1, 0,1,
			GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, new Insets(2,2,2,4), 0,0));
		JScrollPane	scrollsettings	= new JScrollPane(settings);
		scrollsettings.setBorder(null);
		north.add(scrollsettings, BorderLayout.EAST);
		tableSplit.add(north);

		tableSplit.add(graph);

		add(tableSplit);

		setOneTouchExpandable(true);
		setDividerLocation(200);

		tableSplit.setDividerLocation(300);
	}

	/**
	 * Sets the menu for this tracer
	 * @return a JMenuBar for this UI
	 */
	public JMenu[] createMenuBar()
	{
		JMenuBar bar	= new JMenuBar();

		// ----------------- AGENT ---------------------------

		JMenu menu_agent = new JMenu("Agent");
		GuiProperties.setupHelp(menu_agent, "tracer.menu_agent");
		menu_agent.add(new JMenuItem(new AbstractAction("Observe")
		{
			public void actionPerformed(ActionEvent e)
			{
				ctrl.ignoreAgent(getSelectedAgent(), false);
			}

			public boolean isEnabled()
			{
				return getSelectedAgent()!=null && !getSelectedAgent().getAID().equals(ctrl.getAID());
			}
		}));
		menu_agent.add(new JMenuItem(new AbstractAction("Observe all")
		{
			public void actionPerformed(ActionEvent e)
			{
				ctrl.ignoreAll(false);
			}

		}));
		menu_agent.addSeparator();

		menu_agent.add(new JMenuItem(new AbstractAction("Ignore")
		{
			public void actionPerformed(ActionEvent e)
			{
				ctrl.ignoreAgent(getSelectedAgent(), true);
			}

			public boolean isEnabled()
			{
				return getSelectedAgent()!=null;
			}

		}));
		menu_agent.add(new JMenuItem(new AbstractAction("Ignore all")
		{
			public void actionPerformed(ActionEvent e)
			{
				ctrl.ignoreAll(true);
			}

		}));
		ignoreAtFirst = new JCheckBoxMenuItem(new AbstractAction("Ignore At First")
		{
			public void actionPerformed(ActionEvent e)
			{
				JCheckBoxMenuItem cb = (JCheckBoxMenuItem)e.getSource();
				ctrl.getPrototype().setIgnored(cb.isSelected());
			}
		});
		ignoreAtFirst.setSelected(ctrl.getPrototype().isIgnored());
		menu_agent.add(ignoreAtFirst);

		menu_agent.addSeparator();
		menu_agent.add(new JMenuItem(new AbstractAction("Show in graph")
		{
			public void actionPerformed(ActionEvent e)
			{
				graph.showTrace(getSelectedAgent(), true);
			}

			public boolean isEnabled()
			{
				return getSelectedAgent()!=null;
			}

		}));
		menu_agent.add(new JMenuItem(new AbstractAction("Hide from graph")
		{
			public void actionPerformed(ActionEvent e)
			{
				graph.showEffects(getSelectedAgent(), false);
				graph.showTrace(getSelectedAgent(), false);
			}

			public boolean isEnabled()
			{
				return getSelectedAgent()!=null;
			}

		}));
		menu_agent.addSeparator();
		menu_agent.add(new JMenuItem(new AbstractAction("Show in table")
		{
			public void actionPerformed(ActionEvent e)
			{
				TNode node = getSelectedAgent();
				if(node!=null)
				{
					table.addTrace(node);
					table.addEffects(node, true);
				}
			}

			public boolean isEnabled()
			{
				return getSelectedAgent()!=null;
			}
		}));

		menu_agent.add(new JMenuItem(new AbstractAction("Hide from table")
		{
			public void actionPerformed(ActionEvent e)
			{
				TNode node = getSelectedAgent();
				if(node!=null)
				{
					table.addEffects(node, false);
					table.removeTrace(node);
				}
			}

			public boolean isEnabled()
			{
				return getSelectedAgent()!=null;
			}
		}));

		menu_agent.addSeparator();
		menu_agent.add(new JMenuItem(new AbstractAction("Delete")
		{
			public void actionPerformed(ActionEvent e)
			{
				ctrl.removeAgent(getSelectedAgent());
			}

			public boolean isEnabled()
			{
				return getSelectedAgent()!=null;
			}

		}));
		menu_agent.add(new JMenuItem(new AbstractAction("Delete dead agents")
		{
			public void actionPerformed(ActionEvent e)
			{
				ctrl.removeDeathAgents();
			}
		}));
		menu_agent.addSeparator();

		menu_agent.add(tree.getFilterMenu());

		menu_agent.add(getAgentFilterMenu("Default filter ...", new AgentFilterModel()
		{
			public TAgent getNode()
			{
				return ctrl.getPrototype();
			}
		}));

		menu_agent.addMenuListener(MENU_ACTIVATOR);
		bar.add(menu_agent);


		// ----------------- TABLE ---------------------------

		JMenu menu_table = new JMenu("Table");
		GuiProperties.setupHelp(menu_table, "tracer.menu_table");
		menu_table.add(new JMenuItem(new AbstractAction("Select causes")
		{
			public void actionPerformed(ActionEvent e)
			{
				table.selectCauses();
			}
		}));
		menu_table.add(new JMenuItem(new AbstractAction("Select effects")
		{
			public void actionPerformed(ActionEvent e)
			{
				table.selectEffects();
			}

		}));

		menu_table.addSeparator();
		menu_table.add(new JMenuItem(new AbstractAction("Show in graph")
		{
			public void actionPerformed(ActionEvent e)
			{
				graph.showTraces(table.getSelected(), true);
			}
		}));

		menu_table.add(new JMenuItem(new AbstractAction("Hide in graph")
		{
			public void actionPerformed(ActionEvent e)
			{
				graph.showTraces(table.getSelected(), false);
			}
		}));

		menu_table.addSeparator();
		menu_table.add(new JMenuItem(new AbstractAction("Remove")
		{
			public void actionPerformed(ActionEvent e)
			{
				table.removeSelected();
			}
		}));

		menu_table.addSeparator();
		menu_table.add(new JMenuItem(new AbstractAction("Delete")
		{
			public void actionPerformed(ActionEvent e)
			{
				ctrl.removeTraces(table.getSelected());
			}
		}));
		menu_table.addSeparator();
		autoscroll = new JCheckBoxMenuItem(new AbstractAction("Scroll")
		{
			public void actionPerformed(ActionEvent e)
			{
				JCheckBoxMenuItem cb = (JCheckBoxMenuItem)e.getSource();
				table.setAutoScroll(cb.isSelected());
			}
		});
		autoscroll.setSelected(table.isAutoScroll());
		menu_table.add(autoscroll);

		bar.add(menu_table);


		// ----------------- GRAPH ---------------------------

		JMenu menu_graph = new JMenu("Graph");
		GuiProperties.setupHelp(menu_graph, "tracer.menu_graph");
		graph.fillPopupMenu(menu_graph.getPopupMenu());

		bar.add(menu_graph);

		//return bar;
		return new JMenu[]{menu_agent, menu_table, menu_graph};
	}

	/**
	 * @return a JMenu for history limit slider
	 */
	JMenuItem getAgentFilterMenu(final String title, final AgentFilterModel im)
	{
		final JMenuItem nlimit = new JMenuItem(title);
		nlimit.setAction(new AbstractAction(title)
		{
			public boolean isEnabled()
			{
				return im.getNode()!=null;
			}

			public void actionPerformed(ActionEvent e)
			{
				if(im.getNode()!=null)
				{
					final JFrame	w = new JFrame(title);
					// w.setUndecorated(true);

					w.addWindowFocusListener(new WindowFocusListener()
					{

						public void windowGainedFocus(WindowEvent e)
						{ /* NOP */
						}

						public void windowLostFocus(WindowEvent e)
						{
							w.setVisible(false);
						}
					});
					
					JPanel	content	= new JPanel(new BorderLayout());
					afp	= new AgentFilterPanel(im.getNode());
					content.add(afp);
					content.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), " Tracing Settings "));
					w.getContentPane().add(content);
					w.pack();
					GuiProperties.setupHelp(w, "tracer.menu_agent");
					w.setLocationRelativeTo(TracerUI.this);
					w.setVisible(true);
				}
			}
		});

		return nlimit;
	}

	abstract class AgentFilterCB extends JCheckBox
	{
		AgentFilterCB(String text)
		{
			super(text);

			setAction(new AbstractAction(text)
			{

				public void actionPerformed(ActionEvent e)
				{
					set(!get());
				}
			});

			setModel(new DefaultButtonModel()
			{
				/**
				 * @return the specific value
				 * @see javax.swing.JCheckBoxMenuItem#getState()
				 */
				public boolean isSelected()
				{
					return get();
				}
			});
		}

		abstract void set(boolean flag);

		abstract boolean get();
	}

	interface AgentFilterModel
	{
		/**
		 * @return the tracing of an agent
		 */
		public TAgent getNode();
	}


	/**
	 * @param tn is a trace node to be shown in the gui
	 */
	public void addNode(TNode tn)
	{
		if(tn.getCauses().equals(TNode.TOP))
		{
			tn.addParent(root);
		}
		else
		{
			table.addTrace(tn);
		}
		graph.showTrace(tn);
	}

	/**
	 *  Add an edge
	 */
	// Hack!!! needed for reconnecting orphaned nodes due to node limit.
	public void addEdge(TNode from, TNode to)
	{
		graph.tgPanel.addEdge(new TEdge(from, to));
	}

	/**
	 * Removes the node from root children
	 * @param parent
	 * @param node
	 * @param oldIndex
	 */
	public void movedNode(TAgent parent, TNode node, int oldIndex)
	{
		// System.out.println("Stub: TracerUI.movedNode");
		if(oldIndex>= 0)
		{
			tree.model.nodesWereRemoved(parent, new int[]{oldIndex}, new Object[]{node});
		}
	}

	/**
	 * Removes the agent node from the tree
	 * @param agent
	 */
	public void removeNode(TAgent agent)
	{
		// System.out.println("Stub: TracerUI.removeNode");
		hideNode(agent);
		agent.removeParent(root);
		tree.model.nodeStructureChanged(root);
	}

	/**
	 * @param node
	 */
	public void hideNode(TNode node)
	{
		// System.out.println("Stub: TracerUI.removeNode");
		if(table.remove(node))
		{
			table_model.fireTableDataChanged();
		}
		graph.showTrace(node, false);
		graph.showEffects(node, false);
	}

	// ----------------------------------------------------------------------------------

	/**
	 * @return the agent last selected in this gui
	 */
	protected TAgent getSelectedAgent()
	{
		if(selectedAgent==null)
		{
			if(graph!=null)
			{
				TNode n = graph.getSelected();
				if(n instanceof TAgent) return selectedAgent = (TAgent)n;
			}
		}
		return selectedAgent;
	}

	/**
	 * @param e
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent e)
	{
		// System.out.println("Stub: TracerUI.valueChanged");

		TNode node = (TNode)tree.getLastSelectedPathComponent();
		if(node instanceof TAgent)
		{
			selectedAgent = (TAgent)node;
		}
		else
		{
			selectedAgent = null;
		}

	}

	/**
	 * <code>MENU_ACTIVATOR</code>: activates deactivetes actions in a menu
	 */
	protected static final MenuListener MENU_ACTIVATOR = new MenuListener()
	{
		/**
		 * no op
		 * @param e
		 * @see javax.swing.event.MenuListener#menuCanceled(javax.swing.event.MenuEvent)
		 */
		public void menuCanceled(MenuEvent e)
		{
			// System.out.println("Stub:
			// TracerUI.menuCanceled");

		}

		/**
		 * no op
		 * @param e
		 * @see javax.swing.event.MenuListener#menuDeselected(javax.swing.event.MenuEvent)
		 */
		public void menuDeselected(MenuEvent e)
		{
			// System.out.println("Stub:
			// TracerUI.menuDeselected");

		}

		/**
		 * enables
		 * disable
		 * components
		 * @param e
		 * @see javax.swing.event.MenuListener#menuSelected(javax.swing.event.MenuEvent)
		 */
		public void menuSelected(MenuEvent e)
		{
			// System.out.println("Stub:
			// TraceTable.showPopUp");
			Component[] me = ((JMenu)e
					.getSource())
					.getPopupMenu()
					.getComponents();
			int i = me.length;
			while(i-->0)
			{
				if(me[i] instanceof JMenuItem)
				{
					JMenuItem mi = (JMenuItem)me[i];
					if(mi.getAction()!=null)
					{
						mi
								.setEnabled(mi
								.getAction()
								.isEnabled());
					}
				}
			}
		}
	};

	/**
	 * <code>MENU_ACTIVATOR</code>: activates deactivetes actions in a menu
	 */
	protected static final PopupMenuListener PMENU_ACTIVATOR = new PopupMenuListener()
	{

		public void popupMenuWillBecomeVisible(PopupMenuEvent e)
		{
			// System.out.println("Stub:
			// TraceTable.showPopUp");
			Component[] me = ((JPopupMenu)e
					.getSource())
					.getComponents();
			int i = me.length;
			while(i-->0)
			{
				if(me[i] instanceof JMenuItem)
				{
					JMenuItem mi = (JMenuItem)me[i];
					if(mi.getAction()!=null)
					{
						mi
								.setEnabled(mi
								.getAction()
								.isEnabled());
					}
				}
			}
		}

		public void popupMenuCanceled(PopupMenuEvent e)
		{
			// System.out.println("Stub:
			// .popupMenuCanceled");

		}

		public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
		{
			// System.out.println("Stub:
			// .popupMenuWillBecomeInvisible");
		}
	};

	protected JSplitPane	tableSplit;

	/**
	 * @param ctrl
	 * @param title
	 * @return a new tracer ui created on the event thread
	 * /
	public static TracerUI createFrame(final TracerController ctrl, final String title)
	{
		final TracerUI ui = new TracerUI(ctrl);
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JFrame frame = new JFrame(title);
				frame.setDefaultLookAndFeelDecorated(false);
				TracerUI ui = new TracerUI(ctrl);
				frame.setContentPane(ui);

				frame.addWindowListener(new WindowAdapter()
				{
					public void windowClosing(WindowEvent e)
					{
						ctrl.uiClosed();
					}
				});

				frame.setJMenuBar(ui.createMenuBar());

				frame.pack();

				try
				{
					frame.setIconImage(LookAndFeel.TRACER_ICON.getImage());
				}
				catch(Exception e)
				{ // some weird sun exception
				}

				frame.setSize(800, 600);
				frame.setVisible(true);
			}
		});

		return ui;
	}*/
	
	/** 
	 * 
	 * /
	public void dispose()
	{
		Container p = getParent();
		if(p instanceof JFrame)
		{
			((JFrame)p).dispose();
		}
	}*/

	/**
	 * @param ps
	 */
	public void getProperties(Properties ps)
	{
		ps.setProperty("trace.table_autoscroll", ""+table.isAutoScroll());
		ps.setProperty("split.divider", ""+getDividerLocation());
		ps.setProperty("tablesplit.divider", ""+tableSplit.getDividerLocation());
	}

	/**
	 * @param ps
	 */
	public void setProperties(Properties ps)
	{
		table.setAutoScroll("true".equalsIgnoreCase(ps.getProperty("trace.table_autoscroll",
				"false")));

		if(autoscroll!=null)
			autoscroll.setSelected(table.isAutoScroll());
		if(ignoreAtFirst!=null)
			ignoreAtFirst.setSelected(ctrl.getPrototype().isIgnored());
		
		setDividerLocation(Integer.parseInt(ps.getProperty("split.divider", "200")));
		tableSplit.setDividerLocation(Integer.parseInt(ps.getProperty("tablesplit.divider", "300")));

		afp.reset();
	}

	/**
	 * @return the frame this component is located in
	 */
	public Frame getMainFrame()
	{
		Container c = getParent();
		while(!(c instanceof Frame))
		{
			c = c.getParent();
		}

		return (Frame)c;
	}
	
	/**
	 *  Reset the agent filter panel back to the state in the model.
	 */
	public void	resetDefaultFilter()
	{
		afp.reset();
	}
}
