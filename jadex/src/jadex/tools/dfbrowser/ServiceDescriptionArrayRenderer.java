package jadex.tools.dfbrowser;

import jadex.adapter.fipa.ServiceDescription;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

class ServiceDescriptionArrayRenderer extends DefaultTableCellRenderer
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
		ServiceDescription[] sa = (ServiceDescription[])value;
		String content;
		String tooltip;
		if(sa == null || sa.length == 0)
		{
			content = "";
			setToolTipText(null);
		}
		else
		{
			content = sa[0].getName();
			tooltip = content + ", type=" + sa[0].getType() + ", owner=" + sa[0].getOwnership();
			for(int i = 1; i < sa.length; i++)
			{
				content += ", " + sa[i].getName();
				tooltip += "<br>" + sa[i].getName() + ", type=" + sa[i].getType() + ", owner=" + sa[i].getOwnership();
			}
			setToolTipText("<html>" + tooltip + "</html>");
		}
		setText(content);
		return this;
	}
}