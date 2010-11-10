package jadex.tools.dfbrowser;

import jadex.adapter.fipa.*;
import jadex.tools.common.TableSorter;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Properties;

/**
 *  Table showing the df agent services.
 */
public class DFServiceTable extends JTable//JScrollPane
{
	/**
	 * Constructor for DFAgentTable.
	 */
	public DFServiceTable()
	{
		super(new TableSorter(new ServiceTableModel()));
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		TableSorter sorter = (TableSorter)getModel();
		sorter.setTableHeader(getTableHeader());
		setDefaultRenderer(AgentIdentifier.class, new AgentIdentifierRenderer());
		setDefaultRenderer(String[].class, new StringArrayRenderer());
		setDefaultRenderer(Property[].class, new PropertyArrayRenderer());
	}

	/**
	 *  Get the selected agent/service description.
	 *  @return The currently selected service/agent description.
	 */
	public Object[] getSelectedServices()
	{
		Object[] ret = new Object[]{null, null};
		int sel = getSelectedRow();
		if(sel>=0)
		{
			TableSorter sorter = (TableSorter)getModel();
			ServiceTableModel model = (ServiceTableModel)sorter.getTableModel();
			sel = sorter.modelIndex(sel);
			ret = new Object[]{model.getServiceDescription(sel), model.getAgentDescription(sel)};
		}
		return ret;
	}
	
	/**
	 * Sets Agent descriptions for this element
	 * @param ad
	 */
	public void setAgentDescriptions(AgentDescription[] ad)
	{
		TableSorter sorter = (TableSorter)getModel();
		ServiceTableModel model = (ServiceTableModel)sorter.getTableModel();
		model.setAgentDescriptions(ad);
	}

	/**
	 * @param agentDescription
	 */
	public void setAgentDescription(AgentDescription ad)
	{
		TableSorter sorter = (TableSorter)getModel();
		ServiceTableModel model = (ServiceTableModel)sorter.getTableModel();
		model.setAgentDescription(ad);
	}

	/**
	 *  Get the properties.
	 *  @param props The properties.
	 */
	public void getProperties(Properties props)
	{
		TableColumnModel cm = getColumnModel();
		for(int i=0; i<cm.getColumnCount(); i++)
		{
			TableColumn column = cm.getColumn(i);
			props.setProperty("serviceTable.column" + i, Integer.toString(column.getWidth()));
		}
	}

	/**
	 *  Set the properties.
	 *  @param props The properties.
	 */
	public void setProperties(Properties props)
	{
		TableColumnModel cm = getColumnModel();
		for(int i=0; i<cm.getColumnCount(); i++)
		{
			TableColumn column = cm.getColumn(i);
			try
			{
				String p = props.getProperty("serviceTable.column" + i);
				if(p != null)
				{
					column.setPreferredWidth(Integer.parseInt(p));
				}
			}
			catch(Exception e)
			{
			}
		}
	}
}
