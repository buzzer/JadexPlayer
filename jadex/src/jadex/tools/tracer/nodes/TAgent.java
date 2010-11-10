/*
 * TAgent.java
 * Copyright (c) 2004 by University of Hamburg. All Rights Reserved.
 * Departament of Informatics. 
 * Distributed Systems and Information Systems.
 *
 * Created by 9walczak on Nov 17, 2004.  
 * Last revision $Revision: 5024 $ by:
 * $Author: pokahr $ on $Date: 2007-03-15 17:34:04 +0100 (Thu, 15 Mar 2007) $.
 */
package jadex.tools.tracer.nodes;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.tools.ontology.Tracing;
import jadex.tools.tracer.ui.LookAndFeel;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.ImageIcon;

import com.touchgraph.graphlayout.TGPanel;

/** 
 * <code>TAgent</code>
 * @since Nov 17, 2004
 */
public class TAgent extends TNode implements TNodeListener
{
   /** <code>hap</code> hap of this agent */
   protected final String hap;

   /** <code>hap</code> hap of this agent */
   protected final String local;

   /** <code>alive</code> indicates if this agent is death or alive */
   protected boolean      alive;

   /** <code>ignored</code>  says that no observations for this agent wil be stored */
   protected boolean      ignored;

   // ------------ stores all traces of an agent -------------

   /** <code>id2node</code>: maps string id of traces to their nodes */
   protected final Map    id2node;

   /** <code>traces</code> all traces from this agent */
   protected final List   traces;

   /** <code>beliefs</code> stores the traces belonging to a belief */
   protected final Map    beliefs;

   /** <code>enforceNodeLimit</code>: used to enforce the limit of nodes per agent */
   private boolean        enforceNodeLimit;

   /** <code>nodes_limit</code>: */
   private int            nodes_limit;

   private Tracing        tracing          = new Tracing();

   // ------------------------------------------

   /** 
    * Constructor: <code>TAgent</code>.
    * @param aid
    */
   public TAgent(AgentIdentifier aid)
   {
      super(aid);
      String addrs[] = aid.getAddresses();
      if (addrs.length > 0)
      {
         String addr = addrs[0];
         int dp = addr.indexOf('@');
         if (dp > 0)
         {
            local = addr.substring(0, dp);
            hap = addr.substring(dp + 1);
         }
         else
         {
            local = aid.getName();
            hap = "";
         }
      }
      else
      {
         local = aid.getName();
         hap = "";
      }
      alive = true;
      ignored = false;

      id2node = new HashMap();
      traces = new Vector();
      beliefs = new HashMap();
   }

   /** copies all filtering info
    * @param proto
    */
   public void copyFilters(TAgent proto)
   {
      enforceNodeLimit = proto.enforceNodeLimit;
      nodes_limit = proto.nodes_limit;
      ignored = proto.ignored;
      tracing = new Tracing(proto.tracing);
   }

   /** 
    * @return the icon for current agent state
    * @see jadex.tools.tracer.nodes.TNode#getIcon()
    */
   public ImageIcon getIcon()
   {
      if (!alive) return LookAndFeel.DAGENT_ICON;
      if (ignored) return LookAndFeel.IGNORED_AGENT_ICON;
      if (tracing.isTracing()) return LookAndFeel.WATCHED_AGENT_ICON;
      return LookAndFeel.AGENT_ICON;
   }

   /** 
    * @return the color for current agent state
    * @see jadex.tools.tracer.nodes.TNode#getColor()
    */
   public Color getColor()
   {
      if (!alive) return LookAndFeel.DAGENT_COLOR;
      if (ignored) return LookAndFeel.IGNORED_AGENT_COLOR;
      if (tracing.isTracing()) return LookAndFeel.WATCHED_AGENT_COLOR;
      return LookAndFeel.AGENT_COLOR;
   }

   /** 
    * @return "Agent"
    * @see jadex.tools.tracer.nodes.TNode#getTraceType()
    */
   public String getTraceType()
   {
      return "Agent";
   }

   /** 
    * @return a simple tooltip
    * @see java.lang.Object#toString()
    */
   public String getToolTip()
   {
      if (tooltip == null)
      {
         StringBuffer buf = new StringBuffer();

         buf.append("<HTML><TABLE>");
         appendLine(buf, getTraceType().toUpperCase(), name);

         //      Iterator it = aid.getAllAddresses();
         //      while(it.hasNext())
         //      {
         //        appendLine(buf, "address", it.next().toString());
         //      }

         appendLine(buf, "date", date.toString());

         buf.append("</TABLE></HTML>");

         return buf.toString();
      }
      return tooltip;
   }

   /** Paints the Node. 
    * @param g
    * @param tgPanel
    */
   public void paint(Graphics g, TGPanel tgPanel)
   {
      if (!intersects(tgPanel.getSize())) return;
      g.setFont(font);
      fontMetrics = g.getFontMetrics();

      int ix = (int) drawx;
      int iy = (int) drawy;
      int h = getHeight();
      int w = getWidth();
      int h2 = h / 2;
      int w2 = w / 2;

      if (this == tgPanel.getSelect())
      {
         g.setColor(LookAndFeel.SELECT_COLOR);
      }
      else
      {
         g.setColor(LookAndFeel.GRAPH_COLOR);
      }
      g.fillRect(ix - w2, iy - h2, w, h);

      if (this == tgPanel.getDragNode())
      {
         g.setColor(LookAndFeel.DRAG_COLOR);
      }
      else if (this == tgPanel.getMouseOverN())
      {
         g.setColor(LookAndFeel.HIGHLIGHT);
      }
      else
      {
         g.setColor(getColor());
      }
      g.drawRect(ix - w2, iy - h2, w, h);

      g.setColor(darkColor);
      g.drawString(local, ix - w2 + 3, iy - h2 / 2 + fontMetrics.getDescent() + 3);
      g.drawString(hap, ix - w2 + 3, iy + h2 / 2 + fontMetrics.getDescent() + 3);

   }

   /** 
    * @return the height of the label
    * @see com.touchgraph.graphlayout.Node#getHeight()
    */
   public int getHeight()
   {
      if (fontMetrics != null) { return fontMetrics.getHeight() * 2 + 6; }

      return 6;
   }

   /**
    * @return  the width of the label
    * @see com.touchgraph.graphlayout.Node#getWidth()
    */
   public int getWidth()
   {
      if (fontMetrics != null && local != null)
      {
         int w1 = fontMetrics.stringWidth(local);
         int w2 = fontMetrics.stringWidth(hap);
         int w = (w1 > w2) ? w1 : w2;
         return w + 12;
      }

      return 10;
   }

   /** Schould agent be ignored at all
    * @param flag
    */
   public void setIgnored(boolean flag)
   {
      if (ignored != flag)
      {
         ignored = flag;
         fireNodeChanged();
      }
   }

   /** 
    * @return true if this agent is ignored
    */
   public boolean isIgnored()
   {
      return ignored;
   }

   /** 
    * @param flag
    */
   public void setAlive(boolean flag)
   {
      if (alive != flag)
      {
         alive = flag;
         fireNodeChanged();
      }
   }

   // ------------------ model ---------------------

   /** Adds the trace to this model and updates
    * @param tn
    * @return false if a node with given id is present
    */
   public synchronized boolean addTrace(TNode tn)
   {
      if (tracing.isTracing(tn.trace))
      {
         if (!(tn instanceof TBeliefAccess))
         {
            if (id2node.containsKey(tn.getName())) return false;
            id2node.put(tn.getName(), tn);
         }
         traces.add(tn);
         tn.addListener(this);
         // drawing
         tn.drawx = this.drawx + 5.0 - Math.random() * 10.0;
         tn.drawy = this.drawy + 5.0 - Math.random() * 10.0;
         return true;
      }
      return false;
   }

   /**  
    * @return the beliefs hashtable for this agent 
    */
   public Map getBeliefs()
   {
      return beliefs;
   }

   /** Agent traces are accessed by id via a hashtable.
    * @param id of the trace
    * @return the trace for a given id 
    */
   public TNode getTrace(String id)
   {
      return (TNode) id2node.get(id);
   }

   /** 
    * @return all traces strored in this node
    */
   public TNode[] getTraces()
   {
      TNode[] ret;
      synchronized (traces)
      {
         ret = (TNode[]) traces.toArray(new TNode[traces.size()]);
      }
      return ret;
   }

   /** 
    * @return true if the agent is alive
    */
   public boolean isAlive()
   {
      return alive;
   }

   /** 
    * @return false
    */
   public boolean isAgent()
   {
      return true;
   }

   /** no op
    * @param caller
    * @param node 
    * @see jadex.tools.tracer.nodes.TNodeListener#childAdded(jadex.tools.tracer.nodes.TNode, jadex.tools.tracer.nodes.TNode)
    */
   public void childAdded(TNode caller, TNode node)
   { //
   }

   /** no op
    * @param caller
    * @param node 
    * @see jadex.tools.tracer.nodes.TNodeListener#parentAdded(jadex.tools.tracer.nodes.TNode, jadex.tools.tracer.nodes.TNode)
    */
   public void parentAdded(TNode caller, TNode node)
   { //
   }

   /** no op
    * @param caller
    * @param node
    * @param index 
    * @see jadex.tools.tracer.nodes.TNodeListener#childRemoved(jadex.tools.tracer.nodes.TNode, jadex.tools.tracer.nodes.TNode, int)
    */
   public void childRemoved(TNode caller, TNode node, int index)
   {
   // System.out.println("Stub: TAgent.childRemoved");    
   }

   /** no op
    * @param caller
    * @param node
    * @param index 
    * @see jadex.tools.tracer.nodes.TNodeListener#parentRemoved(jadex.tools.tracer.nodes.TNode, jadex.tools.tracer.nodes.TNode, int)
    */
   public void parentRemoved(TNode caller, TNode node, int index)
   {
   // System.out.println("Stub: TAgent.parentRemoved");   
   }

   /** no op
    * @param caller 
    * @see jadex.tools.tracer.nodes.TNodeListener#nodeChanged(jadex.tools.tracer.nodes.TNode)
    */
   public void nodeChanged(TNode caller)
   {
   // System.out.println("Stub: TAgent.nodeChanged");  
   }

   /** removes this node from the trace table and hash tables
    * @param caller 
    * @see jadex.tools.tracer.nodes.TNodeListener#nodeDeleted(jadex.tools.tracer.nodes.TNode)
    */
   public void nodeDeleted(TNode caller)
   {
      // System.out.println("Stub: TAgent.nodeDeleted");
      id2node.remove(caller.getName());
      traces.remove(caller);
      caller.removeListener(this);
   }

   /** 
    * @param b
    */
   public void setEnforceNodeLimit(boolean b)
   {
      enforceNodeLimit = b;
   }

   /** 
    * @param i
    */
   public void setNodesLimit(int i)
   {
      nodes_limit = i;
   }

   /** 
    * @return true if node limit should be enforce for this agent
    */
   public boolean getEnforceNodeLimit()
   {
      return enforceNodeLimit;
   }

   /** 
    * @return the maximum number of nodes
    */
   public int getNodesLimt()
   {
      return nodes_limit;
   }

   /** 
    * @return the information obout events to be traced by this agent
    */
   public Tracing getTracing()
   {
      return tracing;
   }

   /** 
    * @param tracing
    */
   public void setTracing(Tracing tracing)
   {
      this.tracing = new Tracing(tracing);  
   }
}

/*  
 * $Log$
 * Revision 1.3  2005/11/23 15:16:11  walczak
 * Polished the agent filter menu/dialog.
 *
 * Revision 1.2  2005/11/11 12:30:05  walczak
 * -----------------------------------------
 *
 */