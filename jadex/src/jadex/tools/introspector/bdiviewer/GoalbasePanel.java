package jadex.tools.introspector.bdiviewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import jadex.runtime.SystemEvent;
import jadex.tools.common.jtreetable.*;
import jadex.tools.introspector.ToolPanel;
import jadex.util.*;
import jadex.util.jtable.ResizeableTableHeader;


/**
 *  A component for showing the contents of a goalbase.
 */
public class GoalbasePanel	extends BasePanel
{
	//-------- constants --------

	/** The system event types. */
	protected static final String[]	TYPES	= new String[]
	{
		SystemEvent.GOAL_EVENT,
		SystemEvent.PLAN_EVENT,
		SystemEvent.CAPABILITY_EVENT,
		SystemEvent.AGENT_EVENT
	};

	//-------- attributes --------

	/** The goal [name,scope] to goal node mapping (planview). */
	protected Map	pgoalnodes;

	/** The flag indicating goalview mode is on. */
	protected boolean	goalview;

	/** The goal/plan view model. */
	protected TreeTableModel	pmodel;

	/** The capability/goal view model. */
	protected TreeTableModel	gmodel;

	/** The planview/goalview label. */
	protected JLabel	gvlabel;

	//-------- constructors --------

	/**
	 *  Create a new goalbase panel.
	 */
	public GoalbasePanel(ToolPanel tool)
	{
		super(tool, "Goalbase", NODE_RGOAL.getIcon(null), TYPES);
		this.goalview	= false;
		this.gmodel	= goalview ? model : createModel();
		this.pmodel	= !goalview ? model : createModel();
		this.pgoalnodes	= new HashMap();
		this.oldmax	= 40; 	// Hack!!! Todo: Separate oldnode caches for goal/planview.

		// Hack!!! Reduce size of columns, as more is displayed.
		((ResizeableTableHeader)table.getTableHeader()).setAllColumnWidths(100, -1, -1);
		((ResizeableTableHeader)table.getTableHeader()).setColumnWidths(this.table.getColumnModel().getColumn(0), 200, 100, -1);

		Component	toolbar	= getComponent(0);
		JPanel	panel	= new JPanel(new BorderLayout());
		gvlabel	= new JLabel("", JLabel.LEFT);
		gvlabel.setText(goalview ? "Goalview - shows capability/goal structure"
			: "Planview - shows goal/plan hierarchy");
		gvlabel.setIcon(goalview ? defaults.getIcon("goalview")
			: defaults.getIcon("planview"));
		panel.add(BorderLayout.SOUTH, toolbar);
		panel.add(BorderLayout.CENTER, gvlabel);
		this.add(BorderLayout.NORTH, panel);
	}

	//-------- view update methods --------

	/**
	 *  Change between planview and goalview.
	 *  @param goalview	Flag to (de)activate the goalview.
	 */
	public void	setGoalview(boolean goalview)
	{
		if(goalview!=this.goalview)
		{
			this.goalview	= goalview;

			table.setModel(goalview ? gmodel : pmodel);
			gvlabel.setText(goalview ? "Goalview - shows capability/goal structure"
				: "Planview - shows goal/plan hierarchy");
			gvlabel.setIcon(goalview ? defaults.getIcon("goalview")
				: defaults.getIcon("planview"));
		}
	}

	/**
	 *  Update the view, as a change event occured.
	 *  @param events	The change event.
	 */
	public void systemEventsOccurred(SystemEvent[] events)
	{
		// If several goal events arrive at once,
		// keep those events, that can't be handled in the first run
		// due to missing parent/proprietary goals.
		List	unhandled	= null;
		if(events.length>1)
			unhandled = new ArrayList(events.length);

		//System.out.println("BDIViewer event: "+event.toSLString());
		for(int i=0; i<events.length; i++)
		{
			// Handle capability event.
			if(events[i].instanceOf(SystemEvent.CAPABILITY_EVENT)
				|| events[i].instanceOf(SystemEvent.AGENT_EVENT) )
			{
				handleCapabilityUpdate(events[i]);
			}

			// Handle goal event.
			else if(events[i].instanceOf(SystemEvent.GOAL_EVENT))
			{
//				if(((String)((Map)events[i].getSource()).get("name")).startsWith("performlook"))
//					System.out.println(events[i]);
				if(!handleGoalUpdate(events[i]))
				{
					if(unhandled!=null)
					{
						unhandled.add(events[i]);
					}
					else
					{
//						System.err.println("Warning: BDIViewer ignoring event: "+events[i]);
					}
				}
			}

			// Handle plan event.
			else if(events[i].instanceOf(SystemEvent.PLAN_EVENT))
			{
//				if(((String)((Map)events[i].getSource()).get("name")).startsWith("performlook"))
//					System.out.println(events[i]);
				if(!handlePlanUpdate(events[i]))
				{
					if(unhandled!=null)
					{
						unhandled.add(events[i]);
					}
					else
					{
//						System.err.println("Warning: BDIViewer ignoring event: "+events[i]);
					}
				}
			}
		}

		// Handle outstanding goal events.
		while(unhandled!=null && unhandled.size()>0)
		{
//			System.out.println("processing unhandled events: "+unhandled);
			List unhandled2	= new ArrayList(unhandled.size());
			for(int i=0; i<unhandled.size(); i++)
			{
				SystemEvent	event	= (SystemEvent)unhandled.get(i);
				if(event.instanceOf(SystemEvent.GOAL_EVENT))
				{
					if(!handleGoalUpdate(event))
						unhandled2.add(unhandled.get(i));
				}
				else // if(event.instanceOf(SystemEvent.PLAN_EVENT))
				{
					if(!handlePlanUpdate(event))
						unhandled2.add(unhandled.get(i));
				}
			}

			// Could handle some or all of the outstanding events -> proceed.
			if(unhandled2.size()!=unhandled.size())
			{
				unhandled	= unhandled2;
			}

			// Could'nt handle any of the outstanding events -> ignore.
			else
			{
				System.err.println("Warning: BDIViewer ignoring events (all/unhandled):\n"+SUtil.arrayToString(events)+"\n"+unhandled);
				break;
			}
		}
	}

	/**
	 *  Update capability hierarchy.
	 *  @param event	The event.
	 */
	protected void	handleCapabilityUpdate(SystemEvent event)
	{
		// Update only capability/goal view.
		this.model	= gmodel;
		super.handleCapabilityUpdate(event);

		this.model	= goalview ? gmodel : pmodel;
	}

	/**
	 *  Update goalview and planview when the goals have changed. 
	 *  @param event	The event.
	 *  @return false, when the event can't be handled in the first run
	 *    due to missing parent/proprietary goals.
	 */
	protected boolean	handleGoalUpdate(SystemEvent event)
	{
		// Determine node type.
		Map	source	= (Map)event.getSource();
		TreeTableNodeType	type	= getNodeType(event);

		// The base nodes (for planview and goalview).
		DefaultTreeTableNode	pbase	= null;
		DefaultTreeTableNode	gbase	= null;
		boolean	showinplanview	= false;
		
		// Show only real goals (not references) in planview.
		if(type==NODE_RACHIEVEGOAL || type==NODE_RMAINTAINGOAL
			|| type==NODE_RPERFORMGOAL || type==NODE_RQUERYGOAL)
		{
			showinplanview	= true;
			
			// In planview, subgoals get added to parent...
			if(source.get("realparent")!=null)
			{
				Tuple	parent	= new Tuple(source.get("realparent"), source.get("realparentscope"));
				pbase	= (DefaultTreeTableNode)pgoalnodes.get(parent);
			}
			// ...while top-level goals get added to the agent.
			else
			{
				pbase	= (DefaultTreeTableNode)pmodel.getRoot();
			}
		}
		// Metagoals of other goals are shown as subnodes of their triggering goals.
		else if(type==NODE_RMETAGOAL)
		{
			showinplanview	= true;
			
			// For simplicity the trigger is the goal directly (not the goal event).
			if(source.get("trigger")!=null)
			{
				Tuple	parent	= new Tuple(source.get("trigger"), source.get("triggerscope"));
				pbase	= (DefaultTreeTableNode)pgoalnodes.get(parent);
			}
			else
			{
				pbase	= (DefaultTreeTableNode)pmodel.getRoot();
			}
		}

		// For goalview use corresponding base as parent.
		gbase	= getNode(gmodel, (String)source.get("scope")).getChild("Goalbase");

		// When parent nodes can't be found, try to handle the event later.
		if(showinplanview && pbase==null || gbase==null)
			return false;
		
		// New goal has been added.
		if(event.instanceOf(SystemEvent.GOAL_ADDED))
		{
			// For goalview, just add goal.
			DefaultTreeTableNode	gnode	= createNode(event);
			addNode(gbase, gnode);

			// For planview, remember goal to be used as parent for other goals.
			if(showinplanview)
			{
				DefaultTreeTableNode	pnode	= createNode(event);
				addNode(pbase, pnode);
				Tuple	pgoal	= new Tuple(source.get("name"), source.get("scope"));
				pgoalnodes.put(pgoal, pnode);
			}
		}

		// Goal has been removed.
		else if(event.instanceOf(SystemEvent.GOAL_REMOVED))
		{
			// Remove goal from goalview
			DefaultTreeTableNode	goal	= gbase.getChild(source.get("name"));
			if(goal==null)
			{
				// Maybe goal not yet added, try to handle later
				return false;
			}
			goal	= gbase.getChild(source.get("name"));
			goal.setValues(source);
			removeNode(goal);
			
			// Remove goal from planview
			if(showinplanview)
			{
				goal	= pbase.getChild(source.get("name"));
				// Update values as removed goals can also be displayed.
				goal.setValues(source);
				removeNode(goal);
				Tuple	pgoal	= new Tuple(source.get("name"), source.get("scope"));
				pgoalnodes.remove(pgoal);
			}
		}

		// Goal has changed.
		else if(event.instanceOf(SystemEvent.GOAL_CHANGED))
		{
			// Update goal in goalview.
			DefaultTreeTableNode	goal	= gbase.getChild(source.get("name"));
			if(goal==null)
			{
				// Maybe goal not yet added, try to handle later
				return false;
			}
			goal.setValues(source);

			// Update goal in planview.
			if(showinplanview)
			{
				goal	= pbase.getChild(source.get("name"));
				goal.setValues(source);
			}
		}

		return true;
	}

	/**
	 *  Update goal/plan hierarchy when the plans have changed.
	 *  @param event	The event.
	 *  @return false, when the event can't be handled in the first run
	 *    due to missing parent/proprietary goals.
	 */
	protected boolean	handlePlanUpdate(SystemEvent event)
	{
		// Determine node type.
		Map	source	= (Map)event.getSource();
		TreeTableNodeType	type	= getNodeType(event);

		// The base node for planview.
		DefaultTreeTableNode	pbase	= null;
		
		// Plans handling goals get added to goal.
		if(source.get("proprietarygoal")!=null)
		{
			Tuple	parent	= new Tuple(source.get("proprietarygoal"), source.get("proprietarygoalscope"));
			pbase	= (DefaultTreeTableNode)pgoalnodes.get(parent);
		}
		
		// Top-level plans get added to the agent.
		else
		{
			pbase	= (DefaultTreeTableNode)pmodel.getRoot();
		}

		// When parent node can't be found, try to handle the event later.
		if(pbase==null)
			return false;
		
		// New plan has been added.
		if(event.instanceOf(SystemEvent.PLAN_ADDED))
		{
			// Add goal to planview.
			DefaultTreeTableNode	pnode	= createNode(event);
			addNode(pbase, pnode);
			
			// Add node using rootgoal as key to be found as parent for other goals.
			Tuple	pkey	= new Tuple(source.get("rootgoal"), source.get("scope"));
			pgoalnodes.put(pkey, pnode);
		}

		// Plan has been removed.
		else if(event.instanceOf(SystemEvent.PLAN_REMOVED))
		{
			// For planview just remove goal.
			DefaultTreeTableNode	pnode	= pbase.getChild(source.get("name"));
			if(pnode==null)
			{
				// Maybe plan not yet added, try to handle later
				return false;
			}
			// Update values as removed goals can also be displayed.
			pnode.setValues(source);
			removeNode(pnode);

			// Remove node using rootgoal as key (cf. add).
			Tuple	pkey	= new Tuple(source.get("rootgoal"), source.get("scope"));
			pgoalnodes.remove(pkey);
		}

		// Plan has changed.
		else if(event.instanceOf(SystemEvent.PLAN_CHANGED))
		{
 			// For planview just update goal.
			DefaultTreeTableNode	pnode	= pbase.getChild(source.get("name"));
			if(pnode==null)
			{
				// Maybe plan not yet added, try to handle later
				return false;
			}
			pnode.setValues(source);
		}

		return true;
	}

	/**
	 *  Get the (menu/toolbar) actions of the bdi viewer.
	 */
	public Action[]	getActions()
	{
		if(actions==null)
		{
			Action[]	actions2	= super.getActions();
			actions	= new javax.swing.Action[actions2.length+2];
			System.arraycopy(actions2, 0, actions, 0, actions2.length);
	
			actions[actions2.length+1]	= new AbstractAction("Toggle Plan-/Goalview", defaults.getIcon("togglegoalview"))
			{
				public void actionPerformed(ActionEvent ae)
				{
					// Toggle icon.
					this.putValue(SMALL_ICON, goalview ? defaults.getIcon("togglegoalview")
						: defaults.getIcon("toggleplanview"));
	
					// Apply view.
					setGoalview(!goalview);
				}
			};
		}

		return actions;
	}

	//-------- template methods --------

	/**
	 *  Create the tree table model.
	 *  @return The created model.
	 */
	protected TreeTableModel	createModel()
	{
		DefaultTreeTableNode	goals	= new DefaultTreeTableNode(
			NODE_RBDIAGENT, tool.getAgentName());
		return new BDITreeTableModel(tool, goals,
			NODE_RABSTRACTGOAL.getColumnNames());
	}

	/**
	 *  Create a base node.
	 *  To be implemented by the corresponding subclass (e.g. GoalBasePanel).
	 *  @return The created base node.
	 */
	protected DefaultTreeTableNode	createBaseNode()
	{
		return new DefaultTreeTableNode(NODE_RGOALBASE, "Goalbase");
	}

	/**
	 *  Clear the view when refreshing.
	 *  Can be overriden to perform custom cleanup, but
	 *  super.clear() should be called.
	 */
	protected void	clear()
	{
		((DefaultTreeTableNode)(goalview ? pmodel : gmodel).getRoot())
			.removeAllChildren();
		super.clear();
		pgoalnodes.clear();
	}

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
		// Add proprietary goals always before process goals.
		// Also, sort proprietary goals alphabetically, regardless of kind.
		if(node.instanceOf(NODE_RGOAL))
		{
			while(index>0)
			{
				DefaultTreeTableNode	sib	= (DefaultTreeTableNode)parent.getChildAt(index-1);
				if( (oldnodes.containsKey(node)==oldnodes.containsKey(sib)) 
					&& (sib.instanceOf(NODE_RPROCESSGOAL)
					|| ((String)node.getValue(0)).compareTo((String)sib.getValue(0))<0))
				{
					index--;
				}
				else
				{
					break;
				}
			}
		}
		return index;
	}
}

