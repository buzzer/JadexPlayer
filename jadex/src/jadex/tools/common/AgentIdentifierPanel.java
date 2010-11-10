package jadex.tools.common;

import jadex.adapter.fipa.AgentIdentifier;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;

/**
 *  A panel for displaying/editing an agent identifier.
 */
public class AgentIdentifierPanel extends JPanel
{
	//-------- attributes --------

	/** The agent identifier.*/
	protected AgentIdentifier	aid;

	/** The name textfield. */
	protected JTextField	tfname; 

	/** Listener for name updates. */
	protected DocumentListener	namelistener;	
	
	/** Flag indicating that the user is currently editing the name. */
	protected boolean	nameediting;	
	
	/** The addresses table. */
	//protected JTable	taddresses;
	protected EditableList	taddresses;

	/** The editable state. */
	protected boolean	editable;	
	
	//-------- constructors --------

	/**
	 *  Create a new agent identifier panel.
	 *  @param aid	The agent identifier (or null for new).
	 */
	public AgentIdentifierPanel(AgentIdentifier aid)
	{
		this.aid	= aid!=null ? aid : new AgentIdentifier();
		this.editable	= true;

		// Constraints for labels (displayed left).
		int	row	= 0;
		GridBagConstraints	leftcons	= new GridBagConstraints(0, 0, 1, 1, 0, 0,
			GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1,1,1,1), 0, 0);
		// Constraints for components (displayed right of a label).
		GridBagConstraints	rightcons	= (GridBagConstraints)leftcons.clone();
		rightcons.gridx	= 1;
		rightcons.gridwidth	= GridBagConstraints.REMAINDER;
		rightcons.weightx	= 1;
		// Constraints for full-size components (resize in all directions).
		GridBagConstraints	fullcons	= (GridBagConstraints)rightcons.clone();
		fullcons.gridx	= 0;
		fullcons.weighty	= 1;
		fullcons.fill	= GridBagConstraints.BOTH;

		// Initialize component.
		this.setLayout(new GridBagLayout());

		// Name
		tfname	= new JTextField(this.aid.getName(), 20);
		this.namelistener	= new NameListener();
		tfname.getDocument().addDocumentListener(namelistener);
		leftcons.gridy	= rightcons.gridy	= fullcons.gridy	= row++;
		this.add(new JLabel("Name: "), leftcons);
		this.add(tfname, rightcons);

		taddresses = new EditableList("Addresses");
		taddresses.getModel().addTableModelListener(new TableModelListener()
		{
			public void tableChanged(TableModelEvent e)
			{
				//System.out.println("event: "+e);
				AgentIdentifierPanel.this.aid.setAddresses(taddresses.getEntries());
				aidChanged();
			}
		});

		JScrollPane	scroll	= new JScrollPane(taddresses);
		leftcons.gridy	= rightcons.gridy	= fullcons.gridy	= row++;
		this.add(scroll, fullcons);
	}

	/**
	 *  Template method to be overriden by subclasses.
	 *  Called when the AID has been changed through user input.
	 */
	protected void aidChanged()
	{
	}

	//-------- methods --------

	/**
	 *  Get the agent identifier.
	 */
	public AgentIdentifier	getAgentIdentifier()
	{
		return this.aid;
	}

	/**
	 *  Set the agent identifier.
	 */
	public void setAgentIdentifier(AgentIdentifier aid)
	{
		this.aid	= aid!=null ? aid : new AgentIdentifier();
		taddresses.setEntries(this.aid.getAddresses());
		refresh();
	}

	/**
	 *  Change the editable state.
	 */
	public void	setEditable(boolean editable)
	{
		if(this.editable!=editable)
		{
			this.editable	= editable;
			tfname.setEditable(editable);
			refresh();
		}
	}
	
	//-------- helper methods --------
	
	/**
	 *  Update the ui, when the aid has changed.
	 */
	protected void refresh()
	{
		// Update the gui.
		if(!nameediting)
		{
			tfname.getDocument().removeDocumentListener(namelistener);
			tfname.setText(this.aid.getName());
			tfname.getDocument().addDocumentListener(namelistener);
		}

		taddresses.refresh();
		this.invalidate();
		this.validate();
		this.repaint();
	}

	//-------- helper classes --------

	public class NameListener implements DocumentListener
	{
		public void changedUpdate(DocumentEvent e)
		{
//			System.out.println("changedUpdate");
			nameediting	= true;
			AgentIdentifierPanel.this.aid.setName(tfname.getText());
			aidChanged();
			nameediting	= false;
		}

		public void insertUpdate(DocumentEvent e)
		{
//			System.out.println("insertUpdate");
			nameediting	= true;
			AgentIdentifierPanel.this.aid.setName(tfname.getText());
			aidChanged();
			nameediting	= false;
		}

		public void removeUpdate(DocumentEvent e)
		{
//			System.out.println("removeUpdate");
			nameediting	= true;
			AgentIdentifierPanel.this.aid.setName(tfname.getText());
			aidChanged();
			nameediting	= false;
		}
	}
}
