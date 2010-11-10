/*
 * TBeliefAccess.java
 * Copyright (c) 2005 by University of Hamburg. All Rights Reserved.
 * Departament of Informatics. 
 * Distributed Systems and Information Systems.
 *
 * Created by 9walczak on Jan 26, 2005.  
 * Last revision $Revision: 3040 $ by:
 * $Author: braubach $ on $Date: 2005-08-02 15:13:37 +0000 (Tue, 02 Aug 2005) $.
 */
package jadex.tools.tracer.nodes;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.tools.ontology.OTrace;

import java.awt.Graphics;

/** 
 * <code>TBeliefAccess</code>
 * @since Jan 26, 2005
 */
public abstract class TBeliefAccess extends TNode
{

  /** <code>belief_node</code>: is the node denoting the belief */
  protected TBelief belief_node;

  /** Init
   * Constructor: <code>TBeliefAccess</code>.
   * @param aid
   * @param trace
   */
  public TBeliefAccess(AgentIdentifier aid, OTrace trace)
  {
    super(aid, trace);
  }

  /**
   * @return true
   */
  public boolean isBeliefAccess()
  {
    return true;
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

  /** 
   * @return the corresponding belief node
   */
  public TBelief getBeliefNode()
  {
    return belief_node;
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
 * Revision 1.3  2005/03/17 15:08:20  9walczak
 * Tested for multithreading. Major fixes.
 *
 * Revision 1.2  2005/02/05 22:03:42  9walczak
 * Created new Ontology. Removed sync methods.
 * Made all filters positive. User may specify filter from the gui.
 * Many fixes.
 *
 * Revision 1.1  2005/01/26 13:47:29  9walczak
 * Beta release. Many fixes.
 *
 */