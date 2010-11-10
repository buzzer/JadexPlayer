package jadex.tools.dfbrowser;

import jadex.adapter.fipa.AgentIdentifier;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

class AgentIdentifierRenderer extends DefaultTableCellRenderer
{
	/**
	 * @param table
	 * @param value
	 * @param isSelected
	 * @param hasFocus
	 * @param row
	 * @param column
	 * @return this
	 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
		AgentIdentifier aid = (AgentIdentifier)value;
		setText(aid.getName());
		String[] addresses = aid.getAddresses();
		String tooltip = aid.getName();
		for(int i = 0; i < addresses.length; i++)
		{
			tooltip += "<br>" + addresses[i];
		}
		setToolTipText("<html>" + tooltip + "</html>");
		return this;
	}
}
