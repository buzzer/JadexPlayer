/*
 * TBelief.java
 * Copyright (c) 2004 by University of Hamburg. All Rights Reserved.
 * Departament of Informatics. 
 * Distributed Systems and Information Systems.
 *
 * Created by 9walczak on Nov 30, 2004.  
 * Last revision $Revision: 3040 $ by:
 * $Author: braubach $ on $Date: 2005-08-02 15:13:37 +0000 (Tue, 02 Aug 2005) $.
 */
package jadex.tools.tracer.nodes;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.tools.tracer.ui.LookAndFeel;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.ImageIcon;

/** Stores a single belief node to refer to all belief reads and writes
 * <code>TBelief</code>
 * @since Nov 30, 2004
 */
public class TBelief extends TNode
{

  /** 
   * Constructor: <code>TBelief</code>.
   * 
   * @param aid
   * @param id
   */
  public TBelief(AgentIdentifier aid, String id)
  {
    super(aid, id, aid.getName());
  }

  /** 
   * @return 
   * @see jadex.tools.tracer.nodes.TNode#getIcon()
   */
  public ImageIcon getIcon()
  {
    return LookAndFeel.BELIEF_ICON;
  }

  /** 
   * @return 
   * @see jadex.tools.tracer.nodes.TNode#getColor()
   */
  public Color getColor()
  {
    return LookAndFeel.BELIEF_COLOR;
  }

  /** 
   * @return 
   * @see jadex.tools.tracer.nodes.TNode#getTraceType()
   */
  public String getTraceType()
  {
    return "Belief";
  }

  /** Overides the super method in TNode so everytime a child is added 
   * the tooltips are updated. 
   * @param child 
   * @return true if a 
   */
  protected boolean addChild(TNode child)
  {
    if (child instanceof TBeliefAccess && 
        super.addChild(child)) {
      ((TBeliefAccess)child).belief_node = this;
      tooltip = null;
      return true;
    }
    return false;
  }

  /** 
   * @return 
   * @see java.lang.Object#toString()
   */
  public String getToolTip()
  {
    if (tooltip==null)
    {
      StringBuffer buf = new StringBuffer();

      buf.append("<HTML><TABLE>");
      appendLine(buf, getTraceType().toUpperCase(), name);

      int i = getChildCount();
      int limes = i-LookAndFeel.BELIEF_TOOLTIP_SIZE;
      limes = (limes>=0) ? limes : 0;
      while(i-->limes)
      {
        TNode node = (TNode)getChildAt(i);
        appendLine(buf, node.getTraceType(), node.date.toString());
      }

      buf.append("</TABLE></HTML>");

      return tooltip = buf.toString();
    }
    return tooltip;
  }

  /** 
   * @param g
   * @param x
   * @param y
   * @param w
   * @param h
   * @param r 
   * @see jadex.tools.tracer.nodes.TNode#drawFigure(java.awt.Graphics, int, int, int, int, int)
   */
  protected void drawFigure(Graphics g, int x, int y, int w, int h, int r)
  {
    g.drawOval(x, y, w, h);
  }

  /** 
   * @param g
   * @param x
   * @param y
   * @param w
   * @param h
   * @param r 
   * @see jadex.tools.tracer.nodes.TNode#fillFigure(java.awt.Graphics, int, int, int, int, int)
   */
  protected void fillFigure(Graphics g, int x, int y, int w, int h, int r)
  {
    g.fillOval(x, y, w, h);
  }

}

/*  
 * $Log$
 * Revision 1.1  2005/08/02 15:13:11  braubach
 * alpha version of new platform independent tracer
 *
 * Revision 1.1  2005/04/26 13:45:49  pokahr
 * *** empty log message ***
 *
 * Revision 1.6  2005/03/17 15:08:20  9walczak
 * Tested for multithreading. Major fixes.
 *
 * Revision 1.5  2005/02/05 22:03:42  9walczak
 * Created new Ontology. Removed sync methods.
 * Made all filters positive. User may specify filter from the gui.
 * Many fixes.
 *
 * Revision 1.4  2005/01/26 13:47:29  9walczak
 * Beta release. Many fixes.
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
 * Revision 1.1.2.5  2004/12/22 17:36:39  9walczak
 * *** empty log message ***
 *
 * Revision 1.1.2.4  2004/12/13 17:48:23  9walczak
 * Bound the menu items and added collapse beliefs option.
 *
 * Revision 1.1.2.3  2004/11/30 12:37:04  9walczak
 * Added tooltips to the graph.
 * Added selection highlight.
 * Added mousewheel zoom.
 * Added death agent icon.
 *
 */