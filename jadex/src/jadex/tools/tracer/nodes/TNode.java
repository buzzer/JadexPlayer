/*
 * TNode.java
 * Copyright (c) 2004 by University of Hamburg. All Rights Reserved.
 * Departament of Informatics. 
 * Distributed Systems and Information Systems.
 *
 * Created by 9walczak on Nov 10, 2004.  
 * Last revision $Revision: 3613 $ by:
 * $Author: walczak $ on $Date: 2005-11-23 15:16:11 +0000 (Wed, 23 Nov 2005) $.
 */
package jadex.tools.tracer.nodes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.tree.TreeNode;

import com.touchgraph.graphlayout.Node;
import com.touchgraph.graphlayout.TGPanel;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.tools.ontology.OTrace;
import jadex.tools.tracer.ui.LookAndFeel;

/** It is a base class for all nodes in the trace GUI
 * <code>TNode</code>
 * @since Nov 10, 2004
 */
public abstract class TNode extends Node implements TreeNode
{
  /** <code>TOP</code> */
  public final static String TOP = "top";

  /** <code>trace</code> a reference to the ontology object */
  public final OTrace        trace;

  /** <code>causes</code> a string representing a list of causes */
  protected final String     causes;

  /** <code>carray</code>: an array of causes */
  protected final String []  carray;

  /** <code>agent</code>: the agent this node belongs to */
  protected final AgentIdentifier        aid;

  /** <code>seq</code> the sequence nummber */
  protected final long       seq;

  /** <code>time</code> in millis as String */
  protected final String     time;

  /** <code>date</code> the date */
  protected final Date       date;

  /** <code>id</code> the id of this node */
  protected final String     name;

  /** <code>dark</code> a darker color for this node */
  protected final Color      darkColor;

  /** <code>listeners</code>: listen to this node */
  protected final List  listeners;

  /** <code>tooltip</code>  */
  protected String           tooltip;

  /**
   * Constructor: <code>TNode</code>.
   */
  public TNode()
  {
    this(null, TOP, null);
  }

  /**
   * Constructor: <code>TNode</code>.
   * 
   * @param aid
   */
  public TNode(AgentIdentifier aid)
  {
    this(aid, aid.getName(), TOP);
  }

  /**
   * Constructor: <code>TNode</code>.
   * 
   * @param aid
   * @param id
   * @param cause
   */
  public TNode(AgentIdentifier aid, final String id, String cause)
  {
    super(id);
    setType(getType());
    setBackColor(getBackColor());
    this.seq = 0;
    this.date = new Date(System.currentTimeMillis());
    this.time = LookAndFeel.TIME.format(date);
    this.aid = aid;
    this.trace = null;
    this.carray = new String[] {cause};
    this.causes = cause;
    this.name = id;
    this.darkColor = getColor().darker().darker();

    if (id.length()>LookAndFeel.MAX_LABEL_LENGTH)
    {
      setLabel(id.substring(0, LookAndFeel.MAX_LABEL_LENGTH));
    }

    this.listeners = new Vector();
  }

  /** Default
   * Constructor: <code>TNode</code>.
   * @param aid
   * 
   * @param trace
   */
  public TNode(AgentIdentifier aid, OTrace trace)
  {
    super(Double.toString(Math.random()));
    setLabel(trace.getName());
    setType(getType());
    setBackColor(getBackColor());
    this.aid = aid;
    this.seq = Long.decode(trace.getSeq()).longValue();
    long millis = Long.decode(trace.getTime()).longValue();
    this.date = new Date(millis);
    this.time = LookAndFeel.TIME.format(date);

    this.trace = trace;
    this.causes = trace.getCause();
    this.carray = getCausesArray(this.causes);

    this.name = trace.getName();
    this.darkColor = getColor().darker().darker();

    if (name.length()>LookAndFeel.MAX_LABEL_LENGTH)
    {
      setLabel(name.substring(0, LookAndFeel.MAX_LABEL_LENGTH));
    }

    tooltip = getToolTip();

    listeners = new Vector();
  }

  /** Tokenize the causes string
   * @param causes
   * @return an array of causes
   */
  protected static String [] getCausesArray(String causes)
  {
    ArrayList cs = new ArrayList();
    StringTokenizer st = new StringTokenizer(causes, ",");
    while(st.hasMoreTokens())
    {
      String cause = st.nextToken().trim();
      if (cause.length()>0) cs.add(cause);
    }
    return (String [])cs.toArray(new String[cs.size()]);
  }

  /** 
   * @return the causes as a string separeted by ,
   */
  public String getCauses()
  {
    return causes;
  }

  /**  
   * @return the time this node was created as String
   */
  public String getTime()
  {
    return time;
  }

  /** 
   * @return the aid of the corresponding agent
   */
  public AgentIdentifier getAID()
  {
    return aid;
  }

  /** 
   * @return the name of this node
   */
  public String getName()
  {
    return name;
  }

  /** 
   * @return the icon for this node
   */
  public abstract ImageIcon getIcon();

  /** 
   * @return the color of this node
   */
  public abstract Color getColor();

  /** 
   * @return the type of the given trace node (like: data, GOAL, ..)
   */
  public abstract String getTraceType();

  /** Tells if this node represents a write or read event
   * @return by default false
   */
  public boolean isBeliefAccess()
  {
    return false;
  }

  /** Tells if this node represents a send or receive event
   * @return by default false
   */
  public boolean isCommunication()
  {
    return false;
  }

  /** Sets the label but with maximum length
   * @param lbl 
   * @see com.touchgraph.graphlayout.Node#setLabel(java.lang.String)
   */
  public void setLabel(String lbl)
  {
    if (lbl.length()>LookAndFeel.MAX_LABEL_LENGTH)
    {
      super.setLabel(lbl.substring(1, LookAndFeel.MAX_LABEL_LENGTH).trim());
    }
    else
    {
      super.setLabel(lbl.trim());
    }
  }

  // --------------------------- the api for touchgraph -------------

  /** Return the background color of this Node as a Color.
   * @return return the background color of this node in trace graph
   */
  public Color getBackColor()
  {
    return getColor();
  }

  /** paints an icon 
   * @param g
   */
  public void paintIcon(Graphics g) 
  {
    Image img=getIcon().getImage();
    int w=img.getWidth(null);
    int h=img.getHeight(null);
    int x=(int)(drawx-w/2);
    int y=(int)(drawy-h/2);
    
    g.drawImage(img, x, y, null);
  }
  
  /** Paints the Node. 
   * @param g
   * @param tgPanel
   */
  public void paint(Graphics g, TGPanel tgPanel)
  {
    if ( !intersects(tgPanel.getSize())) return;
    g.setFont(font);
    fontMetrics = g.getFontMetrics();

    int ix = (int)drawx;
    int iy = (int)drawy;
    int h = getHeight()+1;
    int w = getWidth()+1;
    int h2 = h/2;
    int w2 = w/2;
    int r = h2+1; // arc radius

    if (this==tgPanel.getSelect())
    {
      g.setColor(LookAndFeel.SELECT_COLOR);
    }
    else
    {
      g.setColor(LookAndFeel.GRAPH_COLOR);
    }
    fillFigure(g, ix-w2, iy-h2, w, h, r);

    if (this==tgPanel.getDragNode())
    {
      g.setColor(LookAndFeel.DRAG_COLOR);
    }
    else if (this==tgPanel.getMouseOverN())
    {
      g.setColor(LookAndFeel.HIGHLIGHT);
    }
    else
    {
      g.setColor(getColor());
    }
    drawFigure(g, ix-w2, iy-h2, w, h, r);

    g.setColor(darkColor);
    g.drawString(lbl, ix-fontMetrics.stringWidth(lbl)/2+1, iy+fontMetrics.getDescent()+3);

  }

  /** Fill the figure representing this object
   * @param g
   * @param x
   * @param y
   * @param w
   * @param h
   * @param r
   */
  protected void fillFigure(Graphics g, int x, int y, int w, int h, int r)
  {
    g.fillRoundRect(x, y, w, h, r, r);
  }

  /** Draw the figure representing this object
   * @param g
   * @param x
   * @param y
   * @param w
   * @param h
   * @param r
   */
  protected void drawFigure(Graphics g, int x, int y, int w, int h, int r)
  {
    g.drawRoundRect(x, y, w, h, r, r);
  }

  // ------------------------ swing tree node interface --------------------

  /** <code>children</code> all descendants of this node */
  protected final Vector    children = new Vector();

  /** <code>parents</code> the parents of this node */
  protected final List      parents  = new Vector();

  /** 
   * @return the children of this node
   */
  public TNode[] getChildren()
  {
    synchronized(children)
    {
      return (TNode[])children.toArray(new TNode[children.size()]);
    }
  }

  /** 
   * @return the parents of this node
   */
  public TNode[] getParents()
  {
    synchronized(parents)
    {
      return (TNode[]) parents.toArray(new TNode[parents.size()]);
    }
  }

  ////////////////// swing.tree.TreeNode /////////////////////
  
  /** 
   * @return 
   * @see javax.swing.tree.TreeNode#children()
   */
  public Enumeration children()
  {
    return children.elements();
  }

  /** 
   * @return true
   * @see javax.swing.tree.TreeNode#getAllowsChildren()
   */
  public boolean getAllowsChildren()
  {
    return true;
  }

  /** 
   * @param childIndex
   * @return 
   * @see javax.swing.tree.TreeNode#getChildAt(int)
   */
  public TreeNode getChildAt(int childIndex)
  {
    return (TreeNode)children.get(childIndex);
  }

  /**
   * @return 
   * @see javax.swing.tree.TreeNode#getChildCount()
   */
  public int getChildCount()
  {
    return children.size();
  }

  /** 
   * @param node
   * @return 
   * @see javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
   */
  public int getIndex(TreeNode node)
  {
    return children.indexOf(node);
  }

  /** 
   * @return 
   * @see javax.swing.tree.TreeNode#getParent()
   */
  public TreeNode getParent()
  {
    if (parents.isEmpty()) return null;
    return (TreeNode)parents.get(0);
  }

  /** 
   * @return 
   * @see javax.swing.tree.TreeNode#isLeaf()
   */
  public boolean isLeaf()
  {
    return children.isEmpty();
  }

  // -------------------------- ui methods ----------------------

  /** 
   * @return 
   * @see java.lang.Object#toString()
   */
  public String getToolTip()
  {
    if (tooltip==null)
    {
      StringBuffer buf = new StringBuffer();

      if (trace!=null)
      {
        buf.append("<HTML><TABLE>");

        appendLine(buf, getTraceType().toUpperCase(), name);

        appendLine(buf, "Value", trace.getValue());

        appendRest(buf);

        buf.append("</TABLE></HTML>");
      }
      else
      {
        buf.append(TOP);
      }

      return buf.toString();
    }

    return tooltip;
  }

  // ---------------------- helpful utils ---------------------------------  

  /** appends aid, seq, causes, date, thread, stack
   * @param buf
   */
  protected void appendRest(StringBuffer buf)
  {
    if (aid!=null)
    {
      appendLine(buf, "Agent", aid.getName());
    }

    appendLine(buf, "Seq", trace.getSeq());

    appendLine(buf, "Causes", causes);

    appendLine(buf, "Date", date.toString());

    appendLine(buf, "Thread", trace.getThread());

    appendStack(buf, LookAndFeel.STACK_DEPTH);
  }

  /** 
   * @param buf
   * @param name
   * @param value
   */
  protected static void appendLine(StringBuffer buf, String name, String value)
  {
    buf.append("<TR><TD><B>");
    buf.append(name);
    buf.append("</B></TD><TD>=</TD><TD>");
    escape(buf, value, false);
    buf.append("</TD></TR>");
  }

  /** 
   * @param buf
   * @param value
   * @param qt
   */
  protected static void escape(StringBuffer buf, String value, boolean qt)
  {
    if (value==null) return;
    char[] chrs=value.toCharArray();
    for(int i=0; i<chrs.length; i++) {
      switch(chrs[i]) {
      case '<' : buf.append("&lt;"); break;
      case '>' : buf.append("&gt;"); break;
      case '&' : buf.append("&amp;"); break;
      case ' ' : buf.append("&nbsp;"); break;
      case '\n': if (qt) {
        buf.append("<br>"); 
        break;
      }
      default:
        buf.append(chrs[i]);
      }
    }
  }

  /** Appends a stack to a tooltip 
   * @param buffer
   * @param max
   */
  protected void appendStack(StringBuffer buffer, int max)
  {
    StringBuffer buf = new StringBuffer();
    buf.append("<TR valign=\"top\"><TD><B>Stack</B></TD><TD>=</TD><TD><TABLE>");

    int lines = 0;
    String se[] = trace.getStack();
    for(int i = 0; i<se.length&&lines<max; i++)
    {
      String line = se[i];

      boolean black = !(line.startsWith("java.")
                ||line.startsWith("jade.")
                ||line.startsWith("jadex.")
                ||line.startsWith("sun.reflect.")
                ||line.startsWith("javax."))
          ||line.startsWith("jadex.examples.");

      if (black||lines>0)
      {
        buf.append("<TR><TD>");
        if ( !black) buf.append("<I>");
        buf.append(line);
        lines++;
        if ( !black) buf.append("</I>");
        buf.append("<TD></TR>");
      }
    }
    buf.append("</TABLE></TD></TR>");
    if (lines>0) buffer.append(buf);
  }

  // ------------------ child ------- parents routines --------------  

  /** 
   * @param child
   * @return true if the node was added as child
   */
  protected synchronized boolean addChild(TNode child)
  {
    // prevent cycles
    if (child==null||child==this) return false;
    if (hasGrandparent(child)) return false;
    if ( !children.contains(child))
    {
      children.add(child);
      return true;
    }
    return false;
  }

  /** removes a node from the child list
   * @param node
   * @return true if child was removed
   */
  private  int removeChild(TNode node)
  {
    // System.out.println("Stub: TAgent.removeChild");
    final int ci;
    synchronized(this)
    {
      ci = children.indexOf(node);
      if (ci>=0) children.remove(ci);
    }
    
    return ci;
  }

  /**  
   * @param child
   * @return true if the node is found on the way towards leafs
   */
  private boolean hasGrandchild(TNode child)
  {
    if (children.contains(child)) return true;
    TNode[] cs=getChildren();
    int i = cs.length;
    while(i-->0)
    {
      TNode node = cs[i];
      if (node.hasGrandchild(child)) return true;
    }
    return false;
  }

  /** 
   * @param node
   * @return the index of child or -1
   */
  public int childIndex(TNode node)
  {
    return children.indexOf(node);
  }

  /**  
   * @param node
   * @return true if this parent was added
   */
  public boolean addParent(TNode node)
  {
    // !sanity
    if (node==null||node==this) return false;
    // !dont link to a grand parent
    if (hasGrandparent(node)) return false;
    // !cyclic
    if (hasGrandchild(node)) return false;

    // unlink
    removeGrandparentsOf(node);

    boolean fire=false;

    synchronized(this)
    {
      if (node.addChild(this))
      {
        parents.add(node);
        
        this.setLocation(node.getLocation());
        fire = true;
      }
    }

    if (fire) {
      fireParentAdded(node);
      node.fireChildAdded(this);
    }
    return fire;
  }

  /** unlinks this node from grandparents of (parent).
   * Assures a spanning tree
   * @param node
   */
  protected  void removeGrandparentsOf(TNode node)
  {
    TNode[] ps=getParents();
    int i = ps.length;
    while(i-->0)
    {
      TNode p = ps[i];
      if (node.hasGrandparent(p))
      {
        removeParent(p);
      }
    }
  }

  /** 
   * @param parent
   * @return true if the node is found on the way towards root
   */
  public boolean hasGrandparent(TNode parent)
  {
    //System.out.println("Stub: TNode.hasGrandparent");
    if (parents.contains(parent)) return true;
    TNode[] ps=getParents();
    int i = ps.length;
    while(i-->0)
    {
      TNode node = ps[i];
      if (node.hasGrandparent(parent)) return true;
    }
    return false;
  }

  /** removes a node from the parent list
   * @param node
   */
  public void removeParent(TNode node)
  {
    // System.out.println("Stub: TNode.removeParent");
    boolean fire = false;
    final int pi;
    final int ci;
    synchronized(this)
    {
      pi = parents.indexOf(node);
      if (pi>=0)
      {
        ci=node.removeChild(this);
        if (ci>=0)
        {
          parents.remove(pi);
          fire = true;
        }
      } else {
        ci=-1;
      }
    }

    if (fire) {
      fireParentRemoved(node, pi);
      node.fireChildRemoved(this, ci);
    }
  }

  /** 
   * @return true if this node has parents
   */
  public boolean hasParents()
  {
    return !parents.isEmpty();
  }

  /** 
   * @return true if node has children
   */
  public boolean hasChildren()
  {
    return !children.isEmpty();
  }

  /**  
   * @return an iterator over the parents of this node
   */
  public Iterator parents()
  {
    return parents.iterator();
  }

  /** 
   * @return the label of this node
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return lbl;
  }

  /**  
   * @return the sequence this node came in
   */
  public long getSeq()
  {
    return seq;
  }

  /**  
   * @return an array of String's containing the IDs of causes
   */
  public String [] getCausesArray()
  {
    return carray;
  }

  /** 
   * @param cause
   * @return true if the node has given cause
   */
  public boolean hasCause(String cause)
  {
    if (carray!=null)
    {
      int i = carray.length;
      while(i-->0)
      {
        if (carray[i]!=null&&carray[i].equals(cause)) return true;
      }
    }
    return false;
  }

  /** Adds a listener to this node
   * @param l
   */
  public  void addListener(TNodeListener l)
  {
    if ( !listeners.contains(l))
    {
      listeners.add(l);
    }
  }

  /** Removes a listener from this node 
   * @param l
   */
  public  void removeListener(TNodeListener l)
  {
    listeners.remove(l);
  }

  /** Fires a node added event and hands it out to the listeners
   * @param node
   */
  public  void fireChildAdded(TNode node)
  {
    int i = listeners.size();
    while(i-->0)
    {
      TNodeListener l = getListener(i);
      l.childAdded(this, node);
    }
  }

  /** Fires a node removed event and hands it out to the listeners
   * @param node
   * @param ci
   */
  public  void fireChildRemoved(TNode node, int ci)
  {
    int i = listeners.size();
    while(i-->0)
    {
      TNodeListener l = getListener(i);
      if (l!=null) l.childRemoved(this, node, ci);
    }
  }

  /** Fires a node added event and hands it out to the listeners
   * @param node
   */
  public  void fireParentAdded(TNode node)
  {
    int i = listeners.size();
    while(i-->0)
    {
      TNodeListener l = getListener(i);
      if (l!=null) l.childAdded(this, node);
    }
  }

  /** Fires a node removed event and hands it out to the listeners
   * @param node
   * @param pi
   */
  public  void fireParentRemoved(TNode node, int pi)
  {
    int i = listeners.size();
    while(i-->0)
    {
      TNodeListener l = getListener(i);
      if (l!=null) l.parentRemoved(this, node, pi);
    }
  }

  /** Fire node changed
   * 
   */
  public void fireNodeChanged()
  {
    int i = listeners.size();
    while(i-->0)
    {
      TNodeListener l = getListener(i);
      if (l!=null) l.nodeChanged(this);
    }
  }

  /** Fires node deleted event.
   * 
   */
  public void deleted()
  {
    int i = listeners.size();
    while(i-->0)
    {
      TNodeListener l = getListener(i);
      if (l!=null) l.nodeDeleted(this);
    }
  }

/** 
 * @param i
 * @return
 */
private TNodeListener getListener(int i)
{
	try {
		return (TNodeListener)listeners.get(i);
	} catch(Exception e) {
		return null;
	}
}

  /** 
   * @return true if any of this node parents are visible
   */
  public boolean hasVisibleParents()
  {
    TNode[] ps=getParents();
    int i = ps.length;
    while(i-->0)
    {
      if (ps[i].isVisible()) return true;
    }
    return false;
  }

  /** 
   * @return true if any of this beliefs node children are visible
   */
  public boolean hasVisibleChildren()
  {
    TNode[] cs=getChildren();
    int i = cs.length;
    while(i-->0)
    {
      if (cs[i].isVisible()) return true;
    }
    return false;
  }

  /** 
   * @return false
   */
  public boolean isRoot()
  {
    return false;
  }

  /** 
   * @return false
   */
  public boolean isAgent()
  {
    return false;
  }

}

/*  
 * $Log$
 * Revision 1.3  2005/11/23 15:16:11  walczak
 * Polished the agent filter menu/dialog.
 *
 * Revision 1.2  2005/11/21 19:24:42  walczak
 * *** empty log message ***
 *
 * Revision 1.1  2005/08/02 15:13:11  braubach
 * alpha version of new platform independent tracer
 *
 * Revision 1.2  2005/05/26 15:41:18  9walczak
 * Fixed a deathlock between AWT and TTGPanel.fireAfterMove().
 *
 * Revision 1.1  2005/04/26 13:45:49  pokahr
 * *** empty log message ***
 *
 * Revision 1.15  2005/03/17 15:08:20  9walczak
 * Tested for multithreading. Major fixes.
 *
 * Revision 1.14  2005/02/07 17:54:56  9walczak
 * LookAndFeel.
 *
 * Revision 1.11  2005/02/07 14:59:12  9walczak
 * Added: show icons feature  to the graph view.
 *
 * Revision 1.10  2005/02/05 22:03:42  9walczak
 * Created new Ontology. Removed sync methods.
 * Made all filters positive. User may specify filter from the gui.
 * Many fixes.
 *
 * Revision 1.7  2005/01/27 18:57:59  9walczak
 * Minor variation.
 *
 * Revision 1.6  2005/01/27 09:24:17  9walczak
 * minor fixes
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
 * Revision 1.1.2.4  2004/12/22 17:36:39  9walczak
 * *** empty log message ***
 *
 * Revision 1.1.2.3  2004/12/13 17:48:23  9walczak
 * Bound the menu items and added collapse beliefs option.
 *
 * Revision 1.1.2.2  2004/11/30 12:37:04  9walczak
 * Added tooltips to the graph.
 * Added selection highlight.
 * Added mousewheel zoom.
 * Added death agent icon.
 *
 * Revision 1.1.2.1  2004/11/24 18:02:50  9walczak
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