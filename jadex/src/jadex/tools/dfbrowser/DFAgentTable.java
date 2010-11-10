package jadex.tools.dfbrowser;

import jadex.adapter.fipa.*;
import jadex.tools.common.TableSorter;
import jadex.util.SGUI;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 *  This class serves for displaying agent descriptions.
 */
public class DFAgentTable extends JTable
{
	static final AgentDescription[] EMPTY = new AgentDescription[0];
	
	/** The image icons. */
	protected static final UIDefaults icons = new UIDefaults(new Object[]
	{
		"remove_agent", SGUI.makeIcon(DFBrowserPlugin.class, "/jadex/tools/common/images/new_remove_service.png"),
	});
	
	//-------- attributes --------
	
	/** The popup menu. */
	protected JPopupMenu popup;

	/** The browser plugin. */
	protected DFBrowserPlugin browser;

	//-------- constructors --------
	
	/**
	 * Constructor for DFAgentTable.
	 */
	public DFAgentTable(final DFBrowserPlugin browser)
	{
		super(new TableSorter(new AgentTableModel()));
		this.browser = browser;
		TableSorter sorter = (TableSorter)getModel();
		
		sorter.setTableHeader(getTableHeader());
		setPreferredScrollableViewportSize(new Dimension(800, 70));
		setDefaultRenderer(AgentIdentifier.class, new AgentIdentifierRenderer());
		setDefaultRenderer(String[].class, new StringArrayRenderer());
		setDefaultRenderer(ServiceDescription[].class, new ServiceDescriptionArrayRenderer());
		setDefaultRenderer(Date.class, new LeaseTimeRenderer());
		
		addMouseListener(new MouseAdapter()
		{
			/*public void mouseClicked(MouseEvent e)
			{
				int selectedRow = getSelectedRow();
				if(e.getClickCount() > 1 && selectedRow >= 0)
				{
					agentSelected(model.getAgentDescription(sorter.modelIndex(selectedRow)));
				}
			}*/

			public void mousePressed(MouseEvent e)
			{
				if(e.isPopupTrigger())
				{
					popup.show(DFAgentTable.this, e.getX(), e.getY());
				}
			}

			public void mouseReleased(MouseEvent e)
			{
				if(e.isPopupTrigger())
				{
					popup.show(DFAgentTable.this, e.getX(), e.getY());
				}
			}
		});

		popup = new JPopupMenu();
		addMenuItems(popup);
	}

	/**
	 *  Add the menu items.
	 *  @param menu The menu.
	 */
	protected void addMenuItems(JPopupMenu menu)
	{
		menu.add(new JMenuItem(new AbstractAction("Remove agent description", icons.getIcon("remove_agent"))
		{
			public void actionPerformed(ActionEvent e)
			{
				int selectedRow = getSelectedRow();
				if(selectedRow >= 0)
				{
					TableSorter sorter = (TableSorter)getModel();
					AgentTableModel model = (AgentTableModel)sorter.getTableModel();
					browser.removeAgentRegistration(model.getAgentDescription(sorter.modelIndex(selectedRow)));
				}
			}

		}));
		/*menu.add(new JMenuItem(new AbstractAction("Show services")
		{
			public void actionPerformed(ActionEvent e)
			{
				TableSorter sorter = (TableSorter)getModel();
				ServiceTableModel model = (ServiceTableModel)sorter.getTableModel();
				int selectedRow = getSelectedRow();
				if(selectedRow >= 0)
				{
					agentSelected(model.getAgentDescription(sorter.modelIndex(selectedRow)));
				}
			}
		}));*/
	}

	/**
	 *  Get the selected agents.
	 *  @return the descriptions of selected agents
	 */
	public AgentDescription[] getSelectedAgents()
	{
		AgentDescription[] ret = EMPTY;
		
		int count = getSelectedRowCount();
		if(count>0)
		{
			TableSorter sorter = (TableSorter)getModel();
			AgentTableModel model = (AgentTableModel)sorter.getTableModel();
		
			ArrayList sa = new ArrayList();
			int[] rows = getSelectedRows();
			for(int i = 0; i < rows.length; i++)
			{
				sa.add(model.getAgentDescription(sorter.modelIndex(rows[i])));
			}
			ret = (AgentDescription[])sa.toArray(new AgentDescription[sa.size()]);
		}

		return ret;
	}

	/**
	 *  Sets Agent descriptions for this element.
	 *  @param ad The agent description.
	 */
	public void setAgentDescriptions(AgentDescription[] ad)
	{
		TableSorter sorter = (TableSorter)getModel();
		AgentTableModel model = (AgentTableModel)sorter.getTableModel();
		model.setAgentDescriptions(ad);
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
			props.setProperty("agentTable.column" + i, Integer.toString(column.getWidth()));
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
				String p = props.getProperty("agentTable.column" + i);
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
