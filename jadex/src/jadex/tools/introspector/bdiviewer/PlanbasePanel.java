package jadex.tools.introspector.bdiviewer;

import java.util.Map;
import jadex.runtime.SystemEvent;
import jadex.tools.common.jtreetable.*;
import jadex.tools.introspector.ToolPanel;


/**
 *  A component for showing the contents of a planbase.
 */
public class PlanbasePanel	extends BasePanel
{
	//-------- constants --------

	/** The system event types. */
	protected static final String[]	TYPES	= new String[]
	{
		SystemEvent.PLAN_EVENT,
 		SystemEvent.CAPABILITY_EVENT,
		SystemEvent.AGENT_EVENT
	};

	//-------- constructors --------

	/**
	 *  Create a new planbase panel.
	 */
	public PlanbasePanel(ToolPanel tool)
	{
		super(tool, "Planbase", NODE_RPLAN.getIcon(null), TYPES);
	}

	//-------- view update methods --------

	/**
	 *  Update the view, as a change event occured.
	 *  @param events	The change event.
	 */
	public void systemEventsOccurred(final SystemEvent[] events)
	{
		//System.out.println("BDIViewer event: "+event.toSLString());
		for(int i=0; i<events.length; i++)
		{
			// Handle capability message.
			if(events[i].instanceOf(SystemEvent.CAPABILITY_EVENT)
				|| events[i].instanceOf(SystemEvent.AGENT_EVENT) )
			{
				handleCapabilityUpdate(events[i]);
			}

			// Handle plan message.
			else if(events[i].instanceOf(SystemEvent.PLAN_EVENT))
			{
				handlePlanUpdate(events[i]);
			}
		}
	}

	/**
	 *  Update (r)plan tree.
	 *  @param event	The event.
	 */
	protected void	handlePlanUpdate(SystemEvent event)
	{
		// Get planbase node.
		Map	source	= (Map)event.getSource();
		DefaultTreeTableNode	base	= getNode(
			(String)source.get("scope")).getChild("Planbase");
		if(base==null)
			return;	// hack

		// New plan has been added.
		if(event.instanceOf(SystemEvent.PLAN_ADDED))
		{
			addNode(base, createNode(event)); 
		}

		// Plan has been removed.
		else if(event.instanceOf(SystemEvent.PLAN_REMOVED))
		{
			// Remove node.
			DefaultTreeTableNode	plan	= base.getChild(source.get("name"));
			if(plan==null)
			{
//				System.err.println("Warning: BDIViewer ignoring unknown plan: "+source.get("name")+", "+source.get("scope"));
				return;
			}
			plan.setValues(source);
			removeNode(plan);
		}

		// Plan has changed.
		else if(event.instanceOf(SystemEvent.PLAN_CHANGED))
		{
			// Update node.
			DefaultTreeTableNode	plan	= base.getChild(source.get("name"));
			if(plan==null)
			{
//				System.err.println("Warning: BDIViewer ignoring unknown plan: "+source.get("name")+", "+source.get("scope"));
				return;
			}
			plan.setValues(source);
		}
	}

	//-------- template methods --------

	/**
	 *  Create the tree table model.
	 *  @return The created model.
	 */
	protected TreeTableModel	createModel()
	{
		DefaultTreeTableNode	plans	= new DefaultTreeTableNode(
			NODE_RBDIAGENT, tool.getAgentName());
		return new BDITreeTableModel(tool,
			plans, NODE_RPLAN.getColumnNames());
	}

	/**
	 *  Create a base node.
	 *  To be implemented by the corresponding subclass (e.g. BeliefBasePanel).
	 *  @return The created base node.
	 */
	protected DefaultTreeTableNode	createBaseNode()
	{
		return new DefaultTreeTableNode(NODE_RPLANBASE, "Planbase");
	}
}

