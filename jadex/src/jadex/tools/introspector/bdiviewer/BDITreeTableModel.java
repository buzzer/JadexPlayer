package jadex.tools.introspector.bdiviewer;

import jadex.tools.common.jtreetable.*;
import jadex.tools.introspector.ToolPanel;
import jadex.tools.ontology.ChangeAttribute;
import jadex.util.SUtil;

/**
 *  A tree table model, which sends change messages, when
 *  cells are edited.
 */
public class BDITreeTableModel	extends DefaultTreeTableModel
{
	//-------- attributes --------

	/** The tool panel (for communication with the observed agent). */
	protected ToolPanel	tool;

	//-------- constructors --------

	/**
	 *  Create a new tree table model with the given root node and column names.
	 *  @param tool	The tool panel.
	 *  @param root	The root node (e.g. RBDIAgent).
	 *  @param columnnames	The column names.
	 */
	public BDITreeTableModel(ToolPanel tool, DefaultTreeTableNode root, String[] columnnames)
	{
		super(root, columnnames);
		this.tool	= tool;
	}

	//-------- TreeTableModel overridings --------

	/**
	 *  Set the value of the given cell.
	 */
	public void	setValueAt(Object value, Object node, int column)
	{
		// Extract properties of node.
		DefaultTreeTableNode	dttn	= (DefaultTreeTableNode)node;
		
		// Only perform action, when value has changed.
		if(!SUtil.equals(dttn.getValue(column), value))
		{
			String	attribute	= dttn.getType().getColumns()[column];
	
			// Create change request.
			ChangeAttribute	ca	= new ChangeAttribute();
			BasePanel.fillElementAction(ca, dttn);
			ca.setAttributeName(attribute);
			ca.setValue(" "+value);	// Hack, add extra space to force quotes???
	
			// Do the action...
			tool.performToolAction(ca);
		}
	}

	/**
	 *  Test if a cell is editable
	 */ 
	public boolean isCellEditable(Object node, int column)
	{
		// Hack !!! Have to make column 0 editable to allow clicking in tree.
		return column==0 ||
			((DefaultTreeTableNode)node).getType().isColumnEditable(column);
	}
}

