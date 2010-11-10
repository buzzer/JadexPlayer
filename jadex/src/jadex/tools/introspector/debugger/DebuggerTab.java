package jadex.tools.introspector.debugger;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import jadex.model.ISystemEventTypes;
import jadex.runtime.SystemEvent;
import jadex.tools.common.GuiProperties;
import jadex.tools.introspector.*;
import jadex.tools.ontology.ExecuteCommand;
import jadex.util.SGUI;
import jadex.util.SUtil;

/**
 *  The debugger panel for (remote) debugging.
 */
public class DebuggerTab	extends ToolTab
{
	//-------- attributes --------

	/* The gui listener. */
	protected GuiListener gl;

	/** The agenda mode. */
	protected JComboBox agendachoice;

	/** The agenda state. */
	protected JTextField agendastateview;

	/** The agenda step request. */
	protected JButton agendastep;

	/** The actual agenda step count. */
	protected JComboBox agendasteps;

	/** The actual agenda step count of the agent. */
	protected JTextField actagendasteps;

	/** The tree. */
	protected JTree tree;

	/** The tree panel. */
	protected JScrollPane treepanel;

	/** The content. */
	protected JSplitPane content;

	/** The classic view. */
	protected ClassicDebuggerPanel cdp;

	//-------- constructors --------

	/**
	 *  Create a new debugger tab.
	 */
	public DebuggerTab(ToolPanel tool)
	{
		super(tool, "Debugger", GuiProperties.getElementIcon("bug_icon"),
			new String[]{ISystemEventTypes.EVENT_TYPE_STEPPABLE});
		createGUI();
		createListener();
		
		/*this.cdp = new ClassicDebuggerPanel();
		JFrame f = new JFrame();
		f.getContentPane().add("Center", cdp);
		f.pack();
		f.setVisible(true);*/
	}

	//-------- methods --------

	/**
	 *  Handle a change occured event.
	 *  @param ces The change occured event.
	 */
	public void systemEventsOccurred(final SystemEvent[] ces)
	{
//		System.out.println("Introspector event: "+SUtil.arrayToString(ces));
		
//		boolean handled	= false;

		for(int e=0; e<ces.length; e++)
		{
			// Handle info messages from the target.
			SystemEvent	ce	= ces[e];

			String what = ce.getType();

			if(ISystemEventTypes.AGENDA_CHANGED.equals(what)
				|| ISystemEventTypes.AGENDA_STEP_DONE.equals(what) || ISystemEventTypes.EVENT_TYPE_STEPPABLE.equals(what))
			{
				//System.out.println("ce: "+what+" "+((Map)ce.getSource()));
				((DefaultTreeModel)tree.getModel()).setRoot(buildTreeStructure((Map)ce.getSource()));
				for(int i=0; i<tree.getRowCount(); i++)
					tree.expandRow(i);
				tree.scrollRowToVisible(tree.getRowCount()-1);
				treepanel.getVerticalScrollBar().setValue(treepanel.getVerticalScrollBar().getMaximum());
//				handled	= true;
			}

			if(ISystemEventTypes.AGENDA_STEP_DONE.equals(what) || ISystemEventTypes.EVENT_TYPE_STEPPABLE.equals(what))
			{
				//System.out.println("Step done.");
				agendastateview.setText("step finished");
				Map	source	= (Map)ce.getSource();
				actagendasteps.setText(""+source.get("steps"));
//				handled	= true;
			}

			if(ISystemEventTypes.AGENDA_MODE_CHANGED.equals(what) || ISystemEventTypes.EVENT_TYPE_STEPPABLE.equals(what))
			{
				Map	source	= (Map)ce.getSource();
				String mode = (String)source.get("mode");
				//System.out.println("Mode changed: "+mode);

				agendachoice.removeActionListener(gl);
				agendachoice.setSelectedItem(mode);
				if(!SUtil.arrayToList(agendachoice.getActionListeners()).contains(gl)
					|| ISystemEventTypes.EVENT_TYPE_STEPPABLE.equals(what))
						agendachoice.addActionListener(gl);

				actagendasteps.setText(""+source.get("steps"));
				
				if("step".equals(mode) || "cycle".equals(mode))
				{
					agendastep.setEnabled(true);
					agendasteps.setEnabled(true);
					actagendasteps.setEnabled(true);
				}
				else
				{
					agendastep.setEnabled(false);
					agendasteps.setEnabled(false);
					actagendasteps.setEnabled(false);
				}
//				handled	= true;
			}

			if(ISystemEventTypes.AGENDA_STEPS_CHANGED.equals(what) || ISystemEventTypes.EVENT_TYPE_STEPPABLE.equals(what))
			{
				Map	source	= (Map)ce.getSource();
				actagendasteps.setText(""+source.get("steps"));
//				handled	= true;
			}
		}
		
//		if(handled)
//		{
//			System.out.println("handled introspector events");
//		}
//		else
//		{
//			System.out.println("unhandled introspector events");
//		}

		//System.out.println("Introspector event end: "+ces);
		//cdp.updateGui(traverseDepthFirst((DefaultMutableTreeNode)tree.getModel().getRoot(), new ArrayList()));
	}

	/**
	 *
	 */
	protected List traverseDepthFirst(DefaultMutableTreeNode root, List list)
	{
		list.add(root.getUserObject());
		for(int i=0; i<root.getChildCount(); i++)
		{
			traverseDepthFirst((DefaultMutableTreeNode)root.getChildAt(i), list);
		}
		return list;
	}

	/**
	 *  Clear the view when refreshing.
	 *  To be overriden to perform custom cleanup.
	 */
	protected void	clear()
	{
		// ?
	}

	//-------- helper methods --------

	/**
	 *  Create the gui elements.
	 */
	protected void createGUI()
	{
		Insets insets = new Insets(2, 6, 4, 4);

		// Agenda
		this.agendastateview = new JTextField("running");
		agendastateview.setEditable(false);
		this.agendachoice = new JComboBox(new String[]{"normal", "cycle", "step"});
		this.agendastep = new JButton();
		this.agendastep.setIcon(GuiProperties.getElementIcon("right"));
		this.agendastep.setToolTipText("Execute a specified number of agenda actions");
		this.agendasteps = new JComboBox(new String[]{"1","2","3","4","5","10","100"});
		this.agendasteps.setEditable(true);
		this.actagendasteps = new JTextField(3);
		this.actagendasteps.setEditable(false);
		JPanel agenda = new JPanel(new GridBagLayout());
		Border agendabord = new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Agenda Control");
		agenda.setBorder(agendabord);
		agenda.add(new JLabel("Execution mode"), new GridBagConstraints(0, 0, 1, 1, 0, 0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		agenda.add(agendachoice, new GridBagConstraints(1, 0, 4, 1, 0, 0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		agenda.add(new JLabel("Execute <n> agenda actions"), new GridBagConstraints(0, 1, 1, 1, 0, 0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		agenda.add(agendasteps, new GridBagConstraints(1, 1, 1, 1, 0, 0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		agenda.add(agendastep, new GridBagConstraints(2, 1, 1, 1, 0, 0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 10, 0));
		agenda.add(new JLabel("Open steps"), new GridBagConstraints(3, 1, 1, 1, 0, 0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		agenda.add(actagendasteps, new GridBagConstraints(4, 1, 1, 1, 0, 0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		agenda.add(new JLabel("Processing state"), new GridBagConstraints(0, 2, 1, 1, 0, 0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		agenda.add(agendastateview, new GridBagConstraints(1, 2, 4, 1, 0, 0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		agenda.add(new JPanel(), new GridBagConstraints(0, 3, 5, 1, 1, 1,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,  new Insets(0,0,0,0), 0, 0));

		DefaultMutableTreeNode root = buildTreeStructure(new HashMap());
		this.tree = new JTree(root);
		for(int i=0; i<tree.getRowCount(); i++)
			tree.expandRow(i);
		this.tree.getRowCount();
		this.tree.setRootVisible(false);
		this.tree.setCellRenderer(new IntrospectorTreeCellRenderer());
		this.tree.addTreeSelectionListener(new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent e)
			{
				//System.out.println("hallo: "+e+" ");
				TreePath tp = e.getNewLeadSelectionPath();
				if(tp!=null)
				{
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)tp.getLastPathComponent();
					if(node!=null)
					{
						AgendaEntryInfo ei = (AgendaEntryInfo)node.getUserObject();
						getToolPanel().showElementDetails(ei.getEntryMap());
					}
				}
			}
		});

		this.treepanel = new JScrollPane(tree);

		// Set minimum and preferred sizes (Hack?)
		agendastep.setMinimumSize(new Dimension(30, (int)agendastep.getMinimumSize().getHeight()));
		agendastep.setPreferredSize(new Dimension(30, (int)agendastep.getPreferredSize().getHeight()));
		agendasteps.setMinimumSize(new Dimension(60, (int)agendasteps.getMinimumSize().getHeight()));
		agendasteps.setPreferredSize(new Dimension(60, (int)agendasteps.getPreferredSize().getHeight()));
		actagendasteps.setMinimumSize(actagendasteps.getPreferredSize());
		treepanel.setMinimumSize(new Dimension(130, (int)treepanel.getMinimumSize().getHeight()));
		treepanel.setPreferredSize(new Dimension(200, (int)treepanel.getPreferredSize().getHeight()));

		/*JPanel agendapanel = new JPanel(new BorderLayout());
		agendapanel.add("Center", treepanel);
		agendapanel.add("South", agenda);*/

		// Construct the complete view
		JPanel top = new JPanel(new GridBagLayout());
		top.add(treepanel, new GridBagConstraints(0, 0, 1, 1, 1, 1,
			GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0));
		top.add(agenda, new GridBagConstraints(0, 1, 1, 1, 1, 0,
			GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0));

		/*JSplitPane right = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		right.add(elp);
		right.setOneTouchExpandable(true);

		JPanel r1 = new JPanel(new GridBagLayout());
		r1.add(selep, new GridBagConstraints(0, 0, 1, 1, 1, 0,
			GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		r1.add(alsp, new GridBagConstraints(0, 1, 1, 1, 1, 1,
			GridBagConstraints.NORTH, GridBagConstraints.BOTH, insets, 0, 0));
		r1.add(slsp, new GridBagConstraints(0, 2, 1, 1, 1, 1,
			GridBagConstraints.NORTH, GridBagConstraints.BOTH, insets, 0, 0));
		right.add(r1);
		top.add(right, new GridBagConstraints(1, 0, 1, 1, 1, 1,
			GridBagConstraints.NORTH, GridBagConstraints.BOTH, insets, 0, 0));

		rlp.setMinimumSize(new Dimension(200, (int)agenda.getMinimumSize().getHeight()));
		rlp.setPreferredSize(new Dimension(200, (int)agenda.getPreferredSize().getHeight()));
		JPanel middle = new JPanel(new GridBagLayout());
		middle.add(agenda, new GridBagConstraints(0, 0, 1, 1, 0, 1,
			GridBagConstraints.NORTH, GridBagConstraints.BOTH, insets, 0, 0));
		middle.add(rlp, new GridBagConstraints(1, 0, 1, 1, 1, 1,
			GridBagConstraints.NORTH, GridBagConstraints.BOTH, insets, 0, 0));*/

		/*this.content = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		this.content.setOneTouchExpandable(true);
		this.content.setResizeWeight(0.5);
		this.content.add(top);
		this.content.add(new JPanel());*/

		this.setLayout(new BorderLayout());
		this.add("Center", top);
		this.add(BorderLayout.NORTH, SGUI.createToolBar("Debugger Options", getActions()));
	}

	// Hack!!! Better way to control state of buttons?
	boolean agendastepenabled;
	boolean agendastepsenabled;
	boolean actagendastepsenabled;
	
	/**
	 *  (De-)Activate agent observation.
	 */
	public void	setActive(boolean active)
	{
		super.setActive(active);

		Color	background	= active ? new UIManager().getColor("List.background") : new UIManager().getColor("Panel.background"); 
		treepanel.getViewport().setBackground(background);
		tree.setBackground(background);
		tree.setEnabled(active);
		
		agendachoice.setEnabled(active);
		
		// Restore active states
		if(active)
		{
			agendastep.setEnabled(agendastepenabled);
			agendasteps.setEnabled(agendastepsenabled);
			actagendasteps.setEnabled(actagendastepsenabled);
		}
		
		// Disable and store state.
		else
		{
			agendastepenabled	= agendastep.isEnabled();
			agendastepsenabled	= agendasteps.isEnabled();
			actagendastepsenabled	= actagendasteps.isEnabled();
			agendastep.setEnabled(false);
			agendasteps.setEnabled(false);
			actagendasteps.setEnabled(false);
		}
	}

	/**
	 *  Create local / remote listeners.
	 */
	protected void createListener()
	{
		this.gl = new GuiListener();
		agendachoice.addActionListener(gl);
		agendastep.addActionListener(gl);

	}

	//-------- inner classes --------

	/**
	 *  The listener.
	 */
	protected class GuiListener extends MouseAdapter implements ActionListener
	{
		int i=0;
		/**
		 *  Called when a gui action occurred.
		 */
		public void actionPerformed(ActionEvent ae)
		{
			Object source = ae.getSource();

			// Scheduler
			if(source==agendachoice)
			{
				String state = (String)agendachoice.getSelectedItem();
				if(state.equals("normal"))
					agendastateview.setText("running");
				else if(state.equals("step") || state.equals("cycle"))
					agendastateview.setText("step finished");
				String cmd = "setAgendaExecutionMode "+state;
				ExecuteCommand com = new ExecuteCommand();
				com.setCommand(cmd);
				tool.performToolAction(com);
			}
			else if(source==agendastep)
			{
				//System.out.println("Requesting agenda step: "+agendachoice.getSelectedItem());
				agendastateview.setText("step requested");
				int steps = -1;
				try{steps = Integer.parseInt((String)agendasteps.getSelectedItem());}
				catch(NumberFormatException e){}
				if(steps>-1)
				{
					String cmd = "setAgendaSteps "+steps;
					ExecuteCommand com = new ExecuteCommand();
					com.setCommand(cmd);
					tool.performToolAction(com);
				}
			}
			//System.out.println("ready: "+i++);
		}
	}

    //protected int num;
	/**
	 *  Create the tree.
	 */
	protected DefaultMutableTreeNode buildTreeStructure(Map agenda)
	{
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");

        //System.out.println("ROOT: ");
		//num = 0;
        for(int i=0; agenda!=null && agenda.get(""+i)!=null; i++)
		{
			root.add(buildTreeStructureForEntry(new AgendaEntryInfo((Map)agenda.get(""+i))));
		    //num++;
        }
        Integer anum = (Integer)agenda.get("num");
        //if(anum!=null && num!=anum.intValue())
        //    System.out.println("ERROR: "+agenda);
		//System.out.println("entries: "+num);
		return root;
	}

	/**
	 *  Build the tree structure for an entry.
	 *  @param entryinfo The entryinfo.
	 *  @return The node.
	 */
	protected DefaultMutableTreeNode buildTreeStructureForEntry(AgendaEntryInfo entryinfo)
	{
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(entryinfo);

		List children = entryinfo.getChildren();
		for(int i=0; i<children.size(); i++)
		{
			DefaultMutableTreeNode tmp = buildTreeStructureForEntry((AgendaEntryInfo)children.get(i));
			node.add(tmp);
            //num++;
		}
		return node;
	}

	/**
	 *  A custom tree cell renderer for changing icons in the tree.
	 */
	class IntrospectorTreeCellRenderer	extends DefaultTreeCellRenderer
	{
		//-------- overridings --------

		/**
		 *  Configures the renderer based on the passed in components.
		 */
		public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			// Change icons depending on entry type.
			DefaultMutableTreeNode	node	= (DefaultMutableTreeNode)value;
			boolean	processed	= false;
			if(node.getUserObject() instanceof AgendaEntryInfo)
			{
				AgendaEntryInfo entry = (AgendaEntryInfo)node.getUserObject();

				// Should support different icons for open/closed/leaf???
				/*Icon icon = GuiProperties.getElementIcon(entry.getActionClassName());
				if(icon!=null)
				{
					setOpenIcon(icon);
					setClosedIcon(icon);
					setLeafIcon(icon);
				}
				else
				{
					setOpenIcon(getDefaultOpenIcon());
					setClosedIcon(getDefaultClosedIcon());
					setLeafIcon(getDefaultLeafIcon());
				}*/
				processed	= entry.isProcessed();
			}
			else
			{
				setOpenIcon(getDefaultOpenIcon());
				setClosedIcon(getDefaultClosedIcon());
				setLeafIcon(getDefaultLeafIcon());
			}

			JComponent	comp	= (JComponent)super.getTreeCellRendererComponent(
				tree, value, sel, expanded, leaf, row, hasFocus);
			//comp.setEnabled(!processed);
			if(processed)
				comp.setForeground(Color.lightGray);
			return comp;
		}
	}
}

/**
 *  The wrapper info object for an agenda entry.
 */
class AgendaEntryInfo
{
	/** The map. */
	protected Map entryinfo;

	/** Pure info without chilren. */
	protected Map cleaninfo;

	/** The children. */
	protected List children;

	/**
	 *  Create a new agenda info.
	 */
	public AgendaEntryInfo(Map entryinfo)
	{
		this.entryinfo = entryinfo;
		this.cleaninfo = new HashMap();
		this.cleaninfo.putAll(entryinfo);
		this.children = new ArrayList();
		for(int i=0; entryinfo.get(""+i)!=null; i++)
		{
            //if(entryinfo.get(""+i) instanceof String)
            //    System.out.println("???");
			children.add(new AgendaEntryInfo((Map)entryinfo.get(""+i)));
			cleaninfo.remove(""+i); // little hack
		}
		cleaninfo.remove("text");
		cleaninfo.remove("hashcode");
        //System.out.println("AgendaEntry: "+getActionClassName());
	}

	/**
	 *  Test if this entry is processed.
	 *  @return True, if processed.
	 */
	public boolean isProcessed()
	{
		return new Boolean((String)entryinfo.get("processed")).booleanValue();
	}

	/**
	 *  Test if this entry was executed.
	 *  @return True, if was executed.
	 */
	public boolean isExecuted()
	{
		return new Boolean((String)entryinfo.get("executed")).booleanValue();
	}

	/**
	 *  Test if this entry is valid.
	 *  @return True, if valid.
	 */
	public boolean isValid()
	{
		// An entry is displayed as valid when it was already executed
		// and therefore valid at the execution time or when it has to
		// be executed and its action currently is valid.
		return isExecuted() || new Boolean((String)entryinfo.get("valid")).booleanValue();
	}

	/**
	 *  Get the action class.
	 */
	public String getActionClassName()
	{
		return ""+entryinfo.get("actionclass");
	}

	/**
	 *  Get the direct children.
	 */
	public List getChildren()
	{
		return Collections.unmodifiableList(children);
	}

	/**
	 *  Get the hashcode of the original object.
	 */
	public String getHashcode()
	{
		return (String)entryinfo.get("hashcode");
	}

	/**
	 *  Get the entry as map.
	 */
	public Map getEntryMap()
	{
		return cleaninfo;
	}

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public  String toString()
	{
		String ret = (String)entryinfo.get("text");
		if(!isValid())
			ret = "(invalid) "+ret;
		return ret;
	}
}

/**
 *
 */
class AgendaActionInfo
{
	/** The map. */
	protected Map entryinfo;

	/**
	 *  Create a new agenda info.
	 */
	public AgendaActionInfo(Map entryinfo)
	{
		this.entryinfo = entryinfo;
	}
}
