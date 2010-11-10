package jadex.tools.introspector.bdiviewer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jadex.runtime.SystemEvent;
import jadex.tools.common.jtreetable.*;
import jadex.tools.introspector.ToolPanel;


/**
 *  A component for showing the contents of a beliefbase.
 */
public class BeliefbasePanel	extends BasePanel
{
	//-------- constants --------

	/** The system event types. */
	protected static final String[]	TYPES	= new String[]
	{
		SystemEvent.BELIEF_EVENT,
		SystemEvent.FACT_CHANGED,
		SystemEvent.BSFACT_ADDED,
		SystemEvent.BSFACT_REMOVED,
		SystemEvent.BSFACT_CHANGED,
		SystemEvent.BSFACTS_CHANGED,
		SystemEvent.CAPABILITY_EVENT,
		SystemEvent.AGENT_EVENT
	};

	//-------- constructors --------

	/**
	 *  Create a new beliefbase panel.
	 */
	public BeliefbasePanel(ToolPanel tool)
	{
		super(tool, "Beliefbase", NODE_RBELIEF.getIcon(null), TYPES);
	}

	//-------- view update methods --------

	/**
	 *  Update the view, as a change event occured.
	 *  @param events	The change event.
	 */
	public void systemEventsOccurred(final SystemEvent[] events)
	{
//		System.out.println("BDIViewer belief update: "+jadex.util.SUtil.arrayToString(events));
		for(int i=0; i<events.length; i++)
		{
			// Handle capability message.
			if(events[i].instanceOf(SystemEvent.CAPABILITY_EVENT)
				|| events[i].instanceOf(SystemEvent.AGENT_EVENT) )
			{
				handleCapabilityUpdate(events[i]);
			}

			// Handle belief message.
			else if(events[i].instanceOf(SystemEvent.BELIEF_EVENT)
				|| events[i].instanceOf(SystemEvent.FACT_CHANGED)
				|| events[i].instanceOf(SystemEvent.BSFACT_EVENT))
			{
				handleBeliefUpdate(events[i]);
			}
		}
	}

	/**
	 *  Update belief tree.
	 *  @param event	The event.
	 */
	protected void	handleBeliefUpdate(SystemEvent event)
	{
		// Extract belief element.
		Map	source	= (Map)event.getSource();
		// Hack!!! Update value, when set in event.
		if(event.getValue()!=null)
			source.put("value", event.getValue());

		// Get beliefbase node.
		DefaultTreeTableNode	base	= getNode(
			(String)source.get("scope")).getChild("Beliefbase");

		// New belief has been added.
		if(event.instanceOf(SystemEvent.BELIEF_ADDED))
		{
			// Determine belief type.
			TreeTableNodeType	type	= getNodeType(event);

			// For beliefsets create container node.
			if(type==NODE_RBELIEFSETREFERENCE)
			{
				type	= NODE_RBELIEFSETREFERENCECONTAINER;
			}
			else if(type==NODE_RBELIEFSET)
			{
				type	= NODE_RBELIEFSETCONTAINER;
			}

			// Create the corresponding node (for belief or set container).
			addNode(base, new DefaultTreeTableNode(type, source.get("name"), source));
		}

		// Belief has been removed.
		else if(event.instanceOf(SystemEvent.BELIEF_REMOVED))
		{
			DefaultTreeTableNode	belief	= base.getChild(source.get("name"));
			if(belief==null)
			{
				System.err.println("Warning: BDIViewer ignoring unknown belief: "+source.get("name")+", "+source.get("scope"));
				return;
			}
			belief.setValues(source);
			removeNode(belief);
		}

		// Fact of (single) belief has changed.
		// (Should support also beliefsets?)
		else if(event.instanceOf(SystemEvent.FACT_CHANGED))
		{
			DefaultTreeTableNode	belief	= base.getChild(source.get("name"));
			if(belief==null)
			{
				System.err.println("Warning: BDIViewer ignoring unknown belief: "+source.get("name")+", "+source.get("scope"));
				return;
			}
			belief.setValues(source);
		}

		// Fact has been added to beliefset.
		else if(event.instanceOf(SystemEvent.BSFACT_ADDED))
		{
			int index	= event.getIndex();
			TreeTableNodeType	type	= getNodeType(event);
			DefaultTreeTableNode	container	= base.getChild(source.get("name"));
			if(container==null || index>container.getChildCount())
			{
				System.err.println("Warning: BDIViewer ignoring unknown belief: "+source.get("name")+", "+source.get("scope"));
				return;
			}
			DefaultTreeTableNode	fact	= new SetTreeTableNode(type, source);
			container.insert(fact, index);
		}

		// Fact has been changed in beliefset.
		else if(event.instanceOf(SystemEvent.BSFACT_CHANGED))
		{
			int index	= event.getIndex();
			DefaultTreeTableNode	container	= base.getChild(source.get("name"));
			if(container==null || index>container.getChildCount())
			{
				System.err.println("Warning: BDIViewer ignoring unknown belief: "+source.get("name")+", "+source.get("scope"));
				return;
			}
			DefaultTreeTableNode	belief	= (DefaultTreeTableNode)container.getChildAt(index);
			belief.setValues(source);
		}

		// Fact has been removed from beliefset.
		else if(event.instanceOf(SystemEvent.BSFACT_REMOVED))
		{
			int index	= event.getIndex();
			DefaultTreeTableNode	container	= base.getChild(source.get("name"));
			if(container==null || index>container.getChildCount())
			{
				System.err.println("Warning: BDIViewer ignoring unknown belief: "+source.get("name")+", "+source.get("scope"));
				return;
			}
			DefaultTreeTableNode	belief	= (DefaultTreeTableNode)container.getChildAt(index);
			belief.setValues(source);
			removeNode(belief);
		}

		// All facts of belief set have changed.
		else if(event.instanceOf(SystemEvent.BSFACTS_CHANGED))
		{
			DefaultTreeTableNode	container	= base.getChild(source.get("name"));
			if(container==null)
			{
				System.err.println("Warning: BDIViewer ignoring unknown belief: "+source.get("name")+", "+source.get("scope"));
				return;
			}

			// Remove previous facts.
			while(container.getChildCount()>0)
				removeNode((DefaultTreeTableNode)container.getChildAt(0));
			
			// Add new facts.
			TreeTableNodeType	type	= getNodeType(event);
			List	vals	= (List)event.getValue();
			for(int i=0; i<vals.size(); i++)
			{
				Map	values	= new HashMap();
				values.putAll(source);
				values.put("value", vals.get(i));
				DefaultTreeTableNode	fact	= new SetTreeTableNode(type, values);
				container.insert(fact, i);			
			}
		}
	}

	//-------- template methods --------

	/**
	 *  Create the tree table model.
	 *  @return The created model.
	 */
	protected TreeTableModel	createModel()
	{
		DefaultTreeTableNode	beliefs	= new DefaultTreeTableNode(
			NODE_RBDIAGENT, tool.getAgentName());
		return new BDITreeTableModel(tool,
			beliefs, NODE_RBELIEF.getColumnNames());
	}

	/**
	 *  Create a base node.
	 *  To be implemented by the corresponding subclass (e.g. BeliefBasePanel).
	 *  @return The created base node.
	 */
	protected DefaultTreeTableNode	createBaseNode()
	{
		return new DefaultTreeTableNode(NODE_RBELIEFBASE, "Beliefbase");
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
		// Add single beliefs always before beliefsets.
		if(node.instanceOf(NODE_RBELIEF))
		{
			while(index>0 && ((DefaultTreeTableNode)parent.getChildAt(index-1))
				.instanceOf(NODE_RBELIEFSETCONTAINER))
			{
				index--;
			}
		}
		return index;
	}
}

