package jadex.examples.booktrading.common;

import jadex.runtime.*;
import jadex.util.SGUI;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.util.List;

/**
 * The gui allows to add and delete buy or sell orders and shows open and
 * finished orders.
 */
public class Gui extends JFrame
{
	//-------- attributes --------

	private String itemlabel;
	private String goalname;
	private String addorderlabel;
	private IExternalAccess agent;
	private List orders = new ArrayList();
	private JTable table;
	private DefaultTableModel detailsdm; 
	private AbstractTableModel items = new AbstractTableModel()
	{

		public int getRowCount()
		{
			return orders.size();
		}

		public int getColumnCount()
		{
			return 7;
		}

		public String getColumnName(int column)
		{
			switch(column)
			{
				case 0:
					return "Title";
				case 1:
					return "Start Price";
				case 2:
					return "Limit";
				case 3:
					return "Deadline";
				case 4:
					return "Execution Price";
				case 5:
					return "Execution Date";
				case 6:
					return "State";
				default:
					return "";
			}
		}

		public boolean isCellEditable(int row, int column)
		{
			return false;
		}

		public Object getValueAt(int row, int column)
		{
			Object value = null;
			Order order = (Order)orders.get(row);
			if(column == 0)
			{
				value = order.getTitle();
			}
			else if(column == 1)
			{
				value = new Integer(order.getStartPrice());
			}
			else if(column == 2)
			{
				value = new Integer(order.getLimit());
			}
			else if(column == 3)
			{
				value = order.getDeadline();
			}
			else if(column == 4)
			{
				value = order.getExecutionPrice();
			}
			else if(column == 5)
			{
				value = order.getExecutionDate();
			}
			else if(column == 6)
			{
				value = order.getState();
			}
			return value;
		}
	};
	private DateFormat dformat = new SimpleDateFormat("yyyy/MM/dd HH:mm");

	//-------- constructors --------

	/**
	 * Create a gui.
	 * @param agent The external access.
	 * @param buy The boolean indicating if buyer or seller gui.
	 */
	public Gui(final IExternalAccess agent, final boolean buy)
	{
		super((buy ? "Buyer: " : "Seller: ") + agent.getAgentIdentifier().getName());
		this.agent = agent;
		if(buy)
		{
			itemlabel = " Books to buy ";
			goalname = "purchase_book";
			addorderlabel = "Add new purchase order";
		}
		else
		{
			itemlabel = " Books to sell ";
			goalname = "sell_book";
			addorderlabel = "Add new sell order";
		}

		JPanel itempanel = new JPanel(new BorderLayout());
		itempanel.setBorder(new TitledBorder(new EtchedBorder(), itemlabel));

		this.table = new JTable(items);
		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer()
		{
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean selected, boolean focus, int row,
					int column)
			{
				Component comp = super.getTableCellRendererComponent(table,
						value, selected, focus, row, column);
				setOpaque(true);
				if(column == 0)
				{
					setHorizontalAlignment(LEFT);
				}
				else
				{
					setHorizontalAlignment(RIGHT);
				}
				if(!selected)
				{
					Object state = items.getValueAt(row, 6);
					if(Order.DONE.equals(state))
					{
						comp.setBackground(new Color(211, 255, 156));
					}
					else if(Order.FAILED.equals(state))
					{
						comp.setBackground(new Color(255, 211, 156));
					}
					else
					{
						comp.setBackground(table.getBackground());
					}
				}
				if(value instanceof Date)
				{
					setValue(dformat.format(value));
				}
				return comp;
			}
		});
		table.setPreferredScrollableViewportSize(new Dimension(600, 120));
		
		JScrollPane scroll = new JScrollPane(table);
		itempanel.add(BorderLayout.CENTER, scroll);
		
		this.detailsdm = new DefaultTableModel(new String[]{"Negotiation Details"}, 0);
		JTable details = new JTable(detailsdm);
		details.setPreferredScrollableViewportSize(new Dimension(600, 120));
		
		JPanel dep = new JPanel(new BorderLayout());
		dep.add(BorderLayout.CENTER, new JScrollPane(details));
	
		JPanel south = new JPanel();
		// south.setBorder(new TitledBorder(new EtchedBorder(), " Control "));
		JButton add = new JButton("Add");
		final JButton remove = new JButton("Remove");
		final JButton edit = new JButton("Edit");
		add.setMinimumSize(remove.getMinimumSize());
		add.setPreferredSize(remove.getPreferredSize());
		edit.setMinimumSize(remove.getMinimumSize());
		edit.setPreferredSize(remove.getPreferredSize());
		south.add(add);
		south.add(remove);
		south.add(edit);
		remove.setEnabled(false);
		edit.setEnabled(false);
		
		JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitter.add(itempanel);
		splitter.add(dep);
		splitter.setOneTouchExpandable(true);
		//splitter.setDividerLocation();
		
		this.getContentPane().add(BorderLayout.CENTER, splitter);
		this.getContentPane().add(BorderLayout.SOUTH, south);

		agent.getBeliefbase().getBeliefSet("orders").addBeliefSetListener(new IBeliefSetListener()
		{
			public void beliefSetChanged(AgentEvent ae)
			{
				refresh();
			}
			
			public void factAdded(AgentEvent ae)
			{
				refresh();
			}
			
			public void factRemoved(AgentEvent ae)
			{
				refresh();
			}
		}, false);
		
		agent.getBeliefbase().getBeliefSet("negotiation_reports")
			.addBeliefSetListener(new IBeliefSetListener()
		{
			public void factAdded(AgentEvent ae)
			{
				//System.out.println("a fact was added");
				refreshDetails();
			}

			public void factRemoved(AgentEvent ae)
			{
				//System.out.println("a fact was removed");
				refreshDetails();
			}

			public void beliefSetChanged(AgentEvent ae)
			{
				//System.out.println("belset changed");
			}
		}, false);
		
		agent.addAgentListener(new IAgentListener()
		{
			public void agentTerminating(AgentEvent ae)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						Gui.this.setVisible(false);
					}
				});
			}
		}, false);
		
		table.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				refreshDetails();
			}
		} );
		
		final InputDialog dia = new InputDialog(buy);
		add.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				while(dia.requestInput())
				{
					try
					{
						String title = dia.title.getText();
						int limit = Integer.parseInt(dia.limit.getText());
						int start = Integer.parseInt(dia.start.getText());
						Date deadline = dformat.parse(dia.deadline.getText());
						Order order = new Order(title, deadline, start, limit, buy);
						IGoal purchase = Gui.this.agent.createGoal(goalname);
						purchase.getParameter("order").setValue(order);
						Gui.this.agent.dispatchTopLevelGoal(purchase);
						orders.add(order);
						items.fireTableDataChanged();
						break;
					}
					catch(NumberFormatException e1)
					{
						JOptionPane.showMessageDialog(Gui.this, "Price limit must be integer.", "Input error", JOptionPane.ERROR_MESSAGE);
					}
					catch(ParseException e1)
					{
						JOptionPane.showMessageDialog(Gui.this, "Wrong date format, use YYYY/MM/DD hh:mm.", "Input error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{

			public void valueChanged(ListSelectionEvent e)
			{
				boolean selected = table.getSelectedRow() >= 0;
				remove.setEnabled(selected);
				edit.setEnabled(selected);
			}
		});

		remove.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				int row = table.getSelectedRow();
				if(row >= 0 && row < orders.size())
				{
					Order order = (Order)orders.remove(row);
					items.fireTableRowsDeleted(row, row);
					IGoal[] goals = Gui.this.agent.getGoalbase().getGoals(goalname);
					for(int i = 0; i < goals.length; i++)
					{
						if(order.equals(goals[i].getParameter("order").getValue()))
						{
							goals[i].drop();
							break;
						}
					}
				}
			}
		});

		final InputDialog edit_dialog = new InputDialog(buy);
		edit.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				int row = table.getSelectedRow();
				if(row >= 0 && row < orders.size())
				{
					Order order = (Order)orders.get(row);
					edit_dialog.title.setText(order.getTitle());
					edit_dialog.limit.setText(Integer.toString(order.getLimit()));
					edit_dialog.start.setText(Integer.toString(order.getStartPrice()));
					edit_dialog.deadline.setText(dformat.format(order.getDeadline()));

					while(edit_dialog.requestInput())
					{
						try
						{
							String title = edit_dialog.title.getText();
							int limit = Integer.parseInt(edit_dialog.limit.getText());
							int start = Integer.parseInt(edit_dialog.start.getText());
							Date deadline = dformat.parse(edit_dialog.deadline.getText());
							order.setTitle(title);
							order.setLimit(limit);
							order.setStartPrice(start);
							order.setDeadline(deadline);
							items.fireTableDataChanged();
							IGoal[] goals = Gui.this.agent.getGoalbase().getGoals(goalname);
							for(int i = 0; i < goals.length; i++)
							{
								if(order.equals(goals[i].getParameter("order").getValue()))
								{
									goals[i].drop();
									break;
								}
							}
							IGoal goal = Gui.this.agent.createGoal(goalname);
							goal.getParameter("order").setValue(order);
							Gui.this.agent.dispatchTopLevelGoal(goal);
							break;
						}
						catch(NumberFormatException e1)
						{
							JOptionPane.showMessageDialog(Gui.this, "Price limit must be integer.", "Input error", JOptionPane.ERROR_MESSAGE);
						}
						catch(ParseException e1)
						{
							JOptionPane.showMessageDialog(Gui.this, "Wrong date format, use YYYY/MM/DD hh:mm.", "Input error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		});

		refresh();
		pack();
		setLocation(SGUI.calculateMiddlePosition(this));
		setVisible(true);
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				agent.killAgent();
			}
		});
	}

	//-------- methods --------

	/**
	 * Method to be called when goals may have changed.
	 */
	public void refresh()
	{
		// Use invoke later as refresh() is called from plan thread.
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				Order[]	aorders = (Order[])agent.getBeliefbase().getBeliefSet("orders").getFacts();
				for(int i = 0; i < aorders.length; i++)
				{
					if(!orders.contains(aorders[i]))
					{
						orders.add(aorders[i]);
					}
				}
				items.fireTableDataChanged();
			}
		});
	}
	
	/**
	 *  Refresh the details panel.
	 */
	public void refreshDetails()
	{
		int sel = table.getSelectedRow();
		if(sel!=-1)
		{
			Order order = (Order)orders.get(sel);
			IExpression exp = agent.getExpressionbase().getExpression("search_reports");
			List res = (List)exp.execute("$order", order);
			//for(int i=0; i<res.size(); i++)
			//	System.out.println(""+i+res.get(i));
			
			while(detailsdm.getRowCount()>0)
				detailsdm.removeRow(0);
			for(int i=0; i<res.size(); i++)
				detailsdm.addRow(new Object[]{res.get(i)});
				//System.out.println(""+i+res.get(i));
		}
	}

	//-------- inner classes --------

	/**
	 *  The input dialog.
	 */
	private class InputDialog extends JDialog
	{
		private JComboBox orders = new JComboBox();
		private JTextField title = new JTextField(20);
		private JTextField limit = new JTextField(20);
		private JTextField start = new JTextField(20);
		private JTextField deadline = new JTextField(dformat.format(new Date(System.currentTimeMillis() + 300000)));
		private boolean aborted;

		InputDialog(boolean buy)
		{
			super(Gui.this, addorderlabel, true);

			if(buy)
			{
				orders.addItem(new Order("All about agents", new Date(), 100, 120, buy));
				orders.addItem(new Order("All about web services", new Date(), 40, 60, buy));
				orders.addItem(new Order("Harry Potter", new Date(), 5, 10, buy));
				orders.addItem(new Order("Agents in the real world", new Date(), 30, 65, buy));
			}
			else
			{
				orders.addItem(new Order("All about agents", new Date(), 130, 110, buy));
				orders.addItem(new Order("All about web services", new Date(), 50, 30, buy));
				orders.addItem(new Order("Harry Potter", new Date(), 15, 9, buy));
				orders.addItem(new Order("Agents in the real world", new Date(), 100, 60, buy));
			}
		
			JPanel center = new JPanel(new GridBagLayout());
			center.setBorder(new EmptyBorder(5, 5, 5, 5));
			getContentPane().add(BorderLayout.CENTER, center);

			JLabel label;
			Dimension labeldim = new JLabel("Preset orders ").getPreferredSize();
			int row = 0;
			GridBagConstraints leftcons = new GridBagConstraints(0, 0, 1, 1, 0, 0,
					GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1), 0, 0);
			GridBagConstraints rightcons = new GridBagConstraints(1, 0, GridBagConstraints.REMAINDER, 1, 1, 0,
					GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1), 0, 0);

			leftcons.gridy = rightcons.gridy = row++;
			label = new JLabel("Preset orders");
			label.setMinimumSize(labeldim);
			label.setPreferredSize(labeldim);
			center.add(label, leftcons);
			center.add(orders, rightcons);

			leftcons.gridy = rightcons.gridy = row++;
			label = new JLabel("Title");
			label.setMinimumSize(labeldim);
			label.setPreferredSize(labeldim);
			center.add(label, leftcons);
			center.add(title, rightcons);

			leftcons.gridy = rightcons.gridy = row++;
			label = new JLabel("Start price");
			label.setMinimumSize(labeldim);
			label.setPreferredSize(labeldim);
			center.add(label, leftcons);
			center.add(start, rightcons);

			leftcons.gridy = rightcons.gridy = row++;
			label = new JLabel("Price limit");
			label.setMinimumSize(labeldim);
			label.setPreferredSize(labeldim);
			center.add(label, leftcons);
			center.add(limit, rightcons);

			leftcons.gridy = rightcons.gridy = row++;
			label = new JLabel("Deadline");
			label.setMinimumSize(labeldim);
			label.setPreferredSize(labeldim);
			center.add(label, leftcons);
			center.add(deadline, rightcons);


			JPanel south = new JPanel();
			// south.setBorder(new TitledBorder(new EtchedBorder(), " Control "));
			this.getContentPane().add(BorderLayout.SOUTH, south);

			JButton ok = new JButton("Ok");
			JButton cancel = new JButton("Cancel");
			ok.setMinimumSize(cancel.getMinimumSize());
			ok.setPreferredSize(cancel.getPreferredSize());
			south.add(ok);
			south.add(cancel);

			ok.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					aborted = false;
					setVisible(false);
				}
			});
			cancel.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setVisible(false);
				}
			});

			orders.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					Order order = (Order)orders.getSelectedItem();
					title.setText(order.getTitle());
					limit.setText("" + order.getLimit());
					start.setText("" + order.getStartPrice());
				}
			});
		}

		public boolean requestInput()
		{
			this.deadline.setText(dformat.format(new Date(System.currentTimeMillis() + 300000)));
			this.aborted = true;
			this.pack();
			this.setLocation(SGUI.calculateMiddlePosition(Gui.this, this));
			this.setVisible(true);
			return !aborted;
		}
	}
}

/**
 *  The dialog for showing details about negotiations.
 * /
class NegotiationDetailsDialog extends JDialog
{
	/**
	 *  Create a new dialog. 
	 *  @param owner The owner.
	 * /
	public NegotiationDetailsDialog(Frame owner, List details)
	{
		super(owner);
		DefaultTableModel tadata = new DefaultTableModel(new String[]{"Negotiation Details"}, 0);
		JTable table = new JTable(tadata);
		for(int i=0; i<details.size(); i++)
			tadata.addRow(new Object[]{details.get(i)});
		
		JButton ok = new JButton("ok");
		ok.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				NegotiationDetailsDialog.this.setVisible(false);
				NegotiationDetailsDialog.this.dispose();
			}
		});
		JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
		south.add(ok);

		this.getContentPane().add("Center", table);
		this.getContentPane().add("South", south);
		this.setSize(400,200);
		this.setLocation(SGUI.calculateMiddlePosition(this));
		this.setVisible(true);
	}
}*/