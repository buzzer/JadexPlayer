/*
 * TraceTree.java
 * Copyright (c) 2004 by University of Hamburg. All Rights Reserved.
 * Departament of Informatics. 
 * Distributed Systems and Information Systems.
 *
 * Created by 9walczak on Nov 17, 2004.  
 * Last revision $Revision: 5024 $ by:
 * $Author: pokahr $ on $Date: 2007-03-15 17:34:04 +0100 (Thu, 15 Mar 2007) $.
 */
package jadex.tools.tracer.ui;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.tools.ontology.Tracing;
import jadex.tools.tracer.nodes.*;

import java.awt.Component;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;


/** 
 * <code>TraceTree</code>
 * @since Nov 17, 2004
 */
public class TraceTree extends JTree implements MouseListener, TNodeListener
{
	/** <code>model</code> the model of this tree */
	protected final DefaultTreeModel	model;

	/** <code>popup</code> rightclick */
	protected final JPopupMenu			popup;

	/** <code>ui</code> uplink */
	protected final TracerUI			tracerUI;

	/** Init
	 * Constructor: <code>TraceTree</code>.
	 * 
	 * @param root
	 * @param ui
	 */
	protected TraceTree(final TNode root, TracerUI ui)
	{
		super(new DefaultTreeModel(root));
		root.addListener(this);

		model = (DefaultTreeModel)getModel();
		setRootVisible(false);
		setCellRenderer(new TraceCellRenderer());
		ToolTipManager.sharedInstance().registerComponent(this);

		this.tracerUI = ui;

		popup = new JPopupMenu();
		addMenuItems(popup);
		addMouseListener(this);
	}

	/** Fills a menu with items for the table 
	 * @param menu
	 */
	protected void addMenuItems(JPopupMenu menu)
	{
		menu.add(new AgentNodeButton("Observe Agent")
		{
			void set(TAgent a, boolean flag)
			{
				tracerUI.ctrl.ignoreAgent(a, !flag);
			}

			boolean get(TAgent a)
			{
				return !a.isIgnored();
			}
		});

		menu.add(getFilterMenu());


		menu.addSeparator();
		menu.add(new JMenuItem(new AbstractAction("Show in graph")
		{
			public void actionPerformed(ActionEvent e)
			{
				TNode node = (TNode)getLastSelectedPathComponent();
				if(node != null)
				{
					tracerUI.graph.showTrace(node, true);
					tracerUI.graph.showEffects(node, true);
				}
			}

			public boolean isEnabled()
			{
				return getLastSelectedPathComponent() != null;
			}
		}));

		menu.add(new JMenuItem(new AbstractAction("Hide from graph")
		{
			public void actionPerformed(ActionEvent e)
			{
				TNode node = (TNode)getLastSelectedPathComponent();
				if(node != null)
				{
					tracerUI.graph.showEffects(node, false);
					tracerUI.graph.showTrace(node, false);
				}
			}

			public boolean isEnabled()
			{
				return getLastSelectedPathComponent() != null;
			}
		}));

		menu.addSeparator();
		menu.add(new JMenuItem(new AbstractAction("Show in table")
		{
			public void actionPerformed(ActionEvent e)
			{
				TNode node = (TNode)getLastSelectedPathComponent();
				if (node!=null) {
					tracerUI.table.addTrace(node);
					tracerUI.table.addEffects(node, true);
				}
			}

			public boolean isEnabled()
			{
				return getLastSelectedPathComponent() != null;
			}
		}));

		menu.add(new JMenuItem(new AbstractAction("Hide from table")
		{
			public void actionPerformed(ActionEvent e)
			{
				TNode node = (TNode)getLastSelectedPathComponent();
				if (node!=null) {
					tracerUI.table.addEffects(node, false);
					tracerUI.table.removeTrace(node);
				}
			}

			public boolean isEnabled()
			{
				return getLastSelectedPathComponent() != null;
			}
		}));

		menu.addSeparator();

		menu.add(new JMenuItem(new AbstractAction("Delete")
		{
			public void actionPerformed(ActionEvent e)
			{
				TNode node = (TNode)getLastSelectedPathComponent();
				if(node != null)
				{
					tracerUI.ctrl.removeTrace(node);
				}
			}

			public boolean isEnabled()
			{
				return getLastSelectedPathComponent() != null;
			}
		}));

	}

	/** 
	 * @return a menu item for current selected agent
	 */
	public JMenuItem getFilterMenu()
	{
		return 	tracerUI.getAgentFilterMenu("Filter", new TracerUI.AgentFilterModel() {

			public TAgent getNode() {
				Object node = getLastSelectedPathComponent();
				if(node instanceof TAgent)
				{
					return (TAgent)node;
				}
				return null;
			}
		});
		
	}
	
	
	abstract class AgentNodeButton extends JCheckBoxMenuItem
	{
		AgentNodeButton(String text)
		{
			super(text);

			setAction(new AbstractAction(text)
			{

				public void actionPerformed(ActionEvent e)
				{
					Object node = getLastSelectedPathComponent();
					if(node instanceof TAgent)
					{
						TAgent a = (TAgent)node;
						set(a, !get(a));
						if(!a.isIgnored()) tracerUI.ctrl.ignoreAgent(a, false);
					}

				}
			});

			setModel(new DefaultButtonModel()
			{
				/**
				 * @return the specific value
				 * @see javax.swing.JCheckBoxMenuItem#getState()
				 */
				public boolean isSelected()
				{
					Object node = getLastSelectedPathComponent();
					return node instanceof TAgent && get((TAgent)node);
				}


				/**
				 * @return true if an agent node is selected
				 * @see java.awt.Component#isEnabled()
				 */
				public boolean isEnabled()
				{
					Object node= getLastSelectedPathComponent();
					 if (node instanceof TAgent)  {
						 AgentIdentifier aid = ((TAgent)node).getAID();
						 
						 return !aid.equals(tracerUI.ctrl.getAID());
					 }
					 return false;

				}
			});
		}

		abstract void set(TAgent a, boolean flag);

		abstract boolean get(TAgent a);
	}


	class TraceCellRenderer extends DefaultTreeCellRenderer
	{

		/**
		 * @param tree
		 * @param value
		 * @param sel
		 * @param expanded
		 * @param leaf
		 * @param row
		 * @param hasFocus
		 * @return the renderer seting text, icon and tooltip
		 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree,
		 *      java.lang.Object, boolean, boolean, boolean, int, boolean)
		 */
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
				boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			TNode tn = (TNode)value;

			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			if(tn.isRoot())
			{
				setText("");
			}
			else
			{
				setIcon(tn.getIcon());

				setText(tn.getName());

				setToolTipText(tn.getToolTip());
			}

			return this;
		}
	}

	/** shows popup
	 * @param x
	 * @param y
	 */
	protected void showPopUp(int x, int y)
	{
		TreePath sel = getClosestPathForLocation(x, y);
		if(sel != null)
		{
			setSelectionPath(sel);
		}

		// System.out.println("Stub: TraceTable.showPopUp");
		Component[] me = popup.getComponents();
		int i = me.length;
		while(i-- > 0)
		{
			if(me[i] instanceof JMenuItem)
			{
				JMenuItem mi = (JMenuItem)me[i];
				if(mi.getAction() != null)
				{
					mi.setEnabled(mi.getAction().isEnabled());
				}
				else
				{
					mi.setEnabled(mi.isEnabled());
				}
			}
		}

		popup.show(this, x, y);
	}

	/** nop
	 * @param e 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e)
	{
		// System.out.println("Stub: TraceTable.mouseClicked");

	}

	/** nop
	 * @param e 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e)
	{
		// System.out.println("Stub: TraceTable.mouseEntered");

	}

	/** nop
	 * @param e 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e)
	{
		//    System.out.println("Stub: TraceTable.mouseExited");

	}

	/** shows popup
	 * @param e 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e)
	{
		//  System.out.println("Stub: TraceTable.mousePressed");

		if(e.isPopupTrigger())
		{
			showPopUp(e.getX(), e.getY());
		}

	}

	/** shows popup
	 * @param e 
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e)
	{
		// System.out.println("Stub: TraceTable.mouseReleased");
		if(e.isPopupTrigger())
		{
			showPopUp(e.getX(), e.getY());
		}
	}

	/** 
	 * @param caller 
	 * @see jadex.tools.tracer.nodes.TNodeListener#nodeChanged(jadex.tools.tracer.nodes.TNode)
	 */
	public void nodeChanged(TNode caller)
	{
		model.nodeChanged(caller);
	}

	/** 
	 * @param caller 
	 * @see jadex.tools.tracer.nodes.TNodeListener#nodeDeleted(jadex.tools.tracer.nodes.TNode)
	 */
	public void nodeDeleted(TNode caller)
	{
		caller.removeListener(this);
	}

	/** inserts new node at the caller
	 * @param caller
	 * @param node 
	 * @see jadex.tools.tracer.nodes.TNodeListener#childAdded(jadex.tools.tracer.nodes.TNode, jadex.tools.tracer.nodes.TNode)
	 */
	public void childAdded(TNode caller, TNode node)
	{
		// System.out.println("Stub: TraceTree.childAdded");
		int ci = caller.childIndex(node);
		if(ci >= 0)
		{
			model.nodesWereInserted(caller, new int[]{ci});
		}
		if(caller.isRoot())
		{
			expandPath(new TreePath(caller));
		}
		node.addListener(this);
	}

	/** no op
	 * @param caller
	 * @param node 
	 * @see jadex.tools.tracer.nodes.TNodeListener#parentAdded(jadex.tools.tracer.nodes.TNode, jadex.tools.tracer.nodes.TNode)
	 */
	public void parentAdded(TNode caller, TNode node)
	{
		// System.out.println("Stub: TraceTree.parentAdded");
	}

	/** informas the model
	 * @param caller
	 * @param node
	 * @param ci - the index of a child removed
	 * @see jadex.tools.tracer.nodes.TNodeListener#childRemoved(jadex.tools.tracer.nodes.TNode, jadex.tools.tracer.nodes.TNode, int)
	 */
	public void childRemoved(TNode caller, TNode node, int ci)
	{
		if(ci >= 0)
		{
			model.nodesWereRemoved(caller, new int[]{ci}, new Object[]{node});
		}
	}

	/** no op
	 * @param caller
	 * @param node
	 * @param index 
	 * @see jadex.tools.tracer.nodes.TNodeListener#parentRemoved(jadex.tools.tracer.nodes.TNode, jadex.tools.tracer.nodes.TNode, int)
	 */
	public void parentRemoved(TNode caller, TNode node, int index)
	{
		// System.out.println("Stub: TraceTree.parentRemoved");
	}

}

/*  
 * $Log$
 * Revision 1.9  2005/11/25 13:36:41  walczak
 * allowed to show tracer in the view
 *
 * Revision 1.8  2005/11/24 18:59:20  walczak
 * Updated menues for tracer
 *
 * Revision 1.7  2005/11/23 15:16:11  walczak
 * Polished the agent filter menu/dialog.
 *
 * Revision 1.6  2005/11/22 18:19:00  walczak
 * *** empty log message ***
 *
 * Revision 1.5  2005/11/22 09:49:16  walczak
 * Added sliders for node limit
 *
 * Revision 1.4  2005/11/18 12:49:10  walczak
 * *** empty log message ***
 *
 * Revision 1.3  2005/11/16 18:13:47  walczak
 * -----------------------------------------
 *
 * Revision 1.2  2005/11/11 12:30:05  walczak
 * -----------------------------------------
 *
 *
 */