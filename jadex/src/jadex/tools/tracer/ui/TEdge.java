/*
 * TEdge.java
 * Copyright (c) 2004 by University of Hamburg. All Rights Reserved.
 * Departament of Informatics. 
 * Distributed Systems and Information Systems.
 *
 * Created by 9walczak on Dec 22, 2004.  
 * Last revision $Revision: 5024 $ by:
 * $Author: pokahr $ on $Date: 2007-03-15 17:34:04 +0100 (Thu, 15 Mar 2007) $.
 */
package jadex.tools.tracer.ui;

import jadex.tools.tracer.nodes.TRead;

import java.awt.Color;
import java.awt.Graphics;

import com.touchgraph.graphlayout.Edge;
import com.touchgraph.graphlayout.Node;
import com.touchgraph.graphlayout.TGPanel;

/** 
 * <code>TEdge</code>
 * Edge implementation with an equals method.
 * @since Dec 22, 2004
 */
public class TEdge extends Edge
{

  /** Init
   * Constructor: <code>TEdge</code>.
   * 
   * @param f
   * @param t
   * @param len
   */
  public TEdge(Node f, Node t, int len)
  {
    super(f, t, len);
  }

  /** Init
   * Constructor: <code>TEdge</code>.
   * 
   * @param f
   * @param t
   */
  public TEdge(Node f, Node t)
  {
    super(f, t);
  }

  /** Prevents adding the same edges to the graph - many times 
   * @param obj
   * @return true if the object is an edge from this.from to this.to
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    // System.out.println("Stub: TEdge.equals");
    if (obj==this) return true;
    if (obj instanceof Edge)
    {
      Edge e = (Edge)obj;
      return e.from==this.from&&e.to==this.to;
    }
    return false;
  }

  /** <code>triangle</code>  */
  protected final int [] tX = new int[3];

  /** <code>triangleY</code> */
  protected final int [] tY = new int[3];

  /** paints an arrow 
   * @param g
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   * @see com.touchgraph.graphlayout.Edge#paintArrow(java.awt.Graphics, int, int, int, int, java.awt.Color)
   */
  public void paintArrow(Graphics g, double x1, double y1, double x2, double y2)
  {
    double dx = x2-x1;
    double dy = y2-y1;

    double d = Math.sqrt(dx*dx+dy*dy)/2.3;

    //  g.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
    if (d>1.0)
    {
      dx /= d;
      dy /= d;

      tX[0] = (int)(x1+dy);
      tY[0] = (int)(y1-dx);
      tX[1] = (int)(x1-dy);
      tY[1] = (int)(y1+dx);
      tX[2] = (int)(x2);
      tY[2] = (int)(y2);

      g.fillPolygon(tX, tY, 3);

      g.setColor(BRIGHTER_COLOR);

      g.drawLine(tX[0], tY[0], tX[2], tY[2]);
      g.drawLine(tX[1], tY[1], tX[2], tY[2]);

    }
  }
  
  static final Color BRIGHTER_COLOR = LookAndFeel.EDGE_COLOR.brighter();

  /** overides Edge method to revers arrows on read
   * @param g
   * @param tgPanel 
   * @see com.touchgraph.graphlayout.Edge#paint(java.awt.Graphics, com.touchgraph.graphlayout.TGPanel)
   */
  public void paint(Graphics g, TGPanel tgPanel)
  {
    if (intersects(tgPanel.getSize()))
    {
      if (tgPanel.getMouseOverE()==this)
      {
        g.setColor(LookAndFeel.EDGE_SELECT_COLOR);
      }
      else
      {
        g.setColor(LookAndFeel.EDGE_COLOR);
      }

      if (to instanceof TRead)
      {
        paintArrow(g, to.drawx, to.drawy, from.drawx, from.drawy);
      }
      else
      {
        paintArrow(g, from.drawx, from.drawy, to.drawx, to.drawy);
      }
    }
  }

  /**
   *  Create a string representation of the edge.
   */
  public String	toString()
  {
	  return "Edge("+from+", "+to+")";
  }
}
