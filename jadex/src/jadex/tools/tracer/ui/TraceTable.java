package jadex.tools.tracer.ui;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.tools.tracer.nodes.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;

/** 
 * <code>TraceTable</code>
 * @since Nov 17, 2004
 */
public class TraceTable extends JTable implements MouseListener, TNodeListener
{

  /** <code>popup</code> rightclick */
  protected final JPopupMenu      popup;

  /** <code>ui</code> uplink */
  protected final TracerUI        ui;

  /** <code>autoScroll</code>: causes new traces to appear at bottom of the table */
  protected boolean               autoScroll = true;

  /** <code>model</code>: the model for the table */
  protected final TraceTableModel model;

  /** 
   * Constructor: <code>TraceTable</code>.
   * @param model
   * @param ui
   */
  public TraceTable(TraceTableModel model, TracerUI ui)
  {
    super(model);
    this.model = model;
    this.ui = ui;
    setDefaultRenderer(String.class, new TraceTableRenderer());
    TableColumnModel cm = getColumnModel();

    Dimension d = new Dimension(420, 100);
    setMinimumSize(d);
    setPreferredScrollableViewportSize(d);

    cm.getColumn(TraceTableModel.SEQ).setPreferredWidth(10);
    cm.getColumn(TraceTableModel.AGENT).setPreferredWidth(50);
    cm.getColumn(TraceTableModel.NAME).setPreferredWidth(40);
    cm.getColumn(TraceTableModel.VALUE).setPreferredWidth(200);
    cm.getColumn(TraceTableModel.CAUSE).setPreferredWidth(80);
    cm.getColumn(TraceTableModel.TIME).setPreferredWidth(40);

    setSelectionBackground(LookAndFeel.SELECT_COLOR);

    popup = new JPopupMenu();
    addMenuItems(popup);
    addMouseListener(this);
  }

  /**
   * Getter: <code>autoScroll</code>.
   * @return Returns the autoScroll.
   */
  public boolean isAutoScroll()
  {
    return autoScroll;
  }

  /**
   * Setter: <code>autoScroll</code>.
   * @param autoScroll The autoScroll to set.
   */
  public void setAutoScroll(boolean autoScroll)
  {
    this.autoScroll = autoScroll;
  }

  /** Fills a menu with items for the table 
   * @param menu
   */
  protected void addMenuItems(JPopupMenu menu)
  {
    //System.out.println("Stub: TraceTable.addMenuItems");
    menu.add(new JMenuItem(new AbstractAction("Select causes")
    {
      public void actionPerformed(ActionEvent e)
      {
        selectCauses();
      }
    }));
    menu.add(new JMenuItem(new AbstractAction("Select effects")
    {
      public void actionPerformed(ActionEvent e)
      {
        selectEffects();
      }

    }));

    menu.addSeparator();
    menu.add(new JMenuItem(new AbstractAction("Show in graph")
    {
      public void actionPerformed(ActionEvent e)
      {
        ui.graph.showTraces(getSelected(), true);
      }
    }));

    menu.add(new JMenuItem(new AbstractAction("Hide from graph")
    {
      public void actionPerformed(ActionEvent e)
      {
        ui.graph.showTraces(getSelected(), false);
      }
    }));

    menu.addSeparator();
    menu.add(new JMenuItem(new AbstractAction("Remove from table")
    {
      public void actionPerformed(ActionEvent e)
      {
        removeSelected();
      }
    }));

    menu.addSeparator();
    menu.add(new JMenuItem(new AbstractAction("Delete")
    {
      public void actionPerformed(ActionEvent e)
      {
        ui.ctrl.removeTraces(getSelected());
      }
    }));
  }

  /** 
   * <code>TraceTableRenderer</code>
   * @since Nov 17, 2004
   */
  class TraceTableRenderer extends DefaultTableCellRenderer
  {

    /** 
     * @param table
     * @param value
     * @param selected
     * @param hasFocus
     * @return this
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean hasFocus, int r, int c)
    {
      super.getTableCellRendererComponent(table, value, selected, hasFocus, r, c);

      TNode tn = (TNode)value;
      setIcon(null);
      setHorizontalAlignment(LEFT);
      if (tn!=null)
      {
        switch(c)
        {
        case TraceTableModel.SEQ:
          setHorizontalAlignment(RIGHT);
          setToolTipText(tn.getToolTip());
          setText(Long.toString(tn.getSeq()));
          break;
        case TraceTableModel.AGENT:
          AgentIdentifier aid = tn.getAID();
          setText((aid!=null) ? aid.getName() : "na");
          setToolTipText(getText());
          break;
        case TraceTableModel.NAME:
          setIcon(tn.getIcon());
          setToolTipText(tn.getTraceType()+": "+tn.getName());
          setText(tn.getName());
          break;
        case TraceTableModel.VALUE:
          if (tn instanceof TAbstractC)
          {
            setText(tn.trace.getValue());
            setToolTipText(((TAbstractC)tn).getMsg());
          }
          else if (tn.trace!=null)
          {
            setText(tn.trace.getValue());
            setToolTipText(getText());
          }
          break;
        case TraceTableModel.CAUSE:
          setText(tn.getCauses());
          setToolTipText(getText());
          break;
        case TraceTableModel.TIME:
          setText(tn.getTime());
          setToolTipText(getText());
        }
      }
      return this;
    }

  }

  /** 
   * @param node
   * @return true if traces where removed
   */
  public boolean remove(TNode node)
  {
    return model.remove(node);
  }

  /** 
   * @return a list of selected traces
   */
  public TNode[] getSelected()
  {
    return model.getTraces(getSelectedRows());
  }

  /** Removes selected traces from the table
   */
  public void removeSelected()
  {
    model.removeRows(getSelectedRows());
  }

  /** Selects effects of selected traces
   */
  public synchronized void selectEffects()
  {
    //  System.out.println("Stub: TraceTable.selectEffects");
    int sel[] = getSelectedRows();
    int i = sel.length;
    while(i-->0)
    {
      TNode tn = model.get(sel[i]);
      if (tn!=null)
      {
        TNode[] ch=tn.getChildren();
        int chI=ch.length;
        while(chI-->0)
        {
          TNode effect = ch[chI];
          int j = model.indexOf(effect);
          if (j>=0)
          {
            addRowSelectionInterval(j, j);
          }
        }
      }
    }
  }

  /** Selects causes of selected traces
   */
  public synchronized void selectCauses()
  {
    int sel[] = getSelectedRows();
    int i = sel.length;
    while(i-->0)
    {
      TNode tn = model.get(sel[i]);
      if (tn!=null)
      {
        TNode[] ps = tn.getParents();
        int pi=ps.length;
        while(pi-->0)
        {
          TNode cause = ps[pi];
          int j = model.indexOf(cause);
          if (j>=0)
          {
            addRowSelectionInterval(j, j);
          }
        }
      }
    }
  }

  /** adds the node to the table
   * @param node
   * @return true if added
   */
  public boolean addTrace(TNode node)
  {
    //  System.out.println("Stub: TraceTable.show");
    if (node.isRoot()||node.isAgent()||node instanceof TBelief) return false;
    if (model.add(node))
    {

      node.addListener(this);
      return true;
    }
    return false;
  }

  /** removes the node from the table
   * @param node
   */
  public void removeTrace(TNode node)
  {
    model.remove(node);
    node.removeListener(this);
  }

  /** 
   * @param e 
   * @see javax.swing.JTable#tableChanged(javax.swing.event.TableModelEvent)
   */
  public void tableChanged(TableModelEvent e)
  {
    super.tableChanged(e);
    if (autoScroll)
    {
      JViewport viewport = (JViewport)getParent();
      Rectangle cellRect = getCellRect(dataModel.getRowCount()-1, -1, true);
      Rectangle viewRect = viewport.getViewRect();
      cellRect.setLocation(cellRect.x-viewRect.x, cellRect.y-viewRect.y);
      cellRect.x = 0; // no horizontal scrolling
      viewport.scrollRectToVisible(cellRect);
    }

  }

  /** 
   * @param node
   * @param b
   */
  public void addEffects(TNode node, boolean b)
  {
    // System.out.println("Stub: TraceTable.showEffects");
    if (b)
    {
      int end = dataModel.getRowCount();
      showEffectsRec(node);
      ((TraceTableModel)dataModel).fireTableRowsInserted(end, dataModel.getRowCount()-1);
    }
    else
    {
      removeEffectsRec(node);
      ((TraceTableModel)dataModel).fireTableDataChanged();
    }

  }

  /** removes all effects recursive 
   * @param node
   */
  private void removeEffectsRec(TNode node)
  {
    // System.out.println("Stub: TraceTable.removeEffectsRec");
    TNode[] effects = node.getChildren();
    int i = effects.length;
    while(i-->0)
    {
      TNode eff = effects[i];
      if (model.remove(eff))
      {
        removeEffectsRec(eff);
      }
    }
  }

  /** adds effects recursive 
   * @param node
   */
  private void showEffectsRec(TNode node)
  {
    // System.out.println("Stub: TraceTable.showEffectsRec");
    TNode[] effects = node.getChildren();
    int i = effects.length;
    while(i-->0)
    {
      TNode eff = effects[i];
      if (addTrace(eff))
      {
        showEffectsRec(eff);
      }
    }
  }

  /** shows popup
   * @param x
   * @param y
   */
  private void showPopUp(int x, int y)
  {
    // System.out.println("Stub: TraceTable.showPopUp");
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

    if (e.isPopupTrigger())
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
    if (e.isPopupTrigger())
    {
      showPopUp(e.getX(), e.getY());
    }
  }

  /** no op
   * @param caller
   * @param node 
   */
  public void childAdded(TNode caller, TNode node)
  {
  // System.out.println("Stub: TraceTable.nodeAdded");
  }

  /** no op
   * @param caller
   * @param node 
   */
  public void parentAdded(TNode caller, TNode node)
  {
  // System.out.println("Stub: TraceTable.nodeAdded");
  }

  /** no op
   * @param caller 
   * @see jadex.tools.tracer.nodes.TNodeListener#nodeChanged(jadex.tools.tracer.nodes.TNode)
   */
  public void nodeChanged(TNode caller)
  {
  // System.out.println("Stub: TraceTable.nodeChanged");
  }

  /** no op 
   * @param caller 
   * @see jadex.tools.tracer.nodes.TNodeListener#nodeDeleted(jadex.tools.tracer.nodes.TNode)
   */
  public void nodeDeleted(TNode caller)
  {
    removeTrace(caller);
  }

  /** no op
   * @param caller
   * @param node
   * @param index 
   * @see jadex.tools.tracer.nodes.TNodeListener#childRemoved(jadex.tools.tracer.nodes.TNode, jadex.tools.tracer.nodes.TNode, int)
   */
  public void childRemoved(TNode caller, TNode node, int index)
  {
  //  System.out.println("Stub: TraceTable.childRemoved"); 
  }

  /** no op
   * @param caller
   * @param node
   * @param index 
   * @see jadex.tools.tracer.nodes.TNodeListener#parentRemoved(jadex.tools.tracer.nodes.TNode, jadex.tools.tracer.nodes.TNode, int)
   */
  public void parentRemoved(TNode caller, TNode node, int index)
  {
  //  System.out.println("Stub: TraceTable.parentRemoved");
  }

}

/*  
 * $Log$
 * Revision 1.3  2006/10/20 10:52:51  braubach
 * no message
 *
 * Revision 1.2  2005/11/23 15:16:11  walczak
 * Polished the agent filter menu/dialog.
 *
 * Revision 1.1  2005/08/02 15:13:11  braubach
 * alpha version of new platform independent tracer
 *
 * Revision 1.1  2005/04/26 13:45:50  pokahr
 * *** empty log message ***
 *
 * Revision 1.9  2005/03/17 15:08:19  9walczak
 * Tested for multithreading. Major fixes.
 *
 * Revision 1.8  2005/02/05 22:03:41  9walczak
 * Created new Ontology. Removed sync methods.
 * Made all filters positive. User may specify filter from the gui.
 * Many fixes.
 *
 * Revision 1.7  2005/01/28 14:34:11  braubach
 * no message
 *
 * Revision 1.6  2005/01/27 09:23:42  9walczak
 * Moved parts table data and function into TraceTableModel.
 * Added many sync attributes.
 *
 * Revision 1.5  2005/01/26 13:47:29  9walczak
 * Beta release. Many fixes.
 *
 * Revision 1.4  2005/01/11 18:29:54  9walczak
 * Addopted IntrospectorPreprocessor approach
 * to communicate with agents to be traced.
 * Replaced many tracer hooks by SystemEvents.
 *
 * Revision 1.3  2005/01/03 15:38:00  9walczak
 * Communication events can be joined by an edge,
 * if they correspond to the same message.
 * The ACLMessages are pretty printed in tooltips.
 * Minor fixes.
 *
 * Revision 1.2  2004/12/22 18:25:43  9walczak
 * First time in main version.
 *
 * Revision 1.1.2.5  2004/12/13 17:48:23  9walczak
 * Bound the menu items and added collapse beliefs option.
 *
 * Revision 1.1.2.4  2004/12/06 15:52:03  9walczak
 * Added a menu to the tracer gui.
 *
 * Revision 1.1.2.3  2004/11/30 12:37:04  9walczak
 * Added tooltips to the graph.
 * Added selection highlight.
 * Added mousewheel zoom.
 * Added death agent icon.
 *
 * Revision 1.1.2.2  2004/11/24 18:02:50  9walczak
 * Added Graph View  to the tracer GUI.
 * Belief traces are differentiated in Writes and Reads.
 *
 * Revision 1.1.2.1  2004/11/18 18:03:28  9walczak
 * Createt new version of tracer with:
 *    -proxy (java 2 jade),
 *    -tracerAgent (jade 2 gui),
 *    -gui (tree, table, [graph]).
 * Tested on blocksworld.
 *
 */