/*
 * TraceTableModel.java
 * Copyright (c) 2004 by University of Hamburg. All Rights Reserved.
 * Departament of Informatics. 
 * Distributed Systems and Information Systems.
 *
 * Created by 9walczak on Nov 17, 2004.  
 * Last revision $Revision: 5025 $ by:
 * $Author: Alex $ on $Date: 2007-03-16 00:03:38 +0100 (Fri, 16 Mar 2007) $.
 */
package jadex.tools.tracer.ui;

import java.util.List;
import java.util.Vector;

import jadex.tools.tracer.nodes.TNode;

import javax.swing.table.AbstractTableModel;

class TraceTableModel extends AbstractTableModel
{
  /** <code>SEQ</code> */
  public final static int SEQ   = 0;

  /** <code>NAME</code>  */
  public final static int AGENT = 1;

  /** <code>NAME</code>  */
  public final static int NAME  = 2;

  /** <code>VALUE</code> */
  public final static int VALUE = 3;

  /** <code>CAUSE</code>  */
  public final static int CAUSE = 4;

  /** <code>TIME</code>  */
  public final static int TIME  = 5;

  /** <code>traces</code> contains traces to be shown in this table model */
  private final List   traces;

  /**
   * Constructor: <code>TraceTableModel</code>.
   */
  protected TraceTableModel()
  {
    this.traces = new Vector();
  }

  /** 
   * @return number of traces
   * @see javax.swing.table.TableModel#getRowCount()
   */
  public int getRowCount()
  {
    return traces.size();
  }

  /** 
   * @return 6 
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount()
  {
    return 6;
  }

  /** 
   * @param c
   * @return name of column c
   * @see javax.swing.table.TableModel#getColumnName(int)
   */
  public String getColumnName(int c)
  {
    switch(c)
    {
    case SEQ:
      return "#";
    case AGENT:
      return "Agent";
    case NAME:
      return "Name";
    case VALUE:
      return "Content";
    case CAUSE:
      return "Cause";
    case TIME:
      return "Time";
    default:
      return "?!";
    }
  }

  /** 
   * @param r
   * @param c
   * @return the trace at row r or null
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public synchronized Object getValueAt(int r, int c)
  {
    if (r>=0&&r<traces.size()) { return (TNode)traces.get(r); }
    return null;
  }

  /** 
   * @param c
   * @return String.class
   * @see javax.swing.table.TableModel#getColumnClass(int)
   */
  public Class getColumnClass(int c)
  {
    return String.class;
  }

  /** 
   * @param rows - the indices of selected nodes
   * @return a list of selcted nodes
   */
  public synchronized TNode[] getTraces(int [] rows)
  {
    int i = rows.length;
    TNode[] ts = new TNode[i];
    while(i-->0)
    {
      int s = rows[i];
      if (s>=0&&s<traces.size())
      {
        ts[i]=(TNode)traces.get(rows[i]);
      }
    }
    return ts;
  }

  /** 
   * @param node
   * @return true if removed
   */
  public boolean remove(TNode node)
  {
    boolean fire = false;
    final int row;
    synchronized(this)
    {
      row = traces.indexOf(node);
      if (row>=0)
      {
        traces.remove(node);
        fire = true;
      }
    }
    if (fire)  fireTableRowsDeleted(row, row);
    return fire;
  }

  /** 
   * @param node
   * @return true if added
   */
  public  boolean add(TNode node)
  {
    boolean fire=false;
    final int row;
    synchronized(this)
    {
      if ( !traces.contains(node))
      {
        row = traces.size();
        traces.add(node);
        fire = true;
      } else {
        row = -1;
      }
    }
    if (fire) fireTableRowsInserted(row, row);
    return fire;
  }

  /** removes rows specified by the array sel
   * @param sel
   */
  public void removeRows(int [] sel)
  {
    synchronized(this)
    {
      int i = sel.length;
      while(i-->0)
      {
        traces.remove(sel[i]);
      }
    }
    fireTableDataChanged();
  }

  /**
   *  Get all traces.
   */
  public TNode[]	getAllTraces()
  {
	 synchronized(this)
	 {
		 return (TNode[])traces.toArray(new TNode[traces.size()]);
	 }
  }
  
  /** 
   * @param i
   * @return a node or null
   */
  public synchronized TNode get(int i)
  {
    if (i>=0&&i<traces.size()) { return (TNode)traces.get(i); }
    return null;
  }

  /** 
   * @param effect
   * @return the index or -1
   */
  public int indexOf(TNode effect)
  {
    return traces.indexOf(effect);
  }

}

/*  
 * $Log$
 * Revision 1.2  2005/11/23 15:16:11  walczak
 * Polished the agent filter menu/dialog.
 *
 * Revision 1.1  2005/08/02 15:13:11  braubach
 * alpha version of new platform independent tracer
 *
 * Revision 1.1  2005/04/26 13:45:50  pokahr
 * *** empty log message ***
 *
 * Revision 1.8  2005/03/17 15:08:19  9walczak
 * Tested for multithreading. Major fixes.
 *
 * Revision 1.7  2005/02/05 22:03:41  9walczak
 * Created new Ontology. Removed sync methods.
 * Made all filters positive. User may specify filter from the gui.
 * Many fixes.
 *
 * Revision 1.6  2005/01/28 14:34:11  braubach
 * no message
 *
 * Revision 1.5  2005/01/27 09:23:42  9walczak
 * Moved parts table data and function into TraceTableModel.
 * Added many sync attributes.
 *
 * Revision 1.4  2005/01/26 13:47:29  9walczak
 * Beta release. Many fixes.
 *
 * Revision 1.3  2005/01/11 18:29:54  9walczak
 * Addopted IntrospectorPreprocessor approach
 * to communicate with agents to be traced.
 * Replaced many tracer hooks by SystemEvents.
 *
 * Revision 1.2  2004/12/22 18:25:43  9walczak
 * First time in main version.
 *
 * Revision 1.1.2.3  2004/12/22 17:36:39  9walczak
 * *** empty log message ***
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