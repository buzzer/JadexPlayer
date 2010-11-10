package jadex.tools.dfbrowser;

import jadex.adapter.fipa.*;

import javax.swing.table.AbstractTableModel;
import java.util.Date;

/**
 *  The table model for agents.
 */
class AgentTableModel extends AbstractTableModel
{
	protected AgentDescription[] ads;


	/**
	 * @return 6
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount()
	{
		return 6;
	}

	/**
	 * @param ad
	 */
	public void setAgentDescriptions(AgentDescription[] ad)
	{
		ads = ad;
		fireTableDataChanged();
	}

	/**
	 * @return all agent subscriptions length
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount()
	{
		return ads != null ? ads.length : 0;
	}

	/**
	 * @param rowIndex
	 * @param columnIndex
	 * @return the values of this table
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		if(ads == null || rowIndex < 0 || rowIndex >= ads.length)
		{
			return null;
		}
		AgentDescription ad = ads[rowIndex];
		switch(columnIndex)
		{
			case 0:
				return ad.getName();
			case 1:
				return ad.getLeaseTime();
			case 2:
				return ad.getServices();
			case 3:
				return ad.getOntologies();
			case 4:
				return ad.getLanguages();
			case 5:
				return ad.getProtocols();
		}

		return null;
	}

	/**
	 * @param columnIndex
	 * @return the name of a columnt
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	public String getColumnName(int columnIndex)
	{
		switch(columnIndex)
		{
			case 0:
				return "Agent";
			case 1:
				return "Leasetime";
			case 2:
				return "Services";
			case 3:
				return "Ontologies";
			case 4:
				return "Languages";
			case 5:
				return "Protocols";
		}
		return null;
	}

	/**
	 * @param columnIndex
	 * @return the class of a column
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	public Class getColumnClass(int columnIndex)
	{
		switch(columnIndex)
		{
			case 0:
				return AgentIdentifier.class;
			case 1:
				return Date.class;
			case 2:
				return ServiceDescription[].class;
			case 3:
				return String[].class;
			case 4:
				return String[].class;
			case 5:
				return String[].class;
		}
		return null;
	}

	/**
	 * @param i
	 * @return the agent description at row i
	 */
	public AgentDescription getAgentDescription(int i)
	{
		return ads == null || i < 0 || i >= ads.length ? null : ads[i];
	}

}