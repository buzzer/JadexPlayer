package jadex.tools.introspector.bdiviewer;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import jadex.runtime.SystemEvent;
import jadex.tools.common.GuiProperties;
import jadex.tools.common.IValidator;
import jadex.tools.common.JValidatorTextField;
import jadex.tools.common.ParserValidator;
import jadex.tools.common.jtreetable.*;
import jadex.tools.introspector.*;
import jadex.tools.ontology.*;
import jadex.util.SGUI;
import jadex.util.jtable.*;


/**
 *  A component for showing the contents of a belief/goal/planbase.
 */
public abstract class BasePanel	extends ToolTab
{
	//-------- static part --------

	/** The image icons. */
	protected static UIDefaults	defaults	= new UIDefaults(new Object[]
	{
		// Menu icons.
		"resize",	SGUI.makeIcon(BasePanel.class, "/jadex/tools/common/images/resize.png"),
		"clear",	SGUI.makeIcon(BasePanel.class, "/jadex/tools/common/images/litter2.png"),
		"togglegoalview",	SGUI.makeIcon(BasePanel.class, "/jadex/tools/common/images/toggle_goalview.png"),
		"toggleplanview",	SGUI.makeIcon(BasePanel.class, "/jadex/tools/common/images/toggle_planview.png"),
		"goalview",	SGUI.makeIcon(BasePanel.class, "/jadex/tools/common/images/cloud2.png"),
		"planview",	SGUI.makeIcon(BasePanel.class, "/jadex/tools/common/images/plan2.png"),
		"keepold",	SGUI.makeIcon(BasePanel.class, "/jadex/tools/common/images/keepold.png"),
		"removeold",	SGUI.makeIcon(BasePanel.class, "/jadex/tools/common/images/removeold.png"),
	});

	//-------- constants --------

	/** The table columns for beliefs. */
	protected static final String[]	COLUMNS_BELIEFS	= new String[]
	{
		"name",
		"valueclass",
		"value"
	};

	/** The table column headers for beliefs. */
	protected static final	String[]	NAMES_BELIEFS	= new String[]
	{
		"Name",
		"Class",
		"Value"
	};

	/** The table columns for plan instances. */
	protected static final String[]	COLUMNS_PLANS	= new String[]
	{
		"name",
		"state"
	};

	/** The table column headers for plan instances. */
	protected static final String[]	NAMES_PLANS	= new String[]
	{
		"Name",
		"State"
	};

	/** The table columns for goals. */
	protected static final String[]	COLUMNS_GOALS	= new String[]
	{
		"name",
		"lifecyclestate",
		"processingstate",
		"type"
	};

	/** The table column´headers for goals. */
	protected static final String[]	NAMES_GOALS	= new String[]
	{
		"Name",
		"Lifecycle State",
		"Processing State",
		"Type"
	};

	//-------- initialize node types --------

	/** Helper method to create node type with icon. */
	protected static	TreeTableNodeType	createNodeType(String type, TreeTableNodeType base)
	{
		return new TreeTableNodeType(type, new Icon[]{GuiProperties.getElementIcon(type)}, base);
	}

	/** The runtime capability node type. */
	protected static final TreeTableNodeType	NODE_RCAPABILITY
		= new TreeTableNodeType("RCapability", new Icon[]{GuiProperties.getElementIcon("RCapability")}, new String[]{"name"}, new String[]{"Name"});

	/** The runtime agent node type. */
	protected static final TreeTableNodeType	NODE_RBDIAGENT
		= createNodeType("RBDIAgent", NODE_RCAPABILITY);

	/** The runtime beliefbase node type. */
	protected static final TreeTableNodeType	NODE_RBELIEFBASE
		= new TreeTableNodeType("RBeliefbase", new Icon[]{GuiProperties.getElementIcon("RBeliefbase")}, new String[]{"name"}, new String[]{"Name"});

	/** The runtime goalbase node type. */
	protected static final TreeTableNodeType	NODE_RGOALBASE
		= new TreeTableNodeType("RGoalbase", new Icon[]{GuiProperties.getElementIcon("RGoalbase")}, new String[]{"name"}, new String[]{"Name"});

	/** The runtime planbase node type. */
	protected static final TreeTableNodeType	NODE_RPLANBASE
		= new TreeTableNodeType("RPlanbase", new Icon[]{GuiProperties.getElementIcon("RPlanbase")}, new String[]{"name"}, new String[]{"Name"});

	/** The runtime belief node type. */
	protected static final TreeTableNodeType	NODE_RBELIEF
		= new TreeTableNodeType("RBelief", new Icon[]{GuiProperties.getElementIcon("RBelief")}, COLUMNS_BELIEFS, NAMES_BELIEFS);

	/** The runtime beliefset node type. */
	protected static final TreeTableNodeType	NODE_RBELIEFSET
		= createNodeType("RBeliefSet", NODE_RBELIEF);

	/** The runtime beliefset container node type. */
	protected static final TreeTableNodeType	NODE_RBELIEFSETCONTAINER
		= new TreeTableNodeType("RBeliefSetContainer", new Icon[]{GuiProperties.getElementIcon("RBeliefSetContainer")}, COLUMNS_BELIEFS, NAMES_BELIEFS);

	/** The runtime belief reference node type. */
	protected static final TreeTableNodeType	NODE_RBELIEFREFERENCE
		= createNodeType("RBeliefReference", NODE_RBELIEF);

	/** The runtime beliefset reference node type. */
	protected static final TreeTableNodeType	NODE_RBELIEFSETREFERENCE
		= createNodeType("RBeliefSetReference", NODE_RBELIEFSET);

	/** The runtime beliefset reference container node type. */
	protected static final TreeTableNodeType	NODE_RBELIEFSETREFERENCECONTAINER
		= createNodeType("RBeliefSetReferenceContainer", NODE_RBELIEFSETCONTAINER);

	/** The runtime abstract goal node type. */
	protected static final TreeTableNodeType	NODE_RABSTRACTGOAL
		= new TreeTableNodeType("RAbstractGoal", null, COLUMNS_GOALS, NAMES_GOALS);

	/** The runtime (proprietary) goal node type. */
	protected static final TreeTableNodeType	NODE_RGOAL
		= createNodeType("RGoal", NODE_RABSTRACTGOAL);

	/** The runtime achieve goal node type. */
	protected static final TreeTableNodeType	NODE_RACHIEVEGOAL
		= createNodeType("RAchieveGoal", NODE_RGOAL);

	/** The runtime maintain goal node type. */
	protected static final TreeTableNodeType	NODE_RMAINTAINGOAL
		= createNodeType("RMaintainGoal", NODE_RGOAL);

	/** The runtime achieve goal node type. */
	protected static final TreeTableNodeType	NODE_RPERFORMGOAL
		= createNodeType("RPerformGoal", NODE_RGOAL);

	/** The runtime query goal node type. */
	protected static final TreeTableNodeType	NODE_RQUERYGOAL
		= createNodeType("RQueryGoal", NODE_RGOAL);

	/** The runtime meta goal node type. */
	protected static final TreeTableNodeType	NODE_RMETAGOAL
		= createNodeType("RMetaGoal", NODE_RGOAL);

	/** The runtime achieve goal reference node type. */
	protected static final TreeTableNodeType	NODE_RACHIEVEGOALREFERENCE
		= createNodeType("RAchieveGoalReference", NODE_RACHIEVEGOAL);

	/** The runtime maintain goal reference node type. */
	protected static final TreeTableNodeType	NODE_RMAINTAINGOALREFERENCE
		= createNodeType("RMaintainGoalReference", NODE_RMAINTAINGOAL);

	/** The runtime achieve goal reference node type. */
	protected static final TreeTableNodeType	NODE_RPERFORMGOALREFERENCE
		= createNodeType("RPerformGoalReference", NODE_RPERFORMGOAL);

	/** The runtime query goal reference node type. */
	protected static final TreeTableNodeType	NODE_RQUERYGOALREFERENCE
		= createNodeType("RQueryGoalReference", NODE_RQUERYGOAL);
	
	/** The runtime query goal reference node type. */
	protected static final TreeTableNodeType	NODE_RMETAGOALREFERENCE
		= createNodeType("RMetaGoalReference", NODE_RMETAGOAL);
	
	/** The runtime process goal node type. */
	protected static final TreeTableNodeType	NODE_RPROCESSGOAL
		= createNodeType("RProcessGoal", NODE_RABSTRACTGOAL);

	/** The runtime plan node type. */
	protected static final TreeTableNodeType	NODE_RPLAN
		= new TreeTableNodeType("RPlan", new Icon[]{GuiProperties.getElementIcon("RPlan")}, COLUMNS_PLANS, NAMES_PLANS);

	/** The node type lookup table (name-to-type mapping). */
	protected static Map	nodetypes;

	static
	{
		// Build node types lookup table.
		nodetypes	= new HashMap();
		nodetypes.put(NODE_RCAPABILITY.getName(), NODE_RCAPABILITY);
		nodetypes.put(NODE_RBDIAGENT.getName(), NODE_RBDIAGENT);
		nodetypes.put(NODE_RBELIEFBASE.getName(), NODE_RBELIEFBASE);
		nodetypes.put(NODE_RGOALBASE.getName(), NODE_RGOALBASE);
		nodetypes.put(NODE_RPLANBASE.getName(), NODE_RPLANBASE);
		nodetypes.put(NODE_RBELIEF.getName(), NODE_RBELIEF);
		nodetypes.put(NODE_RBELIEFSET.getName(), NODE_RBELIEFSET);
		nodetypes.put(NODE_RBELIEFSETCONTAINER.getName(), NODE_RBELIEFSETCONTAINER);
		nodetypes.put(NODE_RBELIEFREFERENCE.getName(), NODE_RBELIEFREFERENCE);
		nodetypes.put(NODE_RBELIEFSETREFERENCE.getName(), NODE_RBELIEFSETREFERENCE);
		nodetypes.put(NODE_RBELIEFSETREFERENCECONTAINER.getName(), NODE_RBELIEFSETREFERENCECONTAINER);
		nodetypes.put(NODE_RABSTRACTGOAL.getName(), NODE_RABSTRACTGOAL);
		nodetypes.put(NODE_RGOAL.getName(), NODE_RGOAL);
		nodetypes.put(NODE_RACHIEVEGOAL.getName(), NODE_RACHIEVEGOAL);
		nodetypes.put(NODE_RMAINTAINGOAL.getName(), NODE_RMAINTAINGOAL);
		nodetypes.put(NODE_RPERFORMGOAL.getName(), NODE_RPERFORMGOAL);
		nodetypes.put(NODE_RQUERYGOAL.getName(), NODE_RQUERYGOAL);
		nodetypes.put(NODE_RMETAGOAL.getName(), NODE_RMETAGOAL);
		nodetypes.put(NODE_RACHIEVEGOALREFERENCE.getName(), NODE_RACHIEVEGOALREFERENCE);
		nodetypes.put(NODE_RMAINTAINGOALREFERENCE.getName(), NODE_RMAINTAINGOALREFERENCE);
		nodetypes.put(NODE_RPERFORMGOALREFERENCE.getName(), NODE_RPERFORMGOALREFERENCE);
		nodetypes.put(NODE_RQUERYGOALREFERENCE.getName(), NODE_RQUERYGOALREFERENCE);
		nodetypes.put(NODE_RMETAGOALREFERENCE.getName(), NODE_RMETAGOALREFERENCE);
		nodetypes.put(NODE_RPROCESSGOAL.getName(), NODE_RPROCESSGOAL);
		nodetypes.put(NODE_RPLAN.getName(), NODE_RPLAN);

		// Set excluded columns for some node types.
		NODE_RBELIEFSETCONTAINER.addExclude("value");
		NODE_RBELIEFSET.addExclude("valueclass");
/*		NODE_RBELIEFSET.addExclude("visibility");
		NODE_RBELIEFSET.addExclude("updaterate");
		NODE_RBELIEFSET.addExclude("updateondemand");
		NODE_RBELIEFSET.addExclude("reference");
*/
		// Set editable columns for some node types.
		NODE_RBELIEF.setEditable("value");
		NODE_RBELIEF.setValidator("value", new ParserValidator());

		// Set the popup actions.
		NODE_RBELIEF.addPopupAction(createPopupAction("update", "Update Fact Value"));
		NODE_RBELIEF.addPopupAction(createPopupAction("delete", "Delete Belief"));
		NODE_RBELIEFSET.addPopupAction(createPopupAction("removeFact", "Remove Fact"));
//		NODE_RBELIEFSETCONTAINER.addPopupAction(createPopupAction("addFact", "Add Fact"));
		NODE_RBELIEFSETCONTAINER.addPopupAction(createPopupAction("removeFacts", "Remove All Facts"));
		NODE_RBELIEFSETCONTAINER.addPopupAction(createPopupAction("delete", "Delete Beliefset"));

		NODE_RPLAN.addPopupAction(createPopupAction("terminate", "Terminate Plan"));
		NODE_RABSTRACTGOAL.addPopupAction(createPopupAction("drop", "Drop Goal"));
		NODE_RGOAL.addPopupAction(createPopupAction("suspend", "Suspend Goal"));
		NODE_RGOAL.addPopupAction(createPopupAction("option", "Make Option"));
		NODE_RGOAL.addPopupAction(createPopupAction("activate", "Activate Goal"));
	}

	//-------- attributes --------

	/** The tree table model. */
	protected TreeTableModel	model;

	/** The tree table. */
	protected BDITreeTable	table;

	/** The scroll pane of the table. */
	protected JScrollPane	scroll;

	/** The removed nodes with their former parents. */
	protected Map	oldnodes;

	/** The flag indicating old nodes should be displayed. */
	protected boolean	keepold;

	/** The maximum number of old nodes, that is remembered. */
	protected int	oldmax;
	
	//-------- constructors --------

	/**
	 *  Create a new base panel.
	 *  @param tool	The tool panel (for communication with the observed agent).
	 *  @param name	The name of the tool component.
	 *  @param icon	The icon of the tool component.
	 *  @param events	The system event types to register for.
	 */
	public BasePanel(ToolPanel tool, String name, Icon icon, String[] events)
	{
		super(tool, name, icon, events);
		this.keepold	= false;
		this.oldmax	= 20;

		// Create cache for old nodes, which automatically
		// removes the oldest node, when a new node is added.
		this.oldnodes	= new LinkedHashMap()
		{
			// Called when entries are added.
			protected boolean	removeEldestEntry(Map.Entry eldest)
			{
				// Check if maximum size is exceeded.
				if(size()>oldmax)
				{
					// When oldnodes are shown, remove node from tree.
					if(keepold)
					{
						DefaultTreeTableNode	node	= (DefaultTreeTableNode)eldest.getKey();
						DefaultTreeTableNode	parent	= (DefaultTreeTableNode)eldest.getValue();
						parent.remove(node);
					}

					// Indicate, that the entry should be removed.
					return true;
				}

				return false;
			}
		};

		// Initialize tool bar.
		this.setLayout(new BorderLayout());
		this.add(BorderLayout.NORTH, SGUI.createToolBar("BDI Viewer Options", getActions()));

		// Initialize tree table.
		this.model	= createModel();
		this.table = new BDITreeTable(model);
		this.table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//this.table.setShowGrid(true);
		this.scroll	= new JScrollPane(this.table);
		this.add(BorderLayout.CENTER, scroll);
		scroll.getViewport().setBackground(new UIManager().getColor("List.background"));
		this.table.setBackground(new UIManager().getColor("List.background"));

		// Initialize visibility of columns and add mouselistener
		VisibilityTableColumnModel columnmodel = new VisibilityTableColumnModel();
		this.table.setColumnModel(columnmodel);
		this.table.createDefaultColumnsFromModel();
		columnmodel.addMouseListener(this.table);
		// Make first column unhideable
		columnmodel.setColumnChangeable(columnmodel.getColumn(0), false);

		// Make headers resizable.
		ResizeableTableHeader header = new ResizeableTableHeader();
		header.setColumnModel(this.table.getColumnModel());
		header.setAutoResizingEnabled(false); //default
		header.setIncludeHeaderWidth(false); //default
		this.table.setTableHeader(header);
		// Set the preffered, minimum and maximum column widths
		header.setAllColumnWidths(145, -1, -1);
		header.setColumnWidths(this.table.getColumnModel().getColumn(0), 200, 100, -1);
	}

	//-------- view update methods --------

	/**
	 *  Update the view, as a change event occured.
	 *  @param events	The change event.
	 */
	public abstract void	systemEventsOccurred(SystemEvent[] events);

	/**
	 *  Update capability hierarchy.
	 *  @param event	The event.
	 */
	protected void	handleCapabilityUpdate(SystemEvent event)
	{
		// Add agent node.
		if(event.instanceOf(SystemEvent.AGENT_BORN))
		{
			DefaultTreeTableNode	cap	= (DefaultTreeTableNode)model.getRoot();

			// Add base node (Hack???).
			addNode(cap, createBaseNode());
		}

		// Add capability to tree.
		else if(event.instanceOf(SystemEvent.CAPABILITY_ADDED))
		{
			Map	source	= (Map)event.getSource();

			// Create sub-capability node.
			DefaultTreeTableNode	owner	= getNode((String)source.get("owner"));
			DefaultTreeTableNode	cap	= createNode(event);
			addNode(owner, cap);

			// Also add base node (Hack???).
			addNode(cap, createBaseNode());
		}

		// Remove capability from tree.
		else if(event.instanceOf(SystemEvent.CAPABILITY_REMOVED))
		{
			Map	source	= (Map)event.getSource();
			if(NODE_RBDIAGENT.equals(getNodeType(event)))
			{
				// Agent died, ignore capa removal...
			}
			else
			{
				// (Sub)capability removed.
				DefaultTreeTableNode	owner	= getNode((String)source.get("owner"));
				removeNode(owner.getChild(source.get("name")));
			}
		}

	}

	/**
	 *  Remove all old nodes from the memory.
	 */
	public void	clearOldNodes()
	{
		// Remove nodes from tree, if necessary.
		if(keepold)
		{
			for(Iterator i=oldnodes.keySet().iterator(); i.hasNext(); )
			{
				DefaultTreeTableNode	node	= (DefaultTreeTableNode)i.next();
				((DefaultTreeTableNode)node.getParent()).remove(node);
			}
		}
		oldnodes.clear();
	}

	/**
	 *  Change the handling of removed nodes.
	 *  @param keepold	Flag to indicate, if removed nodes should be displayed.
	 */
	public void	setKeepNodes(boolean keepold)
	{
		if(keepold!=this.keepold)
		{
			this.keepold	= keepold;

			for(Iterator i=oldnodes.keySet().iterator(); i.hasNext(); )
			{
				DefaultTreeTableNode	node	= (DefaultTreeTableNode)i.next();
				DefaultTreeTableNode	parent	= (DefaultTreeTableNode)oldnodes.get(node);

				if(keepold)
				{
					// Re-add old node.
					// Todo: remember expansion state??
					addNode(parent, node);
				}

				else
				{
					// Remove old node.
					// Todo: remember expansion state??
					parent.remove(node);
				}
			}
		}
	}

	//-------- helper methods --------

	/**
	 *  Create a new node based on an event.
	 *  @param event	The event for extracting node type and values.
	 *  @return The node.
	 */
	protected DefaultTreeTableNode	createNode(SystemEvent event)
	{
		TreeTableNodeType type	= getNodeType(event);
 		return new DefaultTreeTableNode(type, ((Map)event.getSource()).get("name"), (Map)event.getSource());
	}

	/**
	 *  Get a node in the tree table model.
	 *  @param name	The fully qualified name of the node to get.
	 */
	protected DefaultTreeTableNode	getNode(String name)
	{
		return getNode(model, name);
	}

	/**
	 *  Get a node in the tree table model.
	 *  @param model	The tree table model.
	 *  @param name	The fully qualified name of the node to get.
	 */
	protected DefaultTreeTableNode	getNode(TreeTableModel model, String name)
	{
		DefaultTreeTableNode	node	= (DefaultTreeTableNode)model.getRoot();
//System.out.println("getting node "+name+" from "+node);
		// Names of nodes are fully qualified, separated by "."
		StringTokenizer	stok	= new StringTokenizer(name, ".");
		stok.nextToken();	// Skip root node.
		while(stok.hasMoreTokens())
		{
			name	= stok.nextToken();
			node	= node.getChild(name);
//System.out.println("             "+name+" from "+node);
		}
		return node;
	}

	/**
	 *  Add a node to another node assuring node order
	 *  depending on node type and alphabetic order.
	 *  Also expands tree if necessary.
	 *  @param parent	The parent node to add to.
	 *  @param child	The child node to be added.
	 */
	protected void	addNode(DefaultTreeTableNode parent, DefaultTreeTableNode child)
	{
		// Default: append to end.
		int index	= parent.getChildCount();

		// Add everything except capabilities always before capabilities.
		if(!child.instanceOf(NODE_RCAPABILITY))
		{
			while(index>0 && ((DefaultTreeTableNode)parent.getChildAt(index-1))
				.instanceOf(NODE_RCAPABILITY))
			{
				index--;
			}
		}

		// Add current nodes always before old nodes.
		if(!oldnodes.containsKey(child))
		{
			while(index>0 && oldnodes.containsKey(parent.getChildAt(index-1)))
			{
				index--;
			}
		}

		// Apply custom sort orders (e.g. for beliefs / goals).
		index	= sortNode(parent, child, index);

		// Sort nodes of same type alphabetically.
		while(index>0)
		{
			DefaultTreeTableNode	sib	= (DefaultTreeTableNode)parent.getChildAt(index-1);
			if(child.getType()==sib.getType()
				&& oldnodes.containsKey(child)==oldnodes.containsKey(sib)
				&& ((String)child.getValue(0)).compareTo((String)sib.getValue(0))<0)
				index--;
			else
				break;
		}

		// Add node using index.
		parent.insert(child, index);

		// Expand new nodes.
		if(parent.getChildCount()==1)
			this.table.getTree().expandPath(new TreePath(parent.getPath()));

		// As (old) nodes might get added out of order, expand children also.
		if(child.getChildCount()>0)
		{
			List	childnodes	= new LinkedList();
			childnodes.add(child);
			while(childnodes.size()>0)
			{
				child	= (DefaultTreeTableNode)childnodes.remove(0);
				this.table.getTree().expandPath(new TreePath(child.getPath()));
				for(int i=0; i<child.getChildCount(); i++)
				{
					if(child.getChildAt(i).getChildCount()>0)
						childnodes.add(child.getChildAt(i));
				}
			}
		}
	}

	/**
	 *  Remove a node.
	 *  Depending on the keepold flag, the node will still be displayed.
	 *  @param node	The node to remove.
	 */
	protected void	removeNode(DefaultTreeTableNode node)
	{
		DefaultTreeTableNode	parent	= (DefaultTreeTableNode)node.getParent();
		oldnodes.put(node, parent);
		parent.remove(node);

		// Re-add as old node.
		if(keepold && oldnodes.size()>0)
		{
			addNode(parent, node);
		}
	}

	/**
	 *  Get the node type from the event as specified by the type slot.
	 *  @param event	The change event.
	 *  @return	The node type.
	 */
	protected TreeTableNodeType	getNodeType(SystemEvent event)
	{
		Map	source	= (Map)event.getSource();
		TreeTableNodeType	type	=(TreeTableNodeType)nodetypes
			.get(source.get("class"));

		if(type==null)	throw new RuntimeException("No node for type: "
			+ source.get("class")+" "+event);

		return type;
	}
	
	/**
	 *  (De-)Activate agent observation.
	 */
	public void	setActive(boolean active)
	{
		super.setActive(active);

		Color	background	= active ? new UIManager().getColor("List.background") : new UIManager().getColor("Panel.background"); 
		scroll.getViewport().setBackground(background);
		table.setBackground(background);
		table.setEnabled(active);		
	}
	
	/**
	 *  Get the (menu/toolbar) actions of the bdi viewer.
	 */
	public Action[]	getActions()
	{
		if(actions==null)
		{
			Action[]	actions2	= super.getActions();
			actions	= new javax.swing.Action[actions2.length+5];
			System.arraycopy(actions2, 0, actions, 0, actions2.length);

			this.actions[actions2.length+1]	= new AbstractAction("Resize Column Widths to Fit Contents", defaults.getIcon("resize"))
	 		{
				public void actionPerformed(ActionEvent ae)
				{
	                ((ResizeableTableHeader) table.getTableHeader()).resizeAllColumns();
				}
			};
	
			this.actions[actions2.length+3]	= new AbstractAction("Show/Hide Removed Elements", defaults.getIcon("keepold"))
			{
				public void actionPerformed(ActionEvent ae)
				{
					// Toggle icon.
					this.putValue(SMALL_ICON, keepold ? defaults.getIcon("keepold")
						: defaults.getIcon("removeold"));
	
					setKeepNodes(!keepold);
				}
			};
	
			this.actions[actions2.length+4]	= new AbstractAction("Flush Removed Elements", defaults.getIcon("clear"))
			{
				public void actionPerformed(ActionEvent ae)
				{
					clearOldNodes();
				}
			};
		}

		return this.actions;
	}

	/**
	 *  Activate / deactivate the tool tab.
	 */
	public void	setEnabled(boolean enabled)
	{
		Color	bgcolor	= enabled ? new UIManager().getColor("List.background")
			: new UIManager().getColor("Panel.background");
		scroll.getViewport().setBackground(bgcolor);
		table.setBackground(bgcolor);

		super.setEnabled(enabled);
	}
	
	//-------- template methods --------

	/**
	 *  Sort a node relative to its siblings.
	 *  This method can be overriden to add additional sort criteria.
	 *  @param parent	The parent node.
	 *  @param node	The node to insert.
	 *  @param index	The currently intended insertion index.
	 *  @return The index to insert the node into.
	 */
	protected int	sortNode(DefaultTreeTableNode parent,
		DefaultTreeTableNode node, int index)
	{
		// Default: No change.
		return index;
	}

	/**
	 *  Clear the view when refreshing.
	 *  Can be overriden to perform custom cleanup, but
	 *  super.clear() should be called.
	 */
	protected void	clear()
	{
		((DefaultTreeTableNode)model.getRoot()).removeAllChildren();
		oldnodes.clear();
	}

	/**
	 *  Create the tree table model.
	 *  To be implemented by the corresponding subclass (e.g. BeliefbasePanel).
	 *  @return The created model.
	 */
	protected abstract TreeTableModel	createModel();

	/**
	 *  Create a base node.
	 *  To be implemented by the corresponding subclass (e.g. BeliefbasePanel).
	 *  @return The created base node.
	 */
	protected abstract DefaultTreeTableNode	createBaseNode();

	//-------- inner classes --------

	/**
	 *  A custom tree cell renderer for changing icons in the tree.
	 */
	class BDIViewerTreeCellRenderer	extends DefaultTreeCellRenderer
	{
		//-------- overridings --------

		/**
		 *  Configures the renderer based on the passed in components.
		 */
		public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			// Change icons depending on node type.
			DefaultTreeTableNode	node	= (DefaultTreeTableNode)value;
			if(node.getType()!=null)
			{
				// Should support different icons for open/closed/leaf???
				Icon	icon	= node.getType().getIcon(value);
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
				}
			}

			JComponent	comp	= (JComponent)super.getTreeCellRendererComponent(
				tree, value, sel, expanded, leaf, row, hasFocus);

			// Disable nodes for removed elements.
			boolean	enabled	= true;
			while(enabled && node!=null)
			{
				enabled	= !oldnodes.containsKey(node);
				node	= (DefaultTreeTableNode)node.getParent();
			}
			comp.setEnabled(enabled);

			return comp;
		}
	}

	/**
	 *  A tree table with tooltips for the cells.
	 */
	class BDITreeTable	extends JTreeTable
	{
		/**
		 *  Create a bdi tree tabel.
		 */
		public BDITreeTable(TreeTableModel model)
		{
			super(model);
			getTree().setCellRenderer(new BDIViewerTreeCellRenderer());
			new TreeExpansionHandler(getTree());
			this.addMouseListener(new PopupListener());
			getTree().setToggleClickCount(42); // Disable node expansion by double-click.
			this.addMouseListener(new MouseAdapter()
			{
				public void mouseClicked(MouseEvent me)
				{
					if(me.getClickCount()==2)
					{
						int	row	= rowAtPoint(me.getPoint());
						TreePath	path	= getTree().getPathForRow(row);
						if(path!=null)
						{
							DefaultTreeTableNode	node	= (DefaultTreeTableNode)path.getLastPathComponent();
							tool.showElementDetails(node.getValues());
						}
					}
				}
			});
		}

		/**
		 *  @see JTable#getToolTipText
		 */
		public String getToolTipText(MouseEvent event)
		{
			String	tip	= null;
			Point	p	= event.getPoint();
			int	row	= rowAtPoint(p);
			int	col	= columnAtPoint(p);

			if(getRowCount()>row && getColumnCount()>col)
			{
				try
				{
					// compare the component width to the table column width
					TableCellRenderer	renderer	= getCellRenderer(row, col);
					Component	component	= prepareRenderer(renderer, row, col);
					if(component.getPreferredSize().getWidth()
						> getColumnModel().getColumn(col).getWidth())
					{
						tip	= getValueAt(row, col).equals("")
							? null : getValueAt(row, col).toString();
					}
				}
				catch(Exception e)
				{
					// Hack !!! Swing sometimes tries to get the tooltip for a
					// row that is concurrently removed by some other thread!?
				}
			}

			return tip;
		}

		public Dimension	getPreferredScrollableViewportSize()
		{
			Dimension	ret	= super.getPreferredScrollableViewportSize();
			ret.width	= 0;	// Hack ??? What about insets?
			for(int col=0; col<getColumnCount(); col++)
			{
				ret.width	+= getColumnModel().getColumn(col).getPreferredWidth();
			}
			return ret;
		}


		/**
		 *  Returns a table header object, which overrides
		 *  paintComponent to extend the header to the left.
		 * /
		protected JTableHeader createDefaultTableHeader()
		{
			return new JTableHeader(getColumnModel())
			{
				protected void paintComponent(Graphics g)
				{
					super.paintComponent(g);
					Rectangle	r	= getHeaderRect(getColumnModel().getColumnCount()-1);
					Rectangle	b	= getBounds();
					if(r.x+r.width < b.width)
					{
						Component	comp	= getTableHeader().getDefaultRenderer()
							.getTableCellRendererComponent(getTable(), "", false, false, -1, -1);
						comp.setBounds(0, 0, b.width+10-(r.x+r.width), r.height);

						g	= g.create();
						g.setClip(r.x+r.width, 0, b.width-(r.x+r.width), r.height);
						g.translate(r.x+r.width, 0);
						comp.paint(g);
					}
				}
			};
		}*/
		
		/**
		 *  Get a cell editor for a given table cell.
		 */
		public TableCellEditor getCellEditor(int row, int column)
		{
			TableCellEditor	editor;
			
			DefaultTreeTableNode	node	= (DefaultTreeTableNode)getTree()
				.getPathForRow(row).getLastPathComponent();
			
			IValidator	validator	= node.getType().getValidator(convertColumnIndexToModel(column));
			if(validator!=null)
			{
				JValidatorTextField	valt	= new JValidatorTextField(15);
				valt.setValidator(validator);
				editor	= new DefaultCellEditor(valt);
			}
			else
			{
				editor	= super.getCellEditor(row, column);
			}
			
			return editor;
		}

		/**
		 *  Overridden to disable rows of "removed" nodes.
		 */
	    public Component prepareRenderer(TableCellRenderer renderer,
	    	int row, int column)
	    {
	    	Component	comp	= super.prepareRenderer(renderer, row, column);

			// Don't change tree column.
			if(column!=0)
			{
				// Disable nodes for removed elements.
				DefaultTreeTableNode	node	= (DefaultTreeTableNode)
					getTree().getPathForRow(row).getLastPathComponent();
				boolean	enabled	= true;
				while(enabled && node!=null)
				{
					enabled	= !oldnodes.containsKey(node);
					node	= (DefaultTreeTableNode)node.getParent();
				}
				comp.setEnabled(enabled);
			}

			return comp;
	    }

		/**
		 *  Set the model of the tree table.
		 */
		public void	setModel(TreeTableModel treeTableModel)
		{
			

			// Code taken from JTreeTable constructor. (Hack ???)

			// Creates the tree. It will be used as a renderer and editor. 
			tree = new TreeTableCellRenderer(treeTableModel);
			tree.setCellRenderer(new BDIViewerTreeCellRenderer());

			// Installs a tableModel representing the visible rows in the tree. 
			super.setModel(new TreeTableModelAdapter(treeTableModel, tree));

			// Forces the JTable and JTree to share their row selection models. 
			ListToTreeSelectionModelWrapper selectionWrapper = new 
			ListToTreeSelectionModelWrapper();
			tree.setSelectionModel(selectionWrapper);
			setSelectionModel(selectionWrapper.getListSelectionModel()); 

			// Installs the tree editor renderer. 
			setDefaultRenderer(TreeTableModel.class, tree); 

			// And update the height of the trees row to match that of
			// the table.
			if (tree.getRowHeight() < 1)
			{
				// Metal looks better like this.
				setRowHeight(20);
			}

			// Hack!!! Try to show the new table nicely.
			new TreeExpansionHandler(tree);
			tree.setToggleClickCount(42); // Disable node expansion by double-click.
			for(int i=0; i<tree.getRowCount(); i++)
			{
				tree.expandRow(i);
			}
			((ResizeableTableHeader)getTableHeader()).setAllColumnWidths(100, -1, -1);
			((ResizeableTableHeader)getTableHeader()).setColumnWidths(getColumnModel().getColumn(0), 200, 100, -1);
		}
	}

	/**
	 *  A mouse listener to add popup-menus to the base panels.
	 */
	class PopupListener	extends MouseAdapter
	{
		// Is only mouseReleased a popup trigger???
		public void	mousePressed(MouseEvent e)	{doPopup(e);}
		public void	mouseReleased(MouseEvent e)	{doPopup(e);}
		public void	mouseClicked(MouseEvent e)	{doPopup(e);}

		/** Open a popup menu. */
		protected void doPopup(MouseEvent e)
		{
			if(e.isPopupTrigger())
			{
				JTreeTable	table	= (JTreeTable)e.getSource();
				TreePath	path	= table.getTree()
					.getPathForLocation(e.getX(), e.getY());
				// Should also support clicks on other table columns???
				if(path!=null)
				{
					final DefaultTreeTableNode	node
						= (DefaultTreeTableNode)path.getLastPathComponent();
					if(!oldnodes.containsKey(node))
					{
						Action[]	actions	= node.getType().getPopupActions();
						if(actions.length>0)
						{
							// Select rows.
							int row	= table.rowAtPoint(e.getPoint());
							table.clearSelection();
							table.addRowSelectionInterval(row, row);
	
							// Create listener object.
							ActionListener	listener	= new ActionListener()
							{
								public void	actionPerformed(ActionEvent ae)
								{
									// Create perform request and do the action...
									PerformAction	pa	= new PerformAction(ae.getActionCommand(), null, null, "introspector");
									fillElementAction(pa, node);
									tool.performToolAction(pa);
								}
							};
	
							// Show menu.
							JPopupMenu	menu	= new JPopupMenu("Actions");
							for(int i=0; i<actions.length; i++)
							{
								JMenuItem	item	= new JMenuItem(actions[i]);
								item.addActionListener(listener);
								menu.add(item);
							}
							menu.show(table, e.getX(), e.getY());
						}
					}
				}
			}
		}
	}

	/**
	 *  Fill in the slots for an element action
	 *  using the properties of the given node.
	 *  @param action	The action to fill in.
	 *  @param node	The node to use as template.
	 */
	protected static void	fillElementAction(ElementAction action, DefaultTreeTableNode node)
	{
		// Determine properties of change request.
		String	name	= node.getValue(0).toString();
		String	type	= node.getType().getName();
		String	scope	= null;
		while(node.getParent()!=null)
		{
			node	= (DefaultTreeTableNode)node.getParent();
			if(node.instanceOf(NODE_RBELIEFSETCONTAINER))
			{
				// Prepend name of beliefset. (Hack ???)
				name	= node.getValue(0).toString()+ "." + name;
			}
			else if(node.instanceOf(NODE_RCAPABILITY))
			{
				String	capaname	= node.getValue(0).toString();
				// Hack!!! Strip platform name from agent name.
				int	sep	= capaname.indexOf("@");
				if(sep!=-1 && node.instanceOf(NODE_RBDIAGENT))
					capaname	= capaname.substring(0, sep);
				scope	= scope==null ? capaname : capaname+ "." + scope;
			}
		}

		// Fill in values.
		action.setElementName(name);
		action.setElementType(type);
		action.setScope(scope);
	}

	/**
	 *  Create a popup action.
	 */
	public static Action	createPopupAction(String command, String name)
	{
		Action	action	= new AbstractAction(name)
		{
			// dummy action that never gets executed.
			public void actionPerformed(ActionEvent e){}
		};
		action.putValue(Action.ACTION_COMMAND_KEY, command);
		return action;
	}
}

